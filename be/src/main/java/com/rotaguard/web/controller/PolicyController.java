/* Nhom I */
package com.rotaguard.web.controller;

import com.rotaguard.repository.FatiguePolicyRepository;
import com.rotaguard.web.mapper.WebMapper;
import com.rotaguard.web.request.CreatePolicyRequest;
import com.rotaguard.web.response.PolicyResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Policies")
@RestController
@RequestMapping("/api/v1/policies")
@RequiredArgsConstructor
public class PolicyController {

  private final FatiguePolicyRepository policyRepository;

  @Operation(summary = "List fatigue policies")
  @GetMapping
  public List<PolicyResponse> listPolicies() {
    return policyRepository.findAll().stream().map(WebMapper::toPolicyResponse).toList();
  }

  @Operation(summary = "Create fatigue policy")
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public PolicyResponse createPolicy(@Valid @RequestBody CreatePolicyRequest request) {
    return WebMapper.toPolicyResponse(policyRepository.save(WebMapper.toEntity(request)));
  }
}
