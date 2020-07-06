package com.epam.ta.reportportal.core.widget.content.updater.validator;

import com.epam.ta.reportportal.core.widget.util.WidgetOptionUtil;
import com.epam.ta.reportportal.entity.widget.Widget;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.epam.ta.reportportal.core.widget.content.constant.ContentLoaderConstants.ATTRIBUTE_KEYS;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Component("componentHealthCheckValidator")
public class ComponentHealthCheckValidator extends AbstractMultiLevelValidator{
	@Override
	protected List<String> getKeys(Widget widget) {
		return WidgetOptionUtil.getListByKey(ATTRIBUTE_KEYS, widget.getWidgetOptions());
	}
}
