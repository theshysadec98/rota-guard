/* Nhom I */
package com.rotaguard.support;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.StringPath;
import java.util.Locale;

public final class QuerydslExpressions {

  private QuerydslExpressions() {}

  public static BooleanExpression likeIgnoreCase(StringPath path, String likePattern) {
    if (likePattern == null) {
      return null;
    }
    return path.lower().like(likePattern.toLowerCase(Locale.ROOT));
  }
}
