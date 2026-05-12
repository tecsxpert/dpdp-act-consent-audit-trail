package com.internship.tool;

import org.junit.jupiter.api.BeforeAll;
import org.springframework.boot.test.autoconfigure
    .web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context
    .SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context
    .DynamicPropertyRegistry;
import org.springframework.test.context
    .DynamicPropertySource;
import org.testcontainers.containers
    .PostgreSQLContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest(
    webEnvironment =
        SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
public abstract class BaseIntegrationTest {

    static final PostgreSQLContainer<?> postgres =
        new PostgreSQLContainer<>(
            DockerImageName.parse("postgres:15"))
            .withDatabaseName("consent_test_db")
            .withUsername("test_user")
            .withPassword("test_pass");

    static final GenericContainer<?> redis =
        new GenericContainer<>(
            DockerImageName.parse("redis:7"))
            .withExposedPorts(6379);

    @BeforeAll
    static void startContainers() {
        postgres.start();
        redis.start();
    }

    @DynamicPropertySource
    static void configureProperties(
            DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url",
                     postgres::getJdbcUrl);
        registry.add("spring.datasource.username",
                     postgres::getUsername);
        registry.add("spring.datasource.password",
                     postgres::getPassword);
        registry.add("spring.data.redis.host",
                     redis::getHost);
        registry.add("spring.data.redis.port",
            () -> redis.getMappedPort(6379));
    }
}