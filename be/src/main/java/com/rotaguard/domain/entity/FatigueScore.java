/* Nhom I */
package com.rotaguard.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "fatigue_score")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FatigueScore {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "run_id", nullable = false)
  private Long runId;

  @Column(name = "staff_id", nullable = false)
  private Long staffId;

  @Column(name = "total_points", nullable = false)
  private int totalPoints;

  @Column(name = "risk_level", nullable = false)
  private String riskLevel;

  @Column(name = "churn_index", nullable = false)
  @Builder.Default
  private int churnIndex = 0;
}
