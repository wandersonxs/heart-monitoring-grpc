package br.com.itfree.heartmonitoringclient.service;

import br.com.itfree.heartmonitoringclient.dto.PatientClientStreamingDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class HeartbeatSummaryWebSocketHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private final HeartbeatService heartbeatService;

    public HeartbeatSummaryWebSocketHandler(HeartbeatService heartbeatService) {
        this.heartbeatService = heartbeatService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        AtomicBoolean completed = new AtomicBoolean(false);

        // Sink holder
        final FluxSinkHolder sinkHolder = new FluxSinkHolder();

        Flux<PatientClientStreamingDTO> inputFlux = Flux.create(sink -> {
            sinkHolder.sink = sink;
            session.getAttributes().put("sink", sink);
        }, FluxSink.OverflowStrategy.BUFFER);

        heartbeatService.sendHeartbeatsStreamingAndGetHeartbeatSummary(inputFlux)
                .doOnSuccess(summary -> {
                    try {
                        String json = objectMapper.writeValueAsString(summary);
                        if (session.isOpen()) {
                            session.sendMessage(new TextMessage(json));
                        }
                    } catch (Exception e) {
                        log.error("Error sending summary", e);
                    }
                })
                .doOnError(error -> {
                    if (session.isOpen()) {
                        try {
                            session.sendMessage(new TextMessage("ERROR: " + error.getMessage()));
                        } catch (Exception ignored) {}
                    }
                })
                .doFinally(signalType -> {
                    if (!completed.getAndSet(true)) {
                        try {
                            session.close();
                        } catch (Exception ignored) {}
                    }
                })
                .subscribe();
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        FluxSink<PatientClientStreamingDTO> sink = (FluxSink<PatientClientStreamingDTO>) session.getAttributes().get("sink");

        if (sink != null) {
            String payload = message.getPayload().trim();

            // Handle END signal first
            if ("[END]".equalsIgnoreCase(payload)) {
                sink.complete();
                return;
            }

            // Now handle actual DTOs
            if (payload.startsWith("[")) {
                List<PatientClientStreamingDTO> dtoList = objectMapper.readValue(payload,
                        objectMapper.getTypeFactory().constructCollectionType(List.class, PatientClientStreamingDTO.class));
                dtoList.forEach(sink::next);
            } else {
                PatientClientStreamingDTO dto = objectMapper.readValue(payload, PatientClientStreamingDTO.class);
                sink.next(dto);
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        FluxSink<PatientClientStreamingDTO> sink = (FluxSink<PatientClientStreamingDTO>) session.getAttributes().get("sink");
        if (sink != null) {
            sink.complete(); // ✅ triggers summary return
        }
    }

    private static class FluxSinkHolder {
        private FluxSink<PatientClientStreamingDTO> sink;
    }
}