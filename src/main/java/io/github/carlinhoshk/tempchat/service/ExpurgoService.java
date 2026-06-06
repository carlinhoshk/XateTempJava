package io.github.carlinhoshk.tempchat.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class ExpurgoService {
    private final TokenService tokenService;
    private final SalaService salaService;
    private final MidiaService midiaService;

    public ExpurgoService(TokenService tokenService, SalaService salaService, MidiaService midiaService) {
        this.tokenService = tokenService;
        this.salaService = salaService;
        this.midiaService = midiaService;
    }

    @Scheduled(fixedRate = 3_600_000)
    public void expurgar() {
        tokenService.expurgarExpirados();
        salaService.expurgarExpiradas();
        midiaService.expurgarAntigas();
    }
}
