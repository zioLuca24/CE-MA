package com.edile.controller;

import com.edile.entity.Dipendente;
import com.edile.repository.DipendenteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/dipendenti")
@CrossOrigin(origins = "*")
public class DipendenteController {

    @Autowired
    private DipendenteRepository dipendenteRepository;

    @GetMapping
    public ResponseEntity<List<Dipendente>> getAll() {
        return ResponseEntity.ok(dipendenteRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Optional<Dipendente>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(dipendenteRepository.findById(id));
    }

    @GetMapping("/stato/{stato}")
    public ResponseEntity<List<Dipendente>> getByStato(@PathVariable String stato) {
        return ResponseEntity.ok(dipendenteRepository.findByStato(stato));
    }

    @PostMapping
    public ResponseEntity<Dipendente> create(@RequestBody Dipendente dipendente) {
        return ResponseEntity.ok(dipendenteRepository.save(dipendente));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Dipendente> update(@PathVariable Long id, @RequestBody Dipendente dipendente) {
        dipendente.setId(id);
        return ResponseEntity.ok(dipendenteRepository.save(dipendente));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        dipendenteRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

}
