package com.epam.ta.reportportal.core.tms.dto;

import com.epam.ta.reportportal.core.tms.db.entity.TmsMilestone;
import com.epam.ta.reportportal.core.tms.db.entity.TmsTestPlan;

import java.util.Set;

// TODO add DTOs for TestPlan and Milestones

/**
 * Output DTO for milestone
 *
 * @param id
 * @param version
 * @param documentation
 * @param testPlans
 * @param milestones
 *
 * @author Andrei Varabyeu andrei_varabyeu@epam.com
 */
public record ProductVersionRS(Long id, String version, String documentation, Set<TmsTestPlan> testPlans,
                               Set<TmsMilestone> milestones) {

}
