/*
 * Copyright 2024 EPAM Systems
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

import static com.epam.ta.reportportal.commons.querygen.constant.ProjectCriteriaConstant.CRITERIA_PROJECT_ATTRIBUTE_NAME;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.commons.querygen.CriteriaHolder;
import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.commons.querygen.FilterTarget;
import com.epam.ta.reportportal.commons.querygen.Queryable;
import com.epam.ta.reportportal.core.statistics.StatisticsHelper;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.user.UserRole;
import com.epam.ta.reportportal.util.SchemaFactory;
import com.epam.ta.reportportal.ws.resolver.FilterFor;
import com.fasterxml.jackson.databind.JavaType;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverter;
import io.swagger.v3.core.converter.ModelConverterContext;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import jakarta.servlet.ServletContext;
import org.apache.commons.lang3.StringUtils;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springdoc.core.utils.SpringDocUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.Order;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

/**
 * @author <a href="mailto:andrei_piankouski@epam.com">Andrei Piankouski</a>
 */
@Configuration
@ComponentScan(basePackages = "com.epam.ta.reportportal.ws.controller")
public class SpringDocConfiguration {

  static {
    SpringDocUtils.getConfig().addAnnotationsToIgnore(AuthenticationPrincipal.class);
    SpringDocUtils.getConfig().addRequestWrapperToIgnore(Pageable.class, Queryable.class,
        ReportPortalUser.class, UserRole.class);
  }


  private static final Set<String> hiddenParams = ImmutableSet.<String>builder()
      .add(CRITERIA_PROJECT_ATTRIBUTE_NAME).build();

  private static final Map<String, String> ATTRIBUTE_TO_FILTER_PREFIX = new HashMap<>();

  static {
    ATTRIBUTE_TO_FILTER_PREFIX.put("compositeAttribute", "filter.has.");
    ATTRIBUTE_TO_FILTER_PREFIX.put("compositeSystemAttribute", "filter.has.");
  }

  @Autowired
  private ServletContext servletContext;

  @Value("${info.build.version}")
  private String buildVersion;

  @Value("${server.servlet.context-path:/api}")
  private String pathValue;

  @Bean
  public OpenAPI openAPI() {
    final String securitySchemeName = "bearerAuth";
    return new OpenAPI()
        .info(new Info().title("ReportPortal")
            .description("ReportPortal API documentation")
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
        .addServersItem(new Server().url(getPathValue()));
  }

  /**
   * Resolve Iterable schema responses.
   *
   * @return ModelConverter
   */
  @Bean
  public ModelConverter iterableModelConverter() {
    return new ModelConverter() {
      @Override
      public Schema resolve(AnnotatedType annotatedType, ModelConverterContext context,
          Iterator<ModelConverter> chain) {
        JavaType javaType = Json.mapper().constructType(annotatedType.getType());
        if (javaType != null && Iterable.class.equals(javaType.getRawClass())) {
          annotatedType = new AnnotatedType()
              .type(javaType.containedType(0))
              .ctxAnnotations(annotatedType.getCtxAnnotations())
              .parent(annotatedType.getParent())
              .schemaProperty(annotatedType.isSchemaProperty())
              .name(annotatedType.getName())
              .resolveAsRef(annotatedType.isResolveAsRef())
              .jsonViewAnnotation(annotatedType.getJsonViewAnnotation())
              .propertyName(annotatedType.getPropertyName())
              .skipOverride(true);
          return new ArraySchema().items(this.resolve(annotatedType, context, chain));
        }
        return (chain.hasNext()) ? chain.next().resolve(annotatedType, context, chain) : null;
      }
    };
  }

  @Bean
  public OpenApiCustomizer sortTagsAlphabetically() {
    return openApi -> {
      List<Tag> sortedTags = openApi.getTags().stream()
          .sorted(Comparator.comparing(Tag::getName))
          .collect(Collectors.toList());
      openApi.setTags(sortedTags);
    };
  }

  @Bean
  @Order(2)
  public OperationCustomizer sortParametersAlphabetically() {
    return (operation, handlerMethod) -> {
      if (operation.getParameters() != null) {
        operation.setParameters(operation.getParameters().stream()
            .sorted(Comparator.comparing(Parameter::getName))
            .collect(Collectors.toList()));
      }
      return operation;
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

  @Bean
  @Order(1)
  public OperationCustomizer customizeParameters() {
    return (operation, handlerMethod) -> {
      for (MethodParameter parameter : handlerMethod.getMethodParameters()) {
        Class<?> parameterType = parameter.getParameterType();

        if (parameterType == Filter.class) {
          FilterFor filterClass = parameter.getParameterAnnotation(FilterFor.class);

          List<Parameter> defaultParams = Lists.newArrayList();
          if (filterClass != null && (filterClass.value() == TestItem.class
              || filterClass.value() == Launch.class)) {
            defaultParams = StatisticsHelper.defaultStatisticsFields()
                .map(this::buildFilterParameters)
                .collect(Collectors.toList());
          }

          List<CriteriaHolder> criteriaList = FilterTarget.findByClass(filterClass.value())
              .getCriteriaHolders();
          Set<Parameter> filterParams = criteriaList.stream()
              .filter(ch -> !hiddenParams.contains(ch.getFilterCriteria()))
              .map(this::buildFilterParameters)
              .collect(Collectors.toSet());
          filterParams.addAll(defaultParams);
          setParameters(operation, filterParams);
        } else if (parameterType == Pageable.class) {
          setParameters(operation, buildPageParameters());
        }
      }
      return operation;
    };
  }

  private Parameter buildFilterParameters(String parameter) {
    String filterPrefix = ATTRIBUTE_TO_FILTER_PREFIX.getOrDefault(parameter, "filter.eq.");
    return new Parameter()
        .in(ParameterIn.QUERY.toString())
        .name(filterPrefix + parameter)
        .schema(new IntegerSchema())
        .description("Filters by '" + parameter + "'");
  }

  private Parameter buildFilterParameters(CriteriaHolder criteriaHolder) {
    Schema schema = SchemaFactory.createSchemaForType(criteriaHolder.getDataType());
    String parameter = criteriaHolder.getFilterCriteria();
    String filterPrefix = ATTRIBUTE_TO_FILTER_PREFIX.getOrDefault(parameter, "filter.eq.");

    return new Parameter()
        .in(ParameterIn.QUERY.toString())
        .name(filterPrefix + parameter)
        .schema(schema)
        .description("Filters by '" + criteriaHolder.getFilterCriteria() + "'");
  }

  private List<Parameter> buildPageParameters() {
    List<Parameter> pageParams = new ArrayList<>();
    pageParams.add(new Parameter()
        .in(ParameterIn.QUERY.toString())
        .name("page.page")
        .schema(new IntegerSchema())
        .description("Results page you want to retrieve (0..N)"));
    pageParams.add(new Parameter()
        .in(ParameterIn.QUERY.toString())
        .name("page.size")
        .schema(new IntegerSchema())
        .description("Number of records per page"));
    pageParams.add(new Parameter()
        .in(ParameterIn.QUERY.toString())
        .name("page.sort")
        .schema(new StringSchema())
        .description("Sorting criteria in the format: property, (asc|desc). "
            + "Default sort order is ascending. "
            + "Multiple sort criteria are supported."));
    return pageParams;
  }

  private void setParameters(Operation operation, Collection<Parameter> parameters) {
    for (Parameter parameter : parameters) {
      operation.addParametersItem(parameter);
    }
  }

  private String getPathValue() {
    return StringUtils.isEmpty(pathValue) || pathValue.equals("/")  ? "/api" : pathValue;
  }
}
