package com.edile.controller;

import com.edile.entity.AttivitaMaterialeUtilizzo;
import com.edile.entity.AttivitaCantiere;
import com.edile.entity.Cantiere;
import com.edile.entity.MaterialeCatalogo;
import com.edile.entity.MovimentoMateriale;
import com.edile.entity.SogliaMateriale;
import com.edile.repository.AttivitaCantiereRepository;
import com.edile.repository.AttivitaMaterialeUtilizzoRepository;
import com.edile.repository.CantierRepository;
import com.edile.repository.MaterialeCatalogoRepository;
import com.edile.repository.MovimentoMaterialeRepository;
import com.edile.repository.SogliaMaterialeRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/analisi")
@CrossOrigin(origins = "*")
public class AnalisiSprechiController {

    @Autowired
    private SogliaMaterialeRepository sogliaRepository;

    @Autowired
    private MaterialeCatalogoRepository materialeRepository;

    @Autowired
    private CantierRepository cantiereRepository;

    @Autowired
    private MovimentoMaterialeRepository movimentoRepository;

    @Autowired
    private AttivitaCantiereRepository attivitaCantiereRepository;

    @Autowired
    private AttivitaMaterialeUtilizzoRepository utilizzoRepository;

    @GetMapping("/soglie")
    public ResponseEntity<List<SogliaResponse>> getSoglie() {
        return ResponseEntity.ok(sogliaRepository.findAll().stream().map(this::toSogliaResponse).collect(Collectors.toList()));
    }

    @PostMapping("/soglie")
    public ResponseEntity<SogliaResponse> createSoglia(@RequestBody SogliaRequest request) {
        SogliaMateriale soglia = new SogliaMateriale();
        applyRequest(soglia, request);
        if (soglia.getAttiva() == null) {
            soglia.setAttiva(Boolean.TRUE);
        }
        return ResponseEntity.ok(toSogliaResponse(sogliaRepository.save(soglia)));
    }

    @PutMapping("/soglie/{id}")
    public ResponseEntity<SogliaResponse> updateSoglia(@PathVariable Long id, @RequestBody SogliaRequest request) {
        SogliaMateriale soglia = sogliaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Soglia non trovata: " + id));
        applyRequest(soglia, request);
        return ResponseEntity.ok(toSogliaResponse(sogliaRepository.save(soglia)));
    }

