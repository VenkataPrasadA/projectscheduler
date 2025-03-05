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
        basePackages = "com.vts.ProjectScheduler.repository.qms",
        entityManagerFactoryRef = "qmsEntityManager",
        transactionManagerRef = "qmsTransactionManager"
)
public class PersistenceQmsConfiguration {

    @Autowired
    private Environment env;

    @Bean
    public LocalContainerEntityManagerFactoryBean qmsEntityManager() {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(qmsDataSource());
        em.setPackagesToScan(new String[] { "com.vts.ProjectScheduler.entity.qms" });

        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);
        HashMap<String, Object> properties = new HashMap<>();
        properties.put("hibernate.hbm2ddl.auto", env.getProperty("hibernate.hbm2ddl.auto"));
        properties.put("hibernate.dialect",env.getProperty("hibernate.dialect"));
        em.setJpaPropertyMap(properties);
        return em;
    }

    @Bean(name = "qmsDataSource")
    public DataSource qmsDataSource() {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(env.getProperty("app.qmsdatasource.datasource.mysql.url"));
        dataSource.setUsername(env.getProperty("app.qmsdatasource.datasource.mysql.username"));
        dataSource.setPassword(env.getProperty("app.qmsdatasource.datasource.mysql.password"));
        return dataSource;
    }

    @Bean
    public PlatformTransactionManager qmsTransactionManager() {

        JpaTransactionManager transactionManager= new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(qmsEntityManager().getObject());
        return transactionManager;
    }
}
