package com.epam.reportportal.base.infrastructure.persistence.dao;

import static com.epam.reportportal.base.infrastructure.persistence.dao.util.RecordMappers.ASSIGNMENT_DETAILS_MAPPER;
import static com.epam.reportportal.base.infrastructure.persistence.jooq.Tables.ORGANIZATION;
import static com.epam.reportportal.base.infrastructure.persistence.jooq.Tables.ORGANIZATION_USER;
import static com.epam.reportportal.base.infrastructure.persistence.jooq.Tables.PROJECT;
import static com.epam.reportportal.base.infrastructure.persistence.jooq.Tables.PROJECT_USER;

import com.epam.reportportal.base.infrastructure.persistence.entity.organization.MembershipDetails;
import com.epam.reportportal.base.infrastructure.persistence.jooq.enums.JOrganizationRoleEnum;
import com.epam.reportportal.base.infrastructure.persistence.util.SortUtils;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.jooq.DSLContext;
import org.jooq.SortField;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

@Repository
public class ProjectUserRepositoryCustomImpl implements ProjectUserRepositoryCustom {

  private final DSLContext dsl;

  @Autowired
  public ProjectUserRepositoryCustomImpl(DSLContext dsl) {
    this.dsl = dsl;
  }

  @Override
  public Optional<MembershipDetails> findDetailsByUserIdAndProjectKey(Long userId, String projectKey) {

    return dsl.select(
            PROJECT.ID,
            PROJECT_USER.PROJECT_ROLE,
            PROJECT.NAME,
            PROJECT.KEY,
            PROJECT.SLUG,
            PROJECT.ORGANIZATION_ID,
            ORGANIZATION_USER.ORGANIZATION_ROLE,
            ORGANIZATION.NAME)
        .from(PROJECT)
        .join(ORGANIZATION).on(PROJECT.ORGANIZATION_ID.eq(ORGANIZATION.ID))
        .fullJoin(PROJECT_USER)
        .on(PROJECT_USER.PROJECT_ID.eq(PROJECT.ID)
            .and(PROJECT_USER.USER_ID.eq(userId)))
        .join(ORGANIZATION_USER)
        .on(ORGANIZATION_USER.ORGANIZATION_ID.eq(PROJECT.ORGANIZATION_ID)
            .and(ORGANIZATION_USER.USER_ID.eq(userId)))
        .where(PROJECT.KEY.eq(projectKey))
        .fetchOptional(ASSIGNMENT_DETAILS_MAPPER);
  }

  @Override
  public Optional<MembershipDetails> findAdminDetailsProjectKey(String projectKey) {
    return dsl.select(
            PROJECT.ID,
            DSL.val(JOrganizationRoleEnum.MANAGER).as(ORGANIZATION_USER.ORGANIZATION_ROLE),
            PROJECT.NAME,
            PROJECT.KEY,
            PROJECT.SLUG,
            PROJECT.ORGANIZATION_ID,
            ORGANIZATION.NAME)
        .from(PROJECT)
        .join(ORGANIZATION).on(PROJECT.ORGANIZATION_ID.eq(ORGANIZATION.ID))
        .where(PROJECT.KEY.eq(projectKey))
        .fetchOptional(ASSIGNMENT_DETAILS_MAPPER);
  }

  @Override
  public Page<MembershipDetails> findUserProjectsInOrganization(Long userId, Long organizationId, Pageable pageable) {
    pageable.getSort();
    var query = dsl.select(
            PROJECT.ID,
            ORGANIZATION_USER.ORGANIZATION_ROLE,
            PROJECT.NAME,
            PROJECT.KEY,
            PROJECT.SLUG,
            PROJECT.ORGANIZATION_ID,
            PROJECT_USER.PROJECT_ROLE
        )
        .from(PROJECT)
        .join(ORGANIZATION_USER)
        .on(PROJECT.ORGANIZATION_ID.eq(ORGANIZATION_USER.ORGANIZATION_ID))
        .join(PROJECT_USER)
        .on(PROJECT.ID.eq(PROJECT_USER.PROJECT_ID))
        .where(ORGANIZATION_USER.USER_ID.eq(userId))
        .and(ORGANIZATION_USER.ORGANIZATION_ID.eq(organizationId))
        .and(PROJECT_USER.USER_ID.eq(userId));

    List<SortField<?>> sortFields = SortUtils.RESOLVE_SORT_FIELDS.apply(
        pageable.getSort(),
        query.getQuery()
    );

    var result = query
        .orderBy(sortFields)
        .limit(pageable.getPageSize())
        .offset(pageable.getOffset())
        .fetch(ASSIGNMENT_DETAILS_MAPPER);

    return PageableExecutionUtils.getPage(result, pageable,
        () -> dsl.fetchCount(query));
  }

  @Override
  public Set<Long> findUserProjectIdsInOrganization(Long userId, Long organizationId) {
    return dsl.select(PROJECT.ID)
        .from(PROJECT)
        .join(ORGANIZATION_USER)
        .on(PROJECT.ORGANIZATION_ID.eq(ORGANIZATION_USER.ORGANIZATION_ID))
        .join(PROJECT_USER)
        .on(PROJECT.ID.eq(PROJECT_USER.PROJECT_ID))
        .where(ORGANIZATION_USER.USER_ID.eq(userId))
        .and(ORGANIZATION_USER.ORGANIZATION_ID.eq(organizationId))
        .and(PROJECT_USER.USER_ID.eq(userId)).fetchSet(PROJECT.ID);
  }
}
