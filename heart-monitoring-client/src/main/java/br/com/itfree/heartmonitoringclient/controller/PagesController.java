package br.com.itfree.heartmonitoringclient.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class PagesController {

    @GetMapping("/")
    public String form() {
        return "form-unary";
    }

    @GetMapping("/test")
    public String formTest() {
        return "test";
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
    public String formbidirectionalStreaming() {
        return "form-bidirectional-streaming";
    }
}