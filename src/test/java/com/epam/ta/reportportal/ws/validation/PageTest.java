package com.epam.ta.reportportal.ws.validation;

import com.epam.ta.reportportal.model.Page;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link Page.PageMetadata}
 * Initially grabbed from Spring's <a href="https://github.com/spring-projects/spring-hateoas/blob/642b6736764068ffeee38379629db5fc15cf927d/src/test/java/org/springframework/hateoas/PagedResourcesUnitTest.java">PagedResourcesUnitTest.java</a>
 *
 * @author Andrei Varabyeu
 */
public class PageTest {

    private static final Page.PageMetadata METADATA = new Page.PageMetadata(10, 1, 200);

    @Test
    public void preventsNegativePageSize() {
        assertThrows(IllegalArgumentException.class, () -> new Page.PageMetadata(-1, 0, 0));
    }

    @Test
    public void preventsNegativePageNumber() {
        assertThrows(IllegalArgumentException.class, () -> new Page.PageMetadata(0, -1, 0));
    }

    @Test
    public void preventsNegativeTotalElements() {
        assertThrows(IllegalArgumentException.class, () -> new Page.PageMetadata(0, 0, -1));
    }

    @Test
    public void preventsNegativeTotalPages() {
        assertThrows(IllegalArgumentException.class, () -> new Page.PageMetadata(0, 0, 0, -1));
    }

    @Test
    public void allowsOneIndexedPages() {
        assertDoesNotThrow(() -> new Page.PageMetadata(10, 1, 0));
    }

    @Test
    public void calculatesTotalPagesCorrectly() {
        assertEquals(4L, new Page.PageMetadata(5, 0, 16).getTotalPages());
    }
}