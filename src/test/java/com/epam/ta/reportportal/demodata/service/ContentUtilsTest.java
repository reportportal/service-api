package com.epam.ta.reportportal.demodata.service;

import com.epam.ta.reportportal.entity.enums.TestItemTypeEnum;
import java.util.ArrayList;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class ContentUtilsTest {

  @Test
  void getNameFromType() {
    String name = ContentUtils.getNameFromType(TestItemTypeEnum.BEFORE_CLASS);
    Assertions.assertThat(name).isEqualTo("beforeClass");
  }

  @Test
  void getSuiteDescription() {
    String suiteDescription = ContentUtils.getSuiteDescription();
    List<String> result = new ArrayList<>();
    result.add(
        "**This is demonstration description.** This `root-item` contains automatically generated test cases with logs and attachments.");
    result.add(
        "This is a `suite` level. Here you can handle *the aggregated information* per  `suite`.");
    result.add("Here could be **very important information** about `test-cases` that are inside.");
    Assertions.assertThat(result).contains(suiteDescription);
  }

  @Test
  void getStepDescription() {
    String stepDescription = ContentUtils.getStepDescription();
    List<String> result = new ArrayList<>();
    result.add(
        "This is the last **test case** of demo launch. There are only `logs` with `attachments` inside it.");
    result.add(
        "Clear all created and not deleted during test *userFilter*, *widget* and *dashboard* objects.");
    result.add(
        "Greater or equals filter test for test items product bugs criteria. Negative value");
    Assertions.assertThat(result).contains(stepDescription);
  }

  @Test
  void getTestDescription() {
    String testDescription = ContentUtils.getTestDescription();
    List<String> result = new ArrayList<>();
    result.add(
        "**This is demonstration description.** This `test-item` contains automatically generated steps with logs and attachments.");
    result.add(
        "This is a `test` level. Here you can handle *the aggregated information* per  `test`.");
    result.add("Here could be **very important information** about `test-cases` that are inside.");
    Assertions.assertThat(result).contains(testDescription);
  }

  @Test
  void getLaunchDescription() {
    String launchDescription = ContentUtils.getLaunchDescription();
    List<String> result = new ArrayList<>();
    result.add("### **Demonstration launch.**\n"
        + "A typical *Launch structure* comprises the following elements: Suite > Test > Step > Log.\n"
        + "Launch contains *randomly* generated `suites`, `tests`, `steps` with:\n"
        + "* random issues and statuses,\n"
        + "* logs,\n"
        + "* attachments with different formats.");
    Assertions.assertThat(result).contains(launchDescription);
  }

  @Test
  void getWithProbability() {
    boolean withProbability = ContentUtils.getWithProbability(100);
    Assertions.assertThat(withProbability).isEqualTo(true);
  }
}