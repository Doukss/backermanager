package com.immo.common.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DocsController {
    @GetMapping({"/", "/docs", "/swagger"})
    public String swagger() {
        return "redirect:/swagger-ui/index.html";
    }
}
