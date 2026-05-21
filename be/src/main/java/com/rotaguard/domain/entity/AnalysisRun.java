/* Nhom I */
package com.rotaguard.domain.entity;

import com.rotaguard.domain.enums.RunType;
import com.rotaguard.support.AppTimeZones;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "analysis_run")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalysisRun {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "week_start", nullable = false)
  private LocalDate weekStart;

  @Column(name = "policy_id", nullable = false)
  private Long policyId;

  @Column(name = "created_at", nullable = false)
  @Builder.Default
  private ZonedDateTime createdAt = AppTimeZones.now();

  @Column(name = "run_type", nullable = false)
  @Builder.Default
  private String runType = RunType.FULL.name();
}
