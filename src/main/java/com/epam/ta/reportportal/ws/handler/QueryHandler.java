package com.epam.ta.reportportal.ws.handler;

import com.epam.ta.reportportal.ws.rabbit.QueryRQ;

public interface QueryHandler {

	Object find(QueryRQ queryRQ);
}
