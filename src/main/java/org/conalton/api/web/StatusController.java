package org.conalton.api.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/status")
public class StatusController {
  @GetMapping
  ResponseEntity<String> getStatus() {
    return ResponseEntity.ok().body("OK");
  }
}
