/* Nhom I */
package com.rotaguard.repository;

import com.rotaguard.domain.entity.Staff;
import com.rotaguard.domain.enums.StaffListSortDirection;
import com.rotaguard.domain.enums.StaffListSortField;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.Nullable;

public interface StaffQueryDslRepository {

  @NonNull
  Page<Staff> search(
      @Nullable String keyword,
      @Nullable String department,
      @Nullable String role,
      @NonNull StaffListSortField sortBy,
      @NonNull StaffListSortDirection sortDirection,
      @NonNull Pageable pageable);
}
