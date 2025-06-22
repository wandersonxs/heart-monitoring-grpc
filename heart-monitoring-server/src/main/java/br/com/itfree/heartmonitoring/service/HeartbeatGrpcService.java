package br.com.itfree.heartmonitoring.service;


import br.com.itfree.heartmonitoring.model.Heartbeat;
import br.com.itfree.heartmonitoring.model.Patient;
import br.com.itfree.heartmonitoring.proto.*;
import br.com.itfree.heartmonitoring.repository.HeartbeatRepository;
import br.com.itfree.heartmonitoring.repository.PatientRepository;
import br.com.itfree.heartmonitoring.repository.projection.PatientHeartbeatSummaryProjection;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import org.springframework.grpc.server.service.GrpcService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@GrpcService
public class HeartbeatGrpcService extends HeartbeatServiceGrpc.HeartbeatServiceImplBase {

    private final PatientRepository patientRepository;
    private final HeartbeatRepository heartbeatRepository;


    // Unary
    @Override
    public void retrievePatientHeartbeatSummary(PatientRequest patientRequest, StreamObserver<HeartbeatSummaryResponse> heartbeatSummaryResponseStreamObserver) {

        PatientHeartbeatSummaryProjection projection = findSummaryFromLastHourByPatientName(patientRequest, heartbeatSummaryResponseStreamObserver);

        if (projection == null) return;

        HeartbeatSummaryResponse response = HeartbeatSummaryResponse.newBuilder()
                .setName(projection.getName())
                .setAverageBpm(projection.getAverageBpm())
                .setMaxBpm(projection.getMaxBpm())
                .setMinBpm(projection.getMinBpm())
                .setLastBpm(projection.getLastBpm())
                .setTimestamp(projection.getTimestamp())
                .build();

        heartbeatSummaryResponseStreamObserver.onNext(response);
        heartbeatSummaryResponseStreamObserver.onCompleted();

    }


    // Server Streaming
    @Override
    public void streamHeartbeatByPatientName(PatientRequest patientRequest, StreamObserver<HeartbeatResponse> responseObserver) {

        List<Heartbeat> heartbeats = findAllByPatientName(patientRequest, responseObserver);

        if (heartbeats == null) {
            responseObserver.onError(new RuntimeException("No heartbeat found for name: " + patientRequest.getName()));
            return;
        }

        try {
            for (Heartbeat heartbeat : heartbeats) {

                HeartbeatResponse heartbeatResponse = HeartbeatResponse.newBuilder()
                        .setBpm(heartbeat.getBpm())
                        .setTimestamp(heartbeat.getTimestamp().toString())
                        .build();

                responseObserver.onNext(heartbeatResponse);
                TimeUnit.SECONDS.sleep(3);

            }
        } catch (Exception e) {
            responseObserver.onError(new RuntimeException(e));
        }
        responseObserver.onCompleted();
    }


    private PatientHeartbeatSummaryProjection findSummaryFromLastHourByPatientName(PatientRequest patientRequest, StreamObserver<HeartbeatSummaryResponse> responseObserver) {

        Optional<PatientHeartbeatSummaryProjection> optional =
                heartbeatRepository.findSummaryFromLast10RecordsByPatientName(patientRequest.getName());

        if (optional.isEmpty()) {
            Status status = Status.NOT_FOUND.withDescription("No heartbeat found for name: " + patientRequest.getName());
            responseObserver.onError(status.asRuntimeException());
            return null;
        }

        return optional.get();
    }

    private List<Heartbeat> findAllByPatientName(PatientRequest patientRequest, StreamObserver<HeartbeatResponse> heartbeatResponseStreamObserver) {

        List<Heartbeat> heartbeats = heartbeatRepository.findAllByPatientName(patientRequest.getName());

        if (heartbeats == null || heartbeats.isEmpty()) {
            Status status = Status.NOT_FOUND.withDescription("No heartbeat found for name: " + patientRequest.getName());
            heartbeatResponseStreamObserver.onError(status.asRuntimeException());
            return null;
        }

        return heartbeats;
    }


