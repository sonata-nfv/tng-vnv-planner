package com.github.tng.vnv.planner.config;

import java.util.Properties;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@ComponentScan("com.github.tng.vnv.planner.model")
@EnableJpaRepositories(basePackages = "com.github.tng.vnv.planner.repository") // , entityManagerFactoryRef = "entityManagerFactoryBuilder", transactionManagerRef = "transactionManagerBuilder"
@EnableTransactionManagement
public class PersistenceConfig {


    @Autowired
    private Environment env;

    @Bean() 
    @Profile("!test")
    public DataSource dataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(env.getProperty("spring.datasource.driver"));
        dataSource.setUrl(env.getProperty("spring.datasource.url"));
        dataSource.setUsername(env.getProperty("POSTGRES_USER") !=null ? env.getProperty("POSTGRES_USER") : env.getProperty("spring.datasource.username"));
        dataSource.setPassword(env.getProperty("POSTGRES_PASSWORD") !=null ? env.getProperty("POSTGRES_PASSWORD") : env.getProperty("spring.datasource.password"));
        return dataSource;
    }
    final Properties hibernateProperties() {
        final Properties hibernateProperties = new Properties();
        hibernateProperties.setProperty("hibernate.show_sql", env.getProperty("spring.jpa.show-sql"));
        hibernateProperties.setProperty("hibernate.hbm2ddl.auto", env.getProperty("spring.jpa.hibernate.ddl-auto"));
        hibernateProperties.setProperty("hibernate.dialect", env.getProperty("spring.jpa.properties.hibernate.dialect"));
        hibernateProperties.setProperty("hibernate.generate_statistics", env.getProperty("spring.jpa.generate_statistics"));
        hibernateProperties.setProperty("hibernate.release_mode", env.getProperty("spring.jpa.connection.release_mode"));
        hibernateProperties.setProperty("hibernate.format_sql", env.getProperty("spring.jpa.format_sql"));
//        hibernateProperties.setProperty("hibernate.implicit_naming_strategy","spring.jpa.hibernate.naming.implicit-strategy");
        return hibernateProperties;
    }
    @Bean(name = "entityManagerFactory") //
    @Profile("test")
    public LocalContainerEntityManagerFactoryBean h2EntityManagerFactoryBean() {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource());
        em.setPackagesToScan(new String[] { "com.github.tng.vnv.planner.model" });
        em.setPersistenceUnitName("builderPU");
        JpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);
        em.setJpaProperties(hibernateProperties());
        return em;
    }

    @Bean(name = "transactionManager") 
    public PlatformTransactionManager transactionManager(@Qualifier("entityManagerFactory") EntityManagerFactory emf) {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(emf);
        return transactionManager;
    }
}
