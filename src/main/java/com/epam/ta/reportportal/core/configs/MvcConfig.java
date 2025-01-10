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

import com.epam.reportportal.rules.commons.ExceptionMappings;
import com.epam.reportportal.rules.commons.exception.forwarding.ClientResponseForwardingExceptionHandler;
import com.epam.reportportal.rules.commons.exception.rest.DefaultErrorResolver;
import com.epam.reportportal.rules.commons.exception.rest.ReportPortalExceptionResolver;
import com.epam.reportportal.rules.commons.exception.rest.RestExceptionHandler;
import com.epam.ta.reportportal.ws.resolver.ActiveUserWebArgumentResolver;
import com.epam.ta.reportportal.ws.resolver.FilterCriteriaResolver;
import com.epam.ta.reportportal.ws.resolver.JsonViewSupportFactoryBean;
import com.epam.ta.reportportal.ws.resolver.PagingHandlerMethodArgumentResolver;
import com.epam.ta.reportportal.ws.resolver.PredefinedFilterCriteriaResolver;
import com.epam.ta.reportportal.ws.resolver.SortArgumentResolver;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.http.MediaType;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.validation.beanvalidation.BeanValidationPostProcessor;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Class-based Spring MVC Configuration
 *
 * @author Andrei Varabyeu
 */
@Configuration
public class MvcConfig implements WebMvcConfigurer {

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private List<HttpMessageConverter<?>> converters;

  private static final String[] CLASSPATH_RESOURCE_LOCATIONS =
      {"classpath:/public/", "classpath:/META-INF/resources/", "classpath:/resources/"};

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    if (!registry.hasMappingForPattern("/**")) {
      registry.addResourceHandler("/**").addResourceLocations(CLASSPATH_RESOURCE_LOCATIONS);
    }
    if (!registry.hasMappingForPattern("/webjars/**")) {
      registry.addResourceHandler("/webjars/**")
          .addResourceLocations("classpath:/META-INF/resources/webjars/");
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
    PagingHandlerMethodArgumentResolver pageableResolver =
        new PagingHandlerMethodArgumentResolver(sortArgumentResolver());
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
    converters.add(byteArrayConverter());
    converters.add(jsonConverter());
    converters.add(openMetricsTextStringConverter());
    converters.add(stringConverter());
  }

  @Override
  public void configureHandlerExceptionResolvers(
      List<HandlerExceptionResolver> exceptionResolvers) {
    /* to propagate exceptions from downstream services */
    ClientResponseForwardingExceptionHandler forwardingExceptionHandler =
        new ClientResponseForwardingExceptionHandler();
    forwardingExceptionHandler.setOrder(Ordered.HIGHEST_PRECEDENCE);
    exceptionResolvers.add(forwardingExceptionHandler);

    RestExceptionHandler handler = new RestExceptionHandler();
    handler.setOrder(Ordered.HIGHEST_PRECEDENCE + 1);

    DefaultErrorResolver defaultErrorResolver =
        new DefaultErrorResolver(ExceptionMappings.DEFAULT_MAPPING);
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
  public StringHttpMessageConverter openMetricsTextStringConverter() {
    StringHttpMessageConverter converter = new StringHttpMessageConverter();
    converter.setSupportedMediaTypes(Collections.singletonList(
        new MediaType("application", "openmetrics-text", StandardCharsets.UTF_8)));
    return converter;
  }

  @Bean
  public ByteArrayHttpMessageConverter byteArrayConverter() {
    return new ByteArrayHttpMessageConverter();
  }

  @Bean
  HttpMessageConverters httpMessageConverters() {
    return new HttpMessageConverters(converters);
  }

}
