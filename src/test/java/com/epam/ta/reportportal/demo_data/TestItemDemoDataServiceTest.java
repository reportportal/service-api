package com.epam.ta.reportportal.demo_data;

import com.epam.ta.reportportal.database.entity.item.TestItemType;
import org.junit.Test;

import static com.epam.ta.reportportal.database.entity.item.TestItemType.*;
import static com.epam.ta.reportportal.database.entity.item.TestItemType.AFTER_METHOD;
import static org.junit.Assert.*;

public class TestItemDemoDataServiceTest {

	@Test
	public void test() {
		System.out.println(checkHashChild(TEST));
	}

	public boolean checkHashChild(TestItemType testItemType) {
		return !(testItemType == STEP || testItemType == BEFORE_CLASS || testItemType == BEFORE_METHOD || testItemType == AFTER_CLASS
				|| testItemType == AFTER_METHOD);
	}
}