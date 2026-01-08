import Keycloak from "keycloak-js";

const keycloakUrl = import.meta.env.VITE_KEYCLOAK_URL;

const keycloak = new Keycloak({
  url: keycloakUrl,
  realm: "toolrent-realm",
  clientId: "toolrent-frontend",
});

export default keycloak;