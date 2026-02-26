import { Injectable } from "@angular/core";
import { Actions, createEffect, ofType } from "@ngrx/effects";
import { Store } from "@ngrx/store";
import { of } from "rxjs";
import {
  catchError,
  debounceTime,
  map,
  switchMap,
  withLatestFrom,
} from "rxjs/operators";
import { ImageService } from "app/services/landingai/image.service";
import * as ImageUploadActions from "./image-upload.actions";
import {
  selectFilters,
  selectPageSize,
  selectProjectId,
  selectSortMethod,
  selectViewMode,
  selectImageUploadState,
} from "./image-upload.selectors";

@Injectable()
export class ImageUploadEffects {
  // Effect to load images from the backend
  loadImages$ = createEffect(() =>
    this.actions$.pipe(
      ofType(ImageUploadActions.loadImages),
      debounceTime(100), // Debounce to prevent rapid-fire requests
      switchMap((action) =>
        this.imageService
          .getImagesPageableByProjectId(
            action.projectId,
            action.page,
            action.size,
            action.viewMode,
            action.filters,
            action.sortBy
          )
          .pipe(
            map((response) =>
              ImageUploadActions.loadImagesSuccess({ response })
            ),
            catchError((error) =>
              of(
                ImageUploadActions.loadImagesFailure({
                  error: error.message || "Failed to load images",
                })
              )
            )
          )
      )
    )
  );
  // Effect to reload images when view mode changes
  changeViewMode$ = createEffect(() =>
    this.actions$.pipe(
      ofType(ImageUploadActions.changeViewMode),
      withLatestFrom(
        this.store.select(selectProjectId),
        this.store.select(selectImageUploadState),
        this.store.select(selectPageSize),
        this.store.select(selectFilters),
        this.store.select(selectSortMethod)
      ),
      map(([action, projectId, state, size, filters, sortBy]) => {
        if (projectId) {
          return ImageUploadActions.loadImages({
            projectId,
            page: state.currentPage, // Use raw 0-indexed value from state
            size,
            viewMode: action.viewMode,
            filters,
            sortBy,
          });
        }
        return ImageUploadActions.loadImagesFailure({
          error: "No project ID available",
        });
      })
    )
  );
  // Effect to reload images when filters change
  applyFilters$ = createEffect(() =>
    this.actions$.pipe(
      ofType(ImageUploadActions.applyFilters),
      withLatestFrom(
        this.store.select(selectProjectId),
        this.store.select(selectPageSize),
        this.store.select(selectViewMode),
        this.store.select(selectSortMethod)
      ),
      map(([action, projectId, size, viewMode, sortBy]) => {
        if (projectId) {
          return ImageUploadActions.loadImages({
            projectId,
            page: 0, // Reset to first page (0-indexed)
            size,
            viewMode,
            filters: action.filters,
            sortBy,
          });
        }
        return ImageUploadActions.loadImagesFailure({
          error: "No project ID available",
        });
      })
    )
  );
  // Effect to reload images when filters are cleared
  clearFilters$ = createEffect(() =>
    this.actions$.pipe(
      ofType(ImageUploadActions.clearFilters),
      withLatestFrom(
        this.store.select(selectProjectId),
        this.store.select(selectPageSize),
        this.store.select(selectViewMode),
        this.store.select(selectSortMethod)
      ),
      map(([_, projectId, size, viewMode, sortBy]) => {
        if (projectId) {
          return ImageUploadActions.loadImages({
            projectId,
            page: 0, // Reset to first page (0-indexed)
            size,
            viewMode,
            filters: {},
            sortBy,
          });
        }
        return ImageUploadActions.loadImagesFailure({
          error: "No project ID available",
        });
      })
    )
  );
  // Effect to reload images when sort method changes
  changeSortMethod$ = createEffect(() =>
    this.actions$.pipe(
      ofType(ImageUploadActions.changeSortMethod),
      withLatestFrom(
        this.store.select(selectProjectId),
        this.store.select(selectPageSize),
        this.store.select(selectViewMode),
        this.store.select(selectFilters)
      ),
      map(([action, projectId, size, viewMode, filters]) => {
        if (projectId) {
          return ImageUploadActions.loadImages({
            projectId,
            page: 0, // Reset to first page (0-indexed)
            size,
            viewMode,
            filters,
            sortBy: action.sortMethod,
          });
        }
        return ImageUploadActions.loadImagesFailure({
          error: "No project ID available",
        });
      })
    )
  );
  // Effect to reload images when page changes
  changePage$ = createEffect(() =>
    this.actions$.pipe(
      ofType(
        ImageUploadActions.changePage,
        ImageUploadActions.nextPage,
        ImageUploadActions.previousPage
      ),
      withLatestFrom(
        this.store.select(selectProjectId),
        this.store.select(selectImageUploadState),
        this.store.select(selectPageSize),
        this.store.select(selectViewMode),
        this.store.select(selectFilters),
        this.store.select(selectSortMethod)
      ),
      map(([_, projectId, state, size, viewMode, filters, sortBy]) => {
        if (projectId) {
          return ImageUploadActions.loadImages({
            projectId,
            page: state.currentPage, // Use raw 0-indexed value from state
            size,
            viewMode,
            filters,
            sortBy,
          });
        }
        return ImageUploadActions.loadImagesFailure({
          error: "No project ID available",
        });
      })
    )
  );

  constructor(
    private actions$: Actions,
    private store: Store,
    private imageService: ImageService
  ) {}
}
