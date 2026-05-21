/* Nhom I */
package com.rotaguard.web.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class CreatePolicyRequest {

  @NotBlank private String name;
  @NotBlank private String department;

  @Positive private double minRestHours = 12;

  @Min(1)
  private int maxConsecutiveNights = 2;

  @Positive private double maxWeeklyHours = 60;

  @Min(0)
  private int churnThreshold = 60;
}
