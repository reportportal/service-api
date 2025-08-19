package com.epam.ta.reportportal.core.tms.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeleteTagsRQ {

  @NotEmpty(message = "Tag IDs cannot be empty")
  private List<Long> tagIds;
}
