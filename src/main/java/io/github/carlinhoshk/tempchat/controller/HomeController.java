package io.github.carlinhoshk.tempchat.controller;

import io.github.carlinhoshk.tempchat.model.Sala;
import io.github.carlinhoshk.tempchat.model.Token;
import io.github.carlinhoshk.tempchat.service.SalaService;
import io.github.carlinhoshk.tempchat.service.TokenService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class HomeController {

    private final TokenService tokenService;
    private final SalaService salaService;

    public HomeController(TokenService tokenService, SalaService salaService) {
        this.tokenService = tokenService;
        this.salaService = salaService;
    }

    @GetMapping("/")
    public String index(@CookieValue(value = "token", required = false) String tokenUuid,
                        Model model) {
        if (tokenUuid == null) {
            model.addAttribute("temToken", false);
            return "index";
        }
        boolean valido = tokenService.validarToken(tokenUuid);
        model.addAttribute("temToken", valido);
        if (valido) {
            model.addAttribute("token", tokenUuid);
        }
        return "index";
    }

    @PostMapping("/criar-token")
    public String criarToken(HttpServletResponse response) {
        Token token = tokenService.criarToken();
        Cookie cookie = new Cookie("token", token.getUuid());
        cookie.setMaxAge(24 * 60 * 60);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        response.addCookie(cookie);
        return "redirect:/";
    }

    @GetMapping("/minhas-salas")
    @ResponseBody
    public Map<String, Object> minhasSalas(@CookieValue("token") String tokenUuid) {
        Map<String, Object> result = new LinkedHashMap<>();
        if (!tokenService.validarToken(tokenUuid)) {
            result.put("erro", "Token inválido");
            return result;
        }
        List<Sala> salas = salaService.listarPorToken(tokenUuid);
        result.put("salas", salas.stream().map(s -> {
            Map<String, String> m = new LinkedHashMap<>();
            m.put("hash", s.getHash());
            m.put("criadoEm", s.getCriadoEm().toString());
            return m;
        }).collect(Collectors.toList()));
        return result;
    }
}
