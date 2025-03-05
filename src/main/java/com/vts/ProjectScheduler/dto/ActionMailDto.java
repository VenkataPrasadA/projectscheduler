package com.vts.ProjectScheduler.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ActionMailDto {

	private String empid;
	private String Assignor;
	private String progress;
	private String email;
	private String actionNo;
}
