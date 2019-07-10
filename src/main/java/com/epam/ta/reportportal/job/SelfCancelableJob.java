/*
 * Copyright 2018 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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