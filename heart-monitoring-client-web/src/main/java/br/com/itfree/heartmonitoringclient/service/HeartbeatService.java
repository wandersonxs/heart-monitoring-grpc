//package br.com.itfree.heartmonitoringclient.service;
//
//import br.com.itfree.heartmonitoring.proto.HeartbeatCountinuouslyResponse;
//import br.com.itfree.heartmonitoringclient.dto.PatientClientStreamingDTO;
//import br.com.itfree.heartmonitoringclient.dto.PatientRequestDTO;
//import br.com.itfree.heartmonitoringclient.dto.PatientResponsetDTO;
//import br.com.itfree.heartmonitoringclient.dto.PatientSummaryResponsetDTO;
//import io.grpc.Status;
//import io.grpc.StatusRuntimeException;
//import io.grpc.stub.StreamObserver;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Service;
//import br.com.itfree.heartmonitoring.proto.HeartbeatServiceGrpc;
//import br.com.itfree.heartmonitoring.proto.HeartbeatRequest;
//import br.com.itfree.heartmonitoring.proto.HeartbeatResponse;
//import br.com.itfree.heartmonitoring.proto.HeartbeatMessage;
//import br.com.itfree.heartmonitoring.proto.HeartbeatClientStreamRequest;
//import br.com.itfree.heartmonitoring.proto.HeartbeatClientStreamResponse;
//
//import reactor.core.Disposable;
//import reactor.core.publisher.Flux;
//import reactor.core.publisher.FluxSink;
//import reactor.core.publisher.Mono;
//
//import java.time.LocalDateTime;
//import java.util.ArrayList;
//import java.util.List;
//
//@Service
//@RequiredArgsConstructor
//@Slf4j
//public class HeartbeatService {
//
//    private final HeartbeatServiceGrpc.HeartbeatServiceBlockingStub blockingStub;
//    private final HeartbeatServiceGrpc.HeartbeatServiceStub heartbeatServiceStub;
//
//
//    public PatientResponsetDTO fetchHeartbeatData(String name) {
//
//        HeartbeatRequest request = HeartbeatRequest.newBuilder().setName(name).build();
//        try {
//            HeartbeatResponse response = blockingStub.getHeartbeats(request);
//            return PatientResponsetDTO.builder().heartbeat(response.getHeartbeats().getBpm()).timestamp(LocalDateTime.now()).build();
//
//        } catch (StatusRuntimeException e) {
//            if (e.getStatus().getCode() == Status.Code.NOT_FOUND) {
//                String message = "No heartbeat found for patient:" + name;
//                log.info(message);
//                return null;
//            } else {
//                log.error("gRPC error: {}", e.getStatus().getDescription());
//                return null;
//            }
//        }
//    }
//
//    public Flux<PatientResponsetDTO> fetchHeartbeatsContinuasly(String name) {
//        HeartbeatRequest request = HeartbeatRequest.newBuilder()
//                .setName(name)
//                .build();
//
//        return Flux.create(emitter -> {
//            heartbeatServiceStub.getHeartbeatsContinuously(request, new StreamObserver<>() {
//                @Override
//                public void onNext(HeartbeatCountinuouslyResponse response) {
//                    for (HeartbeatMessage message : response.getHeartbeatsList()) {
//                        PatientResponsetDTO dto = PatientResponsetDTO.builder()
//                                .heartbeat(message.getBpm())
//                                .timestamp(LocalDateTime.parse(message.getTimestamp()))
//                                .build();
//
//                        emitter.next(dto);
//                    }
//                }
//
//                @Override
//                public void onError(Throwable throwable) {
//                    emitter.error(throwable);
//                }
//
//                @Override
//                public void onCompleted() {
//                    emitter.complete();
//                }
//            });
//        });
//    }
//
//    public Mono<PatientSummaryResponsetDTO> sendHeartbeatsContinuasly(Flux<PatientClientStreamingDTO> patientClientStreamingDTO) {
//
//        return Mono.create(emitter -> {
//
//            StreamObserver<HeartbeatClientStreamResponse> responseObserver = new StreamObserver<>() {
//                @Override
//                public void onNext(HeartbeatClientStreamResponse response) {
//                    PatientSummaryResponsetDTO summaryDTO = PatientSummaryResponsetDTO.builder()
//                            .name(response.getName())
//                            .averageBpm(response.getAverageBpm())
//                            .minBpm(response.getMinBpm())
//                            .maxBpm(response.getMaxBpm())
//                            .timestamp(LocalDateTime.parse(response.getTimestamp()))
//                            .build();
//
//                    emitter.success(summaryDTO); // ✅ return once
//                }
//
//                @Override
//                public void onError(Throwable throwable) {
//                    emitter.error(throwable);
//                }
//
//                @Override
//                public void onCompleted() {
//                    // No-op: response already returned on onNext()
//                }
//            };
//
//            StreamObserver<HeartbeatClientStreamRequest> requestObserver =
//                    heartbeatServiceStub.sendHeartbeatsContinuously(responseObserver);
//
//            patientClientStreamingDTO
//                    .doOnNext(dto -> {
//                        HeartbeatClientStreamRequest request = HeartbeatClientStreamRequest.newBuilder()
//                                .setName(dto.getName())
//                                .setBpm(dto.getHeartbeat())
//                                .setTimestamp(dto.getTimestamp().toString())
//                                .build();
//                        requestObserver.onNext(request);
//                    })
//                    .doOnComplete(requestObserver::onCompleted)
//                    .doOnError(requestObserver::onError)
//                    .subscribe();
//        });
//    }
//
//    public Flux<PatientSummaryResponsetDTO> sendHeartbeatsContinuouslyReceiveRealTimeSummary(
//            Flux<PatientClientStreamingDTO> patientClientStreamingDTO) {
//
//        return Flux.create(emitter -> {
//
//            // Define the gRPC response handler (receives messages from the server)
//            StreamObserver<HeartbeatClientStreamResponse> responseObserver = new StreamObserver<>() {
//
//                @Override
//                public void onNext(HeartbeatClientStreamResponse response) {
//                    emitter.next(PatientSummaryResponsetDTO.builder()
//                            .name(response.getName())
//                            .averageBpm(response.getAverageBpm())
//                            .minBpm(response.getMinBpm())
//                            .maxBpm(response.getMaxBpm())
//                            .timestamp(LocalDateTime.parse(response.getTimestamp()))
//                            .build());
//                }
//
//                @Override
//                public void onError(Throwable throwable) {
//                    emitter.error(throwable);
//                }
//
//                @Override
//                public void onCompleted() {
//                    emitter.complete();
//                }
//            };
//
//            // Create gRPC request stream
//            StreamObserver<HeartbeatClientStreamRequest> requestObserver =
//                    heartbeatServiceStub.sendHeartbeatsContinuouslyReceiveRealTimeSummary(responseObserver);
//
//            // Subscribe to the incoming Flux and send to gRPC as they arrive
//            patientClientStreamingDTO.subscribe(
//                    dto -> {
//                        HeartbeatClientStreamRequest grpcRequest = HeartbeatClientStreamRequest.newBuilder()
//                                .setName(dto.getName())
//                                .setBpm(dto.getHeartbeat())
//                                .setTimestamp(dto.getTimestamp().toString())
//                                .build();
//                        requestObserver.onNext(grpcRequest);
//                    },
//                    throwable -> {
//                        requestObserver.onError(throwable);
//                        emitter.error(throwable);
//                    },
//                    requestObserver::onCompleted
//            );
//
//            // Cancel subscription if client disconnects
//            emitter.onDispose(() -> {
//                try {
//                    requestObserver.onCompleted();
//                } catch (Exception ignored) {
//                }
//            });
//
//        }, FluxSink.OverflowStrategy.LATEST); // Or BUFFER if many events expected
//    }
//
//}
