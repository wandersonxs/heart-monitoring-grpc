//package br.com.itfree.heartmonitoringclient.service;
//
//import org.springframework.stereotype.Service;
//import reactor.core.publisher.Flux;
//import reactor.core.publisher.Sinks;
//
//@Service
//public class SumService {
//
//    private final Sinks.Many<Integer> sink = Sinks.many().multicast().onBackpressureBuffer();
//    private int total = 0;
//
//    public void add(int number) {
//        total += number;
//        sink.tryEmitNext(total); // Emit updated total
//    }
//
//    public Flux<Integer> getRunningTotalStream() {
//        return sink.asFlux(); // Expose as reactive stream
//    }
//}