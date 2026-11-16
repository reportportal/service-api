/*
 * Copyright 2019 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.reportportal.base.infrastructure.persistence.dao;

import static com.epam.reportportal.base.infrastructure.persistence.jooq.Tables.LOG;
import static com.epam.reportportal.base.infrastructure.persistence.jooq.Tables.TEST_ITEM;
import static com.epam.reportportal.base.infrastructure.persistence.jooq.tables.JAttachment.ATTACHMENT;

import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.QueryBuilder;
import com.epam.reportportal.base.infrastructure.persistence.entity.attachment.Attachment;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import org.jooq.DSLContext;
import org.jooq.Operator;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

/**
 * Implements {@link AttachmentRepositoryCustom}.
 *
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Repository
public class AttachmentRepositoryCustomImpl implements AttachmentRepositoryCustom {

  @Autowired
  private DSLContext dsl;

  @Override
  public int moveForDeletionByProjectId(Long projectId) {
    String condition = DSL.condition(Operator.AND, ATTACHMENT.PROJECT_ID.eq(projectId)).toString();
    return executeMoveQuery(condition);
  }

  @Override
  public int moveForDeletionByLaunchId(Long launchId) {
    String condition = DSL.condition(Operator.AND, ATTACHMENT.LAUNCH_ID.eq(launchId)).toString();
    return executeMoveQuery(condition);
  }

  @Override
  public int moveForDeletionByLaunchIds(Collection<Long> launchIds) {
    String condition = DSL.condition(Operator.AND, ATTACHMENT.LAUNCH_ID.in(launchIds)).toString();
    return executeMoveQuery(condition);
  }

  @Override
  public int moveForDeletionByItems(Collection<Long> itemIds) {
    String condition = DSL.condition(Operator.AND, ATTACHMENT.ITEM_ID.in(itemIds)).toString();
    return executeMoveQuery(condition);
  }

  @Override
  public int moveForDeletion(Long attachmentId) {
    String condition = DSL.condition(Operator.AND, ATTACHMENT.ID.eq(attachmentId)).toString();
    return executeMoveQuery(condition);
  }

  @Override
  public int moveForDeletion(Collection<Long> attachmentIds) {
    String condition = DSL.condition(Operator.AND, ATTACHMENT.ID.in(attachmentIds)).toString();
    return executeMoveQuery(condition);
  }

  private int executeMoveQuery(String condition) {
    return dsl.query(String.format(
        "WITH moved_rows AS (DELETE FROM attachment WHERE %s RETURNING id, file_id, thumbnail_id, creation_date) "
            + "INSERT INTO attachment_deletion (id, file_id, thumbnail_id, creation_attachment_date, deletion_date) "
            + "SELECT id, file_id, thumbnail_id, creation_date, NOW() FROM moved_rows;",
        condition
    )).execute();
  }

  @Override
  public Page<Long> findIdsByProjectId(Long projectId, Pageable pageable) {
    return PageableExecutionUtils.getPage(dsl.select(ATTACHMENT.ID)
            .from(ATTACHMENT)
            .where(ATTACHMENT.PROJECT_ID.eq(projectId))
            .orderBy(ATTACHMENT.ID)
            .limit(pageable.getPageSize())
            .offset(QueryBuilder.retrieveOffsetAndApplyBoundaries(pageable))
            .fetchInto(Long.class),
        pageable,
        () -> dsl.fetchCount(
            dsl.select(ATTACHMENT.ID).from(ATTACHMENT).where(ATTACHMENT.PROJECT_ID.eq(projectId)))
    );
  }

  @Override
  public Page<Long> findIdsByLaunchId(Long launchId, Pageable pageable) {
    return PageableExecutionUtils.getPage(dsl.select(ATTACHMENT.ID)
            .from(ATTACHMENT)
            .where(ATTACHMENT.LAUNCH_ID.eq(launchId))
            .orderBy(ATTACHMENT.ID)
            .limit(pageable.getPageSize())
            .offset(QueryBuilder.retrieveOffsetAndApplyBoundaries(pageable))
            .fetchInto(Long.class),
        pageable,
        () -> dsl.fetchCount(
            dsl.select(ATTACHMENT.ID).from(ATTACHMENT).where(ATTACHMENT.LAUNCH_ID.eq(launchId)))
    );
  }

  @Override
  public Page<Long> findIdsByTestItemId(Collection<Long> itemIds, Pageable pageable) {

    return PageableExecutionUtils.getPage(dsl.select(ATTACHMENT.ID)
            .from(ATTACHMENT)
            .where(ATTACHMENT.ITEM_ID.in(itemIds))
            .orderBy(ATTACHMENT.ID)
            .limit(pageable.getPageSize())
            .offset(QueryBuilder.retrieveOffsetAndApplyBoundaries(pageable))
            .fetchInto(Long.class),
        pageable,
        () -> dsl.fetchCount(
            dsl.select(ATTACHMENT.ID).from(ATTACHMENT).where(ATTACHMENT.ITEM_ID.in(itemIds)))
    );
  }

  @Override
  public int deleteAllByIds(Collection<Long> ids) {
    return dsl.deleteFrom(ATTACHMENT).where(ATTACHMENT.ID.in(ids)).execute();
  }

  @Override
  public List<Attachment> findByItemIdsAndLogTimeBefore(Collection<Long> itemIds,
      Instant before) {
    return dsl.select(ATTACHMENT.ID,
            ATTACHMENT.THUMBNAIL_ID,
            ATTACHMENT.FILE_ID,
            ATTACHMENT.CONTENT_TYPE,
            ATTACHMENT.FILE_SIZE,
            ATTACHMENT.ITEM_ID,
            ATTACHMENT.LAUNCH_ID,
            ATTACHMENT.PROJECT_ID,
            ATTACHMENT.FILE_NAME
        )
        .from(ATTACHMENT)
        .join(LOG)
        .on(LOG.ATTACHMENT_ID.eq(ATTACHMENT.ID))
        .join(TEST_ITEM)
        .on(ATTACHMENT.ITEM_ID.eq(TEST_ITEM.ITEM_ID))
        .where(TEST_ITEM.ITEM_ID.in(itemIds))
        .and(LOG.LOG_TIME.lt(before))
        .and(ATTACHMENT.FILE_ID.isNotNull().or(ATTACHMENT.THUMBNAIL_ID.isNotNull()))
        .fetchInto(Attachment.class);
  }

  @Override
  public List<Attachment> findByLaunchIdsAndLogTimeBefore(Collection<Long> launchIds,
      Instant before) {
    return dsl.select(ATTACHMENT.ID,
            ATTACHMENT.THUMBNAIL_ID,
            ATTACHMENT.FILE_ID,
            ATTACHMENT.CONTENT_TYPE,
            ATTACHMENT.FILE_SIZE,
            ATTACHMENT.ITEM_ID,
            ATTACHMENT.LAUNCH_ID,
            ATTACHMENT.PROJECT_ID,
            ATTACHMENT.FILE_NAME
        )
        .from(ATTACHMENT)
        .join(LOG)
        .on(LOG.ATTACHMENT_ID.eq(ATTACHMENT.ID))
        .where(LOG.LAUNCH_ID.in(launchIds))
        .and(LOG.LOG_TIME.lt(before))
        .and(ATTACHMENT.FILE_ID.isNotNull().or(ATTACHMENT.THUMBNAIL_ID.isNotNull()))
        .fetchInto(Attachment.class);
  }

  @Override
  public List<Attachment> findByProjectIdsAndLogTimeBefore(Long projectId, Instant before,
      int limit, long offset) {
    return dsl.select(ATTACHMENT.ID,
            ATTACHMENT.THUMBNAIL_ID,
            ATTACHMENT.FILE_ID,
            ATTACHMENT.CONTENT_TYPE,
            ATTACHMENT.FILE_SIZE,
            ATTACHMENT.ITEM_ID,
            ATTACHMENT.LAUNCH_ID,
            ATTACHMENT.PROJECT_ID,
            ATTACHMENT.FILE_NAME
        )
        .from(ATTACHMENT)
        .join(LOG)
        .on(LOG.ATTACHMENT_ID.eq(ATTACHMENT.ID))
        .where(ATTACHMENT.PROJECT_ID.eq(projectId))
        .and(LOG.LOG_TIME.lt(before))
        .and(ATTACHMENT.FILE_ID.isNotNull().or(ATTACHMENT.THUMBNAIL_ID.isNotNull()))
        .orderBy(ATTACHMENT.ID)
        .limit(limit)
        .offset(offset)
        .fetchInto(Attachment.class);
  }
}
