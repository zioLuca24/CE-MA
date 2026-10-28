package com.edile.controller;

import com.edile.entity.AssegnazioneCantiere;
import com.edile.entity.MovimentoMateriale;
import com.edile.entity.Turno;
import com.edile.repository.AssegnazioneCantiereRepository;
import com.edile.repository.CantierRepository;
import com.edile.repository.MovimentoMaterialeRepository;
import com.edile.repository.TurnoRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/monitoraggio")
@CrossOrigin(origins = "*")
public class MonitoraggioController {

    @Autowired
    private CantierRepository cantierRepository;

    @Autowired
    private TurnoRepository turnoRepository;

    @Autowired
    private AssegnazioneCantiereRepository assegnazioneRepository;

    @Autowired
    private MovimentoMaterialeRepository movimentoRepository;

    @GetMapping("/cantieri/{cantiereId}")
    public ResponseEntity<CantiereMonitoraggioResponse> getMonitoraggioCantiere(@PathVariable Long cantiereId) {
        if (!cantierRepository.existsById(cantiereId)) {
            return ResponseEntity.notFound().build();
        }

        List<Turno> turni = turnoRepository.findByCantiere_Id(cantiereId);
        List<AssegnazioneCantiere> assegnazioni = assegnazioneRepository.findByCantiere_Id(cantiereId);
        List<MovimentoMateriale> movimenti = movimentoRepository.findByCantiere_Id(cantiereId);

        double oreTotali = turni.stream()
                .filter(turno -> turno.getOraInizio() != null && turno.getOraFine() != null)
                .mapToLong(turno -> Duration.between(turno.getOraInizio(), turno.getOraFine()).toMinutes())
                .sum() / 60.0;

        long dipendentiAttivi = assegnazioni.stream()
                .filter(a -> a.getDipendente() != null)
                .map(a -> a.getDipendente().getId())
                .distinct()
                .count();

        double materialeCaricato = movimenti.stream()
                .filter(m -> m.getQuantita() != null)
                .filter(m -> "CARICO".equalsIgnoreCase(m.getTipoMovimento()))
                .mapToDouble(MovimentoMateriale::getQuantita)
                .sum();

        double materialeUsato = movimenti.stream()
                .filter(m -> m.getQuantita() != null)
                .filter(m -> "SCARICO".equalsIgnoreCase(m.getTipoMovimento()))
                .mapToDouble(MovimentoMateriale::getQuantita)
                .sum();

        List<String> workflowAttivi = turni.stream()
                .map(Turno::getStatoAttivita)
                .filter(stato -> stato != null && !stato.trim().isEmpty())
                .distinct()
                .collect(Collectors.toList());

        CantiereMonitoraggioResponse response = new CantiereMonitoraggioResponse(
                cantiereId,
                oreTotali,
                dipendentiAttivi,
                materialeCaricato,
                materialeUsato,
                workflowAttivi
        );

        return ResponseEntity.ok(response);
    }

    @Data
    @AllArgsConstructor
    private static class CantiereMonitoraggioResponse {
        private Long cantiereId;
        private Double oreTotaliLavorate;
        private Long dipendentiAssegnati;
        private Double materialiCaricati;
        private Double materialiUsati;
        private List<String> statiWorkflowPresenti;
    }
}
