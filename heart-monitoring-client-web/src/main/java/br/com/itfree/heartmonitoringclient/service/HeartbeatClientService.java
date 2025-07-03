//package br.com.itfree.heartmonitoringclient.service;
//
//import br.com.itfree.heartmonitoring.proto.HeartbeatRequest;
//import br.com.itfree.heartmonitoring.proto.HeartbeatSummaryResponse;
//import br.com.itfree.heartmonitoringclient.dto.PatientSummaryResponsetDTO;
//import io.grpc.Status;
//import io.grpc.StatusRuntimeException;
//import io.grpc.stub.StreamObserver;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Service;
//
//import java.time.LocalDateTime;
//import java.util.concurrent.ThreadLocalRandom;
//import java.util.concurrent.TimeUnit;
//
//@Service
//@RequiredArgsConstructor
//@Slf4j
//public class HeartbeatClientService {
//
//    private final br.com.itfree.heartmonitoring.proto.HeartbeatServiceGrpc.HeartbeatServiceBlockingStub blockingStub;
//    private final br.com.itfree.heartmonitoring.proto.HeartbeatServiceGrpc.HeartbeatServiceStub heartbeatServiceStub;
//
//
//    public PatientSummaryResponsetDTO retrievePatientHeartbeatSummary(String name) {
//
//        br.com.itfree.heartmonitoring.proto.PatientRequest request = br.com.itfree.heartmonitoring.proto.PatientRequest.newBuilder().setName(name).build();
//
//        try {
//            HeartbeatSummaryResponse response = blockingStub.retrievePatientHeartbeatSummary(request);
//            return PatientSummaryResponsetDTO.builder()
//                    .name(response.getName())
//                    .averageBpm(response.getAverageBpm())
//                    .minBpm(response.getMinBpm())
//                    .maxBpm(response.getMaxBpm())
//                    .lastBpm(response.getLastBpm())
//                    .timestamp(response.getTimestamp())
//                    .build();
//
//        } catch (StatusRuntimeException e) {
//            if (e.getStatus().getCode() == Status.Code.NOT_FOUND) {
//                log.info("No data found for: {}", name);
//            } else {
//                log.error("gRPC error: {}", e.getStatus().getDescription());
//            }
//            return null;
//        }
//    }
//
//    public void streamHeartbeatByPatientName(String name) {
//
//        br.com.itfree.heartmonitoring.proto.PatientRequest request = br.com.itfree.heartmonitoring.proto.PatientRequest.newBuilder().setName(name).build();
//
//        heartbeatServiceStub.streamHeartbeatByPatientName(request, new StreamObserver<>() {
//
//            @Override
//            public void onNext(br.com.itfree.heartmonitoring.proto.HeartbeatResponse heartbeatResponse) {
//                log.info(heartbeatResponse.toString());
//            }
//
//            @Override
//            public void onError(Throwable throwable) {
//                log.error(throwable.getMessage());
//            }
//
//            @Override
//            public void onCompleted() {
//                log.info("Patient heartbeat completed");
//            }
//        });
//    }
//
//    public void sendHeartbeatsStreamingAndGetHeartbeatSummary(String patientName) {
//
//        StreamObserver<HeartbeatSummaryResponse> responseStreamObserver = new StreamObserver<HeartbeatSummaryResponse>() {
//            @Override
//            public void onNext(HeartbeatSummaryResponse heartbeatSummaryResponse) {
//                log.info(heartbeatSummaryResponse.toString());
//            }
//
//            @Override
//            public void onError(Throwable throwable) {
//                log.error(throwable.getMessage());
//            }
//
//            @Override
//            public void onCompleted() {
//                log.info("Completed!");
//
//            }
//        };
//
//        StreamObserver<HeartbeatRequest> requestStreamObserver = heartbeatServiceStub.sendHeartbeatsStreamingAndGetHeartbeatSummary(responseStreamObserver);
//
//        for(int i = 0 ; i < 10 ; i++){
//            HeartbeatRequest heartbeatRequest = HeartbeatRequest.newBuilder()
//                    .setName(patientName)
//                    .setBpm(ThreadLocalRandom.current().nextInt(70, 96))
//                    .setTimestamp(LocalDateTime.now().toString())
//                    .build();
//            log.info(heartbeatRequest.toString());
//            requestStreamObserver.onNext(heartbeatRequest);
//
//            try {
//                TimeUnit.MILLISECONDS.sleep(3000L);
//            } catch (InterruptedException e) {
//                throw new RuntimeException(e);
//            }
//        }
//        requestStreamObserver.onCompleted();
//    }
//
//    public void sendHeartbeatsReceiveRealTimeHeartbeatSummaryResponse(String patientName) {
//
//        StreamObserver<HeartbeatSummaryResponse> responseStreamObserver = new StreamObserver<HeartbeatSummaryResponse>() {
//            @Override
//            public void onNext(HeartbeatSummaryResponse heartbeatSummaryResponse) {
//                log.info(heartbeatSummaryResponse.toString());
//            }
//
//            @Override
//            public void onError(Throwable throwable) {
//                log.error(throwable.getMessage());
//            }
//
//            @Override
//            public void onCompleted() {
//                log.info("Completed!");
//
//            }
//        };
//
//        StreamObserver<HeartbeatRequest> requestStreamObserver = heartbeatServiceStub.sendHeartbeatsReceiveRealTimeHeartbeatSummaryResponse(responseStreamObserver);
//
//        for(int i = 0 ; i < 10 ; i++){
//            HeartbeatRequest heartbeatRequest = HeartbeatRequest.newBuilder()
//                    .setName(patientName)
//                    .setBpm(ThreadLocalRandom.current().nextInt(70, 96))
//                    .setTimestamp(LocalDateTime.now().toString())
//                    .build();
//            log.info("REQUEST ::::::: {}", heartbeatRequest.toString());
//            requestStreamObserver.onNext(heartbeatRequest);
//
//            try {
//                TimeUnit.MILLISECONDS.sleep(3000L);
//            } catch (InterruptedException e) {
//                throw new RuntimeException(e);
//            }
//        }
//        requestStreamObserver.onCompleted();
//
//    }
//
//
//
//}
