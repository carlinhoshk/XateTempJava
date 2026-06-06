package io.github.carlinhoshk.tempchat.websocket;

import io.github.carlinhoshk.tempchat.service.SalaService;
import io.github.carlinhoshk.tempchat.service.TokenService;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final Map<String, List<WebSocketSession>> salas = new ConcurrentHashMap<>();
    private final Map<String, String> sessionSala = new ConcurrentHashMap<>();
    private final TokenService tokenService;
    private final SalaService salaService;

    public ChatWebSocketHandler(TokenService tokenService, SalaService salaService) {
        this.tokenService = tokenService;
        this.salaService = salaService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        URI uri = session.getUri();
        if (uri == null) {
            session.close(CloseStatus.POLICY_VIOLATION);
            return;
        }

        String path = uri.getPath();
        String hash = path.substring(path.lastIndexOf('/') + 1);

        String query = uri.getQuery();
        String token = null;
        if (query != null && query.contains("token=")) {
            String[] params = query.split("&");
            for (String p : params) {
                if (p.startsWith("token=")) {
                    token = p.substring(6);
                    break;
                }
            }
        }

        if (token == null || !tokenService.validarToken(token) || !salaService.validarSala(hash)) {
            session.close(CloseStatus.POLICY_VIOLATION);
            return;
        }

        List<WebSocketSession> sessions = salas.computeIfAbsent(hash, k -> new CopyOnWriteArrayList<>());
        if (sessions.size() >= 2) {
            Map<String, Object> msg = new LinkedHashMap<>();
            msg.put("type", "error");
            msg.put("text", "Sala lotada (máximo 2 pessoas)");
            session.sendMessage(new TextMessage(toJson(msg)));
            session.close(CloseStatus.POLICY_VIOLATION);
            return;
        }

        sessions.add(session);
        sessionSala.put(session.getId(), hash);

        Map<String, Object> joinMsg = new LinkedHashMap<>();
        joinMsg.put("type", "join");
        joinMsg.put("participantes", sessions.size());
        broadcast(hash, toJson(joinMsg), session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String hash = sessionSala.get(session.getId());
        if (hash == null) return;
        broadcast(hash, message.getPayload(), session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String hash = sessionSala.remove(session.getId());
        if (hash != null) {
            List<WebSocketSession> sessions = salas.get(hash);
            if (sessions != null) {
                sessions.remove(session);
                if (sessions.isEmpty()) {
                    salas.remove(hash);
                }
            }
            Map<String, Object> msg = new LinkedHashMap<>();
            msg.put("type", "leave");
            broadcast(hash, toJson(msg), null);
        }
    }

    private void broadcast(String hash, String message, WebSocketSession sender) {
        List<WebSocketSession> sessions = salas.get(hash);
        if (sessions == null) return;
        for (WebSocketSession s : sessions) {
            if (s.isOpen() && (sender == null || !s.getId().equals(sender.getId()))) {
                try {
                    s.sendMessage(new TextMessage(message));
                } catch (Exception ignored) {}
            }
        }
    }

    private String toJson(Map<String, Object> map) {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (var entry : map.entrySet()) {
            if (!first) sb.append(",");
            first = false;
            sb.append("\"").append(entry.getKey()).append("\":");
            Object v = entry.getValue();
            if (v instanceof String) {
                sb.append("\"").append(escape(v.toString())).append("\"");
            } else {
                sb.append(v);
            }
        }
        sb.append("}");
        return sb.toString();
    }

    private String escape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
    }
}
