import { currentUserReducer } from "./current-user";
import { userRoleReducer } from "./user-role";
import { locationReducer } from "./location";
import { sapCodeReducer } from "app/state/sap-code";
import { systemReducer } from "app/state/system";
import { homeReducer } from "./landingai/home";
import { modelReducer } from "./landingai/model/model.reducer";
import { modelDetailReducer } from "./landingai/model/model-detail/model-detail.reducer";
import { imageUploadReducer } from "./landingai/image-upload";
import { snapshotListReducer } from "./landingai/snapshot-list";

export const AppState = {
  currentUser: currentUserReducer,
  userRoles: userRoleReducer,
  location: locationReducer,
  sapCodes: sapCodeReducer,
  system: systemReducer,
  landingAI: homeReducer,
  model: modelReducer,
  modelDetail: modelDetailReducer,
  imageUpload: imageUploadReducer,
  snapshotList: snapshotListReducer,
};
