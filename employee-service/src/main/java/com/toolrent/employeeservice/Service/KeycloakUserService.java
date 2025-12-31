package com.toolrent.employeeservice.Service;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class KeycloakUserService {

    private final RestTemplate restTemplate;
    private final String keycloakUrl = "http://localhost:9090";
    private final String realm = "toolrent-realm";
    private final String clientId = "admin-cli";
    private final String adminUser = "francoadmin";
    private final String adminPassword = "Admin12";

    public KeycloakUserService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    private String getAdminToken() {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "password");
        form.add("client_id", clientId);
        form.add("username", adminUser);
        form.add("password", adminPassword);

        String tokenUrl = keycloakUrl + "/realms/master/protocol/openid-connect/token";

        try {
            Map<String, Object> response = restTemplate.postForObject(tokenUrl, form, Map.class);
            return (String) response.get("access_token");
        } catch (Exception e) {
            throw new RuntimeException("Error al obtener token de admin. Verifica usuario/contraseña en el código.", e);
        }
    }

    public String createUser(String username, String email, String firstName, String lastName, String password) {
        String token = getAdminToken();

        Map<String, Object> userBody = Map.of(
                "username", username,
                "email", email,
                "firstName", firstName,
                "lastName", lastName,
                "enabled", true,
                "credentials", List.of(Map.of(
                        "type", "password",
                        "value", password,
                        "temporary", false
                ))
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String,Object>> request = new HttpEntity<>(userBody, headers);

        //Create user in toolrent-realm
        restTemplate.postForEntity(
                keycloakUrl + "/admin/realms/" + realm + "/users",
                request,
                String.class
        );

        //Search the created user ID
        List<Map<String, Object>> users = restTemplate.exchange(
                keycloakUrl + "/admin/realms/" + realm + "/users?username=" + username,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                List.class
        ).getBody();

        if (users != null && !users.isEmpty()) {
            return (String) users.get(0).get("id");
        }
        throw new RuntimeException("Error: Usuario creado pero no se pudo recuperar su ID.");
    }

    public void assignRole(String userId, String roleName) {
        String token = getAdminToken();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        String roleUrl = keycloakUrl + "/admin/realms/" + realm + "/roles/" + roleName;

        Map<String, Object> fullRoleData;
        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    roleUrl,
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    Map.class
            );
            fullRoleData = response.getBody();
        } catch (Exception e) {
            throw new RuntimeException("No se encontró el rol '" + roleName + "'", e);
        }

        //Create a clean object for cleaner code
        Map<String, Object> roleToAssign = Map.of(
                "id", fullRoleData.get("id"),
                "name", fullRoleData.get("name")
        );

        //Assign role
        HttpEntity<List<Map<String, Object>>> request = new HttpEntity<>(
                Collections.singletonList(roleToAssign),
                headers
        );

        String mappingUrl = keycloakUrl + "/admin/realms/" + realm + "/users/" + userId + "/role-mappings/realm";

        try {
            restTemplate.postForEntity(
                    mappingUrl,
                    request,
                    String.class
            );
        } catch (Exception e) {
            throw new RuntimeException("Error al asignar rol en Keycloak.", e);
        }
    }
}
