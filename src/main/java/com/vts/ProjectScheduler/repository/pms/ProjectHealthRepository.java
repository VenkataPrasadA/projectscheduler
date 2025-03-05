package com.vts.ProjectScheduler.repository.pms;

import java.util.List;

import com.vts.ProjectScheduler.entity.pms.ProjectHealth;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.vts.ProjectScheduler.entity.pms.ProjectHealth;

import jakarta.transaction.Transactional;

@Transactional
public interface ProjectHealthRepository extends JpaRepository<ProjectHealth, Long>{

	@Modifying
	@Query(value="DELETE FROM project_health where projectid=:projectid",nativeQuery = true)
	public int projectHealthDelete(@Param("projectid") String projectid);

	
	@Query(value="CALL Project_Health_Insert_Data(:projectId)",nativeQuery = true)
	public List<Object[]> projectHealthInsertData(@Param("projectId") String projectId);

}
