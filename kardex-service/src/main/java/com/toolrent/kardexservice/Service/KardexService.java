package com.toolrent.kardexservice.Service;

import com.toolrent.kardexservice.DTO.CreateKardexRequest;
import com.toolrent.kardexservice.Entity.KardexEntity;
import com.toolrent.kardexservice.Repository.KardexRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class KardexService {
    KardexRepository kardexRepository;

    @Autowired
    public KardexService(KardexRepository kardexRepository) {
        this.kardexRepository = kardexRepository;
    }

    public List<KardexEntity> getAllKardex(){
        return kardexRepository.findAll();
    }

    public Optional<List<KardexEntity>> getKardexByToolTypeName(String toolTypeName){
        return kardexRepository.findByToolTypeName(toolTypeName);
    }

    public List<KardexEntity> getKardexByDateRange(LocalDate startDate, LocalDate endDate){
        return kardexRepository.findByDateBetween(startDate, endDate);
    }

    public void createKardexEntry(CreateKardexRequest request){
        KardexEntity kardex = new KardexEntity();
        kardex.setOperationType(request.getOperationType());
        kardex.setDate(LocalDate.now());
        kardex.setStockInvolved(request.getStock());
        kardex.setToolTypeName(request.getToolTypeName());
        kardex.setEmployeeRun(request.getEmployeeRun());
        kardex.setEmployeeNameSnapshot(request.getEmployeeName());
        kardexRepository.save(kardex);
    }
}

