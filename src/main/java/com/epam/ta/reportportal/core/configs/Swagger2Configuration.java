/*
 * Copyright 2018 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.ta.reportportal.core.configs;

import com.epam.ta.reportportal.auth.ReportPortalUser;
import com.epam.ta.reportportal.entity.user.UserRole;
import com.fasterxml.classmate.ResolvedType;
import com.fasterxml.classmate.TypeResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ModelAttribute;
import springfox.documentation.PathProvider;
import springfox.documentation.builders.ParameterBuilder;
import springfox.documentation.schema.ModelReference;
import springfox.documentation.schema.ResolvedTypes;
import springfox.documentation.schema.TypeNameExtractor;
import springfox.documentation.service.*;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.schema.contexts.ModelContext;
import springfox.documentation.spi.service.*;
import springfox.documentation.spi.service.contexts.*;
import springfox.documentation.spring.web.paths.RelativePathProvider;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger.web.UiConfiguration;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import javax.servlet.ServletContext;
import java.util.*;
import java.util.function.Function;

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

	@Autowired
	private ServletContext servletContext;

	@Autowired
	@Value("${spring.application.name}")
	private String eurekaName;

	@Autowired
	@Value("${info.build.version}")
	private String buildVersion;

	@Bean
	public Docket docket() {
		/* For more information see default params at {@link ApiInfo} */
		ApiInfo rpInfo = new ApiInfo(
				"Report Portal",
				"Report Portal API documentation",
				buildVersion,
				"urn:tos",
				new Contact("EPAM Systems", "http://epam.com", "Support EPMC-TST Report Portal <SupportEPMC-TSTReportPortal@epam.com>"),
				"GPLv3",
				"https://www.gnu.org/licenses/licenses.html#GPL",
				Collections.emptyList()
		);

		// @formatter:off
        Docket rpDocket = new Docket(DocumentationType.SWAGGER_2)
                //.ignoredParameterTypes(Principal.class, Filter.class, Pageable.class)
                .pathProvider(rpPathProvider())
                .useDefaultResponseMessages(false)
				.ignoredParameterTypes(UserRole.class, AuthenticationPrincipal.class, ReportPortalUser.ProjectDetails.class)
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
		return new RelativePathProvider(servletContext);
	}

	@Bean
	OperationPageableParameterReader pageableParameterBuilderPlugin(TypeNameExtractor nameExtractor, TypeResolver resolver) {
		return new OperationPageableParameterReader(nameExtractor, resolver);
	}

	@Bean
	public UiConfiguration uiConfig() {
		return new UiConfiguration(null);
	}

	@Component
	public class OperationPageableParameterReader implements OperationBuilderPlugin {
		private final TypeNameExtractor nameExtractor;
		private final TypeResolver resolver;

		private final ResolvedType pageableType;
		//private final ResolvedType filterType;
		private final ResolvedType projectDetailsType;

		@Autowired
		public OperationPageableParameterReader(TypeNameExtractor nameExtractor, TypeResolver resolver) {
			this.nameExtractor = nameExtractor;
			this.resolver = resolver;
			this.pageableType = resolver.resolve(Pageable.class);
			//this.filterType = resolver.resolve(Filter.class);
			this.projectDetailsType = resolver.resolve(ReportPortalUser.ProjectDetails.class);
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

				if (projectDetailsType.equals(resolvedType)) {

					parameters.add(new ParameterBuilder().parameterType("path")
							.name("projectName")
							.modelRef(stringModel)
							.description("Name of project launch starts under")
							.required(true)
							.build());

					context.operationBuilder().parameters(parameters);
				} else if (pageableType.equals(resolvedType)) {

					ModelReference intModel = factory.apply(resolver.resolve(Integer.TYPE));
					//@formatter:off

					parameters.add(new ParameterBuilder()
							.parameterType("query")
							.name("page.page")
							.modelRef(intModel)
							.description("Results page you want to retrieve (0..N)").build());
					parameters.add(new ParameterBuilder()
							.parameterType("query")
							.name("page.size")
							.modelRef(intModel)
							.description("Number of records per page").build());
					parameters.add(new ParameterBuilder()
							.parameterType("query")
							.name("page.sort")
							.modelRef(stringModel)
							.allowMultiple(true)
							.description("Sorting criteria in the format: property(,asc|desc). "
									+ "Default sort order is ascending. "
									+ "Multiple sort criteria are supported.")
							.build());
					context.operationBuilder().parameters(parameters);
					//@formatter:on
				}
				//				} else if (filterType.equals(resolvedType)) {
				//					FilterFor filterClass = methodParameter.findAnnotation(FilterFor.class).get();
				//					CriteriaMap<?> criteriaMap = CriteriaMapFactory.DEFAULT_INSTANCE_SUPPLIER.get().getCriteriaMap(filterClass.value());
				//
				//					//@formatter:off
//					List<Parameter> params = criteriaMap.getAllowedSearchCriterias().stream()
//							.map(searchCriteria -> parameterContext
//									.parameterBuilder()
//										.parameterType("query")
//										.name("filter.eq." + searchCriteria)
//										.modelRef(factory.apply(resolver.resolve(criteriaMap.getCriteriaHolder(searchCriteria).getDataType())))
//									.description("Filters by '" + searchCriteria + "'")
//									.build())
//							/* if type is not a collection and first letter is not capital (all known to swagger types start from lower case) */
//							.filter( p -> !(null == p.getModelRef().getItemType() && Character.isUpperCase(p.getModelRef().getType().toCharArray()[0])))
//							.collect(Collectors.toList());
//					//@formatter:on
				//
				//					context.operationBuilder().parameters(params);
				//				}
			}
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
