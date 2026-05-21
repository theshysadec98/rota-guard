/* Nhom I */
package com.rotaguard.repository;

import com.rotaguard.domain.entity.Shift;
import java.time.ZonedDateTime;
import java.util.List;
import lombok.NonNull;

public interface ShiftQueryDslRepository {

  @NonNull
  List<Shift> findByWeek(@NonNull ZonedDateTime weekStart, @NonNull ZonedDateTime weekEnd);
}
