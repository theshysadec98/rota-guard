/* Nhom I */
package com.rotaguard.web.response;

import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class StaffPageResponse {
  List<StaffResponse> items;
  int page;
  int size;
  long totalElements;
  int totalPages;
}