    // Client Streaming
    @Override
    public StreamObserver<HeartbeatRequest> sendHeartbeatsStreamingAndGetHeartbeatSummary(StreamObserver<HeartbeatSummaryResponse> responseObserver) {

        return new StreamObserver<>() {

            Optional<Patient> optionalPatient = Optional.empty();

            @Override
            public void onNext(HeartbeatRequest heartbeatRequest) {

                optionalPatient = patientRepository.findByName(heartbeatRequest.getName());
                if (optionalPatient.isEmpty()) {
                    Status status = Status.NOT_FOUND.withDescription("No heartbeat found for name: " + heartbeatRequest.getName());
                }
                saveHeartbeat(optionalPatient, heartbeatRequest);
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
                Integer lastBpm = 0;

                List<Heartbeat> heartbeats = heartbeatRepository.findAllByPatientName(optionalPatient.get().getName());

                for (Heartbeat heartbeat : heartbeats) {
                    averageBpm += heartbeat.getBpm();
                    minBpm = minBpm == 0 ? heartbeat.getBpm() : Math.min(minBpm, heartbeat.getBpm());
                    maxBpm = Math.max(maxBpm, heartbeat.getBpm());
                    lastBpm = heartbeat.getBpm();
                }

                averageBpm = averageBpm / heartbeats.size();


                HeartbeatSummaryResponse heartbeatClientStreamResponse = HeartbeatSummaryResponse.newBuilder()
                        .setName(optionalPatient.get().getName())
                        .setLastBpm(lastBpm)
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
    public StreamObserver<HeartbeatRequest> sendHeartbeatsReceiveRealTimeHeartbeatSummaryResponse(StreamObserver<HeartbeatSummaryResponse> responseObserver) {

        return new StreamObserver<>() {

            Optional<Patient> optionalPatient = Optional.empty();

            @Override
            public void onNext(HeartbeatRequest heartbeatRequest) {

                optionalPatient = patientRepository.findByName(heartbeatRequest.getName());
                if (optionalPatient.isEmpty()) {
                    Status status = Status.NOT_FOUND.withDescription("No heartbeat found for name: " + heartbeatRequest.getName());
                }

                saveHeartbeat(optionalPatient, heartbeatRequest);

                HeartbeatSummaryResponse heartbeatSummaryResponse = getHeartbeatSummaryResponse(optionalPatient);
                responseObserver.onNext(heartbeatSummaryResponse);

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

    private void saveHeartbeat(Optional<Patient> optionalPatient, HeartbeatRequest heartbeatRequest){

        heartbeatRepository.save(Heartbeat.builder()
                .patient(optionalPatient.get())
                .bpm(heartbeatRequest.getBpm())
                .timestamp(LocalDateTime.now())
                .build());
    }

    private HeartbeatSummaryResponse getHeartbeatSummaryResponse(Optional<Patient> optionalPatient) {
        Float averageBpm = 0.0F;
        Integer minBpm = 0;
        Integer maxBpm = 0;
        Integer lastBpm = 0;

        List<Heartbeat> heartbeats = heartbeatRepository.findAllByPatientName(optionalPatient.get().getName());

        for (Heartbeat heartbeat : heartbeats) {
            averageBpm += heartbeat.getBpm();
            minBpm = minBpm == 0 ? heartbeat.getBpm() : Math.min(minBpm, heartbeat.getBpm());
            maxBpm = Math.max(maxBpm, heartbeat.getBpm());
            lastBpm = heartbeat.getBpm();
        }

        averageBpm = averageBpm / heartbeats.size();


        return HeartbeatSummaryResponse.newBuilder()
                .setName(optionalPatient.get().getName())
                .setLastBpm(lastBpm)
                .setAverageBpm(averageBpm)
                .setMaxBpm(maxBpm)
                .setMinBpm(minBpm)
                .setTimestamp(LocalDateTime.now().toString())
                .build();
    }
}
