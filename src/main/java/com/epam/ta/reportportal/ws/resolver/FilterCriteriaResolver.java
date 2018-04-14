package com.epam.ta.reportportal.ws.resolver;

import com.epam.ta.reportportal.commons.validation.BusinessRule;
import com.epam.ta.reportportal.store.commons.Predicates;
import com.epam.ta.reportportal.store.commons.querygen.Condition;
import com.epam.ta.reportportal.store.commons.querygen.Filter;
import com.epam.ta.reportportal.store.commons.querygen.FilterCondition;
import com.epam.ta.reportportal.ws.model.ErrorType;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Resolves filter parameters in GET requests. All Parameters should start with
 * <b>'filter.'</b> prefix. For example, if you would like to filter some
 * parameter with name 'age' you have to put in request the following:
 * '?filter.age=20'. Resolves parameter only in case argument marked with
 * annotation {@link FilterFor}. <br>
 * By FilterFor value resolves criterias/parameters to the given domain class
 * and resolves them if possible. If there are no criteria/parameter defined for
 * specified class than will throw exception
 *
 * @author Andrei Varabyeu
 */
public class FilterCriteriaResolver implements HandlerMethodArgumentResolver {

	/**
	 * Default prefix for filter conditions. Since Request contains a lot of
	 * parameters (some of them may not be related to filtering), we have to
	 * introduce this
	 */
	public static final String DEFAULT_FILTER_PREFIX = "filter.";

	/**
	 * Prefix before condition type. 'NOT' filter condition may be marked with
	 * this prefix
	 */
	public static final String NOT_FILTER_MARKER = "!";

	/**
	 * Returns TRUE only for {@link java.util.List} marked with {@link FilterFor}
	 * annotations
	 */
	@Override
	public boolean supportsParameter(MethodParameter methodParameter) {
		return methodParameter.getParameterType().equals(Filter.class) && null != methodParameter.getParameterAnnotation(FilterFor.class);
	}

	@Override
	public Filter resolveArgument(MethodParameter methodParameter, ModelAndViewContainer paramModelAndViewContainer,
			NativeWebRequest webRequest, WebDataBinderFactory paramWebDataBinderFactory) {
		return resolveAsList(methodParameter, webRequest);
	}

	@SuppressWarnings("unchecked")
	private <T> Filter resolveAsList(MethodParameter methodParameter, NativeWebRequest webRequest) {
		Class<T> domainModelType = (Class<T>) methodParameter.getParameterAnnotation(FilterFor.class).value();

		Set<FilterCondition> filterConditions = webRequest.getParameterMap()
				.entrySet()
				.stream()
				.filter(parameter -> parameter.getKey().startsWith(DEFAULT_FILTER_PREFIX) && parameter.getValue().length > 0)
				.map(parameter -> {
					final String[] tokens = parameter.getKey().split("\\.");
					checkTokens(tokens);
					String stringCondition = tokens[1];
					boolean isNegative = stringCondition.startsWith(NOT_FILTER_MARKER);

					Condition condition = getCondition(isNegative ?
							StringUtils.substringAfter(stringCondition, NOT_FILTER_MARKER) :
							stringCondition);
					String criteria = tokens[2];
					return new FilterCondition(condition, isNegative, parameter.getValue()[0], criteria);

				})
				.collect(Collectors.toSet());
		return new Filter(domainModelType, filterConditions);
	}

	private void checkTokens(String[] tokens) {
		BusinessRule.expect(tokens.length, Predicates.equalTo(3))
				.verify(ErrorType.INCORRECT_FILTER_PARAMETERS, "Incorrect format of filtering parameters");
	}

	private Condition getCondition(String marker) {
		Optional<Condition> condition = Condition.findByMarker(marker);
		BusinessRule.expect(condition, Predicates.isPresent())
				.verify(ErrorType.INCORRECT_FILTER_PARAMETERS, "Unable to find condition with marker '" + marker + "'");
		return condition.get();
	}
}