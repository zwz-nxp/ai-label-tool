import { Component, OnDestroy, OnInit } from "@angular/core";
import { Router } from "@angular/router";
import { Store } from "@ngrx/store";
import { combineLatest, map, Observable, Subject, takeUntil } from "rxjs";
import { MatDialog } from "@angular/material/dialog";
import { MatSnackBar } from "@angular/material/snack-bar";
import { animate, style, transition, trigger } from "@angular/animations";
import {
  ProjectGroupName,
  ProjectListItem,
} from "app/models/landingai/project";
import { Location } from "app/models/location";
import { RoleEnum } from "app/models/role";
import { AuthorizationService } from "app/utils/services/authorization.service";
import * as HomeActions from "../../../../state/landingai/home/home.actions";
import * as HomeSelectors from "../../../../state/landingai/home/home.selectors";
import * as LocationSelectors from "../../../../state/location/location.selectors";
import {
  ProjectEditDialogComponent,
  ProjectEditDialogData,
} from "../project-edit-dialog/project-edit-dialog.component";
import {
  ProjectDeleteDialogComponent,
  ProjectDeleteDialogData,
} from "../project-delete-dialog/project-delete-dialog.component";

interface ProjectGroup {
  groupName: string;
  projects: ProjectListItem[];
  expanded: boolean;
}

@Component({
  selector: "app-project-list",
  standalone: false,
  templateUrl: "./project-list.component.html",
  styleUrls: ["./project-list.component.scss"],
  animations: [
    trigger("slideDown", [
      transition(":enter", [
        style({ height: 0, opacity: 0, overflow: "hidden" }),
        animate("300ms ease-out", style({ height: "*", opacity: 1 })),
      ]),
      transition(":leave", [
        style({ height: "*", opacity: 1, overflow: "hidden" }),
        animate("300ms ease-in", style({ height: 0, opacity: 0 })),
      ]),
    ]),
  ],
})
export class ProjectListComponent implements OnInit, OnDestroy {
  projects$: Observable<ProjectListItem[]>;
  projectGroups$: Observable<ProjectGroup[]>;
  loading$: Observable<boolean>;
  error$: Observable<string | null>;
  currentLocation$: Observable<Location | null>;

  viewAll = false;
  isAuthorized = true;
  private destroy$ = new Subject<void>();

  constructor(
    private store: Store,
    private router: Router,
    private dialog: MatDialog,
    private snackBar: MatSnackBar,
    private authorizationService: AuthorizationService
  ) {
    this.projects$ = this.store.select(HomeSelectors.selectProjects);
    this.loading$ = this.store.select(HomeSelectors.selectLoading);
    this.error$ = this.store.select(HomeSelectors.selectError);
    this.currentLocation$ = this.store.select(
      LocationSelectors.selectCurrentLocation
    );

    // Create grouped projects observable
    this.projectGroups$ = this.projects$.pipe(
      map((projects) => this.groupProjects(projects))
    );
  }

  ngOnInit(): void {
    // Re-check auth and reload projects on every location change
    this.currentLocation$
      .pipe(takeUntil(this.destroy$))
      .subscribe((location) => {
        if (!location) return;

        const isSystemAdmin =
          this.authorizationService.doesCurrentUserHaveRoleForCurrentLocation(
            0,
            RoleEnum.ADMINISTRATOR_SYSTEM
          );

        if (isSystemAdmin) {
          this.isAuthorized = true;
        } else {
          const adcSites =
            this.authorizationService.getLinkedLocationAcronymsForRole(
              RoleEnum.ADC_ENGINEER
            );
          this.isAuthorized =
            adcSites.has("*") ||
            adcSites.has(location.acronym) ||
            adcSites.has(location.sapCode);
        }

        if (this.isAuthorized) {
          this.store.dispatch(
            HomeActions.loadProjects({ viewAll: this.viewAll })
          );
        }
      });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  /**
   * Toggle group expansion
   */
  toggleGroup(group: ProjectGroup): void {
    group.expanded = !group.expanded;
  }

  /**
   * Toggle between viewing only user's projects and all projects
   */
  toggleViewAll(): void {
    this.viewAll = !this.viewAll;
    if (this.isAuthorized) {
      this.store.dispatch(HomeActions.loadProjects({ viewAll: this.viewAll }));
    }
  }

  /**
   * Navigate to project creation page
   */
  navigateToCreate(): void {
    this.router.navigate(["/landingai/projects/create"]);
  }

  /**
   * Navigate to project detail page
   */
  navigateToProject(projectId: number): void {
    this.router.navigate(["/landingai/projects", projectId]);
  }

  /**
   * Retry loading projects after an error
   */
  retryLoadProjects(): void {
    this.store.dispatch(HomeActions.loadProjects({ viewAll: this.viewAll }));
  }

  /**
   * Handle edit project event
   */
  onEditProject(project: ProjectListItem): void {
    const dialogData: ProjectEditDialogData = { project };

    const dialogRef = this.dialog.open(ProjectEditDialogComponent, {
      width: "550px",
      data: dialogData,
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        this.store.dispatch(
          HomeActions.updateProject({
            id: project.id,
            name: result.name,
            modelName: result.modelName,
            groupName: result.groupName,
          })
        );
      }
    });
  }

  /**
   * Handle delete project event
   */
  onDeleteProject(project: ProjectListItem): void {
    const dialogData: ProjectDeleteDialogData = { project };

    const dialogRef = this.dialog.open(ProjectDeleteDialogComponent, {
      width: "500px",
      data: dialogData,
    });

    dialogRef.afterClosed().subscribe((confirmed) => {
      if (confirmed) {
        this.store.dispatch(HomeActions.deleteProject({ id: project.id }));
      }
    });
  }

  /**
   * Group projects by groupName
   */
  private groupProjects(projects: ProjectListItem[]): ProjectGroup[] {
    const groupOrder: (ProjectGroupName | "Ungrouped")[] = [
      "WT",
      "FE",
      "BE",
      "QA",
      "AT",
      "Ungrouped",
    ];
    const groupMap = new Map<string, ProjectListItem[]>();

    // Initialize groups
    groupOrder.forEach((group) => groupMap.set(group, []));

    // Group projects
    projects.forEach((project) => {
      const groupName = project.groupName || "Ungrouped";
      const group = groupMap.get(groupName);
      if (group) {
        group.push(project);
      } else {
        groupMap.set(groupName, [project]);
      }
    });

    // Convert to array and filter out empty groups, expand first group
    const groups: ProjectGroup[] = [];
    let firstNonEmptyGroup = true;

    groupOrder.forEach((groupName) => {
      const projects = groupMap.get(groupName) || [];
      if (projects.length > 0) {
        groups.push({
          groupName,
          projects,
          expanded: firstNonEmptyGroup, // Expand first group by default
        });
        firstNonEmptyGroup = false;
      }
    });

    return groups;
  }
}
