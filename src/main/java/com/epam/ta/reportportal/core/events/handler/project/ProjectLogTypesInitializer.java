package com.epam.ta.reportportal.core.events.handler.project;

import com.epam.ta.reportportal.core.events.activity.ProjectCreatedEvent;
import com.epam.ta.reportportal.core.logtype.DefaultLogTypeProvider;
import com.epam.ta.reportportal.dao.LogTypeRepository;
import com.epam.ta.reportportal.entity.log.ProjectLogType;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Event listener that creates default log types when a new project is created.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProjectLogTypesInitializer {

  private final DefaultLogTypeProvider defaultLogTypeProvider;
  private final LogTypeRepository logTypeRepository;

  /**
   * Handles project creation events by initializing default log types for the created project.
   *
   * @param event the event containing details about the created project.
   */
  @TransactionalEventListener
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void onProjectCreated(ProjectCreatedEvent event) {
    if (event == null || event.getProjectId() == null) {
      log.warn("ProjectCreatedEvent is missing projectId. Skipping log type initialization.");
      return;
    }

    Long projectId = event.getProjectId();
    List<ProjectLogType> defaultLogTypes = defaultLogTypeProvider.provideDefaultLogTypes(projectId);
    logTypeRepository.saveAll(defaultLogTypes);

    log.info("Default log types have been initialized for project with ID: {}", projectId);
  }
}
