package com.vts.ProjectScheduler.entity.sis;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.cglib.core.Local;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "sis_mail_track_iInsights")
public class SisMailTrackInsights {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long MailTrackInsightsId;
    private Long MailTrackingId;
    private Long EmpId;
    private String MailStatus;
    private LocalDateTime MailSentDate;
    private LocalDateTime CreatedDate;
}
