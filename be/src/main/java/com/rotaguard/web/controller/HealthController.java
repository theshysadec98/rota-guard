/* Nhom I */
package com.rotaguard.web.controller;

import com.rotaguard.web.response.HealthResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Health")
@RestController
@RequestMapping("/api/v1")
public class HealthController {

  @Operation(summary = "Service health check")
  @GetMapping("/health")
  public HealthResponse health() {
    return HealthResponse.builder().status("ok").service("be").build();
  }
}
