package com.edile.controller;

import com.edile.entity.AssegnazioneCantiere;
import com.edile.repository.AssegnazioneCantiereRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/assegnazioni")
@CrossOrigin(origins = "*")
public class AssegnazioneCantiereController {

    @Autowired
    private AssegnazioneCantiereRepository assegnazioneRepository;

    @GetMapping
    public ResponseEntity<List<AssegnazioneCantiere>> getAll() {
        return ResponseEntity.ok(assegnazioneRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Optional<AssegnazioneCantiere>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(assegnazioneRepository.findById(id));
    }

    @GetMapping("/cantiere/{cantiereId}")
    public ResponseEntity<List<AssegnazioneCantiere>> getByCantiere(@PathVariable Long cantiereId) {
        return ResponseEntity.ok(assegnazioneRepository.findByCantiere_Id(cantiereId));
    }

    @GetMapping("/dipendente/{dipendenteId}")
    public ResponseEntity<List<AssegnazioneCantiere>> getByDipendente(@PathVariable Long dipendenteId) {
        return ResponseEntity.ok(assegnazioneRepository.findByDipendente_Id(dipendenteId));
    }

    @GetMapping("/stato/{stato}")
    public ResponseEntity<List<AssegnazioneCantiere>> getByStato(@PathVariable String stato) {
        return ResponseEntity.ok(assegnazioneRepository.findByStato(stato));
    }

    @PostMapping
    public ResponseEntity<AssegnazioneCantiere> create(@RequestBody AssegnazioneCantiere assegnazione) {
        return ResponseEntity.ok(assegnazioneRepository.save(assegnazione));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AssegnazioneCantiere> update(@PathVariable Long id, @RequestBody AssegnazioneCantiere assegnazione) {
        assegnazione.setId(id);
        return ResponseEntity.ok(assegnazioneRepository.save(assegnazione));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        assegnazioneRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
