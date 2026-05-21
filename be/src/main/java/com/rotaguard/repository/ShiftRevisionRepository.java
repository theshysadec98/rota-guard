/* Nhom I */
package com.rotaguard.repository;

import com.rotaguard.domain.entity.ShiftRevision;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShiftRevisionRepository extends JpaRepository<ShiftRevision, Long> {

  List<ShiftRevision> findByShiftIdOrderByChangedAtDesc(Long shiftId);

  void deleteByShiftIdIn(Collection<Long> shiftIds);
}
