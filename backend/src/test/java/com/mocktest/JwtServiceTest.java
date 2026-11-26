package com.mocktest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.mocktest.model.Role;
import com.mocktest.model.User;
import com.mocktest.security.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;

class JwtServiceTest {
    private static final String SECRET = "test-secret-test-secret-test-secret-123";

    private User user() {
        User u = new User();
        u.setId(7L);
        u.setEmail("a@b.com");
        u.setRole(Role.ADMIN);
        return u;
    }

    @Test
    void tokenRoundTripKeepsIdentityAndRole() {
        JwtService jwt = new JwtService(SECRET, 5);
        Claims c = jwt.parse(jwt.generate(user()));
        assertEquals("a@b.com", c.getSubject());
        assertEquals("ADMIN", c.get("role", String.class));
        assertEquals(7L, ((Number) c.get("uid")).longValue());
    }

    @Test
    void tamperedTokenIsRejected() {
        JwtService jwt = new JwtService(SECRET, 5);
        String token = jwt.generate(user());
        String tampered = token.substring(0, token.length() - 2) + "xx";
        assertThrows(JwtException.class, () -> jwt.parse(tampered));
    }

    @Test
    void tokenSignedWithAnotherKeyIsRejected() {
        String token = new JwtService("another-secret-another-secret-123456", 5).generate(user());
        assertThrows(JwtException.class, () -> new JwtService(SECRET, 5).parse(token));
    }

    @Test
    void shortSecretIsRefused() {
        assertThrows(IllegalStateException.class, () -> new JwtService("short", 5));
    }
}
