package com.epam.ta.reportportal.ws.converter.converters;

import com.epam.ta.reportportal.commons.EntityUtils;
import com.epam.ta.reportportal.database.entity.user.UserCreationBid;
import com.epam.ta.reportportal.ws.model.user.CreateUserRQ;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

public final class UserCreationBidConverter {

    private UserCreationBidConverter() {
        //static only
    }

    public static final Function<CreateUserRQ, UserCreationBid> TO_USER = request -> {
        UserCreationBid user = new UserCreationBid();
        if (Optional.ofNullable(request).isPresent()) {
            user.setId(UUID.randomUUID().toString());
            user.setEmail(EntityUtils.normalizeId(request.getEmail().trim()));
            user.setDefaultProject(EntityUtils.normalizeId(request.getDefaultProject()));
            user.setRole(request.getRole());
        }
        return user;
    };
}
