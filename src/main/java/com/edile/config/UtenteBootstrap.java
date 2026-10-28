package com.edile.config;

import com.edile.service.UtenteAppService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class UtenteBootstrap implements CommandLineRunner {

    @Autowired
    private UtenteAppService utenteService;

    @Override
    public void run(String... args) {
        utenteService.createDefaultAdminIfMissing();
    }
}
