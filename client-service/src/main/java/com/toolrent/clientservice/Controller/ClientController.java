package com.toolrent.clientservice.Controller;

import com.toolrent.clientservice.Entity.ClientEntity;
import com.toolrent.clientservice.Service.ClientService;
import com.toolrent.clientservice.Service.ClientValidationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/client")
public class ClientController {
    private final ClientService clientService;
    private final ClientValidationService clientValidationService;

    @Autowired
    public ClientController(ClientService clientService, ClientValidationService clientValidationService) {
        this.clientService = clientService;
        this.clientValidationService = clientValidationService;
    }

    //Create client
    @PreAuthorize("hasRole('Admin')")
    @PostMapping
    public ResponseEntity<?> createClient(@RequestBody ClientEntity client){
        //First, it's verified that the client doesn't exist
        if(clientService.exists(client.getRun())){
            return new ResponseEntity<>("El cliente ya existe", HttpStatus.CONFLICT);
        }

        //Then, the data is validated for accuracy
        if(clientValidationService.isInvalidRun(client.getRun())){
            return new ResponseEntity<>("Run del cliente invalido", HttpStatus.BAD_REQUEST);
        }

        if(clientValidationService.isInvalidName(client.getName())){
            return new ResponseEntity<>("Nombre del cliente invalido", HttpStatus.BAD_REQUEST);
        }

        if(clientValidationService.isInvalidName(client.getSurname())){
            return new ResponseEntity<>("Apellido del cliente invalido", HttpStatus.BAD_REQUEST);
        }

        if(clientValidationService.isInvalidEmail(client.getEmail())){
            return new ResponseEntity<>("Email del cliente invalido", HttpStatus.BAD_REQUEST);
        }

        if(clientValidationService.isInvalidCellphone(client.getCellphone())){
            return new ResponseEntity<>("Teléfono del cliente invalido", HttpStatus.BAD_REQUEST);
        }

        ClientEntity newClient = clientService.saveClient(client);
        return new ResponseEntity<>(newClient, HttpStatus.CREATED);
    }

    //Get client
    @PreAuthorize("hasAnyRole('Employee','Admin')")
    @GetMapping
    public ResponseEntity<List<ClientEntity>> getAllClients(){
        List<ClientEntity> clients = clientService.getAllClients();
        return new ResponseEntity<>(clients, HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('Employee','Admin')")
    @GetMapping("/{clientRun}")
    public ResponseEntity<ClientEntity> getClientByRun(@PathVariable("clientRun") String run){
        return clientService.getClientByRun(run)
                .map(client -> new ResponseEntity<>(client, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PreAuthorize("hasAnyRole('Employee','Admin')")
    @GetMapping("/status/{status}")
    public ResponseEntity<List<ClientEntity>> getClientsByStatus(@PathVariable String status){
        List<ClientEntity> clients = clientService.getClientsByStatus(status);
        return new ResponseEntity<>(clients, HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('Employee','Admin')")
    @PostMapping("/search-by-runs")
    public ResponseEntity<List<ClientEntity>> getClientsByRuns(@RequestBody List<String> runs){
        List<ClientEntity> clients = clientService.getClientsByRunIn(runs);
        return new ResponseEntity<>(clients, HttpStatus.OK);
    }

    //Update client
    @PreAuthorize("hasRole('Admin')")
    @PutMapping("/{run}")
    public ResponseEntity<?> updateClient(@PathVariable String run, @RequestBody ClientEntity client){
        try {
            //Verify that the client has a run and isn't null
            if (client.getRun() == null || client.getRun().isEmpty()) {
                return new ResponseEntity<>("El run no puede estar vacío o ser nulo", HttpStatus.BAD_REQUEST);
            }

            //Verify that the client exist in the database
            if (clientService.getClientByRun(run).isEmpty()) {
                return new ResponseEntity<>("El cliente no existe en la base de datos", HttpStatus.NOT_FOUND);
            }

            ClientEntity updatedClient = clientService.updateClient(run, client);
            return new ResponseEntity<>(updatedClient, HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PreAuthorize("hasRole('Admin')")
    @DeleteMapping("/{run}")
    public ResponseEntity<String> deleteClient(@PathVariable String run){
        boolean deletedClient = clientService.deleteClientByRun(run);
        return deletedClient ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}
