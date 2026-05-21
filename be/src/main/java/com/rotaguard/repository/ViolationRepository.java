/* Nhom I */
package com.rotaguard.repository;

import com.rotaguard.domain.entity.Violation;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ViolationRepository extends JpaRepository<Violation, Long> {

  List<Violation> findByScoreId(Long scoreId);

  List<Violation> findByScoreIdIn(List<Long> scoreIds);
}
