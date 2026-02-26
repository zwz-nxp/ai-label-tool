import { Injectable } from "@angular/core";
import { HttpClient, HttpParams } from "@angular/common/http";
import { Observable, BehaviorSubject, of } from "rxjs";
import { map, catchError } from "rxjs/operators";
import {
  Model,
  ModelDisplayDto,
  SearchFilters,
  mapModelToDisplayDto,
} from "../../models/landingai/model";
import { Configuration } from "../../utils/configuration";
import { ModelDataUtils } from "../../utils/model-data.utils";

@Injectable({
  providedIn: "root",
})
export class ModelsService {
  private readonly actionUrl: string;
  private readonly FAVORITES_STORAGE_KEY = "model-favorites";
  private readonly USE_MOCK_DATA = false; // Use real API data

  // Local state management
  private favoritesSubject = new BehaviorSubject<Set<number>>(new Set());
  public favorites$ = this.favoritesSubject.asObservable();

  constructor(
    private http: HttpClient,
    private configuration: Configuration
  ) {
    this.actionUrl = configuration.ServerWithApiUrl + "models";
    this.loadFavoriteStates();
  }

  /**
   * Get all models
   * Implementation requirements 1.2: Display all available models on page load
   */
  getModels(): Observable<ModelDisplayDto[]> {
    if (this.USE_MOCK_DATA) {
      // Use mock data for development
      const mockModels = ModelDataUtils.generateSampleModels();
      return of(
        mockModels.map((model) => {
          const dto = mapModelToDisplayDto(model);
          // Ensure DTO favorite state is synced with local storage
          const favoriteIds = this.favoritesSubject.value;
          dto.isFavorite =
            favoriteIds.has(model.id) || model.isFavorite || false;
          return dto;
        })
      );
    }

    console.log("Fetching models from API:", this.actionUrl);
    return this.http.get<Model[]>(this.actionUrl).pipe(
      map((models) => {
        console.log("Received models from API:", models);
        return models.map((model) => {
          const dto = mapModelToDisplayDto(model);
          // Use backend favorite state, don't override with local storage
          console.log(
            `Model ${model.id} isFavorite from backend:`,
            model.isFavorite
          );
          return dto;
        });
      }),
      catchError((error) => {
        console.error("Failed to fetch models:", error);
        console.error("API URL:", this.actionUrl);
        console.error("Error details:", error.message, error.status);
        return of([]);
      })
    );
  }

  /**
   * Get models by project ID
   */
  getModelsByProject(projectId: number): Observable<ModelDisplayDto[]> {
    return this.http
      .get<Model[]>(`${this.actionUrl}/project/${projectId}`)
      .pipe(
        map((models) => models.map((model) => mapModelToDisplayDto(model)))
      );
  }

  /**
   * Search models
   * Implementation requirements 2.1: Filter models in real-time by model name or creator
   */
  searchModels(filters: SearchFilters): Observable<ModelDisplayDto[]> {
    if (this.USE_MOCK_DATA) {
      // Use mock data for search
      let mockModels = ModelDataUtils.generateSampleModels();
      let filteredModels = mockModels;

      // Apply search filter - Requirement 2.1: Filter by model name or creator
      if (filters.searchTerm && filters.searchTerm.trim()) {
        const searchTerm = filters.searchTerm.toLowerCase().trim();
        filteredModels = filteredModels.filter(
          (model) =>
            (model.modelAlias &&
              model.modelAlias.toLowerCase().includes(searchTerm)) ||
            (model.createdBy &&
              model.createdBy.toLowerCase().includes(searchTerm))
        );
      }

      // Apply favorites filter - Requirement 2.3: Only show favorite models
      if (filters.showFavoritesOnly) {
        const favoriteIds = this.favoritesSubject.value;
        filteredModels = filteredModels.filter((model) => {
          // Check local storage favorite state or model's own favorite state
          const isLocalFavorite = favoriteIds.has(model.id);
          const isModelFavorite = model.isFavorite;
          return isLocalFavorite || isModelFavorite;
        });
      }

      // Convert models to display DTO and sync favorite state
      return of(
        filteredModels.map((model) => {
          const dto = mapModelToDisplayDto(model);
          // Ensure DTO favorite state is synced with local storage
          const favoriteIds = this.favoritesSubject.value;
          dto.isFavorite =
            favoriteIds.has(model.id) || model.isFavorite || false;
          return dto;
        })
      );
    }

    let params = new HttpParams();

    if (filters.searchTerm && filters.searchTerm.trim()) {
      params = params.set("query", filters.searchTerm.trim());
    }

    if (filters.showFavoritesOnly) {
      params = params.set("favoritesOnly", "true");
    }

    if (filters.projectId !== undefined && filters.projectId !== null) {
      params = params.set("projectId", filters.projectId.toString());
    }

    return this.http.get<Model[]>(`${this.actionUrl}/search`, { params }).pipe(
      map((models) => {
        console.log("Search results from API:", models);
        return models.map((model) => {
          const dto = mapModelToDisplayDto(model);
          // Use backend favorite state
          console.log(
            `Model ${model.id} isFavorite from search:`,
            model.isFavorite
          );
          return dto;
        });
      }),
      catchError((error) => {
        console.error("Failed to search models:", error);
        return of([]);
      })
    );
  }

