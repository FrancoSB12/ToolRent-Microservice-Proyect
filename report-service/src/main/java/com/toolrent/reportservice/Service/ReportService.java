package com.toolrent.reportservice.Service;

import com.toolrent.reportservice.DTO.ActiveRentsReportDTO;
import com.toolrent.reportservice.DTO.OverdueClientsDTO;
import com.toolrent.reportservice.DTO.RankingReportDTO;
import com.toolrent.reportservice.Model.Client;
import com.toolrent.reportservice.Model.Rent;
import com.toolrent.reportservice.Model.RentXToolItem;
import com.toolrent.reportservice.Model.ToolType;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportService {
    private final RestTemplate restTemplate;
    private HttpServletRequest httpServletRequest;

    @Autowired
    public ReportService(RestTemplate restTemplate, HttpServletRequest httpServletRequest) {
        this.restTemplate = restTemplate;
        this.httpServletRequest = httpServletRequest;
    }

    public List<ActiveRentsReportDTO> getActiveRentsReport() {
        //Get the active rents list
        HttpEntity<Void> getRequestEntity = createAuthEntity(null);
        Rent[] rents;

        try {
            ResponseEntity<Rent[]> response = restTemplate.exchange(
                    "http://rent-service/rent/status/" + "Activo",
                    HttpMethod.GET,
                    getRequestEntity,
                    Rent[].class
            );
            rents = response.getBody();
        } catch (Exception e) {
            throw new RuntimeException("Error al obtener arriendos activos: " + e.getMessage());
        }

        if(rents == null || rents.length == 0){
            throw new RuntimeException("No se encontraron arriendos activos");
        }

        //Fill the report
        List<ActiveRentsReportDTO> activeRentsReport = new ArrayList<>();
        for (Rent rent : rents) {
            ActiveRentsReportDTO activeRentsReportDTO = new ActiveRentsReportDTO();
            activeRentsReportDTO.setRentId(rent.getId());
            activeRentsReportDTO.setRentDate(rent.getRentDate());
            activeRentsReportDTO.setValidity(rent.getValidity());

            //Call to client-service
            try {
                ResponseEntity<Client> clientResponse = restTemplate.exchange(
                        "http://client-service/client/" + rent.getClientRun(),
                        HttpMethod.GET,
                        getRequestEntity,
                        Client.class
                );

                Client client = clientResponse.getBody();
                if(client == null){
                    throw new RuntimeException("No se encontró el cliente con RUN: " +  rent.getClientRun() + " en la base de datos");
                }
                activeRentsReportDTO.setClientName(client.getName()+ " " + client.getSurname());
                activeRentsReportDTO.setClientRun(client.getRun());
            } catch (Exception e) {
                activeRentsReportDTO.setClientName("Desconocido (" + rent.getClientRun() + ")");
            }

            //Call to inventory-service
            List<Long> toolItemIds = new ArrayList<>();
            for (RentXToolItem toolItem : rent.getRentTools()) {
                toolItemIds.add(toolItem.getId());
            }

            HttpEntity<List<Long>> inventoryRequestEntity = createAuthEntity(toolItemIds);
            ToolType[] toolTypes = null;
            try {
                ResponseEntity<ToolType[]> toolsResponse = restTemplate.exchange(
                        "http://inventory-service/inventory/tool-item/get-tool-types",
                        HttpMethod.POST,
                        inventoryRequestEntity,
                        ToolType[].class
                );
                toolTypes = toolsResponse.getBody();
            } catch (Exception e) {
                System.err.println("Error obteniendo detalles de herramientas: " + e.getMessage());
            }

            List<String> names = new ArrayList<>();
            if (toolTypes == null) {
                names = Collections.emptyList();
            } else {
                for (ToolType toolType : toolTypes) {
                    names.add(toolType.getName());
                }
            }

            activeRentsReportDTO.setToolNames(names);
            activeRentsReport.add(activeRentsReportDTO);
        }

        return activeRentsReport;
    }

    public List<OverdueClientsDTO> getOverdueClientsReport() {
        //Get the overdue rents list
        HttpEntity<Void> voidRequest = createAuthEntity(null);
        Rent[] overdueRents;
        try {
            ResponseEntity<Rent[]> response = restTemplate.exchange(
                    "http://rent-service/rent/overdue",
                    HttpMethod.GET,
                    voidRequest,
                    Rent[].class
            );
            overdueRents = response.getBody();
        } catch (Exception e) {
            throw new RuntimeException("Error al consultar arriendos atrasados: " + e.getMessage());
        }

        if (overdueRents == null || overdueRents.length == 0) {
            return Collections.emptyList();
        }

        //Get all the client runs
        List<String> runsToSearch = Arrays.stream(overdueRents)
                .map(Rent::getClientRun)
                .distinct()
                .collect(Collectors.toList());

        HttpEntity<List<String>> clientRequest = createAuthEntity(runsToSearch);

        //Get all client details from client-Service
        Client[] clientsArray;
        try {
            ResponseEntity<Client[]> response = restTemplate.exchange(
                    "http://client-service/client/search-by-runs",
                    HttpMethod.POST,
                    clientRequest,
                    Client[].class
            );
            clientsArray = response.getBody();
        } catch (Exception e) {
            throw new RuntimeException("Error al obtener datos de clientes: " + e.getMessage());
        }

        if (clientsArray == null || clientsArray.length == 0) {
            return Collections.emptyList();
        }

        //It's turned into a Map for faster search
        Map<String, Client> clientMap = Arrays.stream(clientsArray)
                .collect(Collectors.toMap(Client::getRun, client -> client));

        //Fill the report
        List<OverdueClientsDTO> report = new ArrayList<>();

        for (Rent rent : overdueRents) {
            OverdueClientsDTO overdueClientsDTO = new OverdueClientsDTO();

            //Rent details
            overdueClientsDTO.setRentId(rent.getId());
            overdueClientsDTO.setRentDate(rent.getRentDate());
            overdueClientsDTO.setExpectedReturnDate(rent.getReturnDate());

            //Calculation of days overdue
            long days = ChronoUnit.DAYS.between(rent.getReturnDate(), LocalDate.now());
            overdueClientsDTO.setDaysOverdue(days);

            //Client details
            Client client = clientMap.get(rent.getClientRun());
            if (client != null) {
                overdueClientsDTO.setRun(rent.getClientRun());
                overdueClientsDTO.setName(client.getName() + " " + client.getSurname());
                overdueClientsDTO.setEmail(client.getEmail());
                overdueClientsDTO.setCellphone(client.getCellphone());
            } else {
                overdueClientsDTO.setName("Cliente Desconocido");
            }

            report.add(overdueClientsDTO);
        }

        return report;
    }

    public List<RankingReportDTO> getToolRanking(LocalDate from, LocalDate to) {
        //Build URL with dates for rent-service
        String urlRent = UriComponentsBuilder.fromHttpUrl("http://rent-service/rent/stats/ranking")
                .queryParam("from", from)
                .queryParam("to", to)
                .toUriString();

        //Get the ranking
        HttpEntity<Void> getRequestEntity = createAuthEntity(null);
        ToolRankingStat[] stats;
        try {
            ResponseEntity<ToolRankingStat[]> response = restTemplate.exchange(
                    urlRent,
                    HttpMethod.GET,
                    getRequestEntity,
                    ToolRankingStat[].class
            );
            stats = response.getBody();
        } catch (Exception e) {
            throw new RuntimeException("Error al obtener estadísticas de ranking: " + e.getMessage());
        }

        if (stats == null || stats.length == 0) {
            return Collections.emptyList();
        }

        //Extract the tool type IDs
        List<Long> typeIds = Arrays.stream(stats)
                .map(ToolRankingStat::getToolTypeId)
                .collect(Collectors.toList());

        //Get the names from inventory-service
        HttpEntity<List<Long>> inventoryRequest = createAuthEntity(typeIds);
        ToolType[] typesArray;
        try {
            ResponseEntity<ToolType[]> response = restTemplate.exchange(
                    "http://inventory-service/inventory/tool-type/get-tool-types",
                    HttpMethod.POST,
                    inventoryRequest,
                    ToolType[].class
            );
            typesArray = response.getBody();
        } catch (Exception e) {
            throw new RuntimeException("Error al obtener nombres de herramientas: " + e.getMessage());
        }

        if (typesArray == null || typesArray.length == 0) {
            return Collections.emptyList();
        }

        //Turn into a Map for faster search
        Map<Long, String> namesMap = Arrays.stream(typesArray)
                .collect(Collectors.toMap(ToolType::getId, ToolType::getName));

        //Fill the report
        List<RankingReportDTO> report = new ArrayList<>();

        for (ToolRankingStat stat : stats) {
            String name = namesMap.getOrDefault(stat.getToolTypeId(), "Desconocido (ID: " + stat.getToolTypeId() + ")");

            report.add(new RankingReportDTO(name, stat.getToolTypeId(), stat.getCount()));
        }

        return report;
    }

    //Private method for mapping te response of rent-service
    @Data
    private static class ToolRankingStat {
        private Long toolTypeId;
        private Long count;
    }

    private <T> HttpEntity<T> createAuthEntity(T body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Extraer credenciales del request actual
        String userRoles = httpServletRequest.getHeader("X-User-Roles");
        String userId = httpServletRequest.getHeader("X-User-Id");

        // Inyectar credenciales si existen
        if (userRoles != null) {
            headers.set("X-User-Roles", userRoles);
        }
        if (userId != null) {
            headers.set("X-User-Id", userId);
        }

        return new HttpEntity<>(body, headers);
    }

}
