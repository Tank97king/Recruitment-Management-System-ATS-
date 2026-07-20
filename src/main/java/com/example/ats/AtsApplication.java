package com.example.ats;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import org.springframework.scheduling.annotation.EnableAsync;

/**
 * ATS System — Application Entry Point.
 *
 * <p>{@code @SpringBootApplication} is a convenience annotation that combines:
 * <ul>
 *   <li>{@code @Configuration}       — marks this class as a source of bean definitions</li>
 *   <li>{@code @EnableAutoConfiguration} — enables Spring Boot's auto-configuration mechanism</li>
 *   <li>{@code @ComponentScan}        — scans {@code com.example.ats} and all sub-packages
 *                                       for Spring-managed components</li>
 * </ul>
 *
 * <p>{@code @EnableJpaAuditing} activates Spring Data JPA's auditing support,
 * which automatically populates {@code @CreatedDate} and {@code @LastModifiedDate}
 * fields on entities that extend {@code BaseEntity}.
 */
@SpringBootApplication(excludeName = {
    "org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration",
    "org.springframework.boot.autoconfigure.elasticsearch.ElasticsearchClientAutoConfiguration",
    "org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration",
    "org.springframework.boot.autoconfigure.cassandra.CassandraAutoConfiguration",
    "org.springframework.boot.autoconfigure.r2dbc.R2dbcAutoConfiguration"
})
@EnableJpaAuditing
@EnableAsync
public class AtsApplication {

    public static void main(String[] args) {
        SpringApplication.run(AtsApplication.class, args);
    }
}
