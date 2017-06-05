package com.epam.ta.reportportal.ws.converter.converters;

import com.epam.ta.reportportal.database.entity.user.RestorePasswordBid;
import com.epam.ta.reportportal.ws.model.user.RestorePasswordRQ;

import java.util.UUID;
import java.util.function.Function;

public final class RestorePasswordBidConverter {

    private RestorePasswordBidConverter() {
        //static only
    }

    public static final Function<RestorePasswordRQ, RestorePasswordBid> TO_BID = request -> {
        RestorePasswordBid bid = new RestorePasswordBid();
        bid.setEmail(request.getEmail());
        bid.setId(UUID.randomUUID().toString());
        return bid;
    };
}
