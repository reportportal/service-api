package com.epam.ta.reportportal.core.tms.db.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "tms_environment", schema = "public")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Milestone {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    @Column(name = "name")
    private String name;
    
    @Column(name = "type")
    private String type;
    
    @Column(name = "start_date", columnDefinition = "TIMESTAMP")
    private LocalDateTime startDate;
    
    @Column(name = "end_date", columnDefinition = "TIMESTAMP")
    private LocalDateTime endDate;
    
    @ManyToOne
    @JoinColumn(name = "product_version_id", nullable = false)
    private ProductVersion productVersion;
    
    @ManyToOne
    @JoinColumn(name = "test_plan_id")
    private TestPlan testPlan;
}
