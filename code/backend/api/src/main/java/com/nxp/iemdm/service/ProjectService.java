package com.nxp.iemdm.service;

import com.nxp.iemdm.shared.dto.landingai.ProjectCreateRequest;
import com.nxp.iemdm.shared.dto.landingai.ProjectDTO;
import com.nxp.iemdm.shared.dto.landingai.ProjectListItemDTO;
import com.nxp.iemdm.shared.dto.landingai.ProjectUpdateRequest;
import java.util.List;

/** Service interface for Landing AI project operations. */
public interface ProjectService {

  /**
   * Get projects for a user with optional view all mode.
   *
   * @param userId the user identifier
   * @param locationId the location ID
   * @param viewAll if true, return all projects in location; if false, return only user's projects
   * @return list of project list items
   */
  List<ProjectListItemDTO> getProjectsForUser(String userId, Long locationId, boolean viewAll);

  /**
   * Create a new project.
   *
   * @param request the project creation request
   * @param userId the user identifier
   * @param locationId the location ID
   * @return the created project
   */
  ProjectDTO createProject(ProjectCreateRequest request, String userId, Long locationId);

  /**
   * Get project by ID.
   *
   * @param id the project ID
   * @return the project details
   */
  ProjectDTO getProjectById(Long id);

  /**
   * Update project name and model name.
   *
   * @param id the project ID
   * @param request the project update request
   * @param userId the user identifier
   * @return the updated project
   */
  ProjectDTO updateProject(Long id, ProjectUpdateRequest request, String userId);

  /**
   * Delete project by ID.
   *
   * @param id the project ID
   * @param userId the user identifier
   */
  void deleteProject(Long id, String userId);
}
