package br.com.itfree.heartmonitoringclient.service;

import br.com.itfree.heartmonitoring.proto.*;
import br.com.itfree.heartmonitoringclient.dto.PatientClientStreamingDTO;
import br.com.itfree.heartmonitoringclient.dto.PatientResponsetDTO;
import br.com.itfree.heartmonitoringclient.dto.PatientSummaryResponsetDTO;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class HeartbeatService {

    private final HeartbeatServiceGrpc.HeartbeatServiceBlockingStub heartbeatServiceBlockingStub;
    private final HeartbeatServiceGrpc.HeartbeatServiceStub heartbeatServiceStub;

    public PatientSummaryResponsetDTO retrievePatientHeartbeatSummary(String name) {

        br.com.itfree.heartmonitoring.proto.PatientRequest request = br.com.itfree.heartmonitoring.proto.PatientRequest.newBuilder().setName(name).build();

        try {
            HeartbeatSummaryResponse response = heartbeatServiceBlockingStub.retrievePatientHeartbeatSummary(request);
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
                log.info("No data found for: {}", name);
            } else {
                log.error("gRPC error: {}", e.getStatus().getDescription());
            }
            return null;
        }
    }

    public Flux<PatientResponsetDTO> streamHeartbeatByPatientName(String name) {
        PatientRequest request = PatientRequest.newBuilder()
                .setName(name)
                .build();

        return Flux.create(emitter -> {
            heartbeatServiceStub.streamHeartbeatByPatientName(request, new StreamObserver<>() {
                @Override
                public void onNext(HeartbeatResponse response) {
                        PatientResponsetDTO dto = PatientResponsetDTO.builder()
                                .heartbeat(response.getBpm())
                                .timestamp(LocalDateTime.parse(response.getTimestamp()))
                                .build();

                        emitter.next(dto);
                }

                @Override
                public void onError(Throwable throwable) {
                    emitter.error(throwable);
                }

                @Override
                public void onCompleted() {
                    emitter.complete();
                }
            });
        });
    }

    public Mono<PatientSummaryResponsetDTO> sendHeartbeatsStreamingAndGetHeartbeatSummary(Flux<PatientClientStreamingDTO> patientClientStreamingDTO) {

        return Mono.create(emitter -> {

            StreamObserver<HeartbeatSummaryResponse> responseObserver = new StreamObserver<>() {
                @Override
                public void onNext(HeartbeatSummaryResponse response) {
                    PatientSummaryResponsetDTO summaryDTO = PatientSummaryResponsetDTO.builder()
                            .name(response.getName())
                            .averageBpm(response.getAverageBpm())
                            .minBpm(response.getMinBpm())
                            .maxBpm(response.getMaxBpm())
                            .timestamp(response.getTimestamp())
                            .build();

                    emitter.success(summaryDTO); // ✅ return once
                }

                @Override
                public void onError(Throwable throwable) {
                    emitter.error(throwable);
                }

                @Override
                public void onCompleted() {
                    // No-op: response already returned on onNext()
                }
            };

            StreamObserver<HeartbeatRequest> requestObserver =
                    heartbeatServiceStub.sendHeartbeatsStreamingAndGetHeartbeatSummary(responseObserver);

            patientClientStreamingDTO
                    .doOnNext(dto -> {
                        HeartbeatRequest request = HeartbeatRequest.newBuilder()
                                .setName(dto.getName())
                                .setBpm(dto.getHeartbeat())
                                .setTimestamp(dto.getTimestamp().toString())
                                .build();
                        requestObserver.onNext(request);
                    })
                    .doOnComplete(requestObserver::onCompleted)
                    .doOnError(requestObserver::onError)
                    .subscribe();
        });
    }

    public Flux<PatientSummaryResponsetDTO> sendHeartbeatsReceiveRealTimeHeartbeatSummaryResponse(
            Flux<PatientClientStreamingDTO> patientClientStreamingDTO) {

        return Flux.create(emitter -> {

            // Define the gRPC response handler (receives messages from the server)
            StreamObserver<HeartbeatSummaryResponse> responseObserver = new StreamObserver<>() {

                @Override
                public void onNext(HeartbeatSummaryResponse response) {
                    emitter.next(PatientSummaryResponsetDTO.builder()
                            .name(response.getName())
                            .averageBpm(response.getAverageBpm())
                            .minBpm(response.getMinBpm())
                            .maxBpm(response.getMaxBpm())
                            .timestamp(response.getTimestamp())
                            .build());
                }

                @Override
                public void onError(Throwable throwable) {
                    emitter.error(throwable);
                }

                @Override
                public void onCompleted() {
                    emitter.complete();
                }
            };

            // Create gRPC request stream
            StreamObserver<HeartbeatRequest> requestObserver =
                    heartbeatServiceStub.sendHeartbeatsReceiveRealTimeHeartbeatSummaryResponse(responseObserver);

            // Subscribe to the incoming Flux and send to gRPC as they arrive
            patientClientStreamingDTO.subscribe(
                    dto -> {
                        HeartbeatRequest grpcRequest = HeartbeatRequest.newBuilder()
                                .setName(dto.getName())
                                .setBpm(dto.getHeartbeat())
                                .setTimestamp(dto.getTimestamp().toString())
                                .build();
                        requestObserver.onNext(grpcRequest);
                    },
                    throwable -> {
                        requestObserver.onError(throwable);
                        emitter.error(throwable);
                    },
                    requestObserver::onCompleted
            );

            // Cancel subscription if client disconnects
            emitter.onDispose(() -> {
                try {
                    requestObserver.onCompleted();
                } catch (Exception ignored) {
                }
            });

        }, FluxSink.OverflowStrategy.LATEST); // Or BUFFER if many events expected
    }

}
