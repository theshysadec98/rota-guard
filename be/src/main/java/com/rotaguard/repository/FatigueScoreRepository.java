/* Nhom I */
package com.rotaguard.repository;

import com.rotaguard.domain.entity.FatigueScore;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FatigueScoreRepository extends JpaRepository<FatigueScore, Long> {

  List<FatigueScore> findByRunId(Long runId);

  Optional<FatigueScore> findByRunIdAndStaffId(Long runId, Long staffId);
}
