package com.vts.ProjectScheduler.cfg;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.HashMap;

@Configuration

@EnableJpaRepositories(
        basePackages = "com.vts.ProjectScheduler.repository.ibas",
        entityManagerFactoryRef = "ibasEntityManager",
        transactionManagerRef = "ibasTransactionManager"
)
public class PersistenceIbasConfiguration {
    @Autowired
    private Environment env;

    @Bean
    public LocalContainerEntityManagerFactoryBean ibasEntityManager() {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(ibasDataSource());
        em.setPackagesToScan(new String[] { "com.vts.ProjectScheduler.entity.ibas" });

        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);
        HashMap<String, Object> properties = new HashMap<>();
        properties.put("hibernate.hbm2ddl.auto", env.getProperty("hibernate.hbm2ddl.auto"));
        properties.put("hibernate.dialect",env.getProperty("hibernate.dialect"));
        em.setJpaPropertyMap(properties);
        return em;
    }

    @Bean(name = "ibasDataSource")
    public DataSource ibasDataSource() {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(env.getProperty("app.ibasdatasource.datasource.mysql.url"));
        dataSource.setUsername(env.getProperty("app.ibasdatasource.datasource.mysql.username"));
        dataSource.setPassword(env.getProperty("app.ibasdatasource.datasource.mysql.password"));
        return dataSource;
    }

    @Bean
    public PlatformTransactionManager ibasTransactionManager() {

        JpaTransactionManager transactionManager= new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(ibasEntityManager().getObject());
        return transactionManager;
    }
}