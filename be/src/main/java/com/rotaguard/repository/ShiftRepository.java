/* Nhom I */
package com.rotaguard.repository;

import com.rotaguard.domain.entity.Shift;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShiftRepository extends JpaRepository<Shift, Long>, ShiftQueryDslRepository {

  List<Shift> findByStaffIdOrderByStartAtAsc(Long staffId);
}
