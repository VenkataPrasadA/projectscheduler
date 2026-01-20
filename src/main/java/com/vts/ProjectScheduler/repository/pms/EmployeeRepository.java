package com.vts.ProjectScheduler.repository.pms;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.vts.ProjectScheduler.entity.pms.Employee;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    @Query(value="SELECT e.EmpId ,CONCAT(e.EmpName,', ',d.Designation),e.Email,e.DronaEmail FROM employee_desig d, employee e WHERE e.DesigId = d.DesigId AND e.IsActive = 1 AND e.LabCode =:labcode ORDER BY CASE WHEN e.SrNo = 0 THEN 1 ELSE 0 END,e.SrNo",nativeQuery = true)
    public List<Object[]> getEmpDetails(@Param("labcode") String labcode);

    @Query(value = "SELECT a.DivisionId,a.DivisionCode,a.DivisionName,a.DivisionHeadId,a.GroupId FROM division_master a WHERE a.LabCode=:labcode", nativeQuery = true)
    public List<Object[]> divList(@Param("labcode") String labCode);
}
