package com.edile.controller;

import com.edile.entity.Cantiere;
import com.edile.entity.Deposito;
import com.edile.entity.Dipendente;
import com.edile.entity.MovimentoDeposito;
import com.edile.repository.CantierRepository;
import com.edile.repository.DepositoRepository;
import com.edile.repository.DipendenteRepository;
import com.edile.repository.MovimentoDepositoRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/deposito")
@CrossOrigin(origins = "*")
public class DepositoController {

    @Autowired
    private DepositoRepository depositoRepository;

    @Autowired
    private MovimentoDepositoRepository movimentoRepository;

    @Autowired
    private CantierRepository cantiereRepository;

    @Autowired
    private DipendenteRepository dipendenteRepository;

    @GetMapping
    public ResponseEntity<List<Deposito>> getAllDeposito() {
        return ResponseEntity.ok(depositoRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Optional<Deposito>> getDepositoById(@PathVariable Long id) {
        return ResponseEntity.ok(depositoRepository.findById(id));
    }

    @GetMapping("/categoria/{categoria}")
    public ResponseEntity<List<Deposito>> getByCategoria(@PathVariable String categoria) {
        return ResponseEntity.ok(depositoRepository.findByCategoriaIgnoreCase(categoria));
    }

    @PostMapping
    public ResponseEntity<Deposito> createDeposito(@RequestBody Deposito deposito) {
        if (deposito.getNomeArticolo() == null || deposito.getNomeArticolo().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Il nome articolo è obbligatorio.");
        }
        deposito.setNomeArticolo(deposito.getNomeArticolo().trim());

        if (deposito.getQuantitaDisponibile() == null || deposito.getQuantitaDisponibile() <= 0.0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La quantità deve essere maggiore di zero.");
        }

        if (deposito.getCodiceArticolo() == null || deposito.getCodiceArticolo().trim().isEmpty()) {
            deposito.setCodiceArticolo("DEP-" + System.currentTimeMillis());
        }
        if (deposito.getQuantitaUtilizzata() == null) {
            deposito.setQuantitaUtilizzata(0.0);
        }
        return ResponseEntity.ok(depositoRepository.save(deposito));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Deposito> updateDeposito(@PathVariable Long id, @RequestBody Deposito deposito) {
        deposito.setId(id);
        return ResponseEntity.ok(depositoRepository.save(deposito));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDeposito(@PathVariable Long id) {
        depositoRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/movimenti")
    public ResponseEntity<MovimentoDeposito> registraMovimento(@RequestBody RegistraMovimentoRequest request) {
        if (request.getDepositoId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Articolo di deposito obbligatorio.");
        }
        if (request.getQuantita() == null || request.getQuantita() <= 0.0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La quantità deve essere maggiore di zero.");
        }

        Deposito deposito = depositoRepository.findById(request.getDepositoId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Deposito non trovato"));

        String tipoMovimento = request.getTipoMovimento() == null || request.getTipoMovimento().trim().isEmpty()
                ? "PRELIEVO"
                : request.getTipoMovimento().trim().toUpperCase();

        double disponibile = deposito.getQuantitaDisponibile() == null ? 0.0 : deposito.getQuantitaDisponibile();
        double utilizzata = deposito.getQuantitaUtilizzata() == null ? 0.0 : deposito.getQuantitaUtilizzata();
        double residua = disponibile - utilizzata;

        if ("PRELIEVO".equals(tipoMovimento) && request.getQuantita() > residua) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Quantità non disponibile per " + deposito.getNomeArticolo() + ". Residuo: " + residua
            );
        }
        if ("RESTITUZIONE".equals(tipoMovimento) && request.getQuantita() > utilizzata) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Non puoi restituire più di quanto prelevato per " + deposito.getNomeArticolo() + ". Prelevato: " + utilizzata
            );
        }

        MovimentoDeposito movimento = new MovimentoDeposito();
        movimento.setDeposito(deposito);
        movimento.setTipoMovimento(tipoMovimento);
        movimento.setQuantita(request.getQuantita());
        movimento.setDataMovimento(LocalDate.now());
        movimento.setNote(request.getNote());

        if (request.getCantiereId() != null) {
            Cantiere cantiere = cantiereRepository.findById(request.getCantiereId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cantiere non trovato"));
            movimento.setCantiere(cantiere);
        }

        if (request.getDipendenteId() != null) {
            Dipendente dipendente = dipendenteRepository.findById(request.getDipendenteId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Dipendente non trovato"));
            movimento.setDipendente(dipendente);
        }

        MovimentoDeposito saved = movimentoRepository.save(movimento);

        if ("PRELIEVO".equals(tipoMovimento)) {
            deposito.setQuantitaUtilizzata(utilizzata + request.getQuantita());
        } else if ("RESTITUZIONE".equals(tipoMovimento)) {
            deposito.setQuantitaUtilizzata(Math.max(0.0, utilizzata - request.getQuantita()));
        } else if ("CARICO".equals(tipoMovimento)) {
            deposito.setQuantitaDisponibile(disponibile + request.getQuantita());
        }

        depositoRepository.save(deposito);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/movimenti")
    public ResponseEntity<List<MovimentoDeposito>> getMovimenti(@RequestParam(value = "depositoId", required = false) Long depositoId) {
        if (depositoId != null) {
            return ResponseEntity.ok(movimentoRepository.findByDeposito_Id(depositoId));
        }
        return ResponseEntity.ok(movimentoRepository.findAll());
    }

    @GetMapping("/movimenti/cantiere/{cantiereId}")
    public ResponseEntity<List<MovimentoDeposito>> getMovimentiPerCantiere(@PathVariable Long cantiereId) {
        return ResponseEntity.ok(movimentoRepository.findByCantiere_Id(cantiereId));
    }

    @GetMapping("/disponibilita")
    public ResponseEntity<List<DepositoDisponibilitaRow>> getDisponibilita() {
        List<Deposito> items = depositoRepository.findAll();
        return ResponseEntity.ok(items.stream().map(d -> new DepositoDisponibilitaRow(
                d.getId(),
                d.getCodiceArticolo(),
                d.getNomeArticolo(),
                d.getDescrizione(),
                d.getCategoria(),
                d.getUnitaMisura(),
                d.getQuantitaDisponibile() == null ? 0.0 : d.getQuantitaDisponibile(),
                d.getQuantitaUtilizzata() == null ? 0.0 : d.getQuantitaUtilizzata(),
                (d.getQuantitaDisponibile() == null ? 0.0 : d.getQuantitaDisponibile()) - (d.getQuantitaUtilizzata() == null ? 0.0 : d.getQuantitaUtilizzata()),
                d.getUbicazione(),
                d.getPrezzoUnitario()
        )).toList());
    }

    @Data
    @AllArgsConstructor
    public static class DepositoDisponibilitaRow {
        private Long id;
        private String codiceArticolo;
        private String nomeArticolo;
        private String descrizione;
        private String categoria;
        private String unitaMisura;
        private Double quantitaDisponibile;
        private Double quantitaUtilizzata;
        private Double quantitaResidua;
        private String ubicazione;
        private Double prezzoUnitario;
    }

    @Data
    public static class RegistraMovimentoRequest {
        private Long depositoId;
        private Long cantiereId;
        private Long dipendenteId;
        private String tipoMovimento;
        private Double quantita;
        private String note;
    }
}
