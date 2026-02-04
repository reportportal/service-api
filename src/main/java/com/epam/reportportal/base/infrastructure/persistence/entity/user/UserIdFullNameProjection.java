package com.epam.reportportal.base.infrastructure.persistence.entity.user;

/**
 * Projection for User entity containing only ID and full name.
 *
 * @param id       User ID
 * @param fullName User full name
 */
public record UserIdFullNameProjection(
    Long id,
    String fullName
) {

}
