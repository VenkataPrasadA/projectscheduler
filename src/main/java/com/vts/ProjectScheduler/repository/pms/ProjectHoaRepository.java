package com.vts.ProjectScheduler.repository.pms;

import java.util.List;

import com.vts.ProjectScheduler.entity.pms.ProjectHoa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.vts.ProjectScheduler.entity.pms.ProjectHoa;

import jakarta.transaction.Transactional;

@Transactional
public interface ProjectHoaRepository extends JpaRepository<ProjectHoa, Long>{

	
	@Modifying
	@Query(value="DELETE FROM project_hoa WHERE labcode=:labCode",nativeQuery = true)
	public int projectHoaDelete(@Param("labCode") String labCode);

	
	@Query(value="SELECT a.project_id AS id,a.project_code,a.project_name,a.project_main_id,a.project_type AS 'project_director',a.project_director,a.sanction_date,a.pdc FROM project_master a WHERE a.is_active=1 AND a.lab_code =:labCode",nativeQuery = true)
	public List<Object[]> projectList(@Param("labCode") String labCode);


}
