package uk.ac.ebi.tsc.portal.security;

import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.MalformedClaimException;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import uk.ac.ebi.tsc.aap.client.model.Domain;
import uk.ac.ebi.tsc.aap.client.model.User;

import javax.annotation.PostConstruct;
import java.io.InputStream;
import java.security.PublicKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Jose A. Dianes <jdianes@ebi.ac.uk>
 * @since v0.0.1
 * @author Navis Raj <navis@ebi.ac.uk>
 */
/**
 * Extracts the user from a Json Web Token and matches to user account.
 */
@Component
public class TokenHandler {

    private static final Logger logger = LoggerFactory.getLogger(TokenHandler.class);

    @Value("${jwt.certificate}")
    private String certificatePath;

    private final UserDetailsService userService;
    private JwtConsumer jwtConsumer;


    @Autowired
    TokenHandler(UserDetailsService userService) {
        this.userService = userService;
    }

    @PostConstruct
    public void initPropertyDependentFields() throws Exception {
        InputStream in = new DefaultResourceLoader().getResource(certificatePath).getInputStream();
        CertificateFactory f = CertificateFactory.getInstance("X.509");
        X509Certificate certificate = (X509Certificate) f.generateCertificate(in);
        PublicKey verifyingKey = certificate.getPublicKey();
        setJwtConsumer(verifyingKey);
    }

    void setJwtConsumer(PublicKey verifyingKey) {
        jwtConsumer = new JwtConsumerBuilder()
                .setRequireExpirationTime()
                .setAllowedClockSkewInSeconds(30)
                .setRequireSubject()
                .setVerificationKey(verifyingKey)
                .build();
    }

    UserDetails loadUserFromTokenSub(String token) {
        String sub;
        try {
            JwtClaims jwtClaims = jwtConsumer.processToClaims(token);
            sub = jwtClaims.getClaimValue("sub").toString();
        } catch (InvalidJwtException e) {
            logger.debug("cannot parse token", e);
            throw new RuntimeException("Cannot parse token", e);
        }
        return userService.loadUserByUsername(sub);
    }

    UserDetails loadUserFromTokenName(String token) {

        String name;
        try {
            JwtClaims jwtClaims = jwtConsumer.processToClaims(token);
            name = jwtClaims.getClaimValue("name").toString();
        } catch (InvalidJwtException e) {
            logger.debug("cannot parse token", e);
            throw new RuntimeException("Cannot parse token", e);
        }
        return userService.loadUserByUsername(name);
    }

    String parseUserNameFromToken(String token) {
        String username;
        try {
            JwtClaims jwtClaims = jwtConsumer.processToClaims(token);
            username = jwtClaims.getClaimValue("sub").toString();
            logger.debug("Parsed username "+ username +" from token.");
            return username;
        } catch (InvalidJwtException e) {
            logger.debug("cannot parse token", e);
            throw new RuntimeException("Cannot parse token", e);
        }

    }

    String parseEmailFromToken(String token) {
        String email;
        try {
            JwtClaims jwtClaims = jwtConsumer.processToClaims(token);
            email = jwtClaims.getStringClaimValue("email");
            logger.debug("Parsed email "+ email +" from token.");
            return email;
        } catch (InvalidJwtException | MalformedClaimException e) {
            logger.debug("cannot parse token for email ", e);
            throw new RuntimeException("Cannot parse token", e);
        }

    }

    String parseNameFromToken(String token) {
        String name;
        try {
            JwtClaims jwtClaims = jwtConsumer.processToClaims(token);
            name = jwtClaims.getClaimValue("name").toString();
            logger.debug("Parsed name "+ name +" from token.");
            return name;
        } catch (InvalidJwtException e) {
            logger.debug("cannot parse token", e);
            throw new RuntimeException("Cannot parse token", e);
        }

    }
    
    public User parseUserFromToken(String token) {
        try {
            Set<Domain> domainsSet = new HashSet<>();
            JwtClaims jwtClaims = jwtConsumer.processToClaims(token);
            String userReference = jwtClaims.getSubject();
            String nickname = jwtClaims.getStringClaimValue("nickname");
            String email = jwtClaims.getStringClaimValue("email");
            String fullName = jwtClaims.getStringClaimValue("name");
            List<String> domains = jwtClaims.getStringListClaimValue("domains");
            domains.forEach(name->domainsSet.add(new Domain(name,null,null)));
            return new User(nickname, email, userReference, fullName, domainsSet);
        } catch (InvalidJwtException | MalformedClaimException e) {
            logger.debug("cannot parse token", e);
            throw new RuntimeException("Cannot parse token", e);
        } catch (Exception e) {
            logger.info("Exception: " + e.getMessage());
            throw new RuntimeException("Cannot parse token", e);
        }
    }
    

}