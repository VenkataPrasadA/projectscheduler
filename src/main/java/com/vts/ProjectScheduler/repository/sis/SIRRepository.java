package com.vts.ProjectScheduler.repository.sis;

import com.vts.ProjectScheduler.entity.sis.SisSir;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SIRRepository extends JpaRepository<SisSir, Long> {


    @Query(value="SELECT a.SIRId,a.SlNo,a.SIRNo,a.SIRDate,a.SONo,a.SODate,a.Description,a.RecievedQty,a.ItemUnit,a.ConsignorName,a.DivisionId,a.ProjectId,a.FinYear,a.InVoiceNo,a.InVoiceDate,a.RinNo,a.RinDate,a.CrvNo,a.CrvDate,a.IsActive FROM sis_sir a WHERE ((a.RinNo IS NULL AND a.RinDate IS NULL) OR (a.CrvNo IS NULL AND a.CrvDate IS NULL)) AND a.IsActive='1'",nativeQuery = true)
    public List<Object[]> getRinCrvPendingList();

    @Query(value="SELECT a.EmpId,a.DivisionId,a.FormRoleId FROM login a WHERE a.FormRoleId=:formRoleId",nativeQuery = true)
    List<Object[]> storesOfficerList(@Param("formRoleId") int formRoleId);
}
