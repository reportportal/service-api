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

/**
 * By reason of demo data generation is used not so often,
 * we don't need to cache the files' content.
 *
 * @author Pavel_Bortnik
 */
public final class ContentUtils {

    private ContentUtils() {
        //static only
    }

    private static SplittableRandom random = new SplittableRandom();

    static Set<String> getTagsInRange(int limit) {
        List<String> content = readContent("demo/content/tags.txt");
        int fromIndex = random.nextInt(content.size() - limit);
        return ImmutableSet.<String>builder()
                .addAll(content.subList(fromIndex, fromIndex + limit)).build();
    }

    static String getSuiteDescription() {
        List<String> content = readContent("demo/content/suite-description.txt");
        return content.get(random.nextInt(content.size()));
    }

    static String getStepDescription() {
        List<String> content = readContent("demo/content/step-description.txt");
        return content.get(random.nextInt(content.size()));
    }

    static String getTestDescription() {
        List<String> content = readContent("demo/content/test-description.txt");
        return content.get(random.nextInt(content.size()));
    }

    static String getLaunchDescription() {
        List<String> list = readContent("demo/content/description.txt");
        return list.get(0);
    }

    private static List<String> readContent(String resource) {
        List<String> content;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new ClassPathResource(resource).getInputStream()))){
            content = reader.lines().collect(Collectors.toList());
        }catch (IOException e) {
            throw new ReportPortalException("Missing demo content.", e);
        }
        return content;
    }
}
