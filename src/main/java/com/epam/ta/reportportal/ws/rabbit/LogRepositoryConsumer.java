package com.epam.ta.reportportal.ws.rabbit;

import com.epam.ta.reportportal.core.configs.RabbitMqConfiguration;
import com.epam.ta.reportportal.store.database.entity.log.Log;
import com.epam.ta.reportportal.ws.handler.LogFindByTestItemIdHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.epam.ta.reportportal.ws.rabbit.MessageHeaders.*;

@Component
public class LogRepositoryConsumer {

    @Autowired
    private LogFindByTestItemIdHandler logFindByTestItemIdHandler;

    @RabbitListener(queues = RabbitMqConfiguration.LOGS_FIND_BY_TEST_ITEM_ID_QUEUE)
    public List<Log> findByTestItemId(@Header(LOG_ITEM_ID) String itemId, @Header(LIMIT)Integer limit,
                                @Header(IS_LOAD_BINARY_DATA)Boolean isLoadBinaryData) {

        return logFindByTestItemIdHandler.findByTestItemRef(itemId, limit, isLoadBinaryData);
    }
}
