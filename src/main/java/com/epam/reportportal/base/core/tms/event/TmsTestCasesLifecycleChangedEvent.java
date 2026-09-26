package com.epam.reportportal.base.core.tms.event;

import java.util.Collection;
import java.util.Set;

/**
 * Published when the lifecycle status of test cases may have changed or the cases are about to be
 * deleted. For deletions it has to be published before the cases are removed, while whatever links
 * them to other entities (e.g. pipeline stages) still exists.
 */
public record TmsTestCasesLifecycleChangedEvent(Set<Long> testCaseIds) {

  public TmsTestCasesLifecycleChangedEvent(Collection<Long> testCaseIds) {
    this(Set.copyOf(testCaseIds));
  }
}
