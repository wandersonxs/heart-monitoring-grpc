package br.com.itfree.heartmonitoringclient.controller;

import br.com.itfree.heartmonitoringclient.dto.PatientClientStreamingDTO;
import br.com.itfree.heartmonitoringclient.dto.PatientRequestDTO;
import br.com.itfree.heartmonitoringclient.dto.PatientResponsetDTO;
import br.com.itfree.heartmonitoringclient.dto.PatientSummaryResponsetDTO;
import br.com.itfree.heartmonitoringclient.service.HeartbeatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
public class HeartbeatController {


    private final HeartbeatService heartbeatService;

    @PostMapping(value = "/greet", produces = "application/json")
    @ResponseBody
    public PatientSummaryResponsetDTO greet(@RequestBody PatientRequestDTO patientRequestDTO) {
        PatientSummaryResponsetDTO response = heartbeatService.retrievePatientHeartbeatSummary(patientRequestDTO.getName());
        return response;
    }


    @GetMapping(value = "/heartbeat-stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<PatientResponsetDTO> streamHeartbeatByPatientName(@RequestParam String name) {
        return heartbeatService.streamHeartbeatByPatientName(name);
    }

    @PostMapping(value = "/heartbeat-client-stream", consumes = {"application/x-ndjson", "application/x-ndjson;charset=UTF-8"}, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<PatientSummaryResponsetDTO> sendHeartbeatsStreamingAndGetHeartbeatSummary(@RequestBody Flux<PatientClientStreamingDTO> patientClientStreamingDTO) {
        return heartbeatService.sendHeartbeatsStreamingAndGetHeartbeatSummary(patientClientStreamingDTO);
    }

    @PostMapping(value = "/heartbeat-biderectional-stream",
            consumes = MediaType.APPLICATION_NDJSON_VALUE,
            produces = MediaType.TEXT_EVENT_STREAM_VALUE) // 👈 This enables SSE
    public Flux<PatientSummaryResponsetDTO> sendHeartbeatsReceiveRealTimeHeartbeatSummaryResponse(
            @RequestBody Flux<PatientClientStreamingDTO> patientClientStreamingDTO) {
        return heartbeatService.sendHeartbeatsReceiveRealTimeHeartbeatSummaryResponse(patientClientStreamingDTO);
    }

}