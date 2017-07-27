package com.epam.ta.reportportal.util.analyzer;

import com.epam.ta.reportportal.util.analyzer.model.IndexLaunch;
import com.epam.ta.reportportal.util.analyzer.model.IndexRs;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

/**
 * Tests for {@link AnalyzerServiceClient}
 *
 * @author Ivan Sharamet
 *
 */
public class AnalyzerServiceClientTest {

    private static final String SERVICE_URL = "http://analyzer";

    private RestTemplate restTemplate;
    private AnalyzerServiceClient client;

    @Before
    public void setup() {
        restTemplate = Mockito.mock(RestTemplate.class);
        client = new AnalyzerServiceClient(restTemplate, SERVICE_URL);
    }

    @Test
    public void testIndex() {
        IndexRs expectedRs = new IndexRs();
        responseWith("/_index", expectedRs);
        IndexRs actualRs = client.index(Collections.singletonList(new IndexLaunch()));
        Assert.assertSame(expectedRs, actualRs);
    }

    @Test
    public void testAnalyze() {
        IndexLaunch expectedRs = new IndexLaunch();
        responseWith("/_analyze", new IndexLaunch[] {expectedRs});
        IndexLaunch actualRs = client.analyze(new IndexLaunch());
        Assert.assertSame(expectedRs, actualRs);
    }

    @Test
    public void testAnalyzeWithEmptyRs() {
        responseWith("/_analyze", new IndexLaunch[] {});
        IndexLaunch rs = client.analyze(new IndexLaunch());
        Assert.assertNull(rs);
    }

    @SuppressWarnings("unchecked")
    private void responseWith(String path, Object rs) {
        Mockito.when(
                restTemplate.postForEntity(
                        Mockito.eq(SERVICE_URL + path),
                        Mockito.anyListOf(IndexLaunch.class),
                        Mockito.any()))
                .thenReturn(new ResponseEntity(rs, HttpStatus.OK));
    }
}
