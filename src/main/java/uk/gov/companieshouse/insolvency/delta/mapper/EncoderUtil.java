package uk.gov.companieshouse.insolvency.delta.mapper;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class EncoderUtil {

    private static final String SHA_1 = "SHA-1";

    public static String MORTGAGE_ID_SALT;

    /**
     * Initialize by passing in salt for mortgageId.
     *
     * @param mortgageIdSalt input String value
     */
    @Autowired
    public EncoderUtil(@Value("${api.mortgage-id-salt}") String mortgageIdSalt) {
        MORTGAGE_ID_SALT = mortgageIdSalt;
    }

    /**
     * Hash using SHA-1 on the value after concatenating with salt.
     * @param input the string to hash
     * @return the resulting byte array
     */
    public byte[] generateSha1Hash(String input) throws NoSuchAlgorithmException {
        var digest = MessageDigest.getInstance(SHA_1);
        String saltedMortgageId = input + MORTGAGE_ID_SALT;
        return digest.digest(saltedMortgageId.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Encode in base64.
     * @param byteArray the byte array to encode
     * @return the base64 encoded url safe string
     */
    public String base64Encode(final byte[] byteArray) {
        return Base64.encodeBase64URLSafeString(byteArray);
    }
}
