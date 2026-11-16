package com.epam.reportportal.base.infrastructure.persistence.entity.tms.sync;

import com.epam.reportportal.base.infrastructure.persistence.commons.JsonbUserType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Synchronization job progress counters.
 *
 * <ul>
 *   <li>{@code processed} counts every analyzed remote test case.</li>
 *   <li>{@code imported} counts remote cases requiring a local create or update.</li>
 *   <li>{@code skipped} counts unchanged remote cases.</li>
 *   <li>{@code failed} counts cases that could not be synchronized.</li>
 * </ul>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class SyncCounters extends JsonbUserType<SyncCounters> implements Serializable {
  @Schema(description = "Remote test cases analyzed so far (imported + skipped + failed).")
  private int processed;
  @Schema(description = "Remote test cases created or updated locally (new and changed cases).")
  private int imported;
  @Schema(description = "Remote test cases left untouched because they are unchanged.")
  private int skipped;
  @Schema(description = "Remote test cases that could not be synchronized.")
  private int failed;

  @Override
  public Class<SyncCounters> returnedClass() {
    return SyncCounters.class;
  }
}
