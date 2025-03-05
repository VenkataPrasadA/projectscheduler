package com.vts.ProjectScheduler.entity.pms;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Where;

import java.time.LocalDateTime;


@Entity
@Table(name = "sms_sub_category")
@Data
@Where(clause = "isActive =1")
public class SubCategory {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long subCategoryId;
	private String subCategoryName;
	private String subCategoryDescription;
	private String createdBy;
	private LocalDateTime createdDate;
	private String modifiedBy;
	private LocalDateTime modifiedDate;
	private int isActive;
}
