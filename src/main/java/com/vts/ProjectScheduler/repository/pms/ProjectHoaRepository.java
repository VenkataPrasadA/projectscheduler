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

	
	@Query(value="SELECT a.projectid AS id,a.projectcode,a.projectname,a.projectmainid,a.projecttype AS 'project_director',a.projectdirector,a.sanctiondate,a.pdc FROM project_master a  WHERE  a.isactive=1 AND a.labcode =:labCode",nativeQuery = true)
	public List<Object[]> projectList(@Param("labCode") String labCode);


}
