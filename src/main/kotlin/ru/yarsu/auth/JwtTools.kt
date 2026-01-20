package ru.yarsu.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.AlgorithmMismatchException
import com.auth0.jwt.exceptions.IncorrectClaimException
import com.auth0.jwt.exceptions.JWTVerificationException
import com.auth0.jwt.exceptions.MissingClaimException
import com.auth0.jwt.exceptions.SignatureVerificationException
import com.auth0.jwt.exceptions.TokenExpiredException
import com.auth0.jwt.interfaces.DecodedJWT
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.UUID

data class JwtTools(
    val secret: String,
) {
    val algorithm = Algorithm.HMAC512(secret)
    val verifier = JWT.require(algorithm).build()

    fun createJwt(
        sub: UUID,
        exp: LocalDateTime,
    ): String =
        JWT
            .create()
            .withSubject(sub.toString())
            .withExpiresAt(exp.plusDays(1).toInstant(ZoneOffset.UTC))
            .sign(algorithm)

    fun verification(token: String): DecodedJWT? =
        try {
            verifier.verify(token)
        } catch (_: AlgorithmMismatchException) {
            null
        } catch (_: SignatureVerificationException) {
            null
        } catch (_: TokenExpiredException) {
            null
        } catch (_: MissingClaimException) {
            null
        } catch (_: IncorrectClaimException) {
            null
        } catch (_: JWTVerificationException) {
            null
        }
}
