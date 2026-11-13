package com.epam.reportportal.base.core.tms.dto;

/**
 * Request for creating or updating a product version.
 *
 * @param id            product version identifier
 * @param version       version label
 * @param documentation optional documentation
 * @param projectId     project identifier
 * @author Andrei Varabyeu andrei_varabyeu@epam.com
 */
public record ProductVersionRQ(Long id, String version, String documentation, Long projectId) {

}
