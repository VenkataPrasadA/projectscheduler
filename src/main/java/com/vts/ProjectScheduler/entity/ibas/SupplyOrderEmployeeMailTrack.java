package com.vts.ProjectScheduler.entity.ibas;


import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity(name ="supplyorder_employee_mail")
public class SupplyOrderEmployeeMailTrack {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long EmpMailId;
	private long MailId;
	private long EmpId;
	private String FilePath;
	private String SoNos;
	private String MailStatus;
	private String SmsStatus;
	private LocalDateTime CreatedDate;
	
}
