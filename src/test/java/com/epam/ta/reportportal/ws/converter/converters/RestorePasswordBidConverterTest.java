package com.epam.ta.reportportal.ws.converter.converters;

import com.epam.ta.reportportal.entity.user.RestorePasswordBid;
import com.epam.ta.reportportal.ws.model.user.RestorePasswordRQ;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
class RestorePasswordBidConverterTest {

	@Test
	void toBid() {
		final RestorePasswordRQ request = new RestorePasswordRQ();
		request.setEmail("email@example.com");

		final RestorePasswordBid bid = RestorePasswordBidConverter.TO_BID.apply(request);

		assertEquals(bid.getEmail(), request.getEmail());
		assertNotNull(bid.getUuid());
	}
}