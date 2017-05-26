package com.epam.ta.reportportal.core.imprt.format.async;

import java.util.Arrays;

public enum JunitReportTag {
    // the testsuites element for the aggregate document
    TESTSUITES("testsuites"),

    // the testsuite element
    TESTSUITE("testsuite"),

    // the testcase element
    TESTCASE("testcase"),

    // the error element
    ERROR("error"),

    // the failure element
    FAILURE("failure"),

    // the system-err element
    SYSTEM_ERR("system-err"),

    // the system-out element
    SYSTEM_OUT("system-out"),

    // name attribute for property, testcase and testsuite elements
    ATTR_NAME("name"),

    // time attribute for testcase and testsuite elements
    ATTR_TIME("time"),

    SKIPPED("skipped"),

    // type attribute for failure and error elements
    ATTR_TYPE("type"),

    // message attribute for failure elements
    ATTR_MESSAGE("message"),

    // the properties element
    PROPERTIES("properties"),

    // the property element
    PROPERTY("property"),

    // value attribute for property elements
    ATTR_VALUE("value"),

    // timestamp of test cases
    TIMESTAMP("timestamp");

    private String value;

    JunitReportTag(String value) {
        this.value = value;
    }

    public String getValue(){
        return value;
    }

    static JunitReportTag fromString(String type) {
        return Arrays.stream(values()).filter(it -> it.getValue().equalsIgnoreCase(type)).findAny().orElse(null);
    }
}
