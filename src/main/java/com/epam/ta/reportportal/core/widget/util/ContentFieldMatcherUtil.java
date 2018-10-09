package com.epam.ta.reportportal.core.widget.util;

import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Ivan Budaev
 */
public final class ContentFieldMatcherUtil {

	private ContentFieldMatcherUtil() {
		//static only
	}

	public static boolean match(final String patternValue, Collection<String> contentFields) {
		Pattern pattern = Pattern.compile(patternValue);
		return contentFields.stream().map(pattern::matcher).allMatch(Matcher::matches);

	}
}
