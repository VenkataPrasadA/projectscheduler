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
@Table(name="qms_irf_mail_track_insights")
public class IrfMailTrackInsights {

		@Id
		@GeneratedValue(strategy = GenerationType.IDENTITY)
		private Long MailTrackingInsightsId;
	    private Long MailTrackingId;   
	    private Long EmpId; 
	    private String Message;   
	    private String MailPurpose;    
	    private String MailStatus;   
	    private LocalDateTime MailSentDate;   
	    private LocalDateTime CreatedDate; 
}
