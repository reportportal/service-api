/*
 * Copyright 2023 EPAM Systems
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

package com.epam.ta.reportportal.core.configs;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.commons.querygen.Queryable;
import com.epam.ta.reportportal.entity.user.UserRole;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import javax.servlet.ServletContext;
import org.springdoc.core.SpringDocUtils;
import org.springdoc.core.customizers.OpenApiCustomiser;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

/**
 * @author <a href="mailto:andrei_piankouski@epam.com">Andrei Piankouski</a>
 */
@Configuration
@ComponentScan(basePackages = "com.epam.ta.reportportal.ws.controller")
public class SpringDocConfiguration {

  static {
    SpringDocUtils.getConfig().addAnnotationsToIgnore(AuthenticationPrincipal.class);
    SpringDocUtils.getConfig().addRequestWrapperToIgnore(Filter.class, Queryable.class,
        ReportPortalUser.class, UserRole.class);
    SpringDocUtils.getConfig().replaceWithClass(org.springframework.data.domain.Pageable.class,
        org.springdoc.core.converters.models.Pageable.class);
    SpringDocUtils.getConfig().replaceWithClass(Iterable.class, List.class);
  }

  @Autowired
  private ServletContext servletContext;

  @Value("${spring.application.name}")
  private String applicationName;

  @Value("${info.build.version}")
  private String buildVersion;

  @Bean
  public OpenAPI openAPI() {
    final String securitySchemeName = "bearerAuth";
    return new OpenAPI()
        .info(new Info().title("Report Portal")
            .description("Report Portal API documentation")
            .version(buildVersion)
            .contact(new Contact()
                .name("Support")
                .email("support@reportportal.io")
            )
            .license(new License().name("Apache 2.0")
                .url("http://www.apache.org/licenses/LICENSE-2.0")))
        .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
        .components(
            new Components()
                .addSecuritySchemes(securitySchemeName,
                    new SecurityScheme()
                        .name(securitySchemeName)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                )
        )
        .addServersItem(new Server().url("/" + applicationName));
  }

  @Bean
  public OpenApiCustomiser sortSchemasAlphabetically() {
    return openApi -> {
      Map<String, Schema> schemas = openApi.getComponents().getSchemas();
      openApi.getComponents().setSchemas(new TreeMap<>(schemas));
    };
  }

  @Bean
  public OpenApiCustomiser sortTagsAlphabetically() {
    return openApi -> {
      List<Tag> sortedTags = openApi.getTags().stream()
          .sorted(Comparator.comparing(Tag::getName))
          .collect(Collectors.toList());
      openApi.setTags(sortedTags);
    };
  }

  @Bean
  public OperationCustomizer apiSummaryCustomizer() {
    return (operation, handlerMethod) -> {
      if (operation.getSummary() == null || operation.getSummary().isEmpty()) {
        String methodName = handlerMethod.getMethod().getName();
        String summary = convertMethodNameToTitle(methodName);
        operation.setSummary(summary);
      }
      return operation;
    };
  }

  private String convertMethodNameToTitle(String methodName) {
    StringBuilder title = new StringBuilder(methodName.replaceAll("([A-Z])", " $1"));
    return title.substring(0, 1).toUpperCase(Locale.ROOT) + title.substring(1).trim();
  }
}
