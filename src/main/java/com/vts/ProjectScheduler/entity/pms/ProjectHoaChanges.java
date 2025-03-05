package com.vts.ProjectScheduler.entity.pms;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name="project_hoa_changes")
@Data
public class ProjectHoaChanges {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long ChangesId;
	private String ProjectCode;
	private Long ProjectId;
	private Long TodayChanges;
	private Long WeeklyChanges;
	private Long MonthlyChanges;
	private String CreatedBy;
	private LocalDateTime CreatedDate;
	private int IsActive;
}
