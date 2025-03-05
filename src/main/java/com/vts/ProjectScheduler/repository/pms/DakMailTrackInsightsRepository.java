package com.vts.ProjectScheduler.repository.pms;

import com.vts.ProjectScheduler.entity.pms.DakMailTrackingInsights;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.vts.ProjectScheduler.entity.pms.DakMailTrackingInsights;

import jakarta.transaction.Transactional;
@Transactional
public interface DakMailTrackInsightsRepository extends JpaRepository<DakMailTrackingInsights, Long>{

	@Modifying
	@Query(value="UPDATE dak_mail_track_insights SET MailStatus=:mailStatus,MailSentDate= CURRENT_TIMESTAMP WHERE MailPurpose=:mailPurpose AND EmpId=:empId AND MailTrackingId=:mailTrackingId ",nativeQuery = true)
	public int updateParticularEmpMailStatus(@Param("mailPurpose") String mailPurpose,@Param("mailStatus") String mailStatus,@Param("empId") long empId,@Param("mailTrackingId") long mailTrackingId);

}
