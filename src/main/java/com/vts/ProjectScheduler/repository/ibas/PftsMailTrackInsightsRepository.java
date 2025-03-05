package com.vts.ProjectScheduler.repository.ibas;

import com.vts.ProjectScheduler.entity.ibas.PftsMailTrackInsights;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.vts.ProjectScheduler.entity.ibas.PftsMailTrackInsights;

import jakarta.transaction.Transactional;

@Transactional
@Repository
public interface PftsMailTrackInsightsRepository extends JpaRepository<PftsMailTrackInsights, Long>{

	@Modifying
	@Query(value="UPDATE pfts_mail_track_insights SET MailStatus=:mailStatus,MailSentDate= CURRENT_TIMESTAMP WHERE MailPurpose=:mailPurpose AND EmpNo=:empNo AND PftsMailTrackingId=:pftsMailTrackingId ",nativeQuery = true)
	public int updateParticularEmpMailStatusInPfts(@Param("mailPurpose") String mailPurpose,@Param("mailStatus") String mailStatus,@Param("empNo") String empNo,@Param("pftsMailTrackingId") long pftsMailTrackingId);

}
