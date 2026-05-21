/* Nhom I */
package com.rotaguard.web.response;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class StaffResponse {
  Long id;
  String name;
  String role;
  String department;
  double sensitivityFactor;
}
