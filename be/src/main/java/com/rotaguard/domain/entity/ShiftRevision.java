/* Nhom I */
package com.rotaguard.domain.entity;

import com.rotaguard.support.AppTimeZones;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.ZonedDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "shift_revision")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShiftRevision {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "shift_id", nullable = false)
  private Long shiftId;

  @Column(name = "changed_at", nullable = false)
  @Builder.Default
  private ZonedDateTime changedAt = AppTimeZones.now();

  @Column(name = "change_type", nullable = false)
  private String changeType;
}
