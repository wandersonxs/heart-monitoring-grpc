package br.com.itfree.heartmonitoringclient.service;

import br.com.itfree.heartmonitoringclient.dto.PatientClientStreamingDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import java.util.concurrent.atomic.AtomicBoolean;


public class HeartbeatGrpcWebSocketHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private final HeartbeatService heartbeatService;

    public HeartbeatGrpcWebSocketHandler(HeartbeatService heartbeatService) {
        this.heartbeatService = heartbeatService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        AtomicBoolean completed = new AtomicBoolean(false);

        // Declare the sink holder to use inside the subscription
        final FluxSinkHolder sinkHolder = new FluxSinkHolder();

        // Create the inputFlux first
        Flux<PatientClientStreamingDTO> inputFlux = Flux.create((FluxSink<PatientClientStreamingDTO> sink) -> {
            sinkHolder.sink = sink;
            session.getAttributes().put("sink", sink);
        }, FluxSink.OverflowStrategy.BUFFER);

        // Now subscribe to gRPC stream
        heartbeatService.sendHeartbeatsContinuouslyReceiveRealTimeSummary(inputFlux)
                .doOnNext(summary -> {
                    try {
                        String json = objectMapper.writeValueAsString(summary);
                        if (session.isOpen()) {
                            session.sendMessage(new TextMessage(json));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                })
                .doOnError(err -> {
                    if (session.isOpen()) {
                        try {
                            session.sendMessage(new TextMessage("ERROR: " + err.getMessage()));
                        } catch (Exception ignored) {
                        }
                    }
                })
                .doFinally(signalType -> {
                    if (!completed.getAndSet(true)) {
                        try {
                            session.close();
                        } catch (Exception ignored) {
                        }
                    }
                })
                .subscribe();
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        FluxSink<PatientClientStreamingDTO> sink = (FluxSink<PatientClientStreamingDTO>) session.getAttributes().get("sink");
        if (sink != null) {
            PatientClientStreamingDTO dto = objectMapper.readValue(message.getPayload(), PatientClientStreamingDTO.class);
            sink.next(dto);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        FluxSink<PatientClientStreamingDTO> sink = (FluxSink<PatientClientStreamingDTO>) session.getAttributes().get("sink");
        if (sink != null) {
            sink.complete();
        }
    }

    // Inner class to hold the sink reference
    private static class FluxSinkHolder {
        private FluxSink<PatientClientStreamingDTO> sink;
    }
}