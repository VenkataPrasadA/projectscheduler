package com.vts.ProjectScheduler.entity.qms;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name="qms_irf_mail_track")
public class IrfMailTrack {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long MailTrackingId;               
    private LocalDateTime CreatedDate;   
    private String CreatedTime;    
    private Long MailExpectedCount;   
    private Long MailSentCount;   
    private LocalDateTime MailSentDateTime;   
    private String MailSentStatus;   
}
