package com.edile.controller;

import com.edile.entity.AttivitaCantiere;
import com.edile.entity.AttivitaMaterialeUtilizzo;
import com.edile.entity.Cantiere;
import com.edile.entity.BollaAcquisto;
import com.edile.entity.BollaMaterialeImport;
import com.edile.entity.MaterialeCatalogo;
import com.edile.entity.MovimentoMateriale;
import com.edile.entity.SogliaMateriale;
import com.edile.entity.Turno;
import com.edile.repository.AttivitaCantiereRepository;
import com.edile.repository.AttivitaMaterialeUtilizzoRepository;
import com.edile.repository.AssegnazioneCantiereRepository;
import com.edile.repository.BollaAcquistoRepository;
import com.edile.repository.BollaMaterialeImportRepository;
import com.edile.repository.DettaglioBollaRepository;
import com.edile.repository.CantierRepository;
import com.edile.repository.MaterialeCatalogoRepository;
import com.edile.repository.MovimentoMaterialeRepository;
import com.edile.repository.SogliaMaterialeRepository;
import com.edile.repository.TurnoRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/cantieri")
@CrossOrigin(origins = "*")
public class CantierController {

    @Autowired
    private CantierRepository cantierRepository;

    @Autowired
    private AttivitaCantiereRepository attivitaRepository;

    @Autowired
    private AttivitaMaterialeUtilizzoRepository utilizzoRepository;

    @Autowired
    private AssegnazioneCantiereRepository assegnazioneRepository;

    @Autowired
    private BollaAcquistoRepository bollaAcquistoRepository;

    @Autowired
    private BollaMaterialeImportRepository bollaMaterialeImportRepository;

    @Autowired
    private DettaglioBollaRepository dettaglioBollaRepository;

    @Autowired
    private MaterialeCatalogoRepository materialeRepository;

    @Autowired
    private MovimentoMaterialeRepository movimentoRepository;

    @Autowired
    private SogliaMaterialeRepository sogliaRepository;

    @Autowired
    private TurnoRepository turnoRepository;

