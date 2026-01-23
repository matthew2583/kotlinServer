package ru.yarsu.auth

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.UUID

class JwtToolsTest {
    private lateinit var jwtTools: JwtTools

    @BeforeEach
    fun setUp() {
        jwtTools = JwtTools("test-secret-key")
    }

    @Nested
    inner class TokenGenerationTests {
        @Test
        fun createJwtReturnsNonEmptyString() {
            val token =
                jwtTools.createJwt(
                    sub = UUID.randomUUID(),
                    exp = LocalDateTime.now(),
                )

            assertNotNull(token)
            assertTrue(token.isNotEmpty())
        }

        @Test
        fun generatedTokenContainsThreeParts() {
            val token =
                jwtTools.createJwt(
                    sub = UUID.randomUUID(),
                    exp = LocalDateTime.now(),
                )

            val parts = token.split(".")
            assertEquals(3, parts.size)
        }
    }

    @Nested
    inner class TokenValidationTests {
        @Test
        fun validTokenIsVerifiedSuccessfully() {
            val userId = UUID.randomUUID()
            val token =
                jwtTools.createJwt(
                    sub = userId,
                    exp = LocalDateTime.now(),
                )

            val decoded = jwtTools.verification(token)
            assertNotNull(decoded)
        }

        @Test
        fun invalidTokenReturnsNull() {
            val decoded = jwtTools.verification("invalid.token.here")
            assertNull(decoded)
        }

        @Test
        fun tamperedTokenReturnsNull() {
            val token =
                jwtTools.createJwt(
                    sub = UUID.randomUUID(),
                    exp = LocalDateTime.now(),
                )
            val tamperedToken = token + "tampered"

            val decoded = jwtTools.verification(tamperedToken)
            assertNull(decoded)
        }
    }

    @Nested
    inner class SubjectExtractionTests {
        @Test
        fun tokenContainsCorrectSubject() {
            val userId = UUID.randomUUID()
            val token =
                jwtTools.createJwt(
                    sub = userId,
                    exp = LocalDateTime.now(),
                )

            val decoded = jwtTools.verification(token)
            val extractedSubject = decoded?.subject

            assertEquals(userId.toString(), extractedSubject)
        }
    }

    @Nested
    inner class DifferentSecretsTests {
        @Test
        fun tokenFromDifferentSecretIsInvalid() {
            val jwtTools1 = JwtTools("secret1")
            val jwtTools2 = JwtTools("secret2")

            val token =
                jwtTools1.createJwt(
                    sub = UUID.randomUUID(),
                    exp = LocalDateTime.now(),
                )

            val decoded = jwtTools2.verification(token)
            assertNull(decoded)
        }

        @Test
        fun tokenFromSameSecretIsValid() {
            val jwtTools1 = JwtTools("same-secret")
            val jwtTools2 = JwtTools("same-secret")

            val token =
                jwtTools1.createJwt(
                    sub = UUID.randomUUID(),
                    exp = LocalDateTime.now(),
                )

            val decoded = jwtTools2.verification(token)
            assertNotNull(decoded)
        }
    }

    @Nested
    inner class AlgorithmTests {
        @Test
        fun algorithmIsInitialized() {
            assertNotNull(jwtTools.algorithm)
        }

        @Test
        fun verifierIsInitialized() {
            assertNotNull(jwtTools.verifier)
        }
    }
}
