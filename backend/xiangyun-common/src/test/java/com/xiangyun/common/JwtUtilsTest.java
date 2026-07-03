package com.xiangyun.common;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtUtilsTest {

    @Test
    void createAndParseToken() {
        TokenPayload created = JwtUtils.create("1", "admin", "admin", "1", 3600, "secret");
        TokenPayload parsed = JwtUtils.parse(created.token(), "secret");
        assertThat(parsed.userId()).isEqualTo("1");
        assertThat(parsed.role()).isEqualTo("admin");
    }

    @Test
    void rejectsWrongSecret() {
        TokenPayload created = JwtUtils.create("1", "admin", "admin", "1", 3600, "secret");
        assertThatThrownBy(() -> JwtUtils.parse(created.token(), "other"))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void rejectsMalformedToken() {
        assertThatThrownBy(() -> JwtUtils.parse("bad-token", "secret"))
                .isInstanceOf(BusinessException.class);
    }
}
