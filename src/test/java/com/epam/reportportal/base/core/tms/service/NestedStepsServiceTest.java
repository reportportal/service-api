package com.epam.reportportal.base.core.tms.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import com.epam.reportportal.base.core.tms.dto.TmsStepRS;
import com.epam.reportportal.base.core.tms.dto.TmsStepsManualScenarioRS;
import com.epam.reportportal.base.core.tms.dto.TmsTextManualScenarioRS;
import com.epam.reportportal.base.core.tms.mapper.NestedStepItemBuilder;
import com.epam.reportportal.base.infrastructure.persistence.dao.TestItemRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.item.TestItem;
import com.epam.reportportal.base.infrastructure.persistence.entity.launch.Launch;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NestedStepsServiceTest {

  private final Long launchId = 100L;
  private final Long parentId = 200L;
  @Mock
  private TestItemRepository testItemRepository;
  @Mock
  private NestedStepItemBuilder nestedStepItemBuilder;
  @InjectMocks
  private NestedStepsService sut;
  @Captor
  private ArgumentCaptor<List<TestItem>> listCaptor;
  private Launch launch;
  private TestItem parentItem;

  @BeforeEach
  void setUp() {
    launch = new Launch();
    launch.setId(launchId);

    parentItem = new TestItem();
    parentItem.setItemId(parentId);
    parentItem.setPath("200");
  }

  // -------------------------------------------------------------------------
  // CREATE FROM STEPS SCENARIO
  // -------------------------------------------------------------------------

  @Test
  void createNestedStepsFromStepScenario_WithValidSteps_ShouldCreateAndSave() {
    TmsStepsManualScenarioRS scenario = new TmsStepsManualScenarioRS();
    TmsStepRS stepRS1 = new TmsStepRS();
    stepRS1.setInstructions("Step 1 instruction");
    stepRS1.setExpectedResult("Step 1 expected");
    scenario.setSteps(List.of(stepRS1));

    TestItem nestedItem = new TestItem();
    nestedItem.setItemId(300L); // Will be set after first save mock
    nestedItem.setPath("200");

    when(nestedStepItemBuilder.buildStepName(anyString(), anyInt())).thenReturn(
        "Step 0: Step 1 instruction");
    when(nestedStepItemBuilder.buildNestedStepItem(any(), anyString(), anyString(), any()))
        .thenReturn(nestedItem);

    // Mock first save
    when(testItemRepository.saveAll(anyList())).thenAnswer(invocation -> {
      List<TestItem> items = invocation.getArgument(0);
      items.get(0).setItemId(300L);
      return items;
    });

    sut.createNestedStepsFromStepScenario(scenario, parentItem, launch);

    verify(nestedStepItemBuilder).buildStepName("Step 1 instruction", 0);
    verify(nestedStepItemBuilder).buildNestedStepItem(parentItem, "Step 0: Step 1 instruction",
        "Step 1 expected", launch);

    // The method saves twice, once to get IDs, once to update paths
    verify(testItemRepository, times(2)).saveAll(listCaptor.capture());
    
    // Path should be parentPath + "." + newId = "200.300"
    assertEquals("200.300", listCaptor.getValue().get(0).getPath());
  }

  // -------------------------------------------------------------------------
  // CREATE FROM TEXT SCENARIO
  // -------------------------------------------------------------------------

  @Test
  void createNestedStepFromTextScenario_WithValidText_ShouldCreateAndSave() {
    TmsTextManualScenarioRS scenario = new TmsTextManualScenarioRS();
    scenario.setInstructions("Instruction text");
    scenario.setExpectedResult("Expected text");

    TestItem nestedItem = new TestItem();
    nestedItem.setItemId(400L); // Mocked ID after save

    when(nestedStepItemBuilder.buildTextScenarioDescription("Instruction text", "Expected text"))
        .thenReturn("Instruction text\nExpected text");
    when(nestedStepItemBuilder.buildNestedStepItem(any(), anyString(), anyString(), any()))
        .thenReturn(nestedItem);

    // Mock save
    when(testItemRepository.save(any(TestItem.class))).thenAnswer(invocation -> {
      TestItem item = invocation.getArgument(0);
      item.setItemId(400L);
      return item;
    });

    sut.createNestedStepFromTextScenario(scenario, parentItem, launch);

    verify(nestedStepItemBuilder).buildTextScenarioDescription("Instruction text", "Expected text");
    verify(nestedStepItemBuilder).buildNestedStepItem(parentItem, "Instruction text",
        "Instruction text\nExpected text", launch);

    // Path should be updated and saved again
    assertEquals("200.400", nestedItem.getPath());
  }
}
