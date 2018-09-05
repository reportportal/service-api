package com.epam.ta.reportportal.core.widget.util;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ContentFieldMatcherUtil {

	private ContentFieldMatcherUtil() {
		//static only
	}

	public static boolean match(final String patternValue, List<String> contentFields) {
		Pattern pattern = Pattern.compile(patternValue);
		return contentFields.stream().map(pattern::matcher).allMatch(Matcher::matches);

	}
}
