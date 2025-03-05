package com.vts.ProjectScheduler.entity.sis;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.boot.autoconfigure.web.WebProperties;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "sis_sir")
public class SisSir {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long SIRId;
    private Long SlNo;
    private String SIRNo;
    private LocalDate SIRDate;
    private String SONo;
    private LocalDate SODate;
    private String Description;
    private Long RecievedQty;
    private String ItemUnit;
    private String ConsignorName;
    private Long DivisionId;
    private Long ProjectId;
    private String FinYear;
    private String InVoiceNo;

    private LocalDate InVoiceDate;
    private String RinNo;
    private LocalDate RinDate;
    private String CrvNo;
    private LocalDate CrvDate;
    private String IdivNo;
    private LocalDate IdivDate;
    private String CreatedBy;
    private LocalDateTime CreatedDate;
    private String ModifiedBy;
    private LocalDateTime ModifiedDate;
    private int isActive;

}
