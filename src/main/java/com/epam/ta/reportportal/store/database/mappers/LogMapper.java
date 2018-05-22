package com.epam.ta.reportportal.store.database.mappers;

import com.epam.ta.reportportal.store.database.entity.log.Log;
import org.jooq.Record;

import static com.epam.ta.reportportal.store.commons.EntityUtils.TO_LOCAL_DATE_TIME;
import static com.epam.ta.reportportal.store.jooq.Tables.LOG;

public class LogMapper {

	public LogMapper() {
		//static only
	}

	public static Log getLog(Record record) {
		Log log = new Log();
		log.setId(record.getValue(LOG.ID));
		log.setLastModified(TO_LOCAL_DATE_TIME.apply(record.getValue(LOG.LAST_MODIFIED)));
		log.setLogLevel(record.getValue(LOG.LOG_LEVEL));
		log.setLogMessage(record.getValue(LOG.LOG_MESSAGE));
		log.setLogTime(TO_LOCAL_DATE_TIME.apply(record.getValue(LOG.LOG_TIME)));
		// TODO: 21-May-18 : Should we initialize it?
		//            log.setTestItem(record.getValue(LOG.ITEM_ID));

		return log;
	}
}
