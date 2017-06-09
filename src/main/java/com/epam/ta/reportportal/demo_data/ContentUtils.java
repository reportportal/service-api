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

package com.epam.ta.reportportal.demo_data;

import com.epam.ta.reportportal.exception.ReportPortalException;
import com.google.common.collect.ImmutableSet;
import org.springframework.core.io.ClassPathResource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Set;
import java.util.SplittableRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * By reason of demo data generation is used not so often,
 * we don't need to cache the files' content.
 *
 * @author Pavel_Bortnik
 */
public final class ContentUtils {

    private static final int ERRORS_COUNT = 8;

    private ContentUtils() {
        //static only
    }

    private static SplittableRandom random = new SplittableRandom();

    static Set<String> getTagsInRange(int limit) {
        List<String> content = readToList("demo/content/tags.txt");
        int fromIndex = random.nextInt(content.size() - limit);
        return ImmutableSet.<String>builder()
                .addAll(content.subList(fromIndex, fromIndex + limit)).build();
    }

    static String getSuiteDescription() {
        List<String> content = readToList("demo/content/suite-description.txt");
        return content.get(random.nextInt(content.size()));
    }

    static String getStepDescription() {
        List<String> content = readToList("demo/content/step-description.txt");
        return content.get(random.nextInt(content.size()));
    }

    static String getTestDescription() {
        List<String> content = readToList("demo/content/test-description.txt");
        return content.get(random.nextInt(content.size()));
    }

    static String getLaunchDescription() {
        return readToString("demo/content/description.txt");
    }

    private static List<String> readToList(String resource) {
        List<String> content;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new ClassPathResource(resource).getInputStream(), UTF_8))){
            content = reader.lines().collect(Collectors.toList());
        }catch (IOException e) {
            throw new ReportPortalException("Missing demo content.", e);
        }
        return content;
    }

    private static String readToString(String resource) {
        String content;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new ClassPathResource(resource).getInputStream(), UTF_8))){
            content = reader.lines().collect(Collectors.joining("\n"));
        }catch (IOException e) {
            throw new ReportPortalException("Missing demo content.", e);
        }
        return content;
    }

    static List<String> getErrorLogs(int bound) {
        return IntStream.range(0, bound).mapToObj(i -> {
            int errorNumber = random.nextInt(ERRORS_COUNT) + 1;
            return readToString("demo/errors/" + errorNumber + ".txt");
        }).collect(Collectors.toList());
    }

    static String getLogMessage() {
        List<String> logs = readToList("demo/content/demo_logs.txt");
        return logs.get(random.nextInt(logs.size()));
    }
}
