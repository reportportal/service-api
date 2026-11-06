package com.epam.reportportal.base.core.aifactory;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.Test;

/**
 * The pipeline core describes any process (an AI agent run, a CI/CD build, a manual routine) and must not
 * know about test cases. Whatever is specific to test cases, for example linking a stage to the test cases
 * it produced, lives on the test case side (package {@code core.tms.pipeline}) and plugs in through the
 * extension points of the core, such as {@code PipelineRelatedEntityResolver} and {@code PipelineStageContributor}.
 */
class PipelineCoreLayeringTest {

  @Test
  void pipelineCore_ShouldNotDependOnTestCaseCode() {
    var productionClasses = new ClassFileImporter()
        .withImportOption(new ImportOption.DoNotIncludeTests())
        .importPackages("com.epam.reportportal.base");

    noClasses()
        .that().resideInAnyPackage(
            "..core.aifactory..",
            "..infrastructure.persistence.entity.aifactory..",
            "..infrastructure.persistence.dao.aifactory..")
        .should().dependOnClassesThat().resideInAnyPackage(
            "..core.tms..",
            "..infrastructure.persistence.entity.tms..",
            "..infrastructure.persistence.dao.tms..")
        .because("the pipeline core is generic; test case behaviour is an implementation plugged into it")
        .check(productionClasses);
  }
}
