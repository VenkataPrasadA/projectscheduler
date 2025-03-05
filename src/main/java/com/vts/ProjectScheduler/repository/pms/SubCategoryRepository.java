package com.vts.ProjectScheduler.repository.pms;


import com.vts.ProjectScheduler.entity.pms.SubCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;




@Repository
public interface SubCategoryRepository extends JpaRepository<SubCategory,Long> {



}