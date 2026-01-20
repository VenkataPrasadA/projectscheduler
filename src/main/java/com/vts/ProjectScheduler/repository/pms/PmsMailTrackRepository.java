package com.vts.ProjectScheduler.repository.pms;

import java.util.List;

import com.vts.ProjectScheduler.entity.pms.PfmsMailTracking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.vts.ProjectScheduler.entity.pms.PfmsMailTracking;

import jakarta.transaction.Transactional;

@Transactional
public interface PmsMailTrackRepository extends JpaRepository<PfmsMailTracking, Long>{

	
	@Query(value="SELECT COUNT(DISTINCT a.assignee) FROM action_assign a WHERE a.EndDate BETWEEN CURDATE() AND CURDATE()+(:numberOfDays) AND a.assigneeLabCode <> '@EXP'",nativeQuery = true)
	public long pfmsMailSendEmployeesCount(@Param("numberOfDays") int numberOfDays);

	
	@Query(value="SELECT a.ActionNo, (SELECT CONCAT(IFNULL(CONCAT(e.title,' '),IFNULL(CONCAT(e.salutation,' '),'')), e.emp_name) FROM employee e WHERE e.emp_id=a.assignor)AS 'Assignor', (SELECT email FROM employee e WHERE e.emp_id=a.assignee) AS 'AssigneeEmail', a.assignee, a.progress, a.enddate, (SELECT drona_email FROM employee e WHERE e.emp_id=a.assignee)AS 'AssigneeDronaEmail' FROM action_assign a WHERE a.EndDate BETWEEN CURDATE() AND CURDATE()+(:numberOfDays) AND a.assigneeLabCode <> '@EXP' ORDER BY a.assignee",nativeQuery = true)
	public List<Object[]> weeklyActionList(@Param("numberOfDays") int numberOfDays);


	@Modifying
	@Query(value="UPDATE pfms_mail_track SET MailSentDateTime=:sentDate,MailSentStatus='S',MailSentCount=:finalresult WHERE MailTrackingId=:mailTrackingId",nativeQuery = true)
	public int updatemailTrackingExpectedCount(@Param("mailTrackingId") long mailTrackingId,@Param("finalresult") int finalresult,@Param("sentDate") String sentDate);


	@Query(value="SELECT COUNT(DISTINCT a.EmpId) FROM committee_schedules_invitation a,committee_schedule b WHERE a.CommitteeScheduleId=b.ScheduleId AND b.ScheduleDate=:date AND a.LabCode <> '@EXP' AND a.LabCode<> '@IP' AND a.LabCode=:labCode",nativeQuery = true)
	public long pfmsMailSendEmployeesCountForMeetingMail(@Param("date") String date,@Param("labCode") String labCode);


	@Query(value="SELECT cs.scheduleid,cs.projectid,cs.InitiationId,c.CommitteeShortName,c.CommitteeName,cs.MeetingVenue,cs.ScheduleStartTime,pm.project_code,pm.project_short_name FROM committee_schedule cs,committee c ,project_master pm WHERE c.CommitteeId=cs.CommitteeId AND pm.project_id=cs.projectid AND cs.ScheduleDate=:date AND cs.isactive='1'",nativeQuery = true)
	public List<Object[]> getTodaysMeetings(@Param("date") String date);


	@Query(value = "Call Pfms_Committee_Invitation (:committeeScheduleId)", nativeQuery = true)
	public List<Object[]> committeeAttendance(@Param("committeeScheduleId") String committeeScheduleId);


	

}
