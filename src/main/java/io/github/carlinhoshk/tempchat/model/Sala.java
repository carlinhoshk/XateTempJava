package io.github.carlinhoshk.tempchat.model;

import java.time.LocalDateTime;

public class Sala {
    private Long id;
    private String hash;
    private String tokenUuid;
    private LocalDateTime criadoEm;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getHash() { return hash; }
    public void setHash(String hash) { this.hash = hash; }
    public String getTokenUuid() { return tokenUuid; }
    public void setTokenUuid(String tokenUuid) { this.tokenUuid = tokenUuid; }
    public LocalDateTime getCriadoEm() { return criadoEm; }
    public void setCriadoEm(LocalDateTime criadoEm) { this.criadoEm = criadoEm; }
}
