package com.vts.ProjectScheduler.entity.sis;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "sis_mail_track")
public class SisMailTrack {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long MailTrackingId;
    private Long MailExpectedCount;
    private Long MailSentCount;
    private LocalDateTime MailSentDateTime;
    private String TrackingType;
    private LocalDateTime CreatedDate;
    private String CreatedTime;
}
