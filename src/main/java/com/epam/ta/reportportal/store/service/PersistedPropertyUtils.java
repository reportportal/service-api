package com.epam.ta.reportportal.store.service;

import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.ConvertUtilsBean;
import org.apache.commons.beanutils.converters.AbstractConverter;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.time.Instant;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

public class PersistedPropertyUtils {

	private static final Set<Class> supportedTypes = Sets.newHashSet(String.class, Long.class, Integer.class, Instant.class);
	private static final ConvertUtilsBean convertUtils;
	private static final BeanUtilsBean beanUtils;

	static {
		convertUtils = new ConvertUtilsBean();
		convertUtils.register(new AbstractConverter() {

			@Override
			protected <T> T convertToType(Class<T> type, Object value) {
				Instant instant = Instant.parse(value.toString());
				return type.cast(instant);
			}

			@Override
			protected Class<?> getDefaultType() {
				return String.class;
			}
		}, Instant.class);

		beanUtils = new BeanUtilsBean(convertUtils);
	}

	private static final ConcurrentHashMap<Class<?>, Map<String, String>> PERSISTED_PROPERTIES = new ConcurrentHashMap<>();

	private static final Function<Class<?>, Map<String, String>> DESCRIBE_CLASS = clazz -> Arrays.stream(clazz.getDeclaredFields())
			.filter(f -> f.isAnnotationPresent(PersistedProperty.class))
			.peek(f -> {
				if (!supportedTypes.contains(f.getType())) {
					throw new IllegalArgumentException(String.format("Field with type %s not supported", f.getType()));
				}
			})
			.collect(Collectors.toMap(Field::getName, f -> {
				String propertyName = f.getAnnotation(PersistedProperty.class).value();
				return Strings.isNullOrEmpty(propertyName) ? f.getName() : propertyName;
			}));

	public static Map<String, String> toMap(Object o) {
		Map<String, String> fields = PERSISTED_PROPERTIES.computeIfAbsent(o.getClass(), DESCRIBE_CLASS);

		return fields.entrySet().stream().collect(Collectors.toMap(Map.Entry::getValue, f -> {
			try {
				return beanUtils.getProperty(o, f.getKey());
			} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
				throw new IllegalArgumentException("Unable to convert fields to map", e);
			}
		}));
	}

	public static void fromMap(Map<String, String> properties, Object obj) {
		Map<String, String> fields = PERSISTED_PROPERTIES.computeIfAbsent(obj.getClass(), DESCRIBE_CLASS);

		fields.forEach((key, value) -> {
			try {
				beanUtils.setProperty(obj, key, properties.get(value));
			} catch (IllegalAccessException | InvocationTargetException e) {
				throw new IllegalArgumentException("Unable to populate fields from map", e);
			}
		});

	}

}
