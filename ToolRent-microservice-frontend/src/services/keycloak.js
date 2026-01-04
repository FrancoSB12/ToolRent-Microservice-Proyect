import Keycloak from "keycloak-js";

const keycloak = new Keycloak({
  url: "http://localhost:9090",
  realm: "toolrent-realm",
  clientId: "toolrent-frontend",
});

export default keycloak;