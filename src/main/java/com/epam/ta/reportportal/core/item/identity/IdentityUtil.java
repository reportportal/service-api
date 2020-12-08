package com.epam.ta.reportportal.core.item.identity;

import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public class IdentityUtil {

	private IdentityUtil() {
		//static only
	}

	/**
	 * Parse {@link TestItem#getPath()} and get all ids excluding id of the provided {@link TestItem}
	 *
	 * @param testItem {@link TestItem}
	 * @return {@link List} with ids parsed from {@link TestItem#getPath()}
	 */
	public static List<Long> getParentIds(TestItem testItem) {
		return getIds(testItem.getPath(), false);
	}

	/**
	 * * Parse {@link TestItem#getPath()} and get all ids including id of the provided {@link TestItem}
	 *
	 * @param testItem {@link TestItem}
	 * @return {@link List} with ids parsed from {@link TestItem#getPath()}
	 */
	public static List<Long> getItemTreeIds(TestItem testItem) {
		return getIds(testItem.getPath(), true);
	}

	private static List<Long> getIds(String path, boolean includeLast) {
		String[] ids = path.split("\\.");
		return Stream.of(ids).limit(includeLast ? ids.length : ids.length - 1).map(id -> {
			try {
				return Long.parseLong(id);
			} catch (NumberFormatException e) {
				throw new ReportPortalException(ErrorType.BAD_REQUEST_ERROR, "Incorrect path value: " + id);
			}
		}).collect(Collectors.toList());
	}
}
