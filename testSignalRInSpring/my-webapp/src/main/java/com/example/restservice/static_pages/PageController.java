package com.example.restservice.static_pages;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {
    @GetMapping("/client")
    public String client() {
        return "client.html"; // This will resolve to src/main/resources/static/index.html or templates/index.html
    }
}
