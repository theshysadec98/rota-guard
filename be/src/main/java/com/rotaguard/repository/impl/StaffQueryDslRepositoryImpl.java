/* Nhom I */
package com.rotaguard.repository.impl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.JPQLQuery;
import com.rotaguard.domain.entity.QStaff;
import com.rotaguard.domain.entity.Staff;
import com.rotaguard.domain.enums.StaffListSortDirection;
import com.rotaguard.domain.enums.StaffListSortField;
import com.rotaguard.repository.StaffQueryDslRepository;
import com.rotaguard.support.QuerydslExpressions;
import com.rotaguard.support.TextSearchUtils;
import java.util.List;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

@Repository
public class StaffQueryDslRepositoryImpl extends QuerydslRepositorySupport
    implements StaffQueryDslRepository {

  private static final QStaff staff = QStaff.staff;

  public StaffQueryDslRepositoryImpl() {
    super(Staff.class);
  }

  @Override
  public @NonNull Page<Staff> search(
      @Nullable final String keyword,
      @Nullable final String department,
      @Nullable final String role,
      @NonNull final StaffListSortField sortBy,
      @NonNull final StaffListSortDirection sortDirection,
      @NonNull final Pageable pageable) {

    final JPQLQuery<Staff> query = from(staff);
    final BooleanBuilder condition = new BooleanBuilder();

    applyKeywordCondition(condition, keyword);

    final String departmentPattern = TextSearchUtils.toLikePattern(department);
    if (departmentPattern != null) {
      condition.and(QuerydslExpressions.likeIgnoreCase(staff.department, departmentPattern));
    }

    if (StringUtils.hasText(role)) {
      condition.and(staff.role.equalsIgnoreCase(role.trim()));
    }

    query.where(condition);
    applyStaffSort(query, sortBy, sortDirection);

    final long total = query.fetchCount();
    query.offset(pageable.getOffset());
    query.limit(pageable.getPageSize());

    final List<Staff> content = query.fetch();
    return new PageImpl<>(content, pageable, total);
  }

  private static void applyKeywordCondition(
      final BooleanBuilder condition, @Nullable final String keyword) {
    final String likePattern = TextSearchUtils.toLikePattern(keyword);
    if (likePattern == null) {
      return;
    }
    condition.and(
        QuerydslExpressions.likeIgnoreCase(staff.name, likePattern)
            .or(QuerydslExpressions.likeIgnoreCase(staff.department, likePattern))
            .or(QuerydslExpressions.likeIgnoreCase(staff.role, likePattern)));
  }

  private static void applyStaffSort(
      @NonNull final JPQLQuery<Staff> query,
      @NonNull final StaffListSortField sortBy,
      @NonNull final StaffListSortDirection sortDirection) {
    final boolean asc = sortDirection == StaffListSortDirection.ASC;
    if (sortBy == StaffListSortField.ID) {
      if (asc) {
        query.orderBy(staff.id.asc());
      } else {
        query.orderBy(staff.id.desc());
      }
    } else if (sortBy == StaffListSortField.ROLE) {
      if (asc) {
        query.orderBy(staff.role.asc().nullsLast(), staff.id.asc());
      } else {
        query.orderBy(staff.role.desc().nullsLast(), staff.id.desc());
      }
    } else if (sortBy == StaffListSortField.DEPARTMENT) {
      if (asc) {
        query.orderBy(staff.department.asc().nullsLast(), staff.id.asc());
      } else {
        query.orderBy(staff.department.desc().nullsLast(), staff.id.desc());
      }
    } else {
      if (asc) {
        query.orderBy(staff.name.asc().nullsLast(), staff.id.asc());
      } else {
        query.orderBy(staff.name.desc().nullsLast(), staff.id.desc());
      }
    }
  }
}
