package com.toolrent.reportservice.Service;

import com.toolrent.reportservice.DTO.RentReportDTO;
import com.toolrent.reportservice.Model.Client;
import com.toolrent.reportservice.Model.Rent;
import com.toolrent.reportservice.Model.RentXToolItem;
import com.toolrent.reportservice.Model.ToolType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class ReportService {
    private RestTemplate restTemplate;

    @Autowired
    public ReportService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public List<RentReportDTO> getActiveRentsReport() {
        //Get the active rents list
        Rent[] rents = restTemplate.getForObject("http://rent-service/rent/status/" + "Activo", Rent[].class);

        if(rents == null){
            throw new RuntimeException("No se encontraron arriendos activos");
        }

        //Fill the report
        List<RentReportDTO> rentReport = new ArrayList<>();
        for (Rent rent : rents) {
            RentReportDTO rentReportDTO = new RentReportDTO();
            rentReportDTO.setRentId(rent.getId());
            rentReportDTO.setRentDate(rent.getRentDate());
            rentReportDTO.setValidity(rent.getValidity());

            //Call to client-service
            try {
                Client client = restTemplate.getForObject("http://client-service/client/" + rent.getClientRun(), Client.class);
                if(client == null){
                    throw new RuntimeException("No se encontró el cliente con RUN: " +  rent.getClientRun() + " en la base de datos");
                }
                rentReportDTO.setClientName(client.getName()+ " " + client.getSurname());
                rentReportDTO.setClientRun(client.getRun());
            } catch (Exception e) {
                rentReportDTO.setClientName("Desconocido (" + rent.getClientRun() + ")");
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

            rentReportDTO.setToolNames(names);

            rentReport.add(rentReportDTO);
        }

        return rentReport;
    }

}
