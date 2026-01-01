package com.toolrent.reportservice.Service;

import com.toolrent.reportservice.DTO.ActiveRentsReportDTO;
import com.toolrent.reportservice.DTO.OverdueClientsDTO;
import com.toolrent.reportservice.DTO.RankingReportDTO;
import com.toolrent.reportservice.Model.Client;
import com.toolrent.reportservice.Model.Rent;
import com.toolrent.reportservice.Model.RentXToolItem;
import com.toolrent.reportservice.Model.ToolType;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    public ReportService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public List<ActiveRentsReportDTO> getActiveRentsReport() {
        //Get the active rents list
        Rent[] rents = restTemplate.getForObject("http://rent-service/rent/status/" + "Activo", Rent[].class);

        if(rents == null){
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
                Client client = restTemplate.getForObject("http://client-service/client/" + rent.getClientRun(), Client.class);
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

            ToolType[] toolTypes = restTemplate.postForObject("http://inventory-service/inventory/tool-item/get-tool-types", toolItemIds, ToolType[].class);

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
        Rent[] overdueRents = restTemplate.getForObject("http://rent-service/rent/overdue", Rent[].class);

        if (overdueRents == null || overdueRents.length == 0) {
            return Collections.emptyList();
        }

        //Get all the client runs
        List<String> runsToSearch = Arrays.stream(overdueRents)
                .map(Rent::getClientRun)
                .distinct()
                .collect(Collectors.toList());

        //Get all client details from client-Service
        Client[] clientsArray = restTemplate.postForObject("http://client-service/client/search-by-runs", runsToSearch, Client[].class);

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
        ToolRankingStat[] stats = restTemplate.getForObject(urlRent, ToolRankingStat[].class);

        if (stats == null || stats.length == 0) {
            return Collections.emptyList();
        }

        //Extract the tool type IDs
        List<Long> typeIds = Arrays.stream(stats)
                .map(ToolRankingStat::getToolTypeId)
                .collect(Collectors.toList());

        //Get the names from inventory-service
        ToolType[] typesArray = restTemplate.postForObject("http://inventory-service/inventory/tool-item/get-tool-types", typeIds, ToolType[].class);

        if (typesArray == null || typesArray.length == 0) {
            return Collections.emptyList();
        }

        //Turn into a Map for faster search
        Map<Long, String> namesMap = Arrays.stream(typesArray)
                .collect(Collectors.toMap(ToolType::getId, ToolType::getName));

        //Fill the report
        List<RankingReportDTO> report = new ArrayList<>();

        for (ToolRankingStat stat : stats) {
            String name = namesMap.getOrDefault(stat.getToolTypeId(), "Herramienta Desconocida");

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

}
