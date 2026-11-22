package com.epam.reportportal.base.infrastructure.commons.template;

import static com.google.common.base.Strings.isNullOrEmpty;

import com.google.common.base.Preconditions;
import freemarker.core.HTMLOutputFormat;
import freemarker.template.TemplateExceptionHandler;
import freemarker.template.Version;
import jakarta.inject.Provider;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

/**
 * Factory bean for {@link TemplateEngine}
 *
 * @author Andrei Varabyeu
 */
public class TemplateEngineProvider implements Provider<TemplateEngine> {

  private final String basePackagePath;
  private final boolean htmlAutoEscape;

  public TemplateEngineProvider() {
    this("/");
  }

  /**
   * @param basePackagePath Base path for templates in classpath
   */
  public TemplateEngineProvider(String basePackagePath) {
    this(basePackagePath, false);
  }

  /**
   * @param basePackagePath Base path for templates in classpath
   * @param htmlAutoEscape  when true every {@code ${...}} interpolation is HTML-escaped (HTML output format). Use it
   *                        for templates that render HTML from user-controlled data. Values that are deliberately
   *                        markup must be wrapped with {@code ?no_esc} in the template.
   */
  public TemplateEngineProvider(String basePackagePath, boolean htmlAutoEscape) {
    this.htmlAutoEscape = htmlAutoEscape;
    Preconditions.checkArgument(!isNullOrEmpty(basePackagePath),
        "Base path for templates is missed");
    this.basePackagePath = basePackagePath;
  }

  @Override
  public TemplateEngine get() {
    Version version = new Version(2, 3, 34);
    freemarker.template.Configuration cfg = new freemarker.template.Configuration(version);

    cfg.setClassForTemplateLoading(this.getClass(), basePackagePath);

    cfg.setIncompatibleImprovements(version);
    cfg.setDefaultEncoding(StandardCharsets.UTF_8.toString());
    cfg.setLocale(Locale.US);
    cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
    if (htmlAutoEscape) {
      cfg.setOutputFormat(HTMLOutputFormat.INSTANCE);
    }

    return new FreemarkerTemplateEngine(cfg);

  }
}
