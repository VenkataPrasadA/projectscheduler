package com.vts.ProjectScheduler.entity.pms;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;


@Entity
@Data
@Table(name = "pfms_mail_track_insights")
public class PfmsMailTrackingInsights {

	@Id
	@GeneratedValue(strategy= GenerationType.IDENTITY)
	private Long MailTrackingInsightsId;
	private Long MailTrackingId;
    private Long EmpId;
	private String ActionNos;
	private String MailPurpose;
	private String MailStatus;
	private LocalDateTime MailSentDate;
	private LocalDateTime CreatedDate;
}
