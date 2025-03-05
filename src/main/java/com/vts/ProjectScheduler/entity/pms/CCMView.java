package com.vts.ProjectScheduler.entity.pms;

import java.io.Serializable;
import java.math.BigDecimal;

import com.vts.ProjectScheduler.entity.pms.CCMView;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CCMView implements Serializable{

	private static final long serialVersionUID = 1L;
	private long CCMReportId; 
	private long ProjectId;
	private String ProjectCode;
	private long BudgetHeadId;
	private String BudgetHeadDescription;
	private BigDecimal AllotmentCost;
	private BigDecimal Expenditure;
	private BigDecimal Balance;
	private BigDecimal Q1CashOutGo;
	private BigDecimal Q2CashOutGo;
	private BigDecimal Q3CashOutGo;
	private BigDecimal Q4CashOutGo;
	private BigDecimal Required;
	private String CreatedDate;
}
