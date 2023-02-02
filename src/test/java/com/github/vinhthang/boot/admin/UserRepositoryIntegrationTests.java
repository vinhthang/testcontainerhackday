package com.github.vinhthang.boot.admin;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
public class UserRepositoryIntegrationTests {
    @Autowired
    UserRepository userRepository;
    @Container
    static JdbcDatabaseContainer<?> postgreSQL = new PostgreSQLContainer<>("postgres:15.1-alpine")
            .withInitScript("initscript.sql");
    @DynamicPropertySource
    static void registerPostgreSQLProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url",  postgreSQL::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQL::getUsername);
        registry.add("spring.datasource.password", postgreSQL::getPassword);
    }

    @Test
    public void test_that_select_all_user_return_inserted_users() {
        List<User> users = userRepository.findAll();

        assertThat(users).hasSize(1);
        assertThat(users.get(0))
                .returns("Little Dot", User::getUserName)
                .returns(1L, User::getId)
                .returns(LocalDateTime.of(2022, 1, 10, 0, 51, 14), User::getCreated);
    }
}
