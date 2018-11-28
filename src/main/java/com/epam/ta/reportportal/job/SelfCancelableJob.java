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

package com.epam.ta.reportportal.job;

import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.TriggerContext;

import java.util.Date;

/**
 * Job contains run method and trigger. The main idea to provide possibility to
 * cancel next execution from run method. User can override job, call
 * {@link SelfCancelableJob#oneMoreTime(boolean)} method and this way cancel
 * next execution. In case if we need job to keep executed, we do not call
 * anything, next execution time calculation delegated to provided trigger
 *
 * @author Andrei Varabyeu
 */
public abstract class SelfCancelableJob implements Runnable, Trigger {

	private Trigger triggerDelegate;

	private boolean oneMoreTime = true;

	public SelfCancelableJob(Trigger trigger) {
		this.triggerDelegate = trigger;
	}

	@Override
	public Date nextExecutionTime(TriggerContext triggerContext) {
		if (oneMoreTime) {
			return triggerDelegate.nextExecutionTime(triggerContext);
		} else {
			return null;
		}
	}

	protected void oneMoreTime(boolean oneMoreTime) {
		this.oneMoreTime = oneMoreTime;
	}

}