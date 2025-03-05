package com.vts.ProjectScheduler.repository.pms;

import com.vts.ProjectScheduler.entity.pms.MailConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MailConfigurationRepository extends JpaRepository<MailConfiguration, Long> {


    @Query(value="SELECT a.MailConfigurationId,a.TypeOfHost,a.Host,a.Port,a.Username,a.Password,a.CreatedBy,a.CreatedDate FROM mail_configuration a WHERE a.TypeOfHost=:type LIMIT 1",nativeQuery = true)
    public List<Object[]> getMailPropertiesByTypeOfHost(@Param("type") String type);
}
