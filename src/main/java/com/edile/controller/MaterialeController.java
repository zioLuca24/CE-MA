package com.edile.controller;

import com.edile.entity.BollaMaterialeImport;
import com.edile.entity.Cantiere;
import com.edile.entity.Fornitore;
import com.edile.entity.MaterialeCatalogo;
import com.edile.entity.MovimentoMateriale;
import com.edile.repository.BollaMaterialeImportRepository;
import com.edile.repository.CantierRepository;
import com.edile.repository.FornitoreRepository;
import com.edile.repository.MaterialeCatalogoRepository;
import com.edile.repository.MovimentoMaterialeRepository;
import com.edile.service.MaterialeImportService;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/materiali")
@CrossOrigin(origins = "*")
public class MaterialeController {

    @Autowired
    private MaterialeCatalogoRepository materialeRepository;

    @Autowired
    private CantierRepository cantiereRepository;

    @Autowired
    private MovimentoMaterialeRepository movimentoRepository;

    @Autowired
    private BollaMaterialeImportRepository bollaImportRepository;

    @Autowired
    private FornitoreRepository fornitoreRepository;

    @Autowired
    private MaterialeImportService materialeImportService;

    @GetMapping
    public ResponseEntity<List<MaterialeCatalogo>> getAllMateriali() {
        return ResponseEntity.ok(materialeRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Optional<MaterialeCatalogo>> getMaterialeById(@PathVariable Long id) {
        return ResponseEntity.ok(materialeRepository.findById(id));
    }

    @GetMapping("/categoria/{categoria}")
    public ResponseEntity<List<MaterialeCatalogo>> getByCategoria(@PathVariable String categoria) {
        return ResponseEntity.ok(materialeRepository.findByCategoriaIgnoreCase(categoria));
    }

    @PostMapping
    public ResponseEntity<MaterialeCatalogo> createMateriale(@RequestBody MaterialeCatalogo materiale) {
        if (materiale.getQuantitaAcquistataTotale() == null) {
            materiale.setQuantitaAcquistataTotale(0.0);
        }
        if (materiale.getQuantitaUtilizzataTotale() == null) {
            materiale.setQuantitaUtilizzataTotale(0.0);
        }
        if (materiale.getQuantitaResidua() == null) {
            materiale.setQuantitaResidua(0.0);
        }
        return ResponseEntity.ok(materialeRepository.save(materiale));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MaterialeCatalogo> updateMateriale(@PathVariable Long id, @RequestBody MaterialeCatalogo materiale) {
        materiale.setId(id);
        return ResponseEntity.ok(materialeRepository.save(materiale));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMateriale(@PathVariable Long id) {
        materialeRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/movimenti")
    public ResponseEntity<MovimentoMateriale> registraMovimento(@RequestBody MovimentoMateriale movimento) {
        if (movimento.getTipoMovimento() == null || movimento.getTipoMovimento().trim().isEmpty()) {
            movimento.setTipoMovimento("CARICO");
        }
        if (movimento.getDataMovimento() == null) {
            movimento.setDataMovimento(LocalDate.now());
        }
        MovimentoMateriale saved = movimentoRepository.save(movimento);
        if (saved.getMateriale() != null && saved.getMateriale().getId() != null) {
            aggiornaQuantitaMateriale(saved.getMateriale().getId());
        }
        return ResponseEntity.ok(saved);
    }

    @PostMapping(value = "/bolle/importa", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImportBollaResponse> importaBollaMateriali(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "cantiereId", required = false) Long cantiereId,
            @RequestParam(value = "fornitoreNome", required = false) String fornitoreNome) {
        if (file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Allegato vuoto.");
        }

        List<MaterialeImportService.BollaRow> rows;
        try {
            rows = materialeImportService.parseBollaRows(file);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
        }

        String fornitoreOverride = normalizzaFornitore(fornitoreNome);
        if (!fornitoreOverride.isEmpty()) {
            fornitoreRepository.findByNomeIgnoreCase(fornitoreOverride)
                    .orElseGet(() -> fornitoreRepository.save(buildFornitore(fornitoreOverride)));
        }

        Cantiere cantiereSelezionato = null;
        if (cantiereId != null) {
            cantiereSelezionato = cantiereRepository.findById(cantiereId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cantiere selezionato non trovato: " + cantiereId));
        }

        List<String> errori = new ArrayList<>();
        int importate = 0;
        for (MaterialeImportService.BollaRow row : rows) {
            String fornitoreDaUsare = !fornitoreOverride.isEmpty() ? fornitoreOverride : normalizzaFornitore(row.getFornitoreNome());
            if (!fornitoreDaUsare.isEmpty()) {
                row.setFornitoreNome(fornitoreDaUsare);
                fornitoreRepository.findByNomeIgnoreCase(fornitoreDaUsare)
                        .orElseGet(() -> fornitoreRepository.save(buildFornitore(fornitoreDaUsare)));
            }
            Cantiere cantiere = cantiereSelezionato != null ? cantiereSelezionato : risolviCantiere(row.getCantiereRaw());
            if (cantiere == null) {
                errori.add("Cantiere non trovato per riga: " + row.getCantiereRaw());
                continue;
            }

            List<MaterialeCatalogo> trovati = materialeRepository.findByNomeIgnoreCaseList(row.getNomeMateriale());
            MaterialeCatalogo materiale = trovati.isEmpty() ? creaMaterialeBase(row) : aggiornaMaterialeImportato(trovati.get(0), row);

            bollaImportRepository.save(creaImportRecord(row, cantiere, materiale, file.getOriginalFilename()));
            movimentoRepository.save(creaMovimentoCarico(cantiere, materiale, row.getQuantita(), row.getNumeroBolla()));
            aggiornaQuantitaMateriale(materiale.getId());
            importate++;
        }

        return ResponseEntity.ok(new ImportBollaResponse(importate, errori));
    }

    @GetMapping("/anagrafica")
    public ResponseEntity<List<AnagraficaMaterialeRow>> getAnagraficaMateriali(
            @RequestParam(value = "cantiereId", required = false) Long cantiereId) {
        List<MaterialeCatalogo> materiali = materialeRepository.findAll();
        List<MovimentoMateriale> movimenti = movimentoRepository.findAll();

        Map<Long, List<MovimentoMateriale>> perMateriale = movimenti.stream()
                .filter(m -> m.getMateriale() != null && m.getMateriale().getId() != null)
                .collect(Collectors.groupingBy(m -> m.getMateriale().getId()));

        List<AnagraficaMaterialeRow> rows = materiali.stream().map(materiale -> {
            List<MovimentoMateriale> lista = perMateriale.getOrDefault(materiale.getId(), java.util.Collections.emptyList());
            double acquistata = somma(lista, "CARICO");
            double utilizzata = somma(lista, "SCARICO");
            double residua = acquistata - utilizzata;
            double acquistataPerCantiere = cantiereId == null ? 0.0 : lista.stream()
                    .filter(m -> m.getCantiere() != null && cantiereId.equals(m.getCantiere().getId()))
                    .filter(m -> "CARICO".equals(tipo(m.getTipoMovimento())))
                    .mapToDouble(m -> m.getQuantita() == null ? 0.0 : m.getQuantita())
                    .sum();

            return new AnagraficaMaterialeRow(
                    materiale.getId(),
                    materiale.getNome(),
                    materiale.getDescrizione(),
                    acquistata,
                    utilizzata,
                    residua,
                    acquistataPerCantiere
            );
        }).collect(Collectors.toList());

        return ResponseEntity.ok(rows);
    }

    @GetMapping("/fornitori/anagrafica")
    public ResponseEntity<List<AnagraficaFornitoreRow>> getAnagraficaFornitori(
            @RequestParam(value = "fornitore", required = false) String fornitore) {
        List<BollaMaterialeImport> bolle = bollaImportRepository.findAll();
        String filtro = fornitore == null ? "" : fornitore.trim().toLowerCase();

        Map<String, List<BollaMaterialeImport>> perFornitoreEMateriale = bolle.stream()
                .filter(b -> b.getMateriale() != null)
                .filter(b -> b.getFornitoreNome() != null && !b.getFornitoreNome().trim().isEmpty())
                .filter(b -> filtro.isEmpty() || b.getFornitoreNome().toLowerCase().contains(filtro))
                .collect(Collectors.groupingBy(b -> b.getFornitoreNome().trim() + "|" + b.getMateriale().getId()));

        List<AnagraficaFornitoreRow> rows = perFornitoreEMateriale.values().stream()
                .map(lista -> {
                    BollaMaterialeImport ultimo = lista.stream()
                            .sorted((a, b) -> {
                                long idB = b.getId() == null ? 0L : b.getId();
                                long idA = a.getId() == null ? 0L : a.getId();
                                return Long.compare(idB, idA);
                            })
                            .findFirst()
                            .orElse(null);
                    if (ultimo == null || ultimo.getMateriale() == null) {
                        return null;
                    }
                    return new AnagraficaFornitoreRow(
                            ultimo.getFornitoreNome(),
                            ultimo.getMateriale().getNome(),
                            ultimo.getPrezzoUnitario() == null ? 0.0 : ultimo.getPrezzoUnitario()
                    );
                })
                .filter(java.util.Objects::nonNull)
                .sorted((a, b) -> {
                    int byFornitore = a.getNomeFornitore().compareToIgnoreCase(b.getNomeFornitore());
                    return byFornitore != 0 ? byFornitore : a.getMateriale().compareToIgnoreCase(b.getMateriale());
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(rows);
    }

    @GetMapping("/bolle")
    public ResponseEntity<List<BollaMaterialeImport>> getBolleImportate() {
        return ResponseEntity.ok(bollaImportRepository.findAll());
    }

    @GetMapping("/fornitori/storico")
    public ResponseEntity<List<StoricoFornitoreRow>> getStoricoFornitori() {
        List<BollaMaterialeImport> bolle = bollaImportRepository.findAll();
        Map<String, List<BollaMaterialeImport>> perMaterialeEFornitore = bolle.stream()
                .filter(b -> b.getMateriale() != null)
                .filter(b -> b.getFornitoreNome() != null && !b.getFornitoreNome().trim().isEmpty())
                .collect(Collectors.groupingBy(b -> b.getMateriale().getId() + "|" + b.getFornitoreNome().trim()));

        List<StoricoFornitoreRow> rows = perMaterialeEFornitore.values().stream()
                .map(lista -> {
                    BollaMaterialeImport ultimo = lista.stream()
                            .sorted((a, b) -> (b.getDataImport() == null ? LocalDate.MIN : b.getDataImport()).compareTo(a.getDataImport() == null ? LocalDate.MIN : a.getDataImport()))
                            .findFirst()
                            .orElse(null);
                    if (ultimo == null || ultimo.getMateriale() == null) {
                        return null;
                    }
                    double prezzoMedio = lista.stream()
                            .filter(b -> b.getPrezzoUnitario() != null)
                            .mapToDouble(b -> b.getPrezzoUnitario())
                            .average()
                            .orElse(0.0);
                    double quantitaTotale = lista.stream()
                            .filter(b -> b.getQuantita() != null)
                            .mapToDouble(b -> b.getQuantita())
                            .sum();
                    return new StoricoFornitoreRow(
                            ultimo.getMateriale().getId(),
                            ultimo.getMateriale().getNome(),
                            ultimo.getFornitoreNome(),
                            prezzoMedio,
                            ultimo.getPrezzoUnitario() == null ? 0.0 : ultimo.getPrezzoUnitario(),
                            ultimo.getNumeroBolla(),
                            quantitaTotale,
                            lista.size()
                    );
                })
                .filter(java.util.Objects::nonNull)
                .sorted((a, b) -> a.getMaterialeNome().compareToIgnoreCase(b.getMaterialeNome()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(rows);
    }

    private Fornitore buildFornitore(String nome) {
        Fornitore fornitore = new Fornitore();
        fornitore.setNome(nome.trim());
        return fornitore;
    }

    private String normalizzaFornitore(String valore) {
        if (valore == null) {
            return "";
        }
        String trimmed = valore.trim();
        return trimmed.isEmpty() ? "" : trimmed;
    }

    private Cantiere risolviCantiere(String cantiereRaw) {
        if (cantiereRaw == null || cantiereRaw.trim().isEmpty()) {
            return null;
        }

        String val = cantiereRaw.trim();
        try {
            Long id = Long.valueOf(val);
            Optional<Cantiere> byId = cantiereRepository.findById(id);
            if (byId.isPresent()) {
                return byId.get();
            }
        } catch (NumberFormatException ignore) {
        }

        List<Cantiere> byNome = cantiereRepository.findByNomeContainingIgnoreCase(val);
        return byNome.isEmpty() ? null : byNome.get(0);
    }

    private MaterialeCatalogo creaMaterialeBase(MaterialeImportService.BollaRow row) {
        MaterialeCatalogo materiale = new MaterialeCatalogo();
        materiale.setNome(row.getNomeMateriale());
        materiale.setCategoria("DA_CLASSIFICARE");
        materiale.setUnitaMisura("pz");
        materiale.setDescrizione(componiDescrizioneMateriale(row));
        materiale.setPrezzoMedio(row.getPrezzoUnitario());
        materiale.setQuantitaAcquistataTotale(0.0);
        materiale.setQuantitaUtilizzataTotale(0.0);
        materiale.setQuantitaResidua(0.0);
        return materialeRepository.save(materiale);
    }

    private MaterialeCatalogo aggiornaMaterialeImportato(MaterialeCatalogo materiale, MaterialeImportService.BollaRow row) {
        boolean changed = false;
        if ((materiale.getDescrizione() == null || materiale.getDescrizione().trim().isEmpty()) && row.getDescrizione() != null && !row.getDescrizione().trim().isEmpty()) {
            materiale.setDescrizione(componiDescrizioneMateriale(row));
            changed = true;
        }
        if (row.getPrezzoUnitario() != null && row.getPrezzoUnitario() > 0.0) {
            materiale.setPrezzoMedio(row.getPrezzoUnitario());
            changed = true;
        }
        return changed ? materialeRepository.save(materiale) : materiale;
    }

    private String componiDescrizioneMateriale(MaterialeImportService.BollaRow row) {
        String descrizione = row.getDescrizione() == null ? "" : row.getDescrizione().trim();
        String codice = row.getCodiceArticolo() == null ? "" : row.getCodiceArticolo().trim();
        if (descrizione.isEmpty()) {
            return codice;
        }
        if (codice.isEmpty()) {
            return descrizione;
        }
        return "Codice " + codice + " - " + descrizione;
    }

    private BollaMaterialeImport creaImportRecord(MaterialeImportService.BollaRow row, Cantiere cantiere, MaterialeCatalogo materiale, String filename) {
        BollaMaterialeImport record = new BollaMaterialeImport();
        record.setNumeroBolla(row.getNumeroBolla());
        record.setCodiceArticolo(row.getCodiceArticolo());
        record.setDescrizione(row.getDescrizione());
        record.setMateriale(materiale);
        record.setCantiere(cantiere);
        record.setQuantita(row.getQuantita());
        record.setPeso(row.getPeso());
        record.setPrezzoUnitario(row.getPrezzoUnitario());
        record.setPrezzoTotale(row.getPrezzoTotale());
        record.setFornitoreNome(row.getFornitoreNome());
        record.setDataImport(LocalDate.now());
        record.setFileOrigine(filename);
        return record;
    }

    private MovimentoMateriale creaMovimentoCarico(Cantiere cantiere, MaterialeCatalogo materiale, Double quantita, String numeroBolla) {
        MovimentoMateriale movimento = new MovimentoMateriale();
        movimento.setCantiere(cantiere);
        movimento.setMateriale(materiale);
        movimento.setTipoMovimento("CARICO");
        movimento.setQuantita(quantita);
        movimento.setDataMovimento(LocalDate.now());
        movimento.setNote("Carico da bolla " + numeroBolla);
        return movimento;
    }

    private void aggiornaQuantitaMateriale(Long materialeId) {
        MaterialeCatalogo materiale = materialeRepository.findById(materialeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Materiale non trovato: " + materialeId));
        List<MovimentoMateriale> movimenti = movimentoRepository.findByMateriale_Id(materialeId);
        double acquistata = somma(movimenti, "CARICO");
        double utilizzata = somma(movimenti, "SCARICO");
        materiale.setQuantitaAcquistataTotale(acquistata);
        materiale.setQuantitaUtilizzataTotale(utilizzata);
        materiale.setQuantitaResidua(acquistata - utilizzata);
        materialeRepository.save(materiale);
    }

    private double somma(List<MovimentoMateriale> movimenti, String tipoAtteso) {
        return movimenti.stream()
                .filter(movimento -> tipoAtteso.equals(tipo(movimento.getTipoMovimento())))
                .mapToDouble(movimento -> movimento.getQuantita() == null ? 0.0 : movimento.getQuantita())
                .sum();
    }

    private String tipo(String value) {
        return value == null ? "CARICO" : value.trim().toUpperCase();
    }

    @Data
    @AllArgsConstructor
    private static class ImportBollaResponse {
        private Integer righeImportate;
        private List<String> errori;
    }

    @Data
    @AllArgsConstructor
    private static class AnagraficaMaterialeRow {
        private Long materialeId;
        private String nomeMateriale;
        private String descrizione;
        private Double quantitaAcquistata;
        private Double quantitaUtilizzata;
        private Double quantitaResidua;
        private Double quantitaAcquistataPerCantiere;
    }

    @Data
    @AllArgsConstructor
    private static class StoricoFornitoreRow {
        private Long materialeId;
        private String materialeNome;
        private String fornitoreNome;
        private Double prezzoMedio;
        private Double ultimoPrezzoUnitario;
        private String ultimaBolla;
        private Double quantitaTotale;
        private Integer numeroAcquisti;
    }

    @Data
    @AllArgsConstructor
    private static class AnagraficaFornitoreRow {
        private String nomeFornitore;
        private String materiale;
        private Double prezzoUnitarioMateriale;
    }
}
