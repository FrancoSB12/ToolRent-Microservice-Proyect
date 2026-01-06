package com.toolrent.feeservice.Service;

import com.toolrent.feeservice.DTO.ApplyLateFeeRequest;
import com.toolrent.feeservice.DTO.UpdateLateReturnFeeRequest;
import com.toolrent.feeservice.Entity.LateReturnFeeEntity;
import com.toolrent.feeservice.Model.Client;
import com.toolrent.feeservice.Repository.LateReturnFeeRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
public class FeeService {
    private final LateReturnFeeRepository lateReturnFeeRepository;
    private final RestTemplate restTemplate;
    HttpServletRequest httpServletRequest;

    @Autowired
    public FeeService(LateReturnFeeRepository lateReturnFeeRepository, RestTemplate restTemplate, HttpServletRequest httpServletRequest) {
        this.lateReturnFeeRepository = lateReturnFeeRepository;
        this.restTemplate = restTemplate;
        this.httpServletRequest = httpServletRequest;
    }

    private static final Integer DEFAULT_FEE = 5000;    //Default value in case the db is empty

    public Integer getLateReturnFee() {
        return lateReturnFeeRepository.findById(1L)
                .map(LateReturnFeeEntity::getCurrentLateReturnFee)
                .orElse(DEFAULT_FEE);
    }

    @Transactional
    public void updateGlobalLateReturnFee(UpdateLateReturnFeeRequest newFee) {
        LateReturnFeeEntity config = lateReturnFeeRepository.findById(1L)
                .orElse(new LateReturnFeeEntity());

        config.setId(1L); //Ensure that it is the ID of the fee
        config.setCurrentLateReturnFee(newFee.getNewLateReturnFee());
        lateReturnFeeRepository.save(config);
    }

    @Transactional
    public void applyLateReturnFee(ApplyLateFeeRequest lateFeeRequest) {
        Integer lateReturnFee = lateFeeRequest.getLateReturnFee();
        Client client = lateFeeRequest.getClient();

        int daysBetween = (int) ChronoUnit.DAYS.between(lateFeeRequest.getReturnDate(), LocalDate.now());
        Integer lateReturnFeeDays = daysBetween * lateReturnFee;

        try {
            client.setDebt(client.getDebt() + lateReturnFeeDays);
            HttpEntity<Client> requestEntity = createAuthEntity(client);
            restTemplate.exchange(
                    "http://client-service/client/" + client.getRun(),
                    HttpMethod.PUT,
                    requestEntity,
                    Void.class
            );
        } catch (RestClientException e) {
            throw new RuntimeException("Error actualizando datos del cliente.");
        }
    }

    @Transactional
    public void chargeClientFee(Client client, Integer fee) {
        client.setDebt(client.getDebt() + fee);
        client.setStatus("Restringido");

        HttpEntity<Client> requestEntity = createAuthEntity(client);

        try {
            restTemplate.exchange(
                    "http://client-service/client/" + client.getRun(),
                    HttpMethod.PUT,
                    requestEntity,
                    Void.class
            );
        } catch (RestClientException e) {
            throw new RuntimeException("Error al actualizar deuda del cliente: " + e.getMessage());
        }
    }

    //Private method
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
