package com.epam.ta.reportportal.core.item.merge;

import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.epam.ta.reportportal.ws.model.item.MergeTestItemRQ;

public interface MergeTestItemHandler {
    /**
     * Merge test items specified in rq to item
     *
     * @param projectName project name
     * @param item        test item ID
     * @param rq          merge test item request data. Contains list of items we want to merge
     * @param userName    request principal name
     * @return OperationCompletionRS
     */
    OperationCompletionRS mergeTestItem(String projectName, String item, MergeTestItemRQ rq, String userName);
}
