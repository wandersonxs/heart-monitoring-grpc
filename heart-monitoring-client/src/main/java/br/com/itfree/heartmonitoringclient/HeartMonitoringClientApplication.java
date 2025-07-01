package br.com.itfree.heartmonitoringclient;

import br.com.itfree.heartmonitoringclient.service.HeartbeatClientService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Scanner;

@SpringBootApplication
public class HeartMonitoringClientApplication implements CommandLineRunner {

    private final HeartbeatClientService heartbeatClientService;

    public HeartMonitoringClientApplication(HeartbeatClientService heartbeatClientService) {
        this.heartbeatClientService = heartbeatClientService;
    }

    public static void main(String[] args) {
        SpringApplication.run(HeartMonitoringClientApplication.class, args);
    }

    @Override
    public void run(String... args) {

        Scanner scanner = new Scanner(System.in);

        System.out.println("""
                Choose an option:
                1 - retrievePatientHeartbeatSummaryUnary (Unary)
                2 - streamHeartbeatByPatientName (Server Streaming)
                3 - sendHeartbeatsStreamingAndGetHeartbeatSummary (Client Streaming)
                4 - sendHeartbeatsReceiveRealTimeHeartbeatSummaryResponse (Bidirectional Streaming)
                """);

        System.out.print("Enter option number: ");
        int choice = scanner.nextInt();
        scanner.nextLine(); // Consume newline left by nextInt()

        if (choice < 1 || choice > 4) {
            System.out.println("Invalid option.");
            System.exit(1);
        }

        System.out.print("Enter patient name: ");
        String patientName = scanner.nextLine();

        switch (choice) {
            case 1 -> {
                System.out.println("Running Task retrievePatientHeartbeatSummaryUnary...");
                var response = heartbeatClientService.retrievePatientHeartbeatSummary(patientName);
                System.out.println(response);
            }
            case 2 -> {
                System.out.println("Running Task streamHeartbeatByPatientName...");
                heartbeatClientService.streamHeartbeatByPatientName(patientName);
            }
            case 3 -> {
                System.out.println("Running Task sendHeartbeatsStreamingAndGetHeartbeatSummary...");
                heartbeatClientService.sendHeartbeatsStreamingAndGetHeartbeatSummary(patientName);
            }
            case 4 -> {
                System.out.println("Running Task sendHeartbeatsReceiveRealTimeHeartbeatSummaryResponse...");
                heartbeatClientService.sendHeartbeatsReceiveRealTimeHeartbeatSummaryResponse(patientName);
            }
        }

        System.out.println("Finished.");
        System.exit(0);
    }
}