package io.github.carlinhoshk.tempchat.service;

import io.github.carlinhoshk.tempchat.model.Token;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class TokenService {
    private final JdbcTemplate jdbc;

    public TokenService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Token criarToken() {
        String uuid = UUID.randomUUID().toString();
        LocalDateTime agora = LocalDateTime.now();
        LocalDateTime expira = agora.plusHours(24);
        jdbc.update("INSERT INTO tokens (uuid, criado_em, expira_em) VALUES (?, ?, ?)",
            uuid, agora.toString(), expira.toString());
        Token t = new Token();
        t.setUuid(uuid);
        t.setCriadoEm(agora);
        t.setExpiraEm(expira);
        return t;
    }

    public boolean validarToken(String uuid) {
        List<Token> tokens = jdbc.query(
            "SELECT * FROM tokens WHERE uuid = ? AND expira_em > ?",
            tokenMapper, uuid, LocalDateTime.now().toString()
        );
        return !tokens.isEmpty();
    }

    public Token buscarPorUuid(String uuid) {
        List<Token> tokens = jdbc.query(
            "SELECT * FROM tokens WHERE uuid = ?", tokenMapper, uuid
        );
        return tokens.isEmpty() ? null : tokens.get(0);
    }

    public void expurgarExpirados() {
        jdbc.update("DELETE FROM tokens WHERE expira_em <= ?", LocalDateTime.now().toString());
    }

    private final RowMapper<Token> tokenMapper = (rs, rowNum) -> {
        Token t = new Token();
        t.setId(rs.getLong("id"));
        t.setUuid(rs.getString("uuid"));
        t.setCriadoEm(LocalDateTime.parse(rs.getString("criado_em")));
        t.setExpiraEm(LocalDateTime.parse(rs.getString("expira_em")));
        return t;
    };
}
