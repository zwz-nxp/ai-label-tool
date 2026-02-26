import { RxStompService } from "./services/rx-stomp.service";
import { myRxStompConfig } from "./api-access/my-rx-stomp.config";

export function rxStompServiceFactory(): RxStompService {
  const rxStomp = new RxStompService();
  rxStomp.configure(myRxStompConfig);
  rxStomp.activate();
  return rxStomp;
}
