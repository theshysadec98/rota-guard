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
@Table(name = "violation")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Violation {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "score_id", nullable = false)
  private Long scoreId;

  @Column(name = "rule_code", nullable = false)
  private String ruleCode;

  @Column(nullable = false)
  private String severity;

  @Column(nullable = false)
  private String message;

  @Column(name = "evidence_json", nullable = false)
  private String evidenceJson;
}