    @DeleteMapping("/soglie/{id}")
    public ResponseEntity<Void> deleteSoglia(@PathVariable Long id) {
        sogliaRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/alert-sprechi")
    public ResponseEntity<List<AlertSprecoResponse>> getAlertSprechi(
            @RequestParam(value = "cantiereId", required = false) Long cantiereId) {

        List<SogliaMateriale> soglie = sogliaRepository.findByAttivaTrue();
        if (cantiereId != null) {
            soglie = soglie.stream()
                    .filter(s -> s.getCantiere() == null || cantiereId.equals(s.getCantiere().getId()))
                    .collect(Collectors.toList());
        }

        List<AlertSprecoResponse> alert = new ArrayList<>();
        for (SogliaMateriale soglia : soglie) {
            List<MovimentoMateriale> movimenti = getMovimentiBySoglia(soglia);
            double giacenzaAttuale = calcolaGiacenza(soglia, movimenti);
            double consumo30gg = calcolaConsumo30gg(movimenti);
            String statoGiacenza = statoGiacenza(giacenzaAttuale, soglia.getGiacenzaMinima(), soglia.getGiacenzaCritica());

            if ("ATTENZIONE".equals(statoGiacenza) || "CRITICO".equals(statoGiacenza)) {
                alert.add(new AlertSprecoResponse(
                        "SCORTA_BASSA",
                        "CRITICO".equals(statoGiacenza) ? "CRITICO" : "ALTA",
                        soglia.getMateriale().getId(),
                        soglia.getMateriale().getNome(),
                        soglia.getCantiere() == null ? null : soglia.getCantiere().getId(),
                        soglia.getCantiere() == null ? null : soglia.getCantiere().getNome(),
                        "Giacenza sotto soglia",
                        giacenzaAttuale,
                        soglia.getGiacenzaMinima(),
                        "Riduci consumo o pianifica riordino immediato."
                ));
            }

            if (soglia.getConsumoMensileMassimo() != null && consumo30gg > soglia.getConsumoMensileMassimo()) {
                alert.add(new AlertSprecoResponse(
                        "CONSUMO_ECCESSIVO",
                        severitaConsumo(consumo30gg, soglia.getConsumoMensileMassimo()),
                        soglia.getMateriale().getId(),
                        soglia.getMateriale().getNome(),
                        soglia.getCantiere() == null ? null : soglia.getCantiere().getId(),
                        soglia.getCantiere() == null ? null : soglia.getCantiere().getNome(),
                        "Consumo ultimi 30 giorni sopra soglia",
                        consumo30gg,
                        soglia.getConsumoMensileMassimo(),
                        "Verifica attività, errori operativi e possibili sprechi nel cantiere."
                ));
            }
        }

        return ResponseEntity.ok(alert);
    }

    @GetMapping("/indicatori-materiali")
    public ResponseEntity<List<IndicatoreMaterialeResponse>> getIndicatoriMateriali(
            @RequestParam(value = "cantiereId", required = false) Long cantiereId) {

        List<SogliaMateriale> soglie = sogliaRepository.findByAttivaTrue();
        if (cantiereId != null) {
            soglie = soglie.stream()
                    .filter(s -> s.getCantiere() == null || cantiereId.equals(s.getCantiere().getId()))
                    .collect(Collectors.toList());
        }

        List<IndicatoreMaterialeResponse> rows = new ArrayList<>();
        for (SogliaMateriale soglia : soglie) {
            List<MovimentoMateriale> movimenti = getMovimentiBySoglia(soglia);
            double giacenzaAttuale = calcolaGiacenza(soglia, movimenti);
            String stato = statoGiacenza(giacenzaAttuale, soglia.getGiacenzaMinima(), soglia.getGiacenzaCritica());
            rows.add(new IndicatoreMaterialeResponse(
                    soglia.getMateriale().getId(),
                    soglia.getMateriale().getNome(),
                    soglia.getCantiere() == null ? null : soglia.getCantiere().getId(),
                    soglia.getCantiere() == null ? "Globale" : soglia.getCantiere().getNome(),
                    giacenzaAttuale,
                    soglia.getGiacenzaMinima(),
                    soglia.getGiacenzaCritica(),
                    stato,
                    punteggioRischio(stato, giacenzaAttuale, soglia.getGiacenzaCritica(), soglia.getGiacenzaMinima())
            ));
        }

        rows.sort(Comparator.comparing(IndicatoreMaterialeResponse::getPunteggioRischio).reversed());
        return ResponseEntity.ok(rows);
    }

    @GetMapping("/kpi/materiali-cantiere")
    public ResponseEntity<List<KpiMaterialeCantiereResponse>> getKpiMaterialiCantiere(
            @RequestParam("cantiereId") Long cantiereId) {
        Cantiere cantiere = cantiereRepository.findById(cantiereId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cantiere non trovato: " + cantiereId));

        List<AttivitaMaterialeUtilizzo> pianificati = utilizzoRepository.findByAttivita_Cantiere_Id(cantiereId);
        Map<Long, Double> previstoByMateriale = new HashMap<>();
        for (AttivitaMaterialeUtilizzo u : pianificati) {
            if (u.getMateriale() == null || u.getMateriale().getId() == null) {
                continue;
            }
            double q = u.getQuantita() == null ? 0.0 : u.getQuantita();
            previstoByMateriale.put(
                    u.getMateriale().getId(),
                    previstoByMateriale.getOrDefault(u.getMateriale().getId(), 0.0) + q
            );
        }

        List<MovimentoMateriale> movimenti = movimentoRepository.findByCantiere_Id(cantiereId);
        Map<Long, Double> usatoByMateriale = new HashMap<>();
        for (MovimentoMateriale m : movimenti) {
            if (m.getMateriale() == null || m.getMateriale().getId() == null) {
                continue;
            }
            if (!"SCARICO".equals(tipo(m.getTipoMovimento()))) {
                continue;
            }
            double q = m.getQuantita() == null ? 0.0 : m.getQuantita();
            usatoByMateriale.put(
                    m.getMateriale().getId(),
                    usatoByMateriale.getOrDefault(m.getMateriale().getId(), 0.0) + q
            );
        }

        Map<Long, MaterialeCatalogo> materialiMap = materialeRepository.findAll().stream()
                .collect(Collectors.toMap(MaterialeCatalogo::getId, m -> m));

        Map<Long, Boolean> keyUnion = new LinkedHashMap<>();
        previstoByMateriale.keySet().forEach(k -> keyUnion.put(k, true));
        usatoByMateriale.keySet().forEach(k -> keyUnion.put(k, true));

        List<KpiMaterialeCantiereResponse> rows = new ArrayList<>();
        for (Long materialeId : keyUnion.keySet()) {
            MaterialeCatalogo materiale = materialiMap.get(materialeId);
            if (materiale == null) {
                continue;
            }
            double previsto = previstoByMateriale.getOrDefault(materialeId, 0.0);
            double usato = usatoByMateriale.getOrDefault(materialeId, 0.0);
            double scostamento = usato - previsto;
            double scostamentoPercentuale = previsto == 0.0 ? (usato > 0.0 ? 100.0 : 0.0) : (scostamento / previsto) * 100.0;
            rows.add(new KpiMaterialeCantiereResponse(
                    cantiere.getId(),
                    cantiere.getNome(),
                    materialeId,
                    materiale.getNome(),
                    previsto,
                    usato,
                    scostamento,
                    scostamentoPercentuale
            ));
        }

        rows.sort(Comparator.comparing(KpiMaterialeCantiereResponse::getScostamentoPercentuale).reversed());
        return ResponseEntity.ok(rows);
    }

    @GetMapping("/forecast-fabbisogni")
    public ResponseEntity<List<ForecastMaterialeResponse>> getForecastFabbisogni(
            @RequestParam(value = "cantiereId", required = false) Long cantiereId) {

        List<MaterialeCatalogo> materiali = materialeRepository.findAll();
        List<SogliaMateriale> soglie = sogliaRepository.findByAttivaTrue();
        LocalDate oggi = LocalDate.now();
        LocalDate dal = oggi.minusDays(30);

        List<ForecastMaterialeResponse> response = new ArrayList<>();
        for (MaterialeCatalogo materiale : materiali) {
            List<SogliaMateriale> soglieMat = soglie.stream()
                    .filter(s -> s.getMateriale() != null && materiale.getId().equals(s.getMateriale().getId()))
                    .collect(Collectors.toList());
            if (cantiereId != null) {
                soglieMat = soglieMat.stream()
                        .filter(s -> s.getCantiere() == null || cantiereId.equals(s.getCantiere().getId()))
                        .collect(Collectors.toList());
            }

            Double giacenzaMin = soglieMat.stream()
                    .map(SogliaMateriale::getGiacenzaMinima)
                    .filter(v -> v != null)
                    .max(Double::compareTo)
                    .orElse(null);

            List<MovimentoMateriale> movimenti30 = cantiereId == null
                    ? movimentoRepository.findByMateriale_Id(materiale.getId()).stream()
                    .filter(m -> m.getDataMovimento() != null && !m.getDataMovimento().isBefore(dal))
                    .collect(Collectors.toList())
                    : movimentoRepository.findByCantiere_IdAndMateriale_Id(cantiereId, materiale.getId()).stream()
                    .filter(m -> m.getDataMovimento() != null && !m.getDataMovimento().isBefore(dal))
                    .collect(Collectors.toList());

            double consumo30 = movimenti30.stream()
                    .filter(m -> "SCARICO".equals(tipo(m.getTipoMovimento())))
                    .mapToDouble(m -> m.getQuantita() == null ? 0.0 : m.getQuantita())
                    .sum();

            double consumoMedioGiornaliero = consumo30 / 30.0;
            double residua = materiale.getQuantitaResidua() == null ? 0.0 : materiale.getQuantitaResidua();
            double giorniCopertura = consumoMedioGiornaliero <= 0.0 ? 999.0 : residua / consumoMedioGiornaliero;
            double fabbisogno30 = consumoMedioGiornaliero * 30.0;
            double target = (giacenzaMin == null ? 0.0 : giacenzaMin) + fabbisogno30;
            double riordinoSuggerito = Math.max(0.0, target - residua);

            response.add(new ForecastMaterialeResponse(
                    materiale.getId(),
                    materiale.getNome(),
                    cantiereId,
                    residua,
                    giacenzaMin,
                    consumo30,
                    consumoMedioGiornaliero,
                    giorniCopertura,
                    fabbisogno30,
                    riordinoSuggerito
            ));
        }

        response.sort(Comparator.comparing(ForecastMaterialeResponse::getRiordinoSuggerito).reversed());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/report/comparativo-cantieri")
    public ResponseEntity<List<ReportComparativoCantieriResponse>> getComparativoCantieri(
            @RequestParam(value = "month", required = false) String month) {
        YearMonth ym = parseYearMonth(month);
        LocalDate dal = ym.atDay(1);
        LocalDate al = ym.atEndOfMonth();
        List<Cantiere> cantieri = cantiereRepository.findAll();
        List<MovimentoMateriale> movimentiPeriodo = movimentoRepository.findByDataMovimentoBetween(dal, al);

        List<ReportComparativoCantieriResponse> rows = new ArrayList<>();
        for (Cantiere cantiere : cantieri) {
            List<MovimentoMateriale> movCantiere = movimentiPeriodo.stream()
                    .filter(m -> m.getCantiere() != null && cantiere.getId().equals(m.getCantiere().getId()))
                    .collect(Collectors.toList());

            double consumo = movCantiere.stream()
                    .filter(m -> "SCARICO".equals(tipo(m.getTipoMovimento())))
                    .mapToDouble(m -> m.getQuantita() == null ? 0.0 : m.getQuantita())
                    .sum();
            double carico = movCantiere.stream()
                    .filter(m -> "CARICO".equals(tipo(m.getTipoMovimento())))
                    .mapToDouble(m -> m.getQuantita() == null ? 0.0 : m.getQuantita())
                    .sum();
            double costoConsumo = movCantiere.stream()
                    .filter(m -> "SCARICO".equals(tipo(m.getTipoMovimento())))
                    .mapToDouble(m -> {
                        double q = m.getQuantita() == null ? 0.0 : m.getQuantita();
                        double p = m.getMateriale() == null || m.getMateriale().getPrezzoMedio() == null ? 0.0 : m.getMateriale().getPrezzoMedio();
                        return q * p;
                    }).sum();

            rows.add(new ReportComparativoCantieriResponse(
                    ym.toString(),
                    cantiere.getId(),
                    cantiere.getNome(),
                    carico,
                    consumo,
                    costoConsumo
            ));
        }

        rows.sort(Comparator.comparing(ReportComparativoCantieriResponse::getCostoConsumoMateriali).reversed());
        return ResponseEntity.ok(rows);
    }

    @GetMapping("/report/mese-vs-mese")
    public ResponseEntity<List<ReportMeseVsMeseResponse>> getMeseVsMese(
            @RequestParam("cantiereId") Long cantiereId,
            @RequestParam(value = "months", required = false, defaultValue = "6") Integer months) {

        Cantiere cantiere = cantiereRepository.findById(cantiereId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cantiere non trovato: " + cantiereId));
        int mesi = Math.max(1, Math.min(months, 24));
        YearMonth current = YearMonth.now();

        List<ReportMeseVsMeseResponse> rows = new ArrayList<>();
        for (int i = mesi - 1; i >= 0; i--) {
            YearMonth ym = current.minusMonths(i);
            LocalDate dal = ym.atDay(1);
            LocalDate al = ym.atEndOfMonth();
            List<MovimentoMateriale> mov = movimentoRepository.findByCantiere_IdAndDataMovimentoBetween(cantiereId, dal, al);

            double consumo = mov.stream()
                    .filter(m -> "SCARICO".equals(tipo(m.getTipoMovimento())))
                    .mapToDouble(m -> m.getQuantita() == null ? 0.0 : m.getQuantita())
                    .sum();
            double carico = mov.stream()
                    .filter(m -> "CARICO".equals(tipo(m.getTipoMovimento())))
                    .mapToDouble(m -> m.getQuantita() == null ? 0.0 : m.getQuantita())
                    .sum();
            double costoConsumo = mov.stream()
                    .filter(m -> "SCARICO".equals(tipo(m.getTipoMovimento())))
                    .mapToDouble(m -> {
                        double q = m.getQuantita() == null ? 0.0 : m.getQuantita();
                        double p = m.getMateriale() == null || m.getMateriale().getPrezzoMedio() == null ? 0.0 : m.getMateriale().getPrezzoMedio();
                        return q * p;
                    }).sum();
            rows.add(new ReportMeseVsMeseResponse(
                    cantiere.getId(),
                    cantiere.getNome(),
                    ym.toString(),
                    carico,
                    consumo,
                    costoConsumo
            ));
        }
        return ResponseEntity.ok(rows);
    }

    @GetMapping("/report/essenziale")
    public ResponseEntity<List<ReportEssenzialeResponse>> getReportEssenziale(
            @RequestParam(value = "month", required = false) String month) {
        YearMonth ym = parseYearMonth(month);
        LocalDate dal = ym.atDay(1);
        LocalDate al = ym.atEndOfMonth();
        List<Cantiere> cantieri = cantiereRepository.findAll();
        List<MovimentoMateriale> movimentiMese = movimentoRepository.findByDataMovimentoBetween(dal, al);

        List<ReportEssenzialeResponse> rows = new ArrayList<>();
        for (Cantiere cantiere : cantieri) {
            List<MovimentoMateriale> movCantiereMese = movimentiMese.stream()
                    .filter(m -> m.getCantiere() != null && cantiere.getId().equals(m.getCantiere().getId()))
                    .collect(Collectors.toList());

            double consumoMeseQuantita = movCantiereMese.stream()
                    .filter(m -> "SCARICO".equals(tipo(m.getTipoMovimento())))
                    .mapToDouble(m -> m.getQuantita() == null ? 0.0 : m.getQuantita())
                    .sum();

            double consumoMeseCosto = movCantiereMese.stream()
                    .filter(m -> "SCARICO".equals(tipo(m.getTipoMovimento())))
                    .mapToDouble(m -> {
                        double q = m.getQuantita() == null ? 0.0 : m.getQuantita();
                        double p = m.getMateriale() == null || m.getMateriale().getPrezzoMedio() == null ? 0.0 : m.getMateriale().getPrezzoMedio();
                        return q * p;
                    }).sum();

            List<AttivitaCantiere> attivita = attivitaCantiereRepository.findByCantiere_Id(cantiere.getId());
            double costoManodoperaTotale = attivita.stream()
                    .mapToDouble(a -> a.getCostoManodopera() == null ? 0.0 : a.getCostoManodopera())
                    .sum();
            List<MovimentoMateriale> scarichiTotali = movimentoRepository.findByCantiere_IdAndTipoMovimento(cantiere.getId(), "SCARICO");
            double costoMaterialiTotale = scarichiTotali.stream()
                    .mapToDouble(m -> {
                        double q = m.getQuantita() == null ? 0.0 : m.getQuantita();
                        double p = m.getMateriale() == null || m.getMateriale().getPrezzoMedio() == null ? 0.0 : m.getMateriale().getPrezzoMedio();
                        return q * p;
                    }).sum();
            double costoTotaleCantiere = costoManodoperaTotale + costoMaterialiTotale;
            double budget = cantiere.getBudget() == null ? 0.0 : cantiere.getBudget();
            double scostamento = costoTotaleCantiere - budget;
            double scostamentoPercentuale = budget == 0.0 ? 0.0 : (scostamento / budget) * 100.0;

            rows.add(new ReportEssenzialeResponse(
                    ym.toString(),
                    cantiere.getId(),
                    cantiere.getNome(),
                    consumoMeseQuantita,
                    consumoMeseCosto,
                    budget,
                    costoTotaleCantiere,
                    scostamento,
                    scostamentoPercentuale
            ));
        }

        rows.sort(Comparator.comparing(ReportEssenzialeResponse::getScostamentoPercentuale).reversed());
        return ResponseEntity.ok(rows);
    }

    private void applyRequest(SogliaMateriale soglia, SogliaRequest request) {
        if (request.getMaterialeId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Materiale obbligatorio.");
        }
        MaterialeCatalogo materiale = materialeRepository.findById(request.getMaterialeId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Materiale non trovato: " + request.getMaterialeId()));
        soglia.setMateriale(materiale);

        Cantiere cantiere = null;
        if (request.getCantiereId() != null) {
            cantiere = cantiereRepository.findById(request.getCantiereId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cantiere non trovato: " + request.getCantiereId()));
        }
        soglia.setCantiere(cantiere);

        if (request.getGiacenzaMinima() != null && request.getGiacenzaMinima() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Giacenza minima non valida.");
        }
        if (request.getGiacenzaCritica() != null && request.getGiacenzaCritica() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Giacenza critica non valida.");
        }
        if (request.getGiacenzaMinima() != null && request.getGiacenzaCritica() != null
                && request.getGiacenzaCritica() > request.getGiacenzaMinima()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La soglia critica deve essere <= soglia attenzione.");
        }
        if (request.getConsumoMensileMassimo() != null && request.getConsumoMensileMassimo() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Consumo massimo non valido.");
        }
        soglia.setGiacenzaMinima(request.getGiacenzaMinima());
        soglia.setGiacenzaCritica(request.getGiacenzaCritica());
        soglia.setConsumoMensileMassimo(request.getConsumoMensileMassimo());
        soglia.setAttiva(request.getAttiva() == null ? Boolean.TRUE : request.getAttiva());
    }

    private List<MovimentoMateriale> getMovimentiBySoglia(SogliaMateriale soglia) {
        if (soglia.getCantiere() == null) {
            return movimentoRepository.findByMateriale_Id(soglia.getMateriale().getId());
        }
        return movimentoRepository.findByCantiere_IdAndMateriale_Id(soglia.getCantiere().getId(), soglia.getMateriale().getId());
    }

    private double calcolaGiacenza(SogliaMateriale soglia, List<MovimentoMateriale> movimenti) {
        if (soglia.getCantiere() == null) {
            return soglia.getMateriale().getQuantitaResidua() == null ? 0.0 : soglia.getMateriale().getQuantitaResidua();
        }
        return movimenti.stream()
                .mapToDouble(m -> {
                    double q = m.getQuantita() == null ? 0.0 : m.getQuantita();
                    return "SCARICO".equals(tipo(m.getTipoMovimento())) ? -q : q;
                })
                .sum();
    }

    private double calcolaConsumo30gg(List<MovimentoMateriale> movimenti) {
        LocalDate dal = LocalDate.now().minusDays(30);
        return movimenti.stream()
                .filter(m -> "SCARICO".equals(tipo(m.getTipoMovimento())))
                .filter(m -> m.getDataMovimento() != null && !m.getDataMovimento().isBefore(dal))
                .mapToDouble(m -> m.getQuantita() == null ? 0.0 : m.getQuantita())
                .sum();
    }

    private String severitaGiacenza(double valore, double soglia) {
        if (valore <= 0.0) {
            return "CRITICO";
        }
        double ratio = valore / soglia;
        return ratio < 0.5 ? "ALTA" : "MEDIA";
    }

    private String severitaConsumo(double valore, double soglia) {
        if (soglia <= 0.0) {
            return "ALTA";
        }
        double ratio = valore / soglia;
        if (ratio >= 1.5) {
            return "CRITICO";
        }
        return ratio >= 1.2 ? "ALTA" : "MEDIA";
    }

    private String tipo(String value) {
        return value == null ? "CARICO" : value.trim().toUpperCase();
    }

    private String statoGiacenza(double giacenzaAttuale, Double sogliaAttenzione, Double sogliaCritica) {
        if (sogliaAttenzione == null) {
            return "OK";
        }
        if (sogliaCritica != null && giacenzaAttuale <= sogliaCritica) {
            return "CRITICO";
        }
        if (giacenzaAttuale <= sogliaAttenzione) {
            return "ATTENZIONE";
        }
        return "OK";
    }

    private Double punteggioRischio(String stato, double giacenzaAttuale, Double sogliaCritica, Double sogliaAttenzione) {
        if ("CRITICO".equals(stato)) {
            if (sogliaCritica == null || sogliaCritica == 0.0) {
                return 100.0;
            }
            double ratio = Math.max(0.0, giacenzaAttuale / sogliaCritica);
            return 100.0 + (1.0 - Math.min(1.0, ratio)) * 50.0;
        }
        if ("ATTENZIONE".equals(stato)) {
            if (sogliaAttenzione == null || sogliaAttenzione == 0.0) {
                return 60.0;
            }
            double ratio = Math.max(0.0, giacenzaAttuale / sogliaAttenzione);
            return 60.0 + (1.0 - Math.min(1.0, ratio)) * 30.0;
        }
        return 0.0;
    }

    private YearMonth parseYearMonth(String month) {
        if (month == null || month.trim().isEmpty()) {
            return YearMonth.now();
        }
        try {
            return YearMonth.parse(month.trim());
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Formato month non valido. Usa YYYY-MM.");
        }
    }

    private SogliaResponse toSogliaResponse(SogliaMateriale soglia) {
        return new SogliaResponse(
                soglia.getId(),
                soglia.getMateriale() == null ? null : soglia.getMateriale().getId(),
                soglia.getMateriale() == null ? null : soglia.getMateriale().getNome(),
                soglia.getCantiere() == null ? null : soglia.getCantiere().getId(),
                soglia.getCantiere() == null ? null : soglia.getCantiere().getNome(),
                soglia.getGiacenzaMinima(),
                soglia.getGiacenzaCritica(),
                soglia.getConsumoMensileMassimo(),
                Boolean.TRUE.equals(soglia.getAttiva())
        );
    }

    @Data
    private static class SogliaRequest {
        private Long materialeId;
        private Long cantiereId;
        private Double giacenzaMinima;
        private Double giacenzaCritica;
        private Double consumoMensileMassimo;
        private Boolean attiva;
    }

    @Data
    @AllArgsConstructor
    private static class SogliaResponse {
        private Long sogliaId;
        private Long materialeId;
        private String materialeNome;
        private Long cantiereId;
        private String cantiereNome;
        private Double giacenzaMinima;
        private Double giacenzaCritica;
        private Double consumoMensileMassimo;
        private Boolean attiva;
    }

    @Data
    @AllArgsConstructor
    private static class AlertSprecoResponse {
        private String codice;
        private String severita;
        private Long materialeId;
        private String materialeNome;
        private Long cantiereId;
        private String cantiereNome;
        private String descrizione;
        private Double valoreAttuale;
        private Double soglia;
        private String azioneConsigliata;
    }

    @Data
    @AllArgsConstructor
    private static class KpiMaterialeCantiereResponse {
        private Long cantiereId;
        private String cantiereNome;
        private Long materialeId;
        private String materialeNome;
        private Double quantitaPrevista;
        private Double quantitaUsata;
        private Double scostamento;
        private Double scostamentoPercentuale;
    }

    @Data
    @AllArgsConstructor
    private static class ForecastMaterialeResponse {
        private Long materialeId;
        private String materialeNome;
        private Long cantiereId;
        private Double quantitaResidua;
        private Double giacenzaMinima;
        private Double consumoUltimi30gg;
        private Double consumoMedioGiornaliero;
        private Double giorniCoperturaStimati;
        private Double fabbisognoProssimi30gg;
        private Double riordinoSuggerito;
    }

    @Data
    @AllArgsConstructor
    private static class ReportComparativoCantieriResponse {
        private String mese;
        private Long cantiereId;
        private String cantiereNome;
        private Double quantitaCaricata;
        private Double quantitaConsumata;
        private Double costoConsumoMateriali;
    }

    @Data
    @AllArgsConstructor
    private static class ReportMeseVsMeseResponse {
        private Long cantiereId;
        private String cantiereNome;
        private String mese;
        private Double quantitaCaricata;
        private Double quantitaConsumata;
        private Double costoConsumoMateriali;
    }

    @Data
    @AllArgsConstructor
    private static class IndicatoreMaterialeResponse {
        private Long materialeId;
        private String materialeNome;
        private Long cantiereId;
        private String cantiereNome;
        private Double giacenzaAttuale;
        private Double sogliaAttenzione;
        private Double sogliaCritica;
        private String stato;
        private Double punteggioRischio;
    }

    @Data
    @AllArgsConstructor
    private static class ReportEssenzialeResponse {
        private String mese;
        private Long cantiereId;
        private String cantiereNome;
        private Double consumoMeseQuantita;
        private Double consumoMeseCosto;
        private Double budgetCantiere;
        private Double costoTotaleCantiere;
        private Double scostamento;
        private Double scostamentoPercentuale;
    }
}
