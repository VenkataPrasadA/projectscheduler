package com.vts.ProjectScheduler.repository.ibas;

import com.vts.ProjectScheduler.entity.ibas.SupplyOrderMailTrack;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SupplyOrderMailTrackRepository extends JpaRepository<SupplyOrderMailTrack, Long> {


    @Query(value = "SELECT e.EmpId,e.EmpNo,e.EmpName,e.MobileNo,e.Email FROM employee e WHERE e.EmpId=(SELECT LabAuthorityId FROM lab_master WHERE LabCode=:labCode LIMIT 1)", nativeQuery = true)
    public List<Object[]> getLabDirectorDetailsForMail(@Param("labCode") String labCode);


    @Query(value = "SELECT e.EmpId,e.EmpNo,e.EmpName,e.SrNo,e.DesigId,IFNULL((SELECT Designation FROM employee_desig WHERE DesigId=e.DesigId),'NA') AS Designation,e.DivisionId,IFNULL((SELECT DivisionCode FROM division_master WHERE DivisionId=e.DivisionId),'NA') AS DivisionCode,e.MobileNo,e.Email,e.DronaEmail,e.InternalEmail,e.InternetEmail,(CASE WHEN IFNULL((SELECT COUNT(OfficerCode) FROM tblcommitment WHERE OfficerCode=e.EmpNo),0)>0 THEN 'Y' ELSE 'N' END) AS InitiatingOfficer,(CASE WHEN IFNULL((SELECT COUNT(ProjectDirector) FROM project_master WHERE ProjectDirector=e.EmpId),0)>0 THEN 'Y' ELSE 'N' END) AS ProjectDirector,(CASE WHEN IFNULL((SELECT COUNT(DivisionHeadId) FROM division_master WHERE DivisionHeadId=e.EmpId),0)>0 THEN 'Y' ELSE 'N' END) AS DivisionHead,(CASE WHEN IFNULL((SELECT COUNT(GroupHeadId) FROM division_group WHERE GroupHeadId=e.EmpId),0)>0 THEN 'Y' ELSE 'N' END) AS GroupHead FROM employee e WHERE e.EmpId NOT IN (SELECT LabAuthorityId FROM lab_master WHERE LabCode=:labCode) AND e.IsActive='1'", nativeQuery = true)
    public List<Object[]> getEmployeeDetailsForMail(@Param("labCode") String labCode);

    @Query(value = "SELECT c.CommitmentId,c.FileNo,c.SoNo,c.SoDate,c.OrgDate,c.DpDate,c.ProjectId,c.ProjectCode,c.ItemFor,c.VendorCode,IFNULL((SELECT VendorName FROM vendor WHERE VendorCode=c.VendorCode),'NA') AS VendorName,c.DivisionCode,IFNULL((SELECT DivisionName FROM division_master WHERE DivisionCode=c.DivisionCode),'NA') AS DivisionName,c.OfficerCode,IFNULL((SELECT EmpName FROM employee WHERE EmpNo=c.OfficerCode),'NA') AS EmpName,c.DemandNo FROM tblcommitment c WHERE (CASE WHEN 'D'=:officerType THEN 1=1 WHEN 'P'=:officerType THEN c.ProjectCode IN (SELECT ProjectCode FROM project_master WHERE ProjectDirector=:empId) WHEN 'G'=:officerType THEN c.DivisionCode IN (SELECT DivisionCode FROM division_master WHERE GroupId IN (SELECT GroupId FROM division_group WHERE GroupHeadId=:empId)) WHEN 'H'=:officerType THEN c.DivisionCode IN (SELECT DivisionCode FROM division_master WHERE DivisionHeadId=:empId) WHEN 'I'=:officerType THEN c.OfficerCode IN (SELECT EmpNo FROM employee WHERE EmpId=:empId) END) AND (c.DpDate BETWEEN CURDATE() AND DATE_ADD(CURDATE(), INTERVAL 30 DAY))", nativeQuery = true)
    public List<Object[]> getEmployeeSupplyOrderDetails(@Param("officerType") String officerType,@Param("empId") String empId);

}
