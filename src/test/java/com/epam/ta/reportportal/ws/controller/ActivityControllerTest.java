package com.epam.ta.reportportal.ws.controller;

import com.epam.ta.reportportal.ws.BaseMvcTest;
import org.junit.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
public class ActivityControllerTest extends BaseMvcTest {

	@Test
	public void getActivitiesByWrongTestItemId() throws Exception {
		mockMvc.perform(get("/default_personal/activity/1111")).andDo(print());
	}
}