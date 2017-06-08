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

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author Pavel_Bortnik
 */
public class ContentUtils {

    private String launchDescription;

    private List<String> tags;

    private List<String> suiteLines;

    private List<String> testLines;

    private List<String> stepLines;

    private SplittableRandom random;

    public void initContent() {
        try (BufferedReader descr = new BufferedReader(new InputStreamReader(new ClassPathResource("demo/content/description.txt").getInputStream(), UTF_8));
             BufferedReader suite = new BufferedReader(new InputStreamReader(new ClassPathResource("demo/content/suite-description.txt").getInputStream(), UTF_8));
             BufferedReader test = new BufferedReader(new InputStreamReader(new ClassPathResource("demo/content/test-description.txt").getInputStream(), UTF_8));
             BufferedReader step = new BufferedReader(new InputStreamReader(new ClassPathResource("demo/content/step-description.txt").getInputStream(), UTF_8));
             BufferedReader tags = new BufferedReader(new InputStreamReader(new ClassPathResource("demo/content/tags.txt").getInputStream(), UTF_8))
        ) {
            launchDescription = descr.lines().collect(Collectors.joining("\n"));
            suiteLines = suite.lines().collect(Collectors.toList());
            testLines = test.lines().collect(Collectors.toList());
            stepLines = step.lines().collect(Collectors.toList());
            this.tags = tags.lines().collect(Collectors.toList());
        } catch (IOException e) {
            throw new ReportPortalException("Missing demo content attachments.", e);
        }
        random = new SplittableRandom();
    }

    Set<String> getTagsInRange(int limit) {
        int fromIndex = random.nextInt(tags.size() - limit);
        return ImmutableSet.<String>builder()
                .addAll(tags.subList(fromIndex, fromIndex + limit)).build();
    }

    String getSuiteDescription() {
        return suiteLines.get(random.nextInt(suiteLines.size()));
    }

    String getStepDescription() {
        return stepLines.get(random.nextInt(stepLines.size()));
    }

    String getTestDescription() {
        return testLines.get(random.nextInt(testLines.size()));
    }

    String getLaunchDescription() {
        return launchDescription;
    }
}
