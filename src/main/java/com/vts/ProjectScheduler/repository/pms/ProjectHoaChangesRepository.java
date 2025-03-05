package com.vts.ProjectScheduler.repository.pms;

import com.vts.ProjectScheduler.entity.pms.ProjectHoaChanges;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.vts.ProjectScheduler.entity.pms.ProjectHoaChanges;

import jakarta.transaction.Transactional;

@Transactional
public interface ProjectHoaChangesRepository extends JpaRepository<ProjectHoaChanges, Long>{

	@Modifying
	@Query(value="DELETE FROM project_hoa_changes where projectid=:projectId",nativeQuery = true)
	public int projectHoaChangesDelete(@Param("projectId") String projectId);

	
	@Query(value="SELECT ClusterId FROM lab_master WHERE LabCode=:labCode",nativeQuery = true)
	public Object getClusterId(@Param("labCode") String labCode);
}
