package br.com.itfree.heartmonitoringclient.service;

import br.com.itfree.heartmonitoring.proto.HeartbeatServiceGrpc;
import br.com.itfree.heartmonitoring.proto.HeartbeatSummaryResponse;
import br.com.itfree.heartmonitoring.proto.PatientRequest;
import br.com.itfree.heartmonitoringclient.dto.PatientSummaryResponsetDTO;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class HeartbeatClientService {

    private final HeartbeatServiceGrpc.HeartbeatServiceBlockingStub blockingStub;
    private final HeartbeatServiceGrpc.HeartbeatServiceStub heartbeatServiceStub;


    public PatientSummaryResponsetDTO retrievePatientHeartbeatSummary(String name) {

        PatientRequest request = PatientRequest.newBuilder().setName(name).build();

        try {
            HeartbeatSummaryResponse response = blockingStub.retrievePatientHeartbeatSummary(request);
            return PatientSummaryResponsetDTO.builder()
                    .name(response.getName())
                    .averageBpm(response.getAverageBpm())
                    .minBpm(response.getMinBpm())
                    .maxBpm(response.getMaxBpm())
                    .lastBpm(response.getLastBpm())
                    .timestamp(response.getTimestamp())
                    .build();

        } catch (StatusRuntimeException e) {
            if (e.getStatus().getCode() == Status.Code.NOT_FOUND) {
                String message = "No data found for:" + name;
                log.info(message);
                return null;
            } else {
                log.error("gRPC error: {}", e.getStatus().getDescription());
                return null;
            }
        }
    }

}
