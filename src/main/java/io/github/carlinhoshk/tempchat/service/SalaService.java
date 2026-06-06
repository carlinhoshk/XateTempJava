package io.github.carlinhoshk.tempchat.service;

import io.github.carlinhoshk.tempchat.model.Sala;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class SalaService {
    private final JdbcTemplate jdbc;

    public SalaService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Sala criarSala(String tokenUuid) {
        String hash = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        LocalDateTime agora = LocalDateTime.now();
        jdbc.update("INSERT INTO salas (hash, token_uuid, criado_em) VALUES (?, ?, ?)",
            hash, tokenUuid, agora.toString());
        Sala s = new Sala();
        s.setHash(hash);
        s.setTokenUuid(tokenUuid);
        s.setCriadoEm(agora);
        return s;
    }

    public boolean validarSala(String hash) {
        List<Sala> salas = jdbc.query(
            "SELECT * FROM salas WHERE hash = ?", salaMapper, hash
        );
        return !salas.isEmpty();
    }

    public List<Sala> listarPorToken(String tokenUuid) {
        return jdbc.query(
            "SELECT * FROM salas WHERE token_uuid = ? ORDER BY criado_em DESC",
            salaMapper, tokenUuid
        );
    }

    public void expurgarExpiradas() {
        jdbc.update("DELETE FROM salas WHERE criado_em < ?",
            LocalDateTime.now().minusHours(24).toString());
    }

    private final RowMapper<Sala> salaMapper = (rs, rowNum) -> {
        Sala s = new Sala();
        s.setId(rs.getLong("id"));
        s.setHash(rs.getString("hash"));
        s.setTokenUuid(rs.getString("token_uuid"));
        s.setCriadoEm(LocalDateTime.parse(rs.getString("criado_em")));
        return s;
    };
}
