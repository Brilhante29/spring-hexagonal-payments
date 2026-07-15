package com.brilhante29.payments

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Path

class ArchitectureBoundaryTest {
    @Test fun `domain and application do not import frameworks or adapters`() {
        val roots = listOf(
            Path.of("src/main/kotlin/com/brilhante29/payments/domain"),
            Path.of("src/main/kotlin/com/brilhante29/payments/application"),
        )
        val forbidden = listOf("org.springframework", ".adapters.", "java.sql", "jakarta.persistence")
        val violations = roots.flatMap { root ->
            Files.walk(root).use { paths -> paths.filter { Files.isRegularFile(it) }.toList() }
        }.flatMap { file ->
            Files.readAllLines(file).mapIndexedNotNull { index, line ->
                if (line.startsWith("import ") && forbidden.any(line::contains)) "$file:${index + 1}: $line" else null
            }
        }
        assertTrue(violations.isEmpty(), violations.joinToString("\n"))
    }
}
