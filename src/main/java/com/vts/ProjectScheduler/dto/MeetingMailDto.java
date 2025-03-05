package com.vts.ProjectScheduler.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MeetingMailDto {

	private String empid;
	private String empname;
	private String scheduleid;
	private String email;
	private String DronaEmail;
	private String projectid;
	private String InitiationId;
	private String CommitteeShortName;
	private String CommitteeName;
	private String meetingTime;
	private String MeetingVenue;
	private String projectCode;
	private String projectname;
}
