package br.com.itfree.heartmonitoringclient.config;

import br.com.itfree.heartmonitoringclient.service.HeartbeatGrpcWebSocketHandler;
import br.com.itfree.heartmonitoringclient.service.HeartbeatService;
import br.com.itfree.heartmonitoringclient.service.HeartbeatSummaryWebSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

//    private final SumWebSocketHandler sumWebSocketHandler;
//
//    public WebSocketConfig(SumWebSocketHandler sumWebSocketHandler) {
//        this.sumWebSocketHandler = sumWebSocketHandler;
//    }
//
//    @Override
//    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
//        registry.addHandler(sumWebSocketHandler, "/sum-websocket").setAllowedOrigins("*");
//    }
    private final HeartbeatService heartbeatService;

    public WebSocketConfig(HeartbeatService heartbeatService) {
        this.heartbeatService = heartbeatService;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new HeartbeatGrpcWebSocketHandler(heartbeatService), "/ws/heartbeat")
                .setAllowedOrigins("*");
        registry.addHandler(new HeartbeatSummaryWebSocketHandler(heartbeatService), "/ws/summary")
                .setAllowedOrigins("*");
    }

}