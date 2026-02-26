import { RxStompConfig } from "@stomp/rx-stomp";

const protocol = window.location.protocol == "https:" ? "wss://" : "ws://";
const portNumber =
  window.location.port == "4200" || window.location.port == "8080"
    ? ":8080"
    : "";

export const myRxStompConfig: RxStompConfig = {
  brokerURL: protocol + window.location.hostname + portNumber + "/socket",

  // How often to heartbeat?
  // Interval in milliseconds, set to 0 to disable
  heartbeatIncoming: 0, // Typical value 0 - disabled
  heartbeatOutgoing: 2000, // Typical value 20000 - every 20 seconds

  // Wait in milliseconds before attempting auto reconnect
  // Set to 0 to disable
  // Typical value 500 (500 milliseconds)
  reconnectDelay: 500,
};
