package com.mocktest.security;

/** The logged-in user as seen by controllers (taken from the JWT, no DB lookup needed). */
public record AuthUser(Long id, String email) { }
