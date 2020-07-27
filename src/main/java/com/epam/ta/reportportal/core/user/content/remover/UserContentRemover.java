package com.epam.ta.reportportal.core.user.content.remover;

import com.epam.ta.reportportal.entity.user.User;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public interface UserContentRemover {

	void removeContent(User user);
}
