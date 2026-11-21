package com.epam.reportportal.base.util.email;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.epam.reportportal.base.infrastructure.commons.template.TemplateEngine;
import com.epam.reportportal.base.infrastructure.commons.template.TemplateEngineProvider;
import com.epam.reportportal.base.infrastructure.persistence.entity.enums.LaunchModeEnum;
import com.epam.reportportal.base.infrastructure.persistence.entity.enums.StatusEnum;
import com.epam.reportportal.base.infrastructure.persistence.entity.item.issue.IssueType;
import com.epam.reportportal.base.infrastructure.persistence.entity.launch.Launch;
import com.epam.reportportal.base.infrastructure.persistence.entity.launch.LaunchAttribute;
import com.epam.reportportal.base.infrastructure.persistence.entity.launch.LaunchStatistics;
import com.epam.reportportal.base.infrastructure.persistence.entity.project.ProjectIssueType;
import com.epam.reportportal.base.infrastructure.persistence.entity.statistics.StatisticsField;
import com.google.common.collect.Sets;
import java.time.Instant;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * Renders the real email templates with the engine configured as in {@code EmailConfiguration}
 * (HTML auto-escaping on).
 */
class EmailTemplateEscapingTest {

  private final TemplateEngine engine = new TemplateEngineProvider("/templates/email", true).get();
  private final EmailService emailService = new EmailService(new Properties());

  EmailTemplateEscapingTest() {
    emailService.setTemplateEngine(engine);
  }

  @Test
  void launchMarkupIsEscaped() {
    Launch launch = launch("<script>alert(1)</script> \"quoted\"",
        "<a href=\"http://evil\">click</a><form action=\"http://evil\"><input name=pw></form>",
        new LaunchAttribute("k<b>", "v\"><img src=x>", false));

    String html = emailService.mergeFinishLaunchText("http://rp.example.com", launch, issueTypes());

    assertFalse(html.contains("<script>alert(1)"), "raw script tag must not survive");
    assertTrue(html.contains("&lt;script&gt;alert(1)&lt;/script&gt; &quot;quoted&quot;"));
    assertFalse(html.contains("<form action"));
    assertFalse(html.contains("<a href=\"http://evil\">"));
    assertFalse(html.contains("<img src=x>"));
    assertTrue(html.contains("&lt;form action=&quot;http://evil&quot;&gt;"));
  }

  @Test
  void normalLaunchMailRendersUnchanged() {
    Launch launch = launch("Regression build", "Nightly run",
        new LaunchAttribute("browser", "chrome", false));

    String html = emailService.mergeFinishLaunchText("http://rp.example.com", launch, issueTypes());

    assertTrue(html.contains("Launch \"Regression build\" #7"));
    assertTrue(html.contains("Nightly run"));
    assertTrue(html.contains("browser:chrome"));
    assertTrue(html.contains("href=\"http://rp.example.com/launches/all/1\""));
    assertTrue(html.contains(
        "href=\"http://rp.example.com/launches/all?launchesParams="
            + "filter.has.compositeAttribute=browser:chrome\""));
    assertTrue(html.contains("Product Bugs"));
  }

  @Test
  void urlWithAmpersandKeepsWorkingHref() {
    String html = engine.merge("restore-password-template.ftl",
        Map.of("login", "u", "url", "http://rp.example.com/ui/#login?a=1&b=2"));
    assertTrue(html.contains("href=\"http://rp.example.com/ui/#login?a=1&amp;b=2\""));
  }

  @Test
  void plainValuesInOtherTemplatesAreEscapedToo() {
    String html = engine.merge("create-user-template.ftl",
        Map.of("login", "a<i>", "password", "p&\"w", "url", "http://rp"));
    assertTrue(html.contains("a&lt;i&gt;"));
    assertTrue(html.contains("p&amp;&quot;w"));
  }

  @Test
  void defaultProviderStaysUnescapedForPluginHtmlTemplates() {
    // bug_template.ftl style templates receive pre-built HTML and are rendered by the default engine
    String res = new TemplateEngineProvider().get().merge("template.ftl",
        Map.of("var", "<b>x</b>"));
    assertEquals("<b>x</b>", res);
  }

  private Set<ProjectIssueType> issueTypes() {
    ProjectIssueType projectIssueType = new ProjectIssueType();
    IssueType issueType = new IssueType();
    issueType.setLocator("pb001");
    issueType.setLongName("ProductBug");
    projectIssueType.setIssueType(issueType);
    return Sets.newHashSet(projectIssueType);
  }

  private Launch launch(String name, String description, LaunchAttribute attribute) {
    Launch launch = new Launch();
    launch.setId(1L);
    launch.setStatus(StatusEnum.PASSED);
    launch.setProjectId(1L);
    launch.setStartTime(Instant.now());
    launch.setName(name);
    launch.setMode(LaunchModeEnum.DEFAULT);
    launch.setNumber(7L);
    launch.setDescription(description);
    launch.setAttributes(Sets.newHashSet(attribute));
    launch.setStatistics(Sets.newHashSet(
        new LaunchStatistics(new StatisticsField("statistics$executions$total"), 1)));
    return launch;
  }
}
