package com.vts.ProjectScheduler.entity.ibas;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity(name ="supplyorder_mail")
public class SupplyOrderMailTrack {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long MailId;
	private LocalDate MailDate;
	private long MailExpectedCount;
	private long MailSentCount;
	private long SmsSentCount;
	private LocalDateTime MailSentDateTime;
	private LocalDateTime CreatedDate;
	
}
