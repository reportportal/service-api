/*
 * Copyright 2019 EPAM Systems
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
import com.epam.ta.reportportal.commons.querygen.CriteriaHolder;
import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.commons.querygen.FilterTarget;
import com.epam.ta.reportportal.commons.querygen.Queryable;
import com.epam.ta.reportportal.core.statistics.StatisticsHelper;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.user.UserRole;
import com.epam.ta.reportportal.ws.resolver.FilterFor;
import com.fasterxml.classmate.ResolvedType;
import com.fasterxml.classmate.TypeResolver;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import springfox.documentation.PathProvider;
import springfox.documentation.builders.ParameterBuilder;
import springfox.documentation.schema.ModelReference;
import springfox.documentation.schema.ResolvedTypes;
import springfox.documentation.schema.TypeNameExtractor;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.service.Parameter;
import springfox.documentation.service.ResolvedMethodParameter;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.schema.contexts.ModelContext;
import springfox.documentation.spi.service.OperationBuilderPlugin;
import springfox.documentation.spi.service.contexts.OperationContext;
import springfox.documentation.spi.service.contexts.ParameterContext;
import springfox.documentation.spring.web.paths.RelativePathProvider;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger.web.UiConfiguration;
import springfox.documentation.swagger.web.UiConfigurationBuilder;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import javax.servlet.ServletContext;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.epam.ta.reportportal.commons.querygen.constant.ProjectCriteriaConstant.CRITERIA_PROJECT_ATTRIBUTE_NAME;
import static com.google.common.base.Predicates.not;
import static com.google.common.base.Predicates.or;
import static com.google.common.collect.Lists.newArrayList;
import static springfox.documentation.builders.RequestHandlerSelectors.basePackage;
import static springfox.documentation.spi.schema.contexts.ModelContext.inputParam;

/**
 * SWAGGER 2.0 UI page configuration for Report Portal application
 *
 * @author dzmitry_kavalets
 * @author Andrei_Ramanchuk
 * @author Andrei Varabyeu
 */
@Configuration
@Conditional(Conditions.NotTestCondition.class)
@EnableSwagger2
@ComponentScan(basePackages = "com.epam.ta.reportportal.ws.controller")
public class Swagger2Configuration {

	private static final Set<String> hiddenParams = ImmutableSet.<String>builder().add(CRITERIA_PROJECT_ATTRIBUTE_NAME).build();

	@Autowired
	private ServletContext servletContext;

	@Value("${spring.application.name}")
	private String applicationName;

	@Value("${info.build.version}")
	private String buildVersion;

	@Bean
	public Docket docket() {
		/* For more information see default params at {@link ApiInfo} */
		ApiInfo rpInfo = new ApiInfo(
				"Report Portal",
				"Report Portal API documentation",
				buildVersion,
				null,
				new Contact("Support", null, "Support EPMC-TST Report Portal <SupportEPMC-TSTReportPortal@epam.com>"),
				"Apache 2.0",
				"http://www.apache.org/licenses/LICENSE-2.0",
				Collections.emptyList()
		);

		// @formatter:off
        Docket rpDocket = new Docket(DocumentationType.SWAGGER_2)
                .ignoredParameterTypes(ReportPortalUser.class, Filter.class, Queryable.class, Pageable.class, UserRole.class)
                .pathProvider(rpPathProvider())
                .useDefaultResponseMessages(false)
                /* remove default endpoints from listing */
                .select().apis(not(or(
                        basePackage("org.springframework.boot"),
                        basePackage("org.springframework.cloud"))))
                .build();
        //@formatter:on

		rpDocket.apiInfo(rpInfo);
		return rpDocket;
	}

	@Bean
	public PathProvider rpPathProvider() {
		return new RelativePathProvider(servletContext) {
			@Override
			public String getApplicationBasePath() {
				return "/" + applicationName + super.getApplicationBasePath();
			}
		};
	}

	@Bean
	OperationPageableParameterReader pageableParameterBuilderPlugin(TypeNameExtractor nameExtractor, TypeResolver resolver) {
		return new OperationPageableParameterReader(nameExtractor, resolver);
	}

	@Bean
	public UiConfiguration uiConfig() {
		return UiConfigurationBuilder.builder().build();
	}

	@Component
	public class OperationPageableParameterReader implements OperationBuilderPlugin {
		private final TypeNameExtractor nameExtractor;
		private final TypeResolver resolver;

		private final ResolvedType pageableType;
		private final ResolvedType filterType;

		@Autowired
		public OperationPageableParameterReader(TypeNameExtractor nameExtractor, TypeResolver resolver) {
			this.nameExtractor = nameExtractor;
			this.resolver = resolver;
			this.pageableType = resolver.resolve(Pageable.class);
			this.filterType = resolver.resolve(Filter.class);
		}

