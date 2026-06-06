package io.github.carlinhoshk.tempchat.service;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Service
public class MidiaService {

    @Value("${tempchat.storage.midia}")
    private String midiaDir;

    @PostConstruct
    public void init() throws IOException {
        Files.createDirectories(Paths.get(midiaDir));
    }

    public String salvar(MultipartFile file) throws IOException {
        String original = file.getOriginalFilename();
        String ext = "";
        if (original != null && original.contains(".")) {
            ext = original.substring(original.lastIndexOf('.'));
        }
        String filename = UUID.randomUUID().toString() + ext;
        Path target = Paths.get(midiaDir, filename);
        file.transferTo(target);
        return filename;
    }

    public Path getPath(String filename) {
        return Paths.get(midiaDir, filename);
    }

    public void expurgarAntigas() {
        try {
            Files.list(Paths.get(midiaDir)).forEach(p -> {
                try {
                    Instant lastModified = Files.getLastModifiedTime(p).toInstant();
                    if (lastModified.isBefore(Instant.now().minus(Duration.ofHours(1)))) {
                        Files.delete(p);
                    }
                } catch (IOException ignored) {}
            });
        } catch (IOException ignored) {}
    }
}
