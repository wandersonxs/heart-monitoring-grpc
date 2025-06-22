package br.com.itfree.heartmonitoringclient.service;

import br.com.itfree.heartmonitoringclient.controller.NumberDTO;
import br.com.itfree.heartmonitoringclient.controller.TotalDTO;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class SumWebSocketHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private int total = 0;

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        NumberDTO dto = objectMapper.readValue(message.getPayload(), NumberDTO.class);
        total += dto.getNumber();

        String response = objectMapper.writeValueAsString(new TotalDTO(total));
        session.sendMessage(new TextMessage(response));
    }
}