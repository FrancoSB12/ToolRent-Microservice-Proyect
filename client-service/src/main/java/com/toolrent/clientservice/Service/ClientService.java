package com.toolrent.clientservice.Service;

import com.toolrent.clientservice.Entity.ClientEntity;
import com.toolrent.clientservice.Repository.ClientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class ClientService {
    ClientValidationService clientValidationService;
    ClientRepository clientRepository;

    @Autowired
    public ClientService(ClientValidationService clientValidationService, ClientRepository clientRepository) {
        this.clientValidationService = clientValidationService;
        this.clientRepository = clientRepository;
    }

    public List<ClientEntity> getAllClients() {
        return clientRepository.findAll();
    }

    public Optional<ClientEntity> getClientByRun(String run) {
        return clientRepository.findById(run);
    }

    public List<ClientEntity> getClientsByStatus(String status) {
        List<ClientEntity> clients = clientRepository.findByStatus(status);
        return clients != null ? clients : Collections.emptyList();
    }

    public boolean exists(String run){
        return clientRepository.existsById(run);
    }

    public ClientEntity saveClient(ClientEntity client){
        return clientRepository.save(client);
    }

    public ClientEntity updateClient(String run, ClientEntity client){
        //The client is searched in the database
        ClientEntity dbClient = clientRepository.findById(run)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado en la base de datos"));

        //Each attribute is checked to see which one was updated
        if(client.getName() != null){
            if(clientValidationService.isInvalidName(client.getName())){
                throw new IllegalArgumentException("Nombre del cliente invalido");
            }
            dbClient.setName(client.getName());
        }

        if(client.getSurname() != null){
            if(clientValidationService.isInvalidName(client.getSurname())){
                throw new IllegalArgumentException("Apellido del cliente invalido");
            }
            dbClient.setSurname(client.getSurname());
        }

        if(client.getEmail() != null){
            if(clientValidationService.isInvalidEmail(client.getEmail())){
                throw new IllegalArgumentException("Email del cliente invalido");
            }
            dbClient.setEmail(client.getEmail());
        }

        if(client.getCellphone() != null){
            if(clientValidationService.isInvalidCellphone(client.getCellphone())){
                throw new IllegalArgumentException("Teléfono del cliente invalido");
            }
            dbClient.setCellphone(client.getCellphone());
        }

        if(client.getStatus() != null){
            if(clientValidationService.isInvalidStatus(client.getStatus())){
                throw new IllegalArgumentException("Estado del cliente invalido");
            }
            dbClient.setStatus(client.getStatus());
        }

        if(client.getDebt() != null){
            if(clientValidationService.isInvalidDebtOrActiveRents(client.getDebt())){
                throw new IllegalArgumentException("Valor de la deuda del cliente invalido");
            }
            dbClient.setDebt(client.getDebt());
        }

        if(client.getActiveRents() != null){
            if(clientValidationService.isInvalidDebtOrActiveRents(client.getActiveRents())){
                throw new IllegalArgumentException("Número de herramientas arrendadas del cliente invalido");
            }
            dbClient.setActiveRents(client.getActiveRents());
        }

        return clientRepository.save(dbClient);
    }

    public boolean deleteClientByRun(String run){
        if(clientRepository.existsById(run)){
            clientRepository.deleteById(run);
            return true;

        } else{
            return false;
        }
    }

}
