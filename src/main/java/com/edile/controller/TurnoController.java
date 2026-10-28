package com.edile.controller;

import com.edile.entity.Turno;
import com.edile.repository.TurnoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/turni")
@CrossOrigin(origins = "*")
public class TurnoController {

    @Autowired
    private TurnoRepository turnoRepository;

    @GetMapping
    public ResponseEntity<List<Turno>> getAll() {
        return ResponseEntity.ok(turnoRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Optional<Turno>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(turnoRepository.findById(id));
    }

    @GetMapping("/cantiere/{cantiereId}")
    public ResponseEntity<List<Turno>> getByCantiere(@PathVariable Long cantiereId) {
        return ResponseEntity.ok(turnoRepository.findByCantiere_Id(cantiereId));
    }

    @GetMapping("/dipendente/{dipendenteId}")
    public ResponseEntity<List<Turno>> getByDipendente(@PathVariable Long dipendenteId) {
        return ResponseEntity.ok(turnoRepository.findByDipendente_Id(dipendenteId));
    }

    @GetMapping("/cantiere/{cantiereId}/ore")
    public ResponseEntity<Double> getOreByCantiere(
            @PathVariable Long cantiereId,
            @RequestParam("dal") String dal,
            @RequestParam("al") String al) {
        LocalDate dataInizio = LocalDate.parse(dal);
        LocalDate dataFine = LocalDate.parse(al);
        List<Turno> turni = turnoRepository.findByCantiere_IdAndDataBetween(cantiereId, dataInizio, dataFine);
        double totaleOre = calcolaTotaleOre(turni);
        return ResponseEntity.ok(totaleOre);
    }

    @PostMapping
    public ResponseEntity<Turno> create(@RequestBody Turno turno) {
        if (turno.getStatoAttivita() == null || turno.getStatoAttivita().trim().isEmpty()) {
            turno.setStatoAttivita("ASSEGNATO");
        }
        return ResponseEntity.ok(turnoRepository.save(turno));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Turno> update(@PathVariable Long id, @RequestBody Turno turno) {
        turno.setId(id);
        return ResponseEntity.ok(turnoRepository.save(turno));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        turnoRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private double calcolaTotaleOre(List<Turno> turni) {
        double totaleMinuti = turni.stream()
                .filter(turno -> turno.getOraInizio() != null && turno.getOraFine() != null)
                .mapToLong(turno -> Duration.between(turno.getOraInizio(), turno.getOraFine()).toMinutes())
                .sum();
        return totaleMinuti / 60.0;
    }
}
