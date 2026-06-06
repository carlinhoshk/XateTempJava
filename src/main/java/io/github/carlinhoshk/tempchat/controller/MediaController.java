package io.github.carlinhoshk.tempchat.controller;

import io.github.carlinhoshk.tempchat.service.MidiaService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
public class MediaController {

    private final MidiaService midiaService;

    public MediaController(MidiaService midiaService) {
        this.midiaService = midiaService;
    }

    @PostMapping("/upload-midia")
    public Map<String, Object> upload(@RequestParam("file") MultipartFile file) {
        Map<String, Object> result = new LinkedHashMap<>();
        if (file.isEmpty()) {
            result.put("erro", "Arquivo vazio");
            return result;
        }
        try {
            String filename = midiaService.salvar(file);
            result.put("url", "/midia/" + filename);
        } catch (IOException e) {
            result.put("erro", "Erro ao salvar arquivo");
        }
        return result;
    }

    @GetMapping("/midia/{filename}")
    public ResponseEntity<Resource> getMidia(@PathVariable String filename) {
        var file = midiaService.getPath(filename);
        if (!file.toFile().exists()) {
            return ResponseEntity.notFound().build();
        }
        Resource resource = new FileSystemResource(file);
        String contentType = guessContentType(filename);
        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(contentType))
            .body(resource);
    }

    private String guessContentType(String filename) {
        String lower = filename.toLowerCase();
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".gif")) return "image/gif";
        if (lower.endsWith(".webp")) return "image/webp";
        if (lower.endsWith(".mp4")) return "video/mp4";
        if (lower.endsWith(".webm")) return "video/webm";
        if (lower.endsWith(".mov")) return "video/quicktime";
        if (lower.endsWith(".avi")) return "video/x-msvideo";
        return "application/octet-stream";
    }
}
