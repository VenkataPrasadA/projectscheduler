package com.vts.ProjectScheduler.entity.ibas;

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
@Table(name="pfts_mail_track")
public class PftsMailTrack {
	   @Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long PftsMailTrackingId;     
    private LocalDateTime CreatedDate;   
    private String CreatedTime;    
    private Long MailExpectedCount;   
    private Long MailSentCount;   
    private LocalDateTime MailSentDateTime;   
    private String MailSentStatus;
    private String TrackingType;
    
}
