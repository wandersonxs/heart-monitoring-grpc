package br.com.itfree.heartmonitoring.service;


import br.com.itfree.heartmonitoring.model.Heartbeat;
import br.com.itfree.heartmonitoring.model.Patient;
import br.com.itfree.heartmonitoring.proto.HeartbeatServiceGrpc;
import br.com.itfree.heartmonitoring.repository.HeartbeatRepository;
import br.com.itfree.heartmonitoring.util.HeartbeatGenerator;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import org.springframework.grpc.server.service.GrpcService;
import br.com.itfree.heartmonitoring.proto.HeartbeatRequest;
import br.com.itfree.heartmonitoring.proto.HeartbeatResponse;
import br.com.itfree.heartmonitoring.proto.HeartbeatMessage;
import br.com.itfree.heartmonitoring.proto.HeartbeatCountinuouslyResponse;
import br.com.itfree.heartmonitoring.proto.HeartbeatClientStreamRequest;
import br.com.itfree.heartmonitoring.proto.HeartbeatClientStreamResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@GrpcService
public class HeartbeatGrpcService extends HeartbeatServiceGrpc.HeartbeatServiceImplBase {

    private final HeartbeatRepository heartbeatRepository;

    /**
     * Unary
     *
     * @param request
     * @param responseObserver
     */
    @Override
    public void getHeartbeats(HeartbeatRequest request, StreamObserver<HeartbeatResponse> responseObserver) {
        String name = request.getName();
        Heartbeat latest = heartbeatRepository.findLatestByPatientName(name)
                .orElse(null);

        if (latest == null) {
            responseObserver.onError(new RuntimeException("No heartbeat found for name: " + name));
            return;
        }

        HeartbeatMessage message = HeartbeatMessage.newBuilder()
                .setBpm(latest.getBpm())
                .setTimestamp(latest.getTimestamp().toString())
                .build();

        HeartbeatResponse response = HeartbeatResponse.newBuilder()
                .setHeartbeats(message)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    /**
     * Server Streaming
     *
     * @param request
     * @param responseObserver
     */
    @Override
    public void getHeartbeatsContinuously(HeartbeatRequest request, StreamObserver<HeartbeatCountinuouslyResponse> responseObserver) {

        String name = request.getName();
        Heartbeat latest = heartbeatRepository.findLatestByPatientName(name)
                .orElse(null);

        if (latest == null) {
            responseObserver.onError(new RuntimeException("No heartbeat found for name: " + name));
            return;
        }

        try {
            for (int i = 0; i <= 10; i++) {

                heartbeatRepository.save(Heartbeat.builder().patient(Patient.builder().id(latest.getPatient().getId()).build()).bpm(HeartbeatGenerator.generateRandomBpm()).timestamp(LocalDateTime.now()).build());

                latest = heartbeatRepository.findLatestByPatientName(name).orElse(null);

                HeartbeatMessage message = HeartbeatMessage.newBuilder()
                        .setBpm(latest.getBpm())
                        .setTimestamp(latest.getTimestamp().toString())
                        .build();

                HeartbeatCountinuouslyResponse response = HeartbeatCountinuouslyResponse.newBuilder()
                        .addHeartbeats(message)
                        .build();


                responseObserver.onNext(response);
                TimeUnit.SECONDS.sleep(3);

            }
        } catch (Exception e) {
            responseObserver.onError(new RuntimeException(e));
        }
        responseObserver.onCompleted();
    }

    /**
     * Client Streaming
     *
     * @param responseObserver
     * @return
     */
    @Override
    public StreamObserver<HeartbeatClientStreamRequest> sendHeartbeatsContinuously(StreamObserver<HeartbeatClientStreamResponse> responseObserver) {

        return new StreamObserver<>() {
            String name = null;

            @Override
            public void onNext(HeartbeatClientStreamRequest heartbeatClientStreamRequest) {

                name = heartbeatClientStreamRequest.getName();
                Heartbeat latest = heartbeatRepository.findLatestByPatientName(name).orElse(null);

                if (latest == null) {
                    responseObserver.onError(new RuntimeException("No heartbeat found for name: " + name));
                }

                heartbeatRepository.save(Heartbeat.builder().patient(Patient.builder().id(latest.getPatient().getId()).build()).bpm(heartbeatClientStreamRequest.getBpm()).timestamp(LocalDateTime.now()).build());

            }

            @Override
            public void onError(Throwable throwable) {
                responseObserver.onError(throwable);
            }

            @Override
            public void onCompleted() {

                Float averageBpm = 0.0F;
                Integer minBpm = 0;
                Integer maxBpm = 0;

                Heartbeat latest = heartbeatRepository.findLatestByPatientName(name).orElse(null);

                if (latest == null) {
                    responseObserver.onError(new RuntimeException("No heartbeat found for name: " + name));
                }
                List<Heartbeat> list = heartbeatRepository.findByPatientId(latest.getPatient().getId());

                for (Heartbeat heartbeat : list) {
                    averageBpm += heartbeat.getBpm();
                    minBpm = minBpm == 0 ? heartbeat.getBpm() : Math.min(minBpm, heartbeat.getBpm());
                    maxBpm = Math.max(maxBpm, heartbeat.getBpm());
                }

                averageBpm = averageBpm / list.size();

                HeartbeatClientStreamResponse heartbeatClientStreamResponse = HeartbeatClientStreamResponse.newBuilder()
                        .setName(name)
                        .setAverageBpm(averageBpm)
                        .setMaxBpm(maxBpm)
                        .setMinBpm(minBpm)
                        .setTimestamp(LocalDateTime.now().toString())
                        .build();
                responseObserver.onNext(heartbeatClientStreamResponse);
                responseObserver.onCompleted();
            }
        };

    }

    @Override
    public StreamObserver<HeartbeatClientStreamRequest> sendHeartbeatsContinuouslyReceiveRealTimeSummary(StreamObserver<HeartbeatClientStreamResponse> responseObserver) {

        return new StreamObserver<>() {
            String name = null;

            @Override
            public void onNext(HeartbeatClientStreamRequest heartbeatClientStreamRequest) {
                Float averageBpm = 0.0F;
                Integer minBpm = 0;
                Integer maxBpm = 0;
                name = heartbeatClientStreamRequest.getName();

                Heartbeat latest = heartbeatRepository.findLatestByPatientName(name).orElse(null);

                if (latest == null) {
                    responseObserver.onError(new RuntimeException("No heartbeat found for name: " + name));
                }

                heartbeatRepository.save(Heartbeat.builder().patient(Patient.builder().id(latest.getPatient().getId()).build()).bpm(heartbeatClientStreamRequest.getBpm()).timestamp(LocalDateTime.now()).build());

                List<Heartbeat> list = heartbeatRepository.findByPatientId(latest.getPatient().getId());

                for (Heartbeat heartbeat : list) {
                    averageBpm += heartbeat.getBpm();
                    minBpm = minBpm == 0 ? heartbeat.getBpm() : Math.min(minBpm, heartbeat.getBpm());
                    maxBpm = Math.max(maxBpm, heartbeat.getBpm());
                }

                averageBpm = averageBpm / list.size();

                HeartbeatClientStreamResponse heartbeatClientStreamResponse = HeartbeatClientStreamResponse.newBuilder()
                        .setName(name)
                        .setAverageBpm(averageBpm)
                        .setMaxBpm(maxBpm)
                        .setMinBpm(minBpm)
                        .setTimestamp(LocalDateTime.now().toString())
                        .build();

                responseObserver.onNext(heartbeatClientStreamResponse);
//                try {
//                    TimeUnit.SECONDS.sleep(3);
//                } catch (InterruptedException e) {
//                    throw new RuntimeException(e);
//                }
            }

            @Override
            public void onError(Throwable throwable) {
                responseObserver.onError(throwable);
            }

            @Override
            public void onCompleted() {
                responseObserver.onCompleted();
            }
        };

    }

}
