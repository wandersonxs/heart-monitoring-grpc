package br.com.itfree.heartmonitoringclient.config;

import br.com.itfree.heartmonitoringclient.service.HeartbeatClientService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@AllArgsConstructor
@Slf4j
public class StartupTasks {

    private final HeartbeatClientService heartbeatClientService;

//    @Bean
//    public CommandLineRunner retrievePatientHeartbeatSummaryUnary() {
//        System.out.println("Running Task retrievePatientHeartbeatSummaryUnary...");
//        PatientSummaryResponsetDTO patientSummaryResponsetDTO = heartbeatClientService.retrievePatientHeartbeatSummary("Schopenhauer");
//        return args ->  log.info(patientSummaryResponsetDTO.toString());
//    }
//
//    @Bean
//    public CommandLineRunner streamHeartbeatByPatientName() {
//        System.out.println("Running Task streamHeartbeatByPatientName...");
//        heartbeatClientService.streamHeartbeatByPatientName("Schopenhauer");
//        return args ->  log.info("Finished streamHeartbeatByPatientName.");
//    }

//    @Bean
//    public CommandLineRunner sendHeartbeatsStreamingAndGetHeartbeatSummary() {
//        System.out.println("Running Task sendHeartbeatsStreamingAndGetHeartbeatSummary...");
//        heartbeatClientService.sendHeartbeatsStreamingAndGetHeartbeatSummary("Schopenhauer");
//        return args ->  log.info("Finished sendHeartbeatsStreamingAndGetHeartbeatSummary.");
//    }

    @Bean
    public CommandLineRunner sendHeartbeatsReceiveRealTimeHeartbeatSummaryResponse() {
        System.out.println("Running Task sendHeartbeatsReceiveRealTimeHeartbeatSummaryResponse...");
        heartbeatClientService.sendHeartbeatsReceiveRealTimeHeartbeatSummaryResponse("Schopenhauer");
        return args ->  log.info("Finished sendHeartbeatsReceiveRealTimeHeartbeatSummaryResponse.");
    }
}