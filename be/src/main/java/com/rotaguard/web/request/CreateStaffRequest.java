/* Nhom I */
package com.rotaguard.web.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class CreateStaffRequest {

  @NotBlank private String name;

  @NotBlank private String role;

  @NotBlank private String department;

  @Positive private double sensitivityFactor = 1.0;
}
