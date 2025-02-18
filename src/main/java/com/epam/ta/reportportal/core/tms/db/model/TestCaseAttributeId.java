package com.epam.ta.reportportal.core.tms.db.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TestCaseAttributeId implements Serializable {
    
    @Column(name = "test_case_id")
    private Long testCaseId;
    
    @Column(name = "attribute_id")
    private Long attributeId;
    
    //TODO: override equals and hashCode methods


    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        TestCaseAttributeId that = (TestCaseAttributeId) o;
        return Objects.equals(testCaseId, that.testCaseId) && Objects.equals(attributeId, that.attributeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(testCaseId, attributeId);
    }
}
