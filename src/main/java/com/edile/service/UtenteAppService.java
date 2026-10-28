package com.edile.service;

import com.edile.entity.UtenteApp;
import com.edile.repository.UtenteAppRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class UtenteAppService {

    @Autowired
    private UtenteAppRepository utenteRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    private final ConcurrentHashMap<String, SessioneUtente> sessioni = new ConcurrentHashMap<>();

    public LoginResponse login(String username, String password) {
        if (isBlank(username) || isBlank(password)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username e password obbligatori.");
        }

        UtenteApp utente = utenteRepository.findByUsernameIgnoreCase(username.trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenziali non valide."));

        if (!Boolean.TRUE.equals(utente.getAttivo())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Utente disattivato.");
        }
        if (!passwordEncoder.matches(password, utente.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenziali non valide.");
        }

        utente.setUltimoAccesso(LocalDateTime.now());
        utenteRepository.save(utente);

        String token = UUID.randomUUID().toString();
        sessioni.put(token, new SessioneUtente(utente.getId(), utente.getUsername(), utente.getRuolo()));
        return new LoginResponse(token, utente.getUsername(), utente.getNomeCompleto(), utente.getRuolo());
    }

    public UtenteView createUtente(String username, String password, String nomeCompleto, String ruolo, Boolean attivo) {
        if (isBlank(username) || isBlank(password)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username e password obbligatori.");
        }
        if (utenteRepository.findByUsernameIgnoreCase(username.trim()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username già esistente.");
        }

        UtenteApp utente = new UtenteApp();
        utente.setUsername(username.trim());
        utente.setPasswordHash(passwordEncoder.encode(password));
        utente.setNomeCompleto(safeText(nomeCompleto));
        utente.setRuolo(isBlank(ruolo) ? "OPERATORE" : ruolo.trim().toUpperCase());
        utente.setAttivo(attivo == null ? Boolean.TRUE : attivo);
        utente.setDataCreazione(LocalDateTime.now());
        utente.setUltimoAccesso(null);
        return toView(utenteRepository.save(utente));
    }

    public UtenteView updateUtente(Long id, String nomeCompleto, String ruolo, Boolean attivo) {
        UtenteApp utente = utenteRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utente non trovato: " + id));

        utente.setNomeCompleto(safeText(nomeCompleto));
        if (!isBlank(ruolo)) {
            utente.setRuolo(ruolo.trim().toUpperCase());
        }
        if (attivo != null) {
            utente.setAttivo(attivo);
        }
        return toView(utenteRepository.save(utente));
    }

    public void updatePassword(Long id, String nuovaPassword) {
        if (isBlank(nuovaPassword) || nuovaPassword.length() < 8) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La password deve avere almeno 8 caratteri.");
        }
        UtenteApp utente = utenteRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utente non trovato: " + id));
        utente.setPasswordHash(passwordEncoder.encode(nuovaPassword));
        utenteRepository.save(utente);
    }

    public List<UtenteView> getUtenti() {
        return utenteRepository.findAll().stream().map(this::toView).collect(java.util.stream.Collectors.toList());
    }

    public boolean isTokenValido(String token) {
        return !isBlank(token) && sessioni.containsKey(token);
    }

    public void createDefaultAdminIfMissing() {
        if (utenteRepository.findByUsernameIgnoreCase("admin").isPresent()) {
            return;
        }
        UtenteApp admin = new UtenteApp();
        admin.setUsername("admin");
        admin.setPasswordHash(passwordEncoder.encode("Admin123!"));
        admin.setNomeCompleto("Amministratore");
        admin.setRuolo("ADMIN");
        admin.setAttivo(Boolean.TRUE);
        admin.setDataCreazione(LocalDateTime.now());
        utenteRepository.save(admin);
    }

    private UtenteView toView(UtenteApp utente) {
        return new UtenteView(
                utente.getId(),
                utente.getUsername(),
                utente.getNomeCompleto(),
                utente.getRuolo(),
                Boolean.TRUE.equals(utente.getAttivo()),
                utente.getDataCreazione(),
                utente.getUltimoAccesso()
        );
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String safeText(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    public static class LoginResponse {
        private final String token;
        private final String username;
        private final String nomeCompleto;
        private final String ruolo;

        public LoginResponse(String token, String username, String nomeCompleto, String ruolo) {
            this.token = token;
            this.username = username;
            this.nomeCompleto = nomeCompleto;
            this.ruolo = ruolo;
        }

        public String getToken() {
            return token;
        }

        public String getUsername() {
            return username;
        }

        public String getNomeCompleto() {
            return nomeCompleto;
        }

        public String getRuolo() {
            return ruolo;
        }
    }

    public static class UtenteView {
        private final Long id;
        private final String username;
        private final String nomeCompleto;
        private final String ruolo;
        private final Boolean attivo;
        private final LocalDateTime dataCreazione;
        private final LocalDateTime ultimoAccesso;

        public UtenteView(Long id, String username, String nomeCompleto, String ruolo, Boolean attivo, LocalDateTime dataCreazione, LocalDateTime ultimoAccesso) {
            this.id = id;
            this.username = username;
            this.nomeCompleto = nomeCompleto;
            this.ruolo = ruolo;
            this.attivo = attivo;
            this.dataCreazione = dataCreazione;
            this.ultimoAccesso = ultimoAccesso;
        }

        public Long getId() {
            return id;
        }

        public String getUsername() {
            return username;
        }

        public String getNomeCompleto() {
            return nomeCompleto;
        }

        public String getRuolo() {
            return ruolo;
        }

        public Boolean getAttivo() {
            return attivo;
        }

        public LocalDateTime getDataCreazione() {
            return dataCreazione;
        }

        public LocalDateTime getUltimoAccesso() {
            return ultimoAccesso;
        }
    }

    private static class SessioneUtente {
        private final Long utenteId;
        private final String username;
        private final String ruolo;

        private SessioneUtente(Long utenteId, String username, String ruolo) {
            this.utenteId = utenteId;
            this.username = username;
            this.ruolo = ruolo;
        }
    }
}