  /**
   * Toggle model favorite status
   * Implementation requirements 3.2: Persist favorite status changes in local application state
   */
  toggleFavorite(modelId: number): Observable<ModelDisplayDto> {
    if (this.USE_MOCK_DATA) {
      // Mock data mode: directly update local state
      const currentFavorites = this.favoritesSubject.value;
      const newIsFavorite = !currentFavorites.has(modelId);
      this.updateLocalFavoriteState(modelId, newIsFavorite);

      // Create mock updated model
      const mockModels = ModelDataUtils.generateSampleModels();
      const model = mockModels.find((m) => m.id === modelId);
      if (model) {
        model.isFavorite = newIsFavorite;
        const dto = mapModelToDisplayDto(model);
        dto.isFavorite = newIsFavorite;
        return of(dto);
      } else {
        throw new Error(`Model with ID ${modelId} not found`);
      }
    }

    return this.http
      .put<Model>(`${this.actionUrl}/${modelId}/favorite`, {})
      .pipe(
        map((model) => {
          console.log(`Toggle favorite response for model ${modelId}:`, model);
          console.log(`isFavorite value:`, model.isFavorite);
          // Don't update local favorite state, rely entirely on backend
          return mapModelToDisplayDto(model);
        }),
        catchError((error) => {
          console.error("Failed to toggle favorite:", error);
          throw error;
        })
      );
  }

  /**
   * Get specific model by ID
   */
  getModelById(id: number): Observable<ModelDisplayDto> {
    return this.http
      .get<Model>(`${this.actionUrl}/${id}`)
      .pipe(map((model) => mapModelToDisplayDto(model)));
  }

  /**
   * Save favorite state to localStorage
   * Implementation requirements 3.2: Persist favorite status changes
   */
  saveFavoriteState(modelId: number, isFavorite: boolean): void {
    const favorites = this.loadFavoriteStatesFromStorage();
    if (isFavorite) {
      favorites.add(modelId);
    } else {
      favorites.delete(modelId);
    }

    try {
      localStorage.setItem(
        this.FAVORITES_STORAGE_KEY,
        JSON.stringify({
          modelFavorites: Array.from(favorites),
          lastUpdated: Date.now(),
        })
      );

      this.favoritesSubject.next(favorites);
    } catch (error) {
      console.error("Failed to save favorite state to localStorage:", error);
    }
  }

  /**
   * Load favorite states from localStorage
   * Implementation requirements 3.2: Load persisted favorite states
   */
  loadFavoriteStates(): Map<string, boolean> {
    const favorites = this.loadFavoriteStatesFromStorage();
    this.favoritesSubject.next(favorites);

    // Convert to Map format to match interface specification
    const favoriteMap = new Map<string, boolean>();
    favorites.forEach((id) => {
      favoriteMap.set(id.toString(), true);
    });

    return favoriteMap;
  }

  /**
   * Read favorite states from localStorage
   */
  private loadFavoriteStatesFromStorage(): Set<number> {
    try {
      const stored = localStorage.getItem(this.FAVORITES_STORAGE_KEY);
      if (stored) {
        const data = JSON.parse(stored);
        return new Set(data.modelFavorites || []);
      }
    } catch (error) {
      console.warn("Failed to load favorite states from localStorage:", error);
    }
    return new Set();
  }

  /**
   * Update local favorite state
   */
  private updateLocalFavoriteState(modelId: number, isFavorite: boolean): void {
    this.saveFavoriteState(modelId, isFavorite);
  }

  /**
   * Sync model favorite state with local storage
   * Implementation requirements 2.3: Ensure accuracy of favorite filtering
   */
  private syncFavoriteState(dto: ModelDisplayDto): ModelDisplayDto {
    const favoriteIds = this.favoritesSubject.value;
    dto.isFavorite = favoriteIds.has(dto.id);
    return dto;
  }

  /**
   * Check if model is favorite
   */
  isFavorite(modelId: number): boolean {
    return this.favoritesSubject.value.has(modelId);
  }

  /**
   * Get all favorite model IDs
   */
  getFavoriteIds(): number[] {
    return Array.from(this.favoritesSubject.value);
  }
}
