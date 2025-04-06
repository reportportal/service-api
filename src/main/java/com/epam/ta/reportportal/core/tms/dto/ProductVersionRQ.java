package com.epam.ta.reportportal.core.tms.dto;

/**
 * Input DTO for milestone
 *
 * @param id
 * @param version
 * @param documentation
 * @param projectId
 * @author Andrei Varabyeu andrei_varabyeu@epam.com
 */
public record ProductVersionRQ(Long id, String version, String documentation, Long projectId) {

}
