package com.epam.ta.reportportal.ws.converter.converters;

import com.epam.ta.reportportal.entity.ShareableEntity;
import com.epam.ta.reportportal.ws.model.SharedEntity;

import java.util.function.Function;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public final class SharedEntityConverter {

	private SharedEntityConverter() {
		//static only
	}

	public static final Function<? super ShareableEntity, SharedEntity> TO_SHARED_ENTITY = shareable -> {
		SharedEntity sharedEntity = new SharedEntity();
		sharedEntity.setId(String.valueOf(shareable.getId()));
		sharedEntity.setOwner(shareable.getOwner());
		return sharedEntity;
	};
}
