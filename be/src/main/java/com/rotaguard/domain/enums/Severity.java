/* Nhom I */
package com.rotaguard.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Severity {
  INFO(1),
  WARN(3),
  CRITICAL(5);

  private final int weight;
}
