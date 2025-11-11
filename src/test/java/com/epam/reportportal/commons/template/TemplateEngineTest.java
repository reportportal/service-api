/*
 * Copyright 2017 EPAM Systems
 *
 *
 * This file is part of EPAM Report Portal.
 * https://github.com/reportportal/commons
 *
 * Report Portal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Report Portal is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Report Portal.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.epam.reportportal.commons.template;

import static org.hamcrest.MatcherAssert.assertThat;

import com.epam.reportportal.infrastructure.commons.template.FreemarkerTemplateEngine;
import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;
import freemarker.template.Version;
import java.util.Collections;
import java.util.Locale;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:andrei_varabyeu@epam.com">Andrei Varabyeu</a>
 */
public class TemplateEngineTest {

  @Test
  public void merge() throws Exception {
    Version version = new Version(2, 3, 20);
    Configuration cfg = new Configuration(version);

    // Where do we load the templates from:
    cfg.setClassForTemplateLoading(TemplateEngineTest.class, "/");

    // Some other recommended settings:

    cfg.setIncompatibleImprovements(version);
    cfg.setDefaultEncoding("UTF-8");
    cfg.setLocale(Locale.US);
    cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);

    String result = new FreemarkerTemplateEngine(cfg).merge("template.ftl",
        Collections.singletonMap("var", "hello world"));

    assertThat(result, CoreMatchers.is("hello world"));
  }

}
