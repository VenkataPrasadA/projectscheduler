package com.vts.ProjectScheduler.repository.pms;

import com.vts.ProjectScheduler.entity.pms.DakMailTracking;
import com.vts.ProjectScheduler.entity.pms.MailConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DakMailTrackRepository extends JpaRepository<DakMailTracking, Long> {


    @Query(value="SELECT COUNT(*) FROM dak_mail_track WHERE CreatedDate = CURDATE() AND TrackingType=:trackingType",nativeQuery = true)
    public long getMailInitiatedCount(@Param("trackingType") String trackingType);

    @Query(value="SELECT COUNT(DISTINCT m.EmpId) FROM dak_marking m JOIN dak a ON m.DakId = a.DakId WHERE a.ReceiptDate IS NOT NULL AND DATE(a.ReceiptDate) >='2024-04-01' AND m.DakId NOT IN (SELECT r.DakId FROM dak_reply r WHERE r.DakId = m.DakId) AND a.DakStatus IN('DD','DA') AND a.ActionId='2' AND m.IsActive=1",nativeQuery = true)
    public long getDailyExpectedPendingReplyCount();

    @Query(value="SELECT COUNT(DISTINCT m.EmpId) FROM dak_marking m  JOIN dak a ON m.DakId = a.DakId WHERE m.ActionDueDate IS NOT NULL  AND DATE(m.ActionDueDate) >= CURDATE() - INTERVAL WEEKDAY(CURDATE()) DAY AND DATE(m.ActionDueDate) < CURDATE() + INTERVAL 7 - WEEKDAY(CURDATE()) DAY  AND m.EmpId NOT IN (SELECT r.EmpId FROM dak_reply r WHERE r.DakId = m.DakId)  AND a.DakStatus != 'DI' AND m.IsActive=1",nativeQuery = true)
    public long getWeeklyExpectedPendingReplyCount();

    @Query(value="SELECT COUNT(DISTINCT m.EmpId) FROM dak_marking m INNER JOIN dak a ON m.DakId = a.DakId AND DATE(a.DistributedDate) = CURDATE() AND  a.DakStatus != 'DI' AND m.IsActive=1",nativeQuery = true)
    public long getSummaryOfDailyDistributedCount();

    @Query(value="SELECT m.DakId,m.EmpId,empData.EmpName,empData.Email,a.DakNo,sourceData.SourceShortName,a.ActionDueDate,empData.MobileNo,empData.DronaEmail FROM dak_marking m LEFT JOIN employee empData ON empData.EmpId=m.EmpId JOIN dak a ON m.DakId=a.DakId LEFT JOIN dak_source_details sourceData ON sourceData.SourceDetailId=a.SourceDetailId WHERE a.ReceiptDate IS NOT NULL AND DATE(a.ReceiptDate) >='2024-04-01' AND m.DakId NOT IN (SELECT r.DakId FROM dak_reply r WHERE r.DakId=m.DakId) AND a.DakStatus IN('DD','DA') AND a.ActionId='2' AND m.IsActive=1",nativeQuery = true)
    public List<Object[]> getDailyPendingReplyEmpData();

    @Query(value="SELECT m.DakId,m.EmpId,empData.EmpName,empData.Email,a.DakNo,sourceData.SourceShortName,a.ActionDueDate,empData.DronaEmail FROM dak_marking m LEFT JOIN employee empData ON empData.EmpId=m.EmpId INNER JOIN dak a ON m.DakId=a.DakId AND DATE(a.DistributedDate) = CURDATE() AND  a.DakStatus != 'DI' LEFT  JOIN dak_source_details sourceData ON sourceData.SourceDetailId=a.SourceDetailId WHERE m.IsActive=1",nativeQuery = true)
    public List<Object[]> getSummaryDistributedEmpData();

    @Query(value="SELECT  m.DakId,m.EmpId,empData.EmpName,empData.Email,a.DakNo, sourceData.SourceShortName,a.ActionDueDate,empData.DronaEmail FROM dak_marking m LEFT JOIN employee empData ON empData.EmpId = m.EmpId JOIN dak a ON m.DakId = a.DakId LEFT JOIN dak_source_details sourceData ON sourceData.SourceDetailId = a.SourceDetailId WHERE  m.ActionDueDate IS NOT NULL AND DATE(m.ActionDueDate) >= CURDATE() - INTERVAL WEEKDAY(CURDATE()) DAY AND DATE(m.ActionDueDate) < CURDATE() + INTERVAL 7 - WEEKDAY(CURDATE()) DAY  AND m.EmpId NOT IN (SELECT r.EmpId FROM dak_reply r WHERE r.DakId = m.DakId)  AND a.DakStatus != 'DI' AND m.IsActive=1 ORDER BY ActionDueDate",nativeQuery = true)
    public List<Object[]> getWeeklyPendingReplyEmpData();


    @Query(value="SELECT e.EmpId ,CONCAT(e.EmpName,', ',d.Designation),e.Email,e.DronaEmail FROM employee_desig d, employee e WHERE e.DesigId = d.DesigId AND e.IsActive = 1 AND e.LabCode =:labcode ORDER BY CASE WHEN e.SrNo = 0 THEN 1 ELSE 0 END,e.SrNo",nativeQuery = true)
    public List<Object[]> getEmpDetails(@Param("labcode") String labcode);

    @Query(value = "SELECT a.DivisionId,a.DivisionCode,a.DivisionName,a.DivisionHeadId,a.GroupId FROM division_master a WHERE a.LabCode=:labcode", nativeQuery = true)
    public List<Object[]> divList(@Param("labcode") String labCode);
}
