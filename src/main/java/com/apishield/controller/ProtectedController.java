
package com.apishield.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProtectedController {

    @GetMapping("/api/v1/protected")
    public String protectedResource() {
        return "API Shield authenticated your request!";
    }
}
