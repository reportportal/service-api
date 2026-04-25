/*
 * Copyright 2025 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.reportportal.base.core.tms.mapper.processor;

import com.epam.reportportal.base.core.events.domain.tms.TmsTestCaseHistoryOfActionsField;
import com.epam.reportportal.base.core.events.domain.tms.TestCaseFieldChangedEvent;
import com.epam.reportportal.base.infrastructure.persistence.entity.activity.ActivityAction;
import com.epam.reportportal.base.infrastructure.persistence.entity.activity.EventAction;
import com.epam.reportportal.base.model.activity.TestCaseActivityResource;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

public class TmsTmsTestCaseFieldProcessorImpl implements TmsTestCaseFieldProcessor {

    private final TmsTestCaseHistoryOfActionsField field;
    private final Function<TestCaseActivityResource, Object> valueExtractor;
    private final ActivityAction createAction;
    private final ActivityAction updateAction;
    private final ActivityAction deleteAction;

    public TmsTmsTestCaseFieldProcessorImpl(
            TmsTestCaseHistoryOfActionsField field,
            Function<TestCaseActivityResource, Object> valueExtractor,
            ActivityAction createAction,
            ActivityAction updateAction,
            ActivityAction deleteAction) {
        this.field = field;
        this.valueExtractor = valueExtractor;
        this.createAction = createAction;
        this.updateAction = updateAction;
        this.deleteAction = deleteAction;
    }

    @Override
    public Optional<TestCaseFieldChangedEvent> process(TestCaseActivityResource before, TestCaseActivityResource after) {
        Object beforeValue = valueExtractor.apply(before);
        Object afterValue = valueExtractor.apply(after);

        if (Objects.equals(beforeValue, afterValue)) {
            return Optional.empty();
        }

        boolean isBeforeEmpty = isValueEmpty(beforeValue);
        boolean isAfterEmpty = isValueEmpty(afterValue);

        EventAction eventAction;
        ActivityAction activityAction;

        if (isBeforeEmpty && !isAfterEmpty) {
            eventAction = EventAction.CREATE;
            activityAction = createAction != null ? createAction : updateAction;
        } else if (!isBeforeEmpty && isAfterEmpty) {
            eventAction = EventAction.DELETE;
            activityAction = deleteAction != null ? deleteAction : updateAction;
        } else {
            eventAction = EventAction.UPDATE;
            activityAction = updateAction;
        }

        if (activityAction == null) {
            return Optional.empty();
        }

        return Optional.of(new TestCaseFieldChangedEvent(
                after,
                field.getValue(),
                eventAction,
                activityAction,
                beforeValue,
                afterValue,
                null, null, null
        ));
    }

    private boolean isValueEmpty(Object value) {
        if (value == null) {
            return true;
        }
        if (value instanceof String str) {
            return str.isEmpty();
        }
        if (value instanceof java.util.Collection<?> col) {
            return col.isEmpty();
        }
        return false;
    }
}
