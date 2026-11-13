package com.epam.reportportal.base.infrastructure.persistence.dao.widget;

import java.util.function.BiFunction;
import org.jooq.Record;
import org.jooq.Select;

/**
 * Maps a widget query and parameters to a typed content result.
 *
 * @param <U> request or filter type passed to the content loader
 * @param <R> widget content DTO
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public interface WidgetContentProvider<U, R> extends BiFunction<Select<? extends Record>, U, R> {

}
