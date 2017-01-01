package com.epam.ta.reportportal.ws.rabbit;

/**
 * @author Konstantin Antipin
 */
public enum RequestType {
    START_LAUNCH, FINISH_LAUNCH, START_TEST, FINISH_TEST, LOG;

    public static RequestType fromName(String name) {
        for (RequestType requestType : values()) {
            if (requestType.name().equals(name)) {
                return requestType;
            }
        }
        throw new IllegalArgumentException("Illegal request type name " + name);
    }
}
