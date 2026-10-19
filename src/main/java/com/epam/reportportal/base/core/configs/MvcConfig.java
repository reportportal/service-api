/*
 * Copyright 2025 EPAM Systems
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

package com.epam.reportportal.base.core.configs;

import com.epam.reportportal.base.ws.resolver.ActiveUserWebArgumentResolver;
import com.epam.reportportal.base.ws.resolver.FilterCriteriaResolver;
import com.epam.reportportal.base.ws.resolver.JsonViewSupportFactoryBean;
import com.epam.reportportal.base.ws.resolver.OffsetArgumentResolver;
import com.epam.reportportal.base.ws.resolver.PagingHandlerMethodArgumentResolver;
import com.epam.reportportal.base.ws.resolver.PredefinedFilterCriteriaResolver;
import com.epam.reportportal.base.ws.resolver.SortArgumentResolver;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverters;
import org.springframework.http.converter.ResourceHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.validation.beanvalidation.BeanValidationPostProcessor;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import tools.jackson.databind.json.JsonMapper;

/**
 * Class-based Spring MVC Configuration.
 *
 * @author Andrei Varabyeu
 */
@Configuration
@RequiredArgsConstructor
public class MvcConfig implements WebMvcConfigurer {

  private static final String[] CLASSPATH_RESOURCE_LOCATIONS =
      {"classpath:/public/", "classpath:/META-INF/resources/", "classpath:/resources/"};

  private final JsonMapper objectMapper;

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
    argumentResolvers.add(new OffsetArgumentResolver());
  }

  @Override
  public void configureMessageConverters(HttpMessageConverters.ServerBuilder builder) {
    builder.configureMessageConvertersList(converters -> {
      converters.clear();
      converters.add(resourceConverter());
      converters.add(byteArrayConverter());
      converters.add(jsonConverter());
      converters.add(openMetricsTextStringConverter());
      converters.add(stringConverter());
    });
  }

  @Bean
  public BeanValidationPostProcessor beanValidationPostProcessor() {
    return new BeanValidationPostProcessor();
  }

  @Bean
  public JacksonJsonHttpMessageConverter jsonConverter() {
    return new JacksonJsonHttpMessageConverter(objectMapper);
  }

  @Bean
  public StringHttpMessageConverter stringConverter() {
    StringHttpMessageConverter converter = new StringHttpMessageConverter();
    converter.setSupportedMediaTypes(Collections.singletonList(MediaType.TEXT_PLAIN));
    return converter;
  }

  @Bean
  public ResourceHttpMessageConverter resourceConverter() {
    return new ResourceHttpMessageConverter();
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

}
