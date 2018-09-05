package com.epam.ta.reportportal.core.log;

import com.google.common.base.Stopwatch;
import org.apache.commons.lang.time.DurationFormatUtils;

import java.util.concurrent.TimeUnit;

public class Demo {

	public static void main(String[] args) throws InterruptedException {
		Stopwatch started = Stopwatch.createStarted();
		Thread.sleep(1500);

		System.out.println(started.toString());
	}
}
