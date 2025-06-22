package br.com.itfree.heartmonitoringclient.config;

import br.com.itfree.heartmonitoringclient.dto.PatientSummaryResponsetDTO;
import br.com.itfree.heartmonitoringclient.service.HeartbeatClientService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.CompletableFuture;

@Configuration
@AllArgsConstructor
@Slf4j
public class StartupTasks {

    private final HeartbeatClientService heartbeatClientService;

    @Bean
    public CommandLineRunner retrievePatientHeartbeatSummaryUnary() {
        System.out.println("Running Task One...");
        PatientSummaryResponsetDTO patientSummaryResponsetDTO = heartbeatClientService.retrievePatientHeartbeatSummary("Schopenhauer");
        return args ->  log.info(patientSummaryResponsetDTO.toString());
    }


}