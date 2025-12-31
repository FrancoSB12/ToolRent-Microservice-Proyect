import axios from "axios";
import keycloak from "./keycloak";

const toolRentBackendServer = import.meta.env.VITE_TOOLRENT_BACKEND_SERVER;
const toolRentBackendPort = import.meta.env.VITE_TOOLRENT_BACKEND_PORT;

const api = axios.create({
  baseURL: `http://${toolRentBackendServer}:${toolRentBackendPort}`,
  headers: {
    "Content-Type": "application/json"
  }
});

api.interceptors.request.use(async (config) => {
  if (keycloak.authenticated) {
    await keycloak.updateToken(30);
    config.headers.Authorization = `Bearer ${keycloak.token}`;
  }

  return config;
}, (error) => {
  return Promise.reject(error);
});

export default api;