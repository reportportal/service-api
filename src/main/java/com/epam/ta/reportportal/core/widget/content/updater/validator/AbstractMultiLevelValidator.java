package com.epam.ta.reportportal.core.widget.content.updater.validator;

import com.epam.ta.reportportal.commons.validation.BusinessRule;
import com.epam.ta.reportportal.entity.widget.Widget;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.google.common.collect.Sets;
import org.apache.commons.collections.CollectionUtils;

import java.util.List;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public abstract class AbstractMultiLevelValidator implements WidgetValidator {

	abstract protected List<String> getKeys(Widget widget);

	@Override
	public void validate(Widget widget) {
		validateKeys(getKeys(widget));
	}

	protected void validateKeys(List<String> keys) {
		BusinessRule.expect(keys, CollectionUtils::isNotEmpty).verify(ErrorType.BAD_SAVE_WIDGET_REQUEST, "Attribute keys are not provided");
		BusinessRule.expect(keys.size(), size -> size.equals(Sets.newHashSet(keys).size()))
				.verify(ErrorType.BAD_SAVE_WIDGET_REQUEST, "Attribute keys should be unique");
	}
}
