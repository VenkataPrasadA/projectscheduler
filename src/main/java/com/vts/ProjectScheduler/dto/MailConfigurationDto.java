package com.vts.ProjectScheduler.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
//@NoArgsConstructor
public class MailConfigurationDto {

	private long mailConfigurationId;
	private String typeOfHost;
	private String host;
	private String port;
	private String username;
	private String password;
	private String createdBy;
	private String createdDate;




}
