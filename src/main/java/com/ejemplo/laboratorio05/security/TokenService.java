package com.ejemplo.laboratorio05.security;

import java.time.Instant;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

/**
 * Servicio de emisión de JSON Web Tokens (JWT) mediante Nimbus.
 * Emplea firma simétrica HMAC-SHA256 (HS256) y formatea roles en scopes.
 */
@Service
public class TokenService {

    private final JwtEncoder jwtEncoder;
    private final long expiracion;

    public TokenService(JwtEncoder jwtEncoder,
                        @Value("${app.jwt.expiration:3600}") long expiracion) {
        this.jwtEncoder = jwtEncoder;
        this.expiracion = expiracion;
    }

    public String generarToken(Authentication authentication) {
        Instant ahora = Instant.now();

        String scope = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(autoridad -> autoridad.replace("ROLE_", ""))
                .collect(Collectors.joining(" "));

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("upn-laboratorio05")
                .issuedAt(ahora)
                .expiresAt(ahora.plusSeconds(expiracion))
                .subject(authentication.getName())
                .claim("scope", scope)
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims))
                .getTokenValue();
    }
}
