package com.vts.ProjectScheduler.repository.pms;

import com.vts.ProjectScheduler.entity.pms.PFMSCCMData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.vts.ProjectScheduler.entity.pms.PFMSCCMData;

import jakarta.transaction.Transactional;

@Transactional
public interface CcmViewRepository extends JpaRepository<PFMSCCMData, Long>{

	
	@Modifying
	@Query(value="DELETE FROM pfms_ccm_data where LabCode=:labCode",nativeQuery = true)
	public int cCMDataDelete(@Param("labCode") String labCode);
}
