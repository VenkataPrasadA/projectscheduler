package com.vts.ProjectScheduler.entity.ibas;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
@Entity
@Table(name="pfts_mail_track_insights")
public class PftsMailTrackInsights {
	

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long PftsMailTrackingInsightsId;
	private Long PftsMailTrackingId;
	private String EmpNo;  
	private String DemandNo;  
	private String MailPurpose;
	private String MailStatus;
	private LocalDateTime CreatedDate;
	private LocalDateTime MailSentDate;
	
	
	
	
	
	
}
