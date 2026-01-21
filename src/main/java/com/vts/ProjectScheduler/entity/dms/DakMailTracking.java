package com.vts.ProjectScheduler.entity.dms;

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
@Table(name="dak_mail_track")
public class DakMailTracking {
	@Id
	@GeneratedValue(strategy= GenerationType.IDENTITY)
	private Long MailTrackingId;
	private String TrackingType;
	private Long MailExpectedCount;
	private Long MailSentCount;
	private String MailSentStatus;
	private LocalDateTime CreatedDate;
	private String CreatedTime;
	private LocalDateTime MailSentDateTime;
}
