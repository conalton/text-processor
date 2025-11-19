package org.conalton.textprocessor.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;

@TestConfiguration
class MysqlContainerConfig {
  static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0");

  static {
    mysql.start();
  }

  @DynamicPropertySource
  static void register(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", mysql::getJdbcUrl);
    registry.add("spring.datasource.username", mysql::getUsername);
    registry.add("spring.datasource.password", mysql::getPassword);
  }
}
