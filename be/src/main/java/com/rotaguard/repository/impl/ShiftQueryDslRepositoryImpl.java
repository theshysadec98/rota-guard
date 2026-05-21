/* Nhom I */
package com.rotaguard.repository.impl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.JPQLQuery;
import com.rotaguard.domain.entity.QShift;
import com.rotaguard.domain.entity.Shift;
import com.rotaguard.repository.ShiftQueryDslRepository;
import java.time.ZonedDateTime;
import java.util.List;
import lombok.NonNull;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import org.springframework.stereotype.Repository;

@Repository
public class ShiftQueryDslRepositoryImpl extends QuerydslRepositorySupport
    implements ShiftQueryDslRepository {

  private static final QShift shift = QShift.shift;

  public ShiftQueryDslRepositoryImpl() {
    super(Shift.class);
  }

  @Override
  public @NonNull List<Shift> findByWeek(
      @NonNull final ZonedDateTime weekStart, @NonNull final ZonedDateTime weekEnd) {
    final JPQLQuery<Shift> query = from(shift);
    final BooleanBuilder condition = new BooleanBuilder();

    condition.and(shift.startAt.goe(weekStart));
    condition.and(shift.startAt.lt(weekEnd));

    query.where(condition);
    query.orderBy(shift.staffId.asc(), shift.startAt.asc());

    return query.fetch();
  }
}
