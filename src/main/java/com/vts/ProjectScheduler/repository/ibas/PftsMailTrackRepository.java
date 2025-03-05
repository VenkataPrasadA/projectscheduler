package com.vts.ProjectScheduler.repository.ibas;

import java.util.List;

import com.vts.ProjectScheduler.entity.ibas.PftsMailTrack;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.vts.ProjectScheduler.entity.ibas.PftsMailTrack;

@Repository
public interface PftsMailTrackRepository extends JpaRepository<PftsMailTrack, Long> {

	@Query(value = "CALL Pfts_MailDailySummaryList()", nativeQuery = true)
	public List<Object[]> pftsExpectedDailySummaryList();

	@Query(value = "SELECT COUNT(*) FROM pfts_mail_track WHERE CreatedDate = CURDATE() AND TrackingType=:trackingType", nativeQuery = true)
	public long getPftsMailInitiatedCount(@Param("trackingType") String trackingType) throws Exception;

	@Query(value = "SELECT DISTINCT a.ForwardedTo,e.MobileNo,e.Email,e.DronaEmail FROM pfts_filetracking a,employee e WHERE a.ForwardedTo=e.EmpNo AND a.StatusId='1'", nativeQuery = true)
	public List<Object[]> pftsDailySendEmployees() throws Exception;

	@Query(value = "SELECT a.DemandNo,a.ForwardedTo,b.EventName,a.EventDate,e.MobileNo,e.EMail,e.DronaEMail,d.FileStageId,f.ItemFor,ft.EmpName,fd.Designation FROM pfts_filetracking a ,pfts_fileevents b,employee e,pfts_filestage d,pfts_demand f,employee ft,employee_desig fd WHERE a.EventId=b.FileEventId AND a.ForwardedTo=e.EmpNo AND a.ForwardedTo=:forwardedTo AND b.FileStageId=d.FileStageId AND a.DemandNo=f.DemandNo AND a.ForwardedBy=ft.EmpNo AND ft.DesigId=fd.DesigId AND a.StatusId='1'", nativeQuery = true)
	public List<Object[]> pftsDailySendEmployeesDetails(@Param("forwardedTo") String forwardedTo) throws Exception;
}
