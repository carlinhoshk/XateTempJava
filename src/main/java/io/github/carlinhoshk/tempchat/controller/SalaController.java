package io.github.carlinhoshk.tempchat.controller;

import io.github.carlinhoshk.tempchat.model.Sala;
import io.github.carlinhoshk.tempchat.service.SalaService;
import io.github.carlinhoshk.tempchat.service.TokenService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.LinkedHashMap;
import java.util.Map;

@Controller
public class SalaController {

    private final SalaService salaService;
    private final TokenService tokenService;

    public SalaController(SalaService salaService, TokenService tokenService) {
        this.salaService = salaService;
        this.tokenService = tokenService;
    }

    @PostMapping("/criar-sala")
    @ResponseBody
    public Map<String, Object> criarSala(@CookieValue("token") String tokenUuid,
                                          HttpServletRequest request) {
        Map<String, Object> result = new LinkedHashMap<>();
        if (!tokenService.validarToken(tokenUuid)) {
            result.put("erro", "Token inválido ou expirado");
            return result;
        }
        Sala sala = salaService.criarSala(tokenUuid);
        String baseUrl = request.getScheme() + "://" + request.getServerName()
            + (request.getServerPort() != 80 && request.getServerPort() != 443
                ? ":" + request.getServerPort() : "");
        result.put("hash", sala.getHash());
        result.put("url", baseUrl + "/sala/" + sala.getHash());
        return result;
    }

    @GetMapping("/sala/{hash}")
    public String entrarSala(@PathVariable String hash,
                             @CookieValue(value = "token", required = false) String tokenUuid,
                             Model model,
                             HttpServletRequest request) {
        if (tokenUuid == null || !tokenService.validarToken(tokenUuid)) {
            return "redirect:/";
        }
        if (!salaService.validarSala(hash)) {
            model.addAttribute("erro", "Sala não encontrada ou expirada");
            return "error";
        }
        model.addAttribute("hash", hash);
        model.addAttribute("token", tokenUuid);
        String baseUrl = request.getScheme() + "://" + request.getServerName()
            + (request.getServerPort() != 80 && request.getServerPort() != 443
                ? ":" + request.getServerPort() : "");
        model.addAttribute("shareUrl", baseUrl + "/sala/" + hash);
        return "sala";
    }
}
