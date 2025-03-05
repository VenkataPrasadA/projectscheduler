package com.vts.ProjectScheduler.entity.ibas;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity(name ="supplyorder_employee_mail_details")
public class SupplyOrderEmployeeMailTrackDetails {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long MailDetailsId;
	private long EmpMailId;
	private String SoNo;
	private LocalDateTime CreatedDate;
	
}
