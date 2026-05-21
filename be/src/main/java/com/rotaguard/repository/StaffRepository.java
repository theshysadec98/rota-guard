/* Nhom I */
package com.rotaguard.repository;

import com.rotaguard.domain.entity.Staff;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StaffRepository extends JpaRepository<Staff, Long>, StaffQueryDslRepository {

  List<Staff> findByDepartment(String department);

  Optional<Staff> findByNameIgnoreCaseAndDepartment(String name, String department);
}
