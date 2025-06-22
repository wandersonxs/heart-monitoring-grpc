//package br.com.itfree.heartmonitoringclient.controller;
//
//import br.com.itfree.heartmonitoringclient.dto.PatientClientStreamingDTO;
//import br.com.itfree.heartmonitoringclient.dto.PatientRequestDTO;
//import br.com.itfree.heartmonitoringclient.dto.PatientResponsetDTO;
//import br.com.itfree.heartmonitoringclient.dto.PatientSummaryResponsetDTO;
////import br.com.itfree.heartmonitoringclient.service.HeartbeatService;
//import br.com.itfree.heartmonitoringclient.service.HeartbeatService;
//import br.com.itfree.heartmonitoringclient.service.SumService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.MediaType;
//import org.springframework.web.bind.annotation.*;
//import reactor.core.publisher.Flux;
//import reactor.core.publisher.Mono;
//
//import java.time.Duration;
//import java.time.LocalDateTime;
//import java.util.Map;
//
//@RestController
//@RequiredArgsConstructor
//public class HeartbeatController {
//
//
//    private final SumService sumService;
//    private final HeartbeatService heartbeatService;
//
//    @PostMapping(value = "/greet", produces = "application/json")
//    @ResponseBody
//    public PatientResponsetDTO greet(@RequestBody PatientRequestDTO patientRequestDTO) {
//        PatientResponsetDTO response = heartbeatService.fetchHeartbeatData(patientRequestDTO.getName());
//        return response;
//    }
//
//    @GetMapping(value = "/heartbeat-stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
//    public Flux<PatientResponsetDTO> streamHeartbeats(@RequestParam String name) {
//        return heartbeatService.fetchHeartbeatsContinuasly(name);
//    }
//
//    @PostMapping(value = "/heartbeat-client-stream", consumes = {"application/x-ndjson", "application/x-ndjson;charset=UTF-8"}, produces = MediaType.APPLICATION_JSON_VALUE)
//    public Mono<PatientSummaryResponsetDTO> streamHeartbeats(@RequestBody Flux<PatientClientStreamingDTO> patientClientStreamingDTO) {
//        return heartbeatService.sendHeartbeatsContinuasly(patientClientStreamingDTO);
//    }
//
////    @PostMapping(
////            value = "/heartbeat-biderectional-stream",
////            consumes = {MediaType.APPLICATION_NDJSON_VALUE},
////            produces = {MediaType.APPLICATION_NDJSON_VALUE}
////    )    @ResponseBody
////    public Flux<PatientSummaryResponsetDTO> bidirectionalStreamHeartbeats(@RequestBody Flux<PatientClientStreamingDTO> patientClientStreamingDTO) {
////        return heartbeatService.sendHeartbeatsContinuouslyReceiveRealTimeSummary(patientClientStreamingDTO);
////    }
//
//    @PostMapping(value = "/heartbeat-biderectional-stream",
//            consumes = MediaType.APPLICATION_NDJSON_VALUE,
//            produces = MediaType.TEXT_EVENT_STREAM_VALUE) // 👈 This enables SSE
//    public Flux<PatientSummaryResponsetDTO> bidirectionalStreamHeartbeats(
//            @RequestBody Flux<PatientClientStreamingDTO> patientClientStreamingDTO) {
//        return heartbeatService.sendHeartbeatsContinuouslyReceiveRealTimeSummary(patientClientStreamingDTO);
//    }
//
//}