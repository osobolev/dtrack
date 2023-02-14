package dtrack.web;

import org.jose4j.jwa.AlgorithmConstraints;
import org.jose4j.jwk.PublicJsonWebKey;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.MalformedClaimException;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.jose4j.lang.JoseException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Duration;

final class KeyStore {

    private static final String KEY_ALGORITHM = AlgorithmIdentifiers.RSA_USING_SHA256;
    private static final String RSA = "RSA";
    private static final String USER_ID = "userId";
    private static final String USER_DISPLAY = "userDisplay";
    static final Duration DURATION = Duration.ofDays(3653);

    private static KeyStore instance = null;
    
    private final PublicJsonWebKey jsonWebKey;

    private KeyStore(PublicJsonWebKey jsonWebKey) {
        this.jsonWebKey = jsonWebKey;
    }

    private static KeyStore create() throws InvalidKeySpecException, IOException, JoseException, NoSuchAlgorithmException {
        Path publicKeyFile = Paths.get("public.key");
        Path privateKeyFile = Paths.get("private.key");
        PublicKey publicKey;
        PrivateKey privateKey;
        if (Files.exists(publicKeyFile) && Files.exists(privateKeyFile)) {
            KeyFactory kf = KeyFactory.getInstance(RSA);

            byte[] publicKeyBytes = Files.readAllBytes(publicKeyFile);
            X509EncodedKeySpec publicSpec = new X509EncodedKeySpec(publicKeyBytes);
            publicKey = kf.generatePublic(publicSpec);

            byte[] privateKeyBytes = Files.readAllBytes(privateKeyFile);
            PKCS8EncodedKeySpec privateSpec = new PKCS8EncodedKeySpec(privateKeyBytes);
            privateKey = kf.generatePrivate(privateSpec);
        } else {
            KeyPairGenerator rsa = KeyPairGenerator.getInstance(RSA);
            rsa.initialize(2048);
            KeyPair keyPair = rsa.generateKeyPair();
            publicKey = keyPair.getPublic();
            privateKey = keyPair.getPrivate();

            Files.write(publicKeyFile, publicKey.getEncoded());
            Files.write(privateKeyFile, privateKey.getEncoded());
        }
        PublicJsonWebKey jsonWebKey = PublicJsonWebKey.Factory.newPublicJwk(publicKey);
        jsonWebKey.setPrivateKey(privateKey);
        return new KeyStore(jsonWebKey);
    }

    static synchronized KeyStore get() throws NoSuchAlgorithmException, InvalidKeySpecException, JoseException, IOException {
        if (instance == null) {
            instance = create();
        }
        return instance;
    }

    String createToken(UserInfo loginInfo) throws JoseException {
        JwtClaims claims = new JwtClaims();
        claims.setClaim(USER_ID, String.valueOf(loginInfo.id));
        claims.setClaim(USER_DISPLAY, loginInfo.displayName);
        claims.setExpirationTimeMinutesInTheFuture(DURATION.toMinutes());
        claims.setGeneratedJwtId();
        claims.setIssuedAtToNow();

        JsonWebSignature jws = new JsonWebSignature();
        jws.setPayload(claims.toJson());
        jws.setKey(jsonWebKey.getPrivateKey());
        jws.setKeyIdHeaderValue(jsonWebKey.getKeyId());
        jws.setAlgorithmHeaderValue(KEY_ALGORITHM);
        return jws.getCompactSerialization();
    }

    UserInfo getLoginInfo(String token) throws MalformedClaimException, InvalidJwtException {
        JwtConsumer jwtConsumer = new JwtConsumerBuilder()
            .setRequireExpirationTime()
            .setAllowedClockSkewInSeconds(30)
            .setVerificationKey(jsonWebKey.getKey())
            .setJwsAlgorithmConstraints(new AlgorithmConstraints(
                AlgorithmConstraints.ConstraintType.PERMIT, KEY_ALGORITHM
            ))
            .build();
        JwtClaims claims = jwtConsumer.processToClaims(token);
        int id = Integer.parseInt(claims.getStringClaimValue(USER_ID));
        String displayName = claims.getStringClaimValue(USER_DISPLAY);
        return new UserInfo(id, displayName);
    }
}
