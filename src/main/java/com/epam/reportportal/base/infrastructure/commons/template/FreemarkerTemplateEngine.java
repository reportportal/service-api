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

package com.epam.reportportal.base.infrastructure.commons.template;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

/**
 * Implementation of TemplateEngine based on Freemaker
 *
 * @author <a href="mailto:andrei_varabyeu@epam.com">Andrei Varabyeu</a>
 */
public class FreemarkerTemplateEngine implements TemplateEngine {

  private final Configuration cfg;

  public FreemarkerTemplateEngine(Configuration cfg) {
    this.cfg = cfg;
  }

  @Override
  public String merge(String template, Map<?, ?> data) {
    try (StringWriter writer = new StringWriter()) {
      Template tmpl = cfg.getTemplate(template);
      tmpl.process(data, writer);
      return writer.toString();
    } catch (TemplateException | IOException e) {
      throw new RuntimeException("Unable to process template '" + template + "'", e);
    }
  }
}
