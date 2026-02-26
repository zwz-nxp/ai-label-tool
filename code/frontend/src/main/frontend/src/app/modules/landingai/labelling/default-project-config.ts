import { ProjectClass } from "app/models/landingai/project-class.model";
import { ProjectTag } from "app/services/landingai/tag.service";
import { ProjectMetadata } from "app/services/landingai/metadata.service"; /**
 * Default project configuration
 * Used when a project has no classes, tags, or metadata defined
 */

/**
 * Default project configuration
 * Used when a project has no classes, tags, or metadata defined
 */

/**
 * Default project classes
 */
export const DEFAULT_PROJECT_CLASSES: ProjectClass[] = [
  {
    id: -1,
    className: "Defect",
    colorCode: "#FF0000",
    description: "Default defect class",
    project: { id: 0 },
  },
  {
    id: -2,
    className: "Normal",
    colorCode: "#00FF00",
    description: "Default normal class",
    project: { id: 0 },
  },
  {
    id: -3,
    className: "Warning",
    colorCode: "#FFA500",
    description: "Default warning class",
    project: { id: 0 },
  },
];

/**
 * Default project tags
 */
export const DEFAULT_PROJECT_TAGS: ProjectTag[] = [
  {
    id: -1,
    name: "Quality Check",
    color: "#2196F3",
  },
  {
    id: -2,
    name: "Inspection",
    color: "#9C27B0",
  },
  {
    id: -3,
    name: "Review Required",
    color: "#FF9800",
  },
];

/**
 * Default project metadata
 */
export const DEFAULT_PROJECT_METADATA: ProjectMetadata[] = [
  {
    id: -1,
    name: "Location",
    type: "text",
    valueFrom: "INPUT",
  },
  {
    id: -2,
    name: "Inspector",
    type: "text",
    valueFrom: "INPUT",
  },
  {
    id: -3,
    name: "Severity",
    type: "dropdown",
    valueFrom: "PREDEFINED",
    predefinedValues: "Low,Medium,High,Critical",
    multipleValues: false,
  },
  {
    id: -4,
    name: "Date",
    type: "text",
    valueFrom: "INPUT",
  },
];
