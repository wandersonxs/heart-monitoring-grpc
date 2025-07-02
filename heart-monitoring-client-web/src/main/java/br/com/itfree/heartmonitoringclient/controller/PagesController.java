package br.com.itfree.heartmonitoringclient.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class PagesController {

    @GetMapping("/")
    public String root() {
        return "home";
    }

    @GetMapping("/home")
    public String home() {
        return "home";
    }

    @GetMapping("/form-unary")
    public String formUnary() {
        return "form-unary";
    }

    @GetMapping("/form-server-streaming")
    public String formServerStreaming() {
        return "form-server-streaming";
    }

    @GetMapping("/form-client-streaming")
    public String formClientStreaming() {
        return "form-client-streaming";
    }

    @GetMapping("/form-bidirectional-streaming")
    public String formBidirectionalStreaming() {
        return "form-bidirectional-streaming";
    }
}