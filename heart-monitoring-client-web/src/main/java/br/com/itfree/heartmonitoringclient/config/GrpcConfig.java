package br.com.itfree.heartmonitoringclient.config;

import br.com.itfree.heartmonitoring.proto.HeartbeatServiceGrpc;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.grpc.client.GrpcChannelFactory;

@Configuration
public class GrpcConfig {

    @Bean
    public HeartbeatServiceGrpc.HeartbeatServiceBlockingStub heartbeatServiceBlockingStub(GrpcChannelFactory channelFactory) {
        return HeartbeatServiceGrpc.newBlockingStub(channelFactory.createChannel("static://localhost:9090"));
    }

    @Bean
    public HeartbeatServiceGrpc.HeartbeatServiceStub heartbeatServiceStub(GrpcChannelFactory channelFactory) {
        return HeartbeatServiceGrpc.newStub(channelFactory.createChannel("static://localhost:9090"));
    }

}


