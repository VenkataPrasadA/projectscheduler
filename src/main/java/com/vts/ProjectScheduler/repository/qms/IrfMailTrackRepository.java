package com.vts.ProjectScheduler.repository.qms;

import com.vts.ProjectScheduler.entity.qms.IrfMailTrack;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface IrfMailTrackRepository extends JpaRepository<IrfMailTrack,Long> {


    @Query(value = "SELECT c.EmpId , c.MemberType,c.InspGroupType,e.UserName FROM  qms_members c,login e WHERE e.EmpId = c.EmpId AND NOW() BETWEEN c.fromdate AND c.todate", nativeQuery = true)
    public List<Object[]> getMembersList();

    @Query(value = "call Qms_irf_OIC_QRAG_Approval(:empId,:formRoleId)", nativeQuery = true)
    public List<Object[]> getIrfQAGDApprovalList(@Param("empId") String empId,@Param("formRoleId")String formRoleId);

    @Query(value = "call Qms_irf_Assign_List(:empid)", nativeQuery = true)
    public List<Object[]> getIrfCaseWorkerReqList(@Param("empid") String empid );
}
