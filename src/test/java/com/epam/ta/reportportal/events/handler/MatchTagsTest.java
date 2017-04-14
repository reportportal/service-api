/*
 * Copyright 2016 EPAM Systems
 *
 *
 * This file is part of EPAM Report Portal.
 * https://github.com/reportportal/service-api
 *
 * Report Portal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Report Portal is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Report Portal.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.epam.ta.reportportal.events.handler;

import com.epam.ta.reportportal.database.entity.Launch;
import com.epam.ta.reportportal.database.entity.project.email.EmailSenderCase;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashSet;

import static com.epam.ta.reportportal.events.handler.LaunchFinishedEventHandler.isTagsMatched;
import static java.util.Arrays.asList;
import static java.util.Collections.*;

public class MatchTagsTest {

	@Test
	public void nullTagsInRule() {
		boolean matched = isTagsMatched(null, new EmailSenderCase(null, "ALWAYS", null, null));
		Assert.assertTrue(matched);
	}

	@Test
	public void emptyTagsInRule() {
		boolean matched = isTagsMatched(null, new EmailSenderCase(null, "ALWAYS", null, emptyList()));
		Assert.assertTrue(matched);
	}

	@Test
	public void launchNullTags() {
		boolean matched = isTagsMatched(new Launch(), new EmailSenderCase(null, "ALWAYS", null, singletonList("tag")));
		Assert.assertFalse(matched);
	}

	@Test
	public void equalTags() {
		Launch launch = new Launch();
		launch.setTags(singleton("tag"));
		boolean matched = isTagsMatched(launch, new EmailSenderCase(null, "ALWAYS", null, singletonList("tag")));
		Assert.assertTrue(matched);
	}

	@Test
	public void launchTagsSmallerSubsetOfSendCaseTags() {
		Launch launch = new Launch();
		launch.setTags(new HashSet<>(asList("tag1", "tag2")));
		boolean isTagsMatched = isTagsMatched(launch, new EmailSenderCase(null, "ALWAYS", null, asList("tag1", "tag2", "tag3")));
		Assert.assertFalse(isTagsMatched);
	}
}