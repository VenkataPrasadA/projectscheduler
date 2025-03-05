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
        basePackages = "com.vts.ProjectScheduler.repository.sis",
        entityManagerFactoryRef = "sisEntityManager",
        transactionManagerRef = "sisTransactionManager"
)
public class PersistenceSisConfiguration {

    @Autowired
    private Environment env;

    @Bean
    public LocalContainerEntityManagerFactoryBean sisEntityManager() {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(sisdatasource());
        em.setPackagesToScan(new String[] { "com.vts.ProjectScheduler.entity.sis" });

        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);
        HashMap<String, Object> properties = new HashMap<>();
        properties.put("hibernate.hbm2ddl.auto", env.getProperty("hibernate.hbm2ddl.auto"));
        properties.put("hibernate.dialect", env.getProperty("hibernate.dialect", "org.hibernate.dialect.OracleDialect"));
        em.setJpaPropertyMap(properties);
        return em;
    }

    @Bean(name = "sisdatasource")
    public DataSource sisdatasource() {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(env.getProperty("app.sisdatasource.datasource.mysql.url"));
        dataSource.setUsername(env.getProperty("app.sisdatasource.datasource.mysql.username"));
        dataSource.setPassword(env.getProperty("app.sisdatasource.datasource.mysql.password"));
        dataSource.setDriverClassName(env.getProperty("app.sisdatasource.datasource.mysql.driver-class-name"));
        return dataSource;
    }

    @Bean
    public PlatformTransactionManager sisTransactionManager() {

        JpaTransactionManager transactionManager= new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(sisEntityManager().getObject());
        return transactionManager;
    }
}