		@Override
		public void apply(OperationContext context) {
			List<ResolvedMethodParameter> methodParameters = context.getParameters();
			List<Parameter> parameters = newArrayList();

			for (ResolvedMethodParameter methodParameter : methodParameters) {
				ResolvedType resolvedType = methodParameter.getParameterType();
				ParameterContext parameterContext = new ParameterContext(
						methodParameter,
						new ParameterBuilder(),
						context.getDocumentationContext(),
						context.getGenericsNamingStrategy(),
						context
				);
				Function<ResolvedType, ? extends ModelReference> factory = createModelRefFactory(parameterContext);
				ModelReference stringModel = factory.apply(resolver.resolve(List.class, String.class));

				if (pageableType.equals(resolvedType)) {

					ModelReference intModel = factory.apply(resolver.resolve(Integer.TYPE));

					parameters.add(new ParameterBuilder().parameterType("query")
							.name("page.page")
							.modelRef(intModel)
							.description("Results page you want to retrieve (0..N)")
							.build());
					parameters.add(new ParameterBuilder().parameterType("query")
							.name("page.size")
							.modelRef(intModel)
							.description("Number of records per page")
							.build());
					parameters.add(new ParameterBuilder().parameterType("query")
							.name("page.sort")
							.modelRef(stringModel)
							.allowMultiple(true)
							.description("Sorting criteria in the format: property, (asc|desc). " + "Default sort order is ascending. "
									+ "Multiple sort criteria are supported.")
							.build());
					context.operationBuilder().parameters(parameters);

				} else if (filterType.equals(resolvedType)) {
					FilterFor filterClass = methodParameter.findAnnotation(FilterFor.class).get();

					List<Parameter> defaultParams = Lists.newArrayList();
					if (filterClass.value() == TestItem.class || filterClass.value() == Launch.class) {
						defaultParams = StatisticsHelper.defaultStatisticsFields()
								.map(it -> buildParameters(parameterContext, factory, it))
								.collect(Collectors.toList());
					}

					List<CriteriaHolder> criteriaList = FilterTarget.findByClass(filterClass.value()).getCriteriaHolders();
					List<Parameter> params = criteriaList.stream()
							.filter(ch -> !hiddenParams.contains(ch.getFilterCriteria()))
							.map(it -> buildParameters(parameterContext, factory, it))
							/* if type is not a collection and first letter is not capital (all known to swagger types start from lower case) */
							.filter(p -> !(null == p.getModelRef().getItemType() && Character.isUpperCase(p.getModelRef()
									.getType()
									.toCharArray()[0])))
							.collect(Collectors.toList());

					params.addAll(defaultParams);
					context.operationBuilder().parameters(params);
				}
			}
		}

		private Parameter buildParameters(ParameterContext parameterContext, Function<ResolvedType, ? extends ModelReference> factory,
				CriteriaHolder criteriaHolder) {
			return parameterContext.parameterBuilder()
					.parameterType("query")
					.name("filter.eq." + criteriaHolder.getFilterCriteria())
					.allowMultiple(true)
					.modelRef(factory.apply(resolver.resolve(
							criteriaHolder.getDataType() == Timestamp.class ? Date.class : criteriaHolder.getDataType())))
					.description("Filters by '" + criteriaHolder.getFilterCriteria() + "'")
					.build();
		}

		private Parameter buildParameters(ParameterContext parameterContext, Function<ResolvedType, ? extends ModelReference> factory,
				String parameter) {
			return parameterContext.parameterBuilder()
					.parameterType("query")
					.name("filter.eq." + parameter)
					.allowMultiple(true)
					.modelRef(factory.apply(resolver.resolve(Long.class)))
					.description("Filters by '" + parameter + "'")
					.build();
		}

		@Override
		public boolean supports(DocumentationType delimiter) {
			return true;
		}

		private Function<ResolvedType, ? extends ModelReference> createModelRefFactory(ParameterContext context) {
			ModelContext modelContext = inputParam(
					Docket.DEFAULT_GROUP_NAME,
					context.resolvedMethodParameter().getParameterType().getErasedType(),
					context.getDocumentationType(),
					context.getAlternateTypeProvider(),
					context.getGenericNamingStrategy(),
					context.getIgnorableParameterTypes()
			);
			return ResolvedTypes.modelRefFactory(modelContext, nameExtractor);
		}
	}

	@SuppressWarnings("unused")
	private static class RPPathProvider extends RelativePathProvider {

		private String gatewayPath;

		RPPathProvider(ServletContext servletContext, String gatewayPath) {
			super(servletContext);
			this.gatewayPath = gatewayPath;
		}

		@Override
		protected String applicationPath() {
			return "/" + gatewayPath + super.applicationPath();
		}
	}
}
