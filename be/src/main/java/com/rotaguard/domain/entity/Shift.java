/* Nhom I */
package com.rotaguard.domain.entity;

import com.rotaguard.domain.enums.ShiftType;
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
@Table(name = "shift")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Shift {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "staff_id", nullable = false)
  private Long staffId;

  @Column(name = "start_at", nullable = false)
  private ZonedDateTime startAt;

  @Column(name = "end_at", nullable = false)
  private ZonedDateTime endAt;

  @Column(name = "shift_type", nullable = false)
  private String shiftType;

  @Column(name = "revision_count", nullable = false)
  @Builder.Default
  private int revisionCount = 0;

  @Column(name = "updated_at", nullable = false)
  @Builder.Default
  private ZonedDateTime updatedAt = AppTimeZones.now();

  public ShiftType typeEnum() {
    return ShiftType.valueOf(shiftType);
  }
}
