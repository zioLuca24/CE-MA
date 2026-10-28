package com.edile.controller;

import com.edile.service.UtenteAppService;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/utenti")
@CrossOrigin(origins = "*")
public class UtenteController {

    @Autowired
    private UtenteAppService utenteService;

    @GetMapping
    public ResponseEntity<List<UtenteAppService.UtenteView>> getAll() {
        return ResponseEntity.ok(utenteService.getUtenti());
    }

    @PostMapping
    public ResponseEntity<UtenteAppService.UtenteView> create(@RequestBody CreateUtenteRequest request) {
        return ResponseEntity.ok(
                utenteService.createUtente(
                        request.getUsername(),
                        request.getPassword(),
                        request.getNomeCompleto(),
                        request.getRuolo(),
                        request.getAttivo()
                )
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<UtenteAppService.UtenteView> update(@PathVariable Long id, @RequestBody UpdateUtenteRequest request) {
        return ResponseEntity.ok(utenteService.updateUtente(id, request.getNomeCompleto(), request.getRuolo(), request.getAttivo()));
    }

    @PutMapping("/{id}/password")
    public ResponseEntity<Void> updatePassword(@PathVariable Long id, @RequestBody UpdatePasswordRequest request) {
        utenteService.updatePassword(id, request.getNuovaPassword());
        return ResponseEntity.noContent().build();
    }

    @Data
    private static class CreateUtenteRequest {
        private String username;
        private String password;
        private String nomeCompleto;
        private String ruolo;
        private Boolean attivo;
    }

    @Data
    private static class UpdateUtenteRequest {
        private String nomeCompleto;
        private String ruolo;
        private Boolean attivo;
    }

    @Data
    private static class UpdatePasswordRequest {
        private String nuovaPassword;
    }
}
