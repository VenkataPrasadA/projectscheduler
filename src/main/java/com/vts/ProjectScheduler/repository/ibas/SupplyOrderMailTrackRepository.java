package com.vts.ProjectScheduler.repository.ibas;

import com.vts.ProjectScheduler.entity.ibas.SupplyOrderMailTrack;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SupplyOrderMailTrackRepository extends JpaRepository<SupplyOrderMailTrack, Long> {


    @Query(value = "SELECT e.emp_id,e.emp_no,e.emp_name,e.mobile_no,e.email FROM ibas_employee_view e WHERE e.emp_id=(SELECT lab_authority_id FROM ibas_lab_master_view WHERE lab_code=:labCode LIMIT 1)", nativeQuery = true)
    public List<Object[]> getLabDirectorDetailsForMail(@Param("labCode") String labCode);


    @Query(value = """
    		SELECT e.emp_id,e.emp_no,e.emp_name,e.sr_no,e.desig_id,
    		IFNULL((SELECT designation FROM ibas_employee_desig_view WHERE desig_id=e.desig_id),'NA') AS Designation,e.division_id,
    		IFNULL((SELECT division_code FROM ibas_division_master_view WHERE division_id=e.division_id),'NA') AS division_code,e.mobile_no,e.email,e.drona_email,e.internal_email,e.internet_email,
    		(CASE WHEN IFNULL((SELECT COUNT(officer_code) FROM ibas_tbl_commitment WHERE officer_code=e.emp_no),0)>0 THEN 'Y' ELSE 'N' END) AS InitiatingOfficer,
    		(CASE WHEN IFNULL((SELECT COUNT(project_director) FROM ibas_project_master_view WHERE project_director=e.emp_id),0)>0 THEN 'Y' ELSE 'N' END) AS ProjectDirector,
    		(CASE WHEN IFNULL((SELECT COUNT(division_head_id) FROM ibas_division_master_view WHERE division_head_id=e.emp_id),0)>0 THEN 'Y' ELSE 'N' END) AS DivisionHead,
    		(CASE WHEN IFNULL((SELECT COUNT(group_head_id) FROM ibas_division_group_view WHERE group_head_id=e.emp_id),0)>0 THEN 'Y' ELSE 'N' END) AS GroupHead 
    		FROM ibas_employee_view e WHERE e.emp_id NOT IN (SELECT lab_authority_id FROM lab_master WHERE lab_code=:labCode) AND e.is_active='1'
    		""", nativeQuery = true)
    public List<Object[]> getEmployeeDetailsForMail(@Param("labCode") String labCode);

    @Query(value = """
    		SELECT c.commitment_id,c.file_no,c.so_no,c.so_date,c.org_date,c.dp_date,c.project_id,c.project_code,c.item_for,c.vendor_code,
			IFNULL((SELECT vendor_name FROM vendor WHERE vendor_code=c.vendor_code),'NA') AS VendorName,c.division_code,
			IFNULL((SELECT division_name FROM ibas_division_master_view WHERE division_code=c.division_code),'NA') AS DivisionName,c.officer_code,
			IFNULL((SELECT emp_name FROM ibas_employee_view WHERE emp_no=c.officer_code),'NA') AS EmpName,c.demand_no 
			FROM ibas_tbl_commitment c 
			WHERE (CASE WHEN 'D'=:officerType THEN 1=1 WHEN 'P'=:officerType THEN c.project_code IN (SELECT project_code FROM ibas_project_master_view WHERE project_director=:empId) WHEN 'G'=:officerType THEN c.division_code IN (SELECT division_code FROM ibas_division_master_view WHERE group_id IN (SELECT group_id FROM ibas_division_group_view WHERE group_head_id=:empId)) WHEN 'H'=:officerType THEN c.division_code IN (SELECT division_code FROM ibas_division_master_view WHERE division_head_id=:empId) WHEN 'I'=:officerType THEN c.officer_code IN (SELECT emp_no FROM ibas_employee_view WHERE emp_id=:empId) END) 
			AND (c.dp_date BETWEEN CURDATE() AND DATE_ADD(CURDATE(), INTERVAL 30 DAY)) AND c.is_closed <> 'Y' AND c.is_cancel <> 'Y' AND c.commitment_flag='A'
    		""", nativeQuery = true)
    public List<Object[]> getEmployeeSupplyOrderDetails(@Param("officerType") String officerType,@Param("empId") String empId);

}
