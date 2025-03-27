package com.example.upload.domain.member.member.service

import com.example.upload.standard.util.Ut.jwt.createToken
import com.example.upload.standard.util.Ut.jwt.getPayload
import com.example.upload.standard.util.Ut.jwt.isValidToken
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import java.util.Map

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AuthTokenServiceTest {
    @Autowired
    private lateinit var authTokenService: AuthTokenService

    @Autowired
    private lateinit var memberService: MemberService

    @Value("\${custom.jwt.secret-key}")
    private lateinit var keyString: String

    @Value("\${custom.jwt.expire-seconds}")
    private var expireSeconds = 0

    @Test
    @DisplayName("AuthTokenService 생성")
    fun init() {
        Assertions.assertThat(authTokenService).isNotNull()
    }

    @Test
    @DisplayName("jwt 생성")
    fun createToken() {
        val originPayload = Map.of<String?, Any?>("name", "john", "age", 23)

        val jwtStr = createToken(keyString, expireSeconds, originPayload)
        Assertions.assertThat(jwtStr).isNotBlank()
        val parsedPayload = getPayload(keyString, jwtStr)

        Assertions.assertThat(parsedPayload).containsAllEntriesOf(originPayload)
    }

    @Test
    @DisplayName("user1 - access token 생성")
    fun accessToken() {
        // jwt -> access token jwt

        val member = memberService.findByUsername("user1").get()
        val accessToken = authTokenService.genAccessToken(member)

        Assertions.assertThat(accessToken).isNotBlank()

        println("accessToken = $accessToken")
    }

    @Test
    @DisplayName("jwt valid check")
    fun checkValid() {
        val member = memberService!!.findByUsername("user1").get()
        val accessToken = authTokenService.genAccessToken(member)
        val isValid = isValidToken(keyString, accessToken)
        Assertions.assertThat(isValid).isTrue()

        val parsedPayload = authTokenService.getPayload(accessToken)

        Assertions.assertThat(parsedPayload).containsAllEntriesOf(
            Map.of("id", member.id, "username", member.username)
        )
    }
}
