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

import com.epam.ta.reportportal.commons.ExceptionMappings;
import com.epam.ta.reportportal.commons.exception.forwarding.ClientResponseForwardingExceptionHandler;
import com.epam.ta.reportportal.commons.exception.rest.DefaultErrorResolver;
import com.epam.ta.reportportal.commons.exception.rest.ReportPortalExceptionResolver;
import com.epam.ta.reportportal.commons.exception.rest.RestExceptionHandler;
import com.epam.ta.reportportal.ws.resolver.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.validation.beanvalidation.BeanValidationPostProcessor;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.multipart.support.MultipartFilter;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Collections;
import java.util.List;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * Class-based Spring MVC Configuration
 *
 * @author Andrei Varabyeu
 */
@Configuration
@EnableConfigurationProperties(MvcConfig.MultipartConfig.class)
public class MvcConfig implements WebMvcConfigurer {

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private List<HttpMessageConverter<?>> converters;

	private static final String[] CLASSPATH_RESOURCE_LOCATIONS = { "classpath:/public/", "classpath:/META-INF/resources/",
			"classpath:/resources/" };

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		if (!registry.hasMappingForPattern("/**")) {
			registry.addResourceHandler("/**").addResourceLocations(CLASSPATH_RESOURCE_LOCATIONS);
		}
		if (!registry.hasMappingForPattern("/webjars/**")) {
			registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
		}
	}

	@Bean
	public SortArgumentResolver sortArgumentResolver() {
		SortArgumentResolver argumentResolver = new SortArgumentResolver();
		argumentResolver.setSortParameter("page.sort");
		argumentResolver.setQualifierDelimiter("+");
		return argumentResolver;
	}

	@Bean
	public JsonViewSupportFactoryBean jsonViewSupportFactoryBean() {
		return new JsonViewSupportFactoryBean();
	}

	@Override
	public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
		argumentResolvers.clear();
		PagingHandlerMethodArgumentResolver pageableResolver = new PagingHandlerMethodArgumentResolver(sortArgumentResolver());
		pageableResolver.setPrefix("page.");
		pageableResolver.setOneIndexedParameters(true);

		argumentResolvers.add(pageableResolver);

		argumentResolvers.add(new ActiveUserWebArgumentResolver());
		argumentResolvers.add(new FilterCriteriaResolver());
		argumentResolvers.add(new PredefinedFilterCriteriaResolver());
	}

	@Override
	public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
		converters.clear();
		converters.add(jsonConverter());
		converters.add(stringConverter());
	}

	@Override
	public void configureHandlerExceptionResolvers(List<HandlerExceptionResolver> exceptionResolvers) {
		/* to propagate exceptions from downstream services */
		ClientResponseForwardingExceptionHandler forwardingExceptionHandler = new ClientResponseForwardingExceptionHandler();
		forwardingExceptionHandler.setOrder(Ordered.HIGHEST_PRECEDENCE);
		exceptionResolvers.add(forwardingExceptionHandler);

		RestExceptionHandler handler = new RestExceptionHandler();
		handler.setOrder(Ordered.HIGHEST_PRECEDENCE + 1);

		DefaultErrorResolver defaultErrorResolver = new DefaultErrorResolver(ExceptionMappings.DEFAULT_MAPPING);
		handler.setErrorResolver(new ReportPortalExceptionResolver(defaultErrorResolver));
		handler.setMessageConverters(Collections.singletonList(jsonConverter()));
		exceptionResolvers.add(handler);
	}

	@Override
	public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
		configurer.favorPathExtension(false);
	}

	@Override
	public void configurePathMatch(PathMatchConfigurer configurer) {
		configurer.setUseSuffixPatternMatch(false);
	}

	@Bean
	public BeanValidationPostProcessor beanValidationPostProcessor() {
		return new BeanValidationPostProcessor();
	}

	@Bean
	public MappingJackson2HttpMessageConverter jsonConverter() {
		return new MappingJackson2HttpMessageConverter(objectMapper);
	}

	@Bean
	public StringHttpMessageConverter stringConverter() {
		StringHttpMessageConverter converter = new StringHttpMessageConverter();
		converter.setSupportedMediaTypes(Collections.singletonList(MediaType.TEXT_PLAIN));
		return converter;
	}

	@Bean
	HttpMessageConverters httpMessageConverters() {
		return new HttpMessageConverters(converters);
	}

	@Profile("!unittest")
	@Bean
	@Order(0)
	public MultipartFilter multipartFilter() {
		MultipartFilter multipartFilter = new MultipartFilter();
		multipartFilter.setMultipartResolverBeanName(DispatcherServlet.MULTIPART_RESOLVER_BEAN_NAME);
		return multipartFilter;
	}

	@Profile("!unittest")
	@Bean(name = DispatcherServlet.MULTIPART_RESOLVER_BEAN_NAME)
	public CommonsMultipartResolver multipartResolver(MultipartConfig multipartConfig) {
		CommonsMultipartResolver commonsMultipartResolver = new CommonsMultipartResolver() {
			@Override
			protected DiskFileItemFactory newFileItemFactory() {
				DiskFileItemFactory diskFileItemFactory = super.newFileItemFactory();
				diskFileItemFactory.setFileCleaningTracker(null);
				return diskFileItemFactory;
			}

			@Override
			public void cleanupMultipart(MultipartHttpServletRequest request) {
				//
			}
		};

		//Lazy resolving gives a way to process file limits inside a controller
		//level and handle exceptions in proper way. Fixes reportportal/reportportal#19
		commonsMultipartResolver.setResolveLazily(true);

		commonsMultipartResolver.setMaxUploadSize(multipartConfig.maxUploadSize);
		commonsMultipartResolver.setMaxUploadSizePerFile(multipartConfig.maxFileSize);
		return commonsMultipartResolver;
	}

	@ConfigurationProperties("rp.upload")
	public static class MultipartConfig {
		long maxUploadSize = 128L * 1024L * 1024L;
		long maxFileSize = 128L * 1024L * 1024L;

		public void setMaxUploadSize(String maxUploadSize) {
			this.maxUploadSize = parseSize(maxUploadSize);
		}

		public void setMaxFileSize(String maxFileSize) {
			this.maxFileSize = parseSize(maxFileSize);
		}

		private long parseSize(String size) {
			Preconditions.checkArgument(!isNullOrEmpty(size), "Size must not be empty");
			size = size.toUpperCase();
			if (size.endsWith("KB")) {
				return Long.parseLong(size.substring(0, size.length() - 2)) * 1024;
			}
			if (size.endsWith("MB")) {
				return Long.parseLong(size.substring(0, size.length() - 2)) * 1024 * 1024;
			}
			if (size.endsWith("GB")) {
				return Long.parseLong(size.substring(0, size.length() - 2)) * 1024 * 1024 * 1024;
			}
			return Long.parseLong(size);
		}
	}

}
