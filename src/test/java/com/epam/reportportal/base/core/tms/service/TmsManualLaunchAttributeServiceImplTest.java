package com.epam.reportportal.base.core.tms.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.reportportal.base.infrastructure.persistence.dao.LaunchAttributeRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.launch.Launch;
import com.epam.reportportal.base.infrastructure.persistence.entity.launch.LaunchAttribute;
import com.epam.reportportal.base.reporting.ItemAttributesRQ;
import java.util.List;
import java.util.stream.StreamSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TmsManualLaunchAttributeServiceImplTest {

  @Mock
  private LaunchAttributeRepository launchAttributeRepository;

  private TmsManualLaunchAttributeServiceImpl service;
  private Launch launch;

  @BeforeEach
  void setUp() {
    service = new TmsManualLaunchAttributeServiceImpl(launchAttributeRepository);
    launch = new Launch();
    launch.setId(10L);
    launch.getAttributes().add(new LaunchAttribute("retentionPolicy", "important", true));
    launch.getAttributes().add(new LaunchAttribute("isAgentic", "false", true));
    launch.getAttributes().add(new LaunchAttribute("team", "old", false));
    when(launchAttributeRepository.saveAll(any())).thenAnswer(invocation ->
        StreamSupport.stream(((Iterable<?>) invocation.getArgument(0)).spliterator(), false)
            .toList());
  }

  @Test
  void updateAttributes_keepsSystemRetentionWhenRequestOmitsIt() {
    service.updateAttributes(launch, List.of(new ItemAttributesRQ("environment", "staging")));

    assertTrue(launch.getAttributes().stream().anyMatch(attribute ->
        Boolean.TRUE.equals(attribute.isSystem())
            && "retentionPolicy".equals(attribute.getKey())
            && "important".equals(attribute.getValue())));
    assertTrue(launch.getAttributes().stream().anyMatch(attribute ->
        Boolean.TRUE.equals(attribute.isSystem()) && "isAgentic".equals(attribute.getKey())));
    assertTrue(launch.getAttributes().stream().anyMatch(attribute ->
        "environment".equals(attribute.getKey()) && !Boolean.TRUE.equals(attribute.isSystem())));
    assertTrue(
        launch.getAttributes().stream().noneMatch(attribute -> "old".equals(attribute.getValue())));

    @SuppressWarnings("unchecked")
    ArgumentCaptor<Iterable<LaunchAttribute>> deleted = ArgumentCaptor.forClass(Iterable.class);
    verify(launchAttributeRepository).deleteAll(deleted.capture());
    assertEquals("team", deleted.getValue().iterator().next().getKey());
  }

  @Test
  void updateAttributes_replacesSystemRetentionWhenRequestSendsOne() {
    service.updateAttributes(launch, List.of(
        new ItemAttributesRQ("retentionPolicy", "REGULAR", true),
        new ItemAttributesRQ("environment", "staging")
    ));

    assertTrue(launch.getAttributes().stream().anyMatch(attribute ->
        Boolean.TRUE.equals(attribute.isSystem())
            && "retentionPolicy".equalsIgnoreCase(attribute.getKey())
            && "REGULAR".equals(attribute.getValue())));
    assertTrue(launch.getAttributes().stream().noneMatch(attribute ->
        "important".equals(attribute.getValue())));
    assertTrue(launch.getAttributes().stream().anyMatch(attribute ->
        Boolean.TRUE.equals(attribute.isSystem()) && "isAgentic".equals(attribute.getKey())));
  }
}
