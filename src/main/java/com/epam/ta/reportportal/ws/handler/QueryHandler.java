package com.epam.ta.reportportal.ws.handler;

import com.epam.ta.reportportal.ws.rabbit.QueryRQ;

/**
 * @author Yauheni_Martynau
 */
public interface QueryHandler {

	Object find(QueryRQ queryRQ);
}
