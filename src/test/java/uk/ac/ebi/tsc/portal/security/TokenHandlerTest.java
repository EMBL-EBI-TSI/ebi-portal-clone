package uk.ac.ebi.tsc.portal.security;

import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.NumericDate;
import org.jose4j.lang.JoseException;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.security.*;
import java.util.Base64;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.ThrowableAssert.catchThrowable;
import static org.mockito.Mockito.when;

/**
 * @author Jose A. Dianes <jdianes@ebi.ac.uk>
 * @since v0.0.1
 * @author Navis Raj <navis@ebi.ac.uk>
 */
public class TokenHandlerTest {

    static final long ttlMins = 5;
    static final String username = "alice";
    static final String issuer = "https://tsi.ebi.ac.uk";

    TokenHandler subject;

    UserDetailsService mockService;
    PrivateKey signingKey;
    PublicKey verifyingKey;

    public TokenHandlerTest() throws NoSuchAlgorithmException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("EC");
        KeyPair testKeyPair = keyGen.generateKeyPair();
        signingKey = testKeyPair.getPrivate();
        verifyingKey = testKeyPair.getPublic();

        UserDetails mockUser = Mockito.mock(UserDetails.class);
        when(mockUser.getUsername()).thenReturn(username);
        mockService = Mockito.mock(UserDetailsService.class);
        when(mockService.loadUserByUsername(username)).thenReturn(mockUser);

        subject = new TokenHandler(mockService);
        subject.setJwtConsumer(verifyingKey);
    }

//    @Test
//    public void
//    accepts_valid_token() throws Exception {
//        String validToken = token();
//        UserDetails user = subject.parseUserFromToken(validToken);
//        assertThat(user.getUsername(), is(username));
//    }

    @Test
    public void
    detects_tampered_token() throws Exception {
        String validToken = token();
        String encodedPayload = validToken.split("\\.")[1];
        String decodedPayload = new String(Base64.getDecoder().decode(encodedPayload));
        String tamperedPayload = decodedPayload.replace(username, "bob");
        String tamperedEncodedPayload = new String(Base64.getEncoder().encode(tamperedPayload.getBytes()));
        String tamperedToken = validToken.replace(encodedPayload, tamperedEncodedPayload);

        Throwable thrown = catchThrowable(() -> {
            subject.loadUserFromTokenSub(tamperedToken);
        });

        assertThat(thrown);
    }

    @Test
    public void
    rejects_token_signed_with_different_private_key() throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("EC");
        PrivateKey privateKey = keyGen.generateKeyPair().getPrivate();
        String tokenFromUntrusted = token(privateKey, AlgorithmIdentifiers.ECDSA_USING_P256_CURVE_AND_SHA256);

        Throwable thrown = catchThrowable(() -> {
            subject.loadUserFromTokenSub(tokenFromUntrusted);
        });

        assertThat(thrown);
    }

    @Test
    public void
    rejects_token_signed_with_different_algorithm() throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        PrivateKey privateKey = keyGen.generateKeyPair().getPrivate();
        String tokenFromUntrusted = token(privateKey, AlgorithmIdentifiers.RSA_USING_SHA256);

        Throwable thrown = catchThrowable(() -> {
            subject.loadUserFromTokenSub(tokenFromUntrusted);
        });

        assertThat(thrown);
    }

    @Test
    public void
    rejects_expired_tokens() {
        String expiredToken = expiredToken();

        Throwable thrown = catchThrowable(() -> {
            subject.loadUserFromTokenSub(expiredToken);
        });

        assertThat(thrown);
    }

    private String token() {
        return token(signingKey, AlgorithmIdentifiers.ECDSA_USING_P256_CURVE_AND_SHA256);
    }

    private String token(PrivateKey privateKey, String alg) {
        JwtClaims claims = new JwtClaims();
        claims.setIssuer(issuer);
        claims.setExpirationTimeMinutesInTheFuture(ttlMins);
        claims.setGeneratedJwtId();
        claims.setIssuedAtToNow();
        claims.setSubject(username);

        return build(claims, privateKey, alg);
    }

    private String expiredToken() {
        long now = new Date().getTime();
        // past must be earlier than skew allowed by TokenHandler jwtConsumer
        NumericDate past = NumericDate.fromMilliseconds(now - (60 * 1000));

        JwtClaims claims = new JwtClaims();
        claims.setSubject(username);
        claims.setExpirationTime(past);

        return build(claims, signingKey, AlgorithmIdentifiers.ECDSA_USING_P256_CURVE_AND_SHA256);
    }

    private String build(JwtClaims claims, PrivateKey privateKey, String alg) {
        JsonWebSignature jws = new JsonWebSignature();
        jws.setPayload(claims.toJson());
        jws.setKey(privateKey);
        jws.setAlgorithmHeaderValue(alg);

        String token;
        try {
            token = jws.getCompactSerialization();
        } catch (JoseException e) {
            throw new RuntimeException(e);
        }
        return token;
    }

}
