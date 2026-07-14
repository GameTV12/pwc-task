package com.pwc.routing.api;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * The Swagger UI is the start page: hitting the service root lands on the
 * interactive API documentation.
 */
@Controller
public class HomeController {

    @Hidden
    @GetMapping("/")
    public String home() {
        return "redirect:/swagger-ui/index.html";
    }
}
