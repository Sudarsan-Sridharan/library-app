package utils

import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_METHOD
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit.jupiter.SpringExtension

/**
 * Qualifier for Integration Tests with Spring Boot:
 *
 * - [tagged][Tag] as `integration-test`
 * - [test instance][TestInstance] generation: `PER_METHOD`
 * - [spring boot test features][SpringBootTest] activated
 */
@Retention
@Target(AnnotationTarget.CLASS)
@SpringBootTest
@Tag("integration-test")
@TestInstance(PER_METHOD)
@ExtendWith(SpringExtension::class)
annotation class SpringBootIntegrationTest