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

package com.epam.reportportal.core.configs.rabbit;

import static java.util.function.Function.identity;

import com.epam.reportportal.core.events.domain.AbstractEvent;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import jakarta.annotation.PostConstruct;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.support.converter.Jackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.MessageConversionException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.stereotype.Component;

/**
 * Type mapper that resolves event classes from RabbitMQ message headers.
 * <p>
 * Primary resolution uses fully qualified class name from {@code __TypeId__} header. Falls back to
 * simple class name matching when the class is not found locally, enabling seamless integration
 * with external services using different package structures.
 */
@Slf4j
@Component
public class FallbackEventTypeMapper implements Jackson2JavaTypeMapper {

  private static final String TYPE_ID_HEADER = "__TypeId__";
  private static final TypeFactory TYPE_FACTORY = TypeFactory.defaultInstance();

  private Map<String, Class<?>> registry;

  @PostConstruct
  void init() {
    var scanner = new ClassPathScanningCandidateComponentProvider(false);
    scanner.addIncludeFilter(new AssignableTypeFilter(AbstractEvent.class));

    registry = scanner.findCandidateComponents("com.epam.reportportal").stream()
        .map(BeanDefinition::getBeanClassName)
        .filter(Objects::nonNull)
        .flatMap(name -> loadClass(name).stream())
        .filter(clazz -> !Modifier.isAbstract(clazz.getModifiers()))
        .collect(Collectors.toMap(Class::getSimpleName, identity(), (a, b) -> a));

    log.debug("Registered {} fallback types", registry.size());
  }

  /**
   * Resolves target class from message properties.
   *
   * @param properties message properties containing {@code __TypeId__} header
   * @return resolved class for deserialization
   * @throws MessageConversionException if header is missing or type cannot be resolved
   */
  @Override
  public Class<?> toClass(MessageProperties properties) {
    var typeId = getRequiredHeader(properties);

    try {
      return Class.forName(typeId);
    } catch (ClassNotFoundException e) {
      return resolveFallback(typeId);
    }
  }

  /**
   * Resolves target JavaType from message properties.
   *
   * @param properties message properties containing {@code __TypeId__} header
   * @return resolved JavaType for deserialization
   * @throws MessageConversionException if header is missing or type cannot be resolved
   */
  @Override
  public JavaType toJavaType(MessageProperties properties) {
    return TYPE_FACTORY.constructType(toClass(properties));
  }

  /**
   * Sets {@code __TypeId__} header from the given class.
   *
   * @param clazz      source class
   * @param properties target message properties
   */
  @Override
  public void fromClass(Class<?> clazz, MessageProperties properties) {
    properties.setHeader(TYPE_ID_HEADER, clazz.getName());
  }

  /**
   * Sets {@code __TypeId__} header from the given JavaType.
   *
   * @param javaType   source type
   * @param properties target message properties
   */
  @Override
  public void fromJavaType(JavaType javaType, MessageProperties properties) {
    fromClass(javaType.getRawClass(), properties);
  }

  /**
   * Returns the type precedence strategy.
   * <p>
   * Uses {@link TypePrecedence#TYPE_ID} to resolve types from {@code __TypeId__} header, consistent
   * with {@link org.springframework.amqp.support.converter.DefaultJackson2JavaTypeMapper}.
   *
   * @return TYPE_ID precedence
   */
  @Override
  public TypePrecedence getTypePrecedence() {
    return TypePrecedence.TYPE_ID;
  }

  /**
   * Returns inferred type from message properties.
   * <p>
   * Not used when {@link TypePrecedence#TYPE_ID} is set. Defined by
   * {@link org.springframework.amqp.support.converter.Jackson2JavaTypeMapper}.
   *
   * @param properties message properties
   * @return null (type inference not supported)
   */
  @Override
  public JavaType getInferredType(MessageProperties properties) {
    return null;
  }

  private String getRequiredHeader(MessageProperties properties) {
    return Optional.ofNullable(properties.getHeaders().get(TYPE_ID_HEADER))
        .map(Object::toString)
        .orElseThrow(() -> new MessageConversionException("Missing " + TYPE_ID_HEADER));
  }

  private Class<?> resolveFallback(String typeId) {
    var simpleName = typeId.substring(typeId.lastIndexOf('.') + 1);

    return Optional.ofNullable(registry.get(simpleName))
        .orElseThrow(() -> new MessageConversionException("Unknown event: " + typeId));
  }

  private Optional<Class<?>> loadClass(String className) {
    try {
      return Optional.of(Class.forName(className));
    } catch (ClassNotFoundException e) {
      return Optional.empty();
    }
  }
}