    @GetMapping
    public ResponseEntity<List<Cantiere>> getAll() {
        return ResponseEntity.ok(cantierRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Optional<Cantiere>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(cantierRepository.findById(id));
    }

    @GetMapping("/stato/{stato}")
    public ResponseEntity<List<Cantiere>> getByStato(@PathVariable String stato) {
        return ResponseEntity.ok(cantierRepository.findByStato(stato));
    }

    @PostMapping
    public ResponseEntity<Cantiere> create(@RequestBody Cantiere cantiere) {
        return ResponseEntity.ok(cantierRepository.save(cantiere));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Cantiere> update(@PathVariable Long id, @RequestBody Cantiere cantiere) {
        cantiere.setId(id);
        return ResponseEntity.ok(cantierRepository.save(cantiere));
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        Cantiere cantiere = ensureCantiere(id);
        cantiere.setStato("ARCHIVIATO");
        if (cantiere.getDataFine() == null) {
            cantiere.setDataFine(LocalDate.now());
        }
        cantierRepository.save(cantiere);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{cantiereId}/attivita")
    public ResponseEntity<List<AttivitaResponse>> getAttivita(@PathVariable Long cantiereId) {
        ensureCantiere(cantiereId);
        List<AttivitaCantiere> attivita = attivitaRepository.findByCantiere_Id(cantiereId);
        List<AttivitaResponse> response = attivita.stream()
                .map(this::toAttivitaResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{cantiereId}/attivita")
    public ResponseEntity<AttivitaResponse> createAttivita(@PathVariable Long cantiereId, @RequestBody AttivitaRequest request) {
        Cantiere cantiere = ensureCantiere(cantiereId);

        if (request.getTitolo() == null || request.getTitolo().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Il titolo dell'attività è obbligatorio.");
        }
        if (request.getCostoManodopera() != null && request.getCostoManodopera() < 0.0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Il costo manodopera non può essere negativo.");
        }

        AttivitaCantiere attivita = new AttivitaCantiere();
        attivita.setCantiere(cantiere);
        attivita.setTitolo(request.getTitolo().trim());
        attivita.setDescrizione(request.getDescrizione());
        attivita.setStato(request.getStato() == null ? "PIANIFICATA" : request.getStato().trim().toUpperCase());
        attivita.setDataPianificata(parseDataPianificata(request.getDataPianificata()));
        attivita.setCostoManodopera(request.getCostoManodopera() == null ? 0.0 : request.getCostoManodopera());
        AttivitaCantiere saved = attivitaRepository.save(attivita);
        return ResponseEntity.ok(toAttivitaResponse(saved));
    }

    private LocalDate parseDataPianificata(String value) {
        if (value == null || value.trim().isEmpty()) {
            return LocalDate.now();
        }
        try {
            return LocalDate.parse(value.trim());
        } catch (DateTimeParseException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Data pianificata non valida: " + value);
        }
    }

    @PutMapping("/{cantiereId}/attivita/{attivitaId}")
    public ResponseEntity<AttivitaResponse> updateAttivita(
            @PathVariable Long cantiereId,
            @PathVariable Long attivitaId,
            @RequestBody AttivitaRequest request) {

        ensureCantiere(cantiereId);
        AttivitaCantiere attivita = ensureAttivita(attivitaId, cantiereId);

        if (request.getCostoManodopera() != null && request.getCostoManodopera() < 0.0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Il costo manodopera non può essere negativo.");
        }

        if (request.getTitolo() != null && !request.getTitolo().trim().isEmpty()) {
            attivita.setTitolo(request.getTitolo().trim());
        }
        if (request.getDescrizione() != null) {
            attivita.setDescrizione(request.getDescrizione());
        }
        attivita.setStato(request.getStato() == null ? attivita.getStato() : request.getStato().trim().toUpperCase());
        if (request.getDataPianificata() != null && !request.getDataPianificata().trim().isEmpty()) {
            attivita.setDataPianificata(parseDataPianificata(request.getDataPianificata()));
        }
        if (request.getCostoManodopera() != null) {
            attivita.setCostoManodopera(request.getCostoManodopera());
        }
        if ("COMPLETATA".equals(attivita.getStato()) && attivita.getDataCompletamento() == null) {
            attivita.setDataCompletamento(LocalDate.now());
        }
        AttivitaCantiere saved = attivitaRepository.save(attivita);
        if ("COMPLETATA".equals(saved.getStato())) {
            generaScarichiAutomatici(saved);
        }
        return ResponseEntity.ok(toAttivitaResponse(saved));
    }

    @PostMapping("/{cantiereId}/attivita/{attivitaId}/materiali")
    public ResponseEntity<AttivitaResponse> addMaterialeUtilizzato(
            @PathVariable Long cantiereId,
            @PathVariable Long attivitaId,
            @RequestBody UtilizzoMaterialeRequest request) {

        if (request.getMaterialeId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Materiale obbligatorio.");
        }
        if (request.getQuantita() == null || request.getQuantita() <= 0.0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La quantità deve essere maggiore di zero.");
        }

        ensureCantiere(cantiereId);
        AttivitaCantiere attivita = ensureAttivita(attivitaId, cantiereId);
        MaterialeCatalogo materiale = materialeRepository.findById(request.getMaterialeId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Materiale non trovato: " + request.getMaterialeId()));

        double disponibile = calcolaDisponibilitaMateriale(materiale.getId());
        if (request.getQuantita() > disponibile) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Quantità non disponibile per " + materiale.getNome() + ". Disponibile: " + disponibile
            );
        }

        AttivitaMaterialeUtilizzo utilizzo = new AttivitaMaterialeUtilizzo();
        utilizzo.setAttivita(attivita);
        utilizzo.setMateriale(materiale);
        utilizzo.setQuantita(request.getQuantita());
        utilizzo.setScaricato(false);
        utilizzoRepository.save(utilizzo);

        if ("COMPLETATA".equals(attivita.getStato())) {
            generaScarichiAutomatici(attivita);
        }

        return ResponseEntity.ok(toAttivitaResponse(attivita));
    }

    @GetMapping("/{cantiereId}/attivita/{attivitaId}/movimenti")
    public ResponseEntity<List<MovimentoAttivitaResponse>> getMovimentiAttivita(
            @PathVariable Long cantiereId,
            @PathVariable Long attivitaId) {
        ensureCantiere(cantiereId);
        ensureAttivita(attivitaId, cantiereId);
        List<MovimentoMateriale> movimenti = movimentoRepository.findByAttivita_IdOrderByDataMovimentoDescIdDesc(attivitaId);
        List<MovimentoAttivitaResponse> response = movimenti.stream()
                .map(m -> new MovimentoAttivitaResponse(
                        m.getId(),
                        m.getDataMovimento() == null ? null : m.getDataMovimento().toString(),
                        m.getTipoMovimento(),
                        m.getMateriale() == null ? null : m.getMateriale().getId(),
                        m.getMateriale() == null ? null : m.getMateriale().getNome(),
                        m.getQuantita(),
                        m.getNote()
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{cantiereId}/consuntivo")
    public ResponseEntity<ConsuntivoResponse> getConsuntivo(@PathVariable Long cantiereId) {
        Cantiere cantiere = ensureCantiere(cantiereId);
        List<AttivitaCantiere> attivita = attivitaRepository.findByCantiere_Id(cantiereId);
        List<MovimentoMateriale> scarichi = movimentoRepository.findByCantiere_IdAndTipoMovimento(cantiereId, "SCARICO");

        double costoManodopera = attivita.stream()
                .mapToDouble(a -> a.getCostoManodopera() == null ? 0.0 : a.getCostoManodopera())
                .sum();

        double costoMateriali = scarichi.stream()
                .mapToDouble(s -> {
                    double q = s.getQuantita() == null ? 0.0 : s.getQuantita();
                    double p = (s.getMateriale() == null || s.getMateriale().getPrezzoMedio() == null) ? 0.0 : s.getMateriale().getPrezzoMedio();
                    return q * p;
                })
                .sum();

        double budget = cantiere.getBudget() == null ? 0.0 : cantiere.getBudget();
        double costoTotale = costoManodopera + costoMateriali;
        double scostamento = costoTotale - budget;
        double scostamentoPercentuale = budget == 0.0 ? 0.0 : (scostamento / budget) * 100.0;

        long completate = attivita.stream().filter(a -> "COMPLETATA".equals(a.getStato())).count();
        double avanzamento = attivita.isEmpty() ? 0.0 : (completate * 100.0) / attivita.size();

        return ResponseEntity.ok(new ConsuntivoResponse(
                budget,
                costoManodopera,
                costoMateriali,
                costoTotale,
                scostamento,
                scostamentoPercentuale,
                avanzamento
        ));
    }

    private void generaScarichiAutomatici(AttivitaCantiere attivita) {
        List<AttivitaMaterialeUtilizzo> utilizzi = utilizzoRepository.findByAttivita_IdAndScaricatoFalse(attivita.getId());
        validateDisponibilitaPerScarico(utilizzi);
        for (AttivitaMaterialeUtilizzo utilizzo : utilizzi) {
            MovimentoMateriale scarico = new MovimentoMateriale();
            scarico.setCantiere(attivita.getCantiere());
            scarico.setAttivita(attivita);
            scarico.setMateriale(utilizzo.getMateriale());
            scarico.setTipoMovimento("SCARICO");
            scarico.setQuantita(utilizzo.getQuantita());
            scarico.setDataMovimento(LocalDate.now());
            scarico.setNote("Scarico automatico attività #" + attivita.getId() + ": " + attivita.getTitolo());
            movimentoRepository.save(scarico);
            utilizzo.setScaricato(true);
            utilizzoRepository.save(utilizzo);
            aggiornaQuantitaMateriale(utilizzo.getMateriale().getId());
        }
    }

    private void aggiornaQuantitaMateriale(Long materialeId) {
        MaterialeCatalogo materiale = materialeRepository.findById(materialeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Materiale non trovato: " + materialeId));
        List<MovimentoMateriale> movimenti = movimentoRepository.findByMateriale_Id(materialeId);
        double carico = movimenti.stream()
                .filter(m -> "CARICO".equals(tipo(m.getTipoMovimento())))
                .mapToDouble(m -> m.getQuantita() == null ? 0.0 : m.getQuantita())
                .sum();
        double scarico = movimenti.stream()
                .filter(m -> "SCARICO".equals(tipo(m.getTipoMovimento())))
                .mapToDouble(m -> m.getQuantita() == null ? 0.0 : m.getQuantita())
                .sum();
        materiale.setQuantitaAcquistataTotale(carico);
        materiale.setQuantitaUtilizzataTotale(scarico);
        materiale.setQuantitaResidua(carico - scarico);
        materialeRepository.save(materiale);
    }

    private void validateDisponibilitaPerScarico(List<AttivitaMaterialeUtilizzo> utilizzi) {
        Map<Long, Double> richiestoPerMateriale = new LinkedHashMap<>();
        for (AttivitaMaterialeUtilizzo utilizzo : utilizzi) {
            if (utilizzo.getMateriale() == null || utilizzo.getMateriale().getId() == null) {
                continue;
            }
            double q = utilizzo.getQuantita() == null ? 0.0 : utilizzo.getQuantita();
            richiestoPerMateriale.put(
                    utilizzo.getMateriale().getId(),
                    richiestoPerMateriale.getOrDefault(utilizzo.getMateriale().getId(), 0.0) + q
            );
        }

        for (Map.Entry<Long, Double> entry : richiestoPerMateriale.entrySet()) {
            MaterialeCatalogo materiale = materialeRepository.findById(entry.getKey())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Materiale non trovato: " + entry.getKey()));
            double disponibile = calcolaDisponibilitaMateriale(materiale.getId());
            if (entry.getValue() > disponibile) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Giacenza insufficiente per " + materiale.getNome() + ". Richiesta: " + entry.getValue() + ", disponibile: " + disponibile
                );
            }
        }
    }

    private double calcolaDisponibilitaMateriale(Long materialeId) {
        List<MovimentoMateriale> movimenti = movimentoRepository.findByMateriale_Id(materialeId);
        double carico = movimenti.stream()
                .filter(m -> "CARICO".equals(tipo(m.getTipoMovimento())))
                .mapToDouble(m -> m.getQuantita() == null ? 0.0 : m.getQuantita())
                .sum();
        double scarico = movimenti.stream()
                .filter(m -> "SCARICO".equals(tipo(m.getTipoMovimento())))
                .mapToDouble(m -> m.getQuantita() == null ? 0.0 : m.getQuantita())
                .sum();
        return carico - scarico;
    }

    private Cantiere ensureCantiere(Long cantiereId) {
        return cantierRepository.findById(cantiereId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cantiere non trovato: " + cantiereId));
    }

    private AttivitaCantiere ensureAttivita(Long attivitaId, Long cantiereId) {
        AttivitaCantiere attivita = attivitaRepository.findById(attivitaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Attività non trovata: " + attivitaId));
        if (attivita.getCantiere() == null || !cantiereId.equals(attivita.getCantiere().getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "L'attività non appartiene al cantiere selezionato.");
        }
        return attivita;
    }

    private AttivitaResponse toAttivitaResponse(AttivitaCantiere attivita) {
        List<AttivitaMaterialeUtilizzo> utilizzi = utilizzoRepository.findByAttivita_Id(attivita.getId());
        List<MaterialeUtilizzoResponse> materiali = utilizzi.stream()
                .map(u -> new MaterialeUtilizzoResponse(
                        u.getId(),
                        u.getMateriale() == null ? null : u.getMateriale().getId(),
                        u.getMateriale() == null ? null : u.getMateriale().getNome(),
                        u.getQuantita(),
                        u.getScaricato()
                ))
                .collect(Collectors.toList());

        return new AttivitaResponse(
                attivita.getId(),
                attivita.getTitolo(),
                attivita.getDescrizione(),
                attivita.getStato(),
                attivita.getDataPianificata() == null ? null : attivita.getDataPianificata().toString(),
                attivita.getDataCompletamento() == null ? null : attivita.getDataCompletamento().toString(),
                attivita.getCostoManodopera() == null ? 0.0 : attivita.getCostoManodopera(),
                materiali
        );
    }

    private String tipo(String value) {
        return value == null ? "CARICO" : value.trim().toUpperCase();
    }

    @Data
    private static class AttivitaRequest {
        private String titolo;
        private String descrizione;
        private String stato;
        private String dataPianificata;
        private Double costoManodopera;
    }

    @Data
    private static class UtilizzoMaterialeRequest {
        private Long materialeId;
        private Double quantita;
    }

    @Data
    @AllArgsConstructor
    private static class MaterialeUtilizzoResponse {
        private Long utilizzoId;
        private Long materialeId;
        private String materialeNome;
        private Double quantita;
        private Boolean scaricato;
    }

    @Data
    @AllArgsConstructor
    private static class AttivitaResponse {
        private Long attivitaId;
        private String titolo;
        private String descrizione;
        private String stato;
        private String dataPianificata;
        private String dataCompletamento;
        private Double costoManodopera;
        private List<MaterialeUtilizzoResponse> materialiUtilizzati;
    }

    @Data
    @AllArgsConstructor
    private static class ConsuntivoResponse {
        private Double budgetCantiere;
        private Double costoManodopera;
        private Double costoMateriali;
        private Double costoTotale;
        private Double scostamento;
        private Double scostamentoPercentuale;
        private Double avanzamentoPercentuale;
    }

    @Data
    @AllArgsConstructor
    private static class MovimentoAttivitaResponse {
        private Long movimentoId;
        private String dataMovimento;
        private String tipoMovimento;
        private Long materialeId;
        private String materialeNome;
        private Double quantita;
        private String note;
    }
}
