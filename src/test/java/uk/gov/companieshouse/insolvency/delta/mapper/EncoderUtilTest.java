package uk.gov.companieshouse.insolvency.delta.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.security.NoSuchAlgorithmException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@TestPropertySource(
        properties = {
                "api.mortgage-id-salt=testsalt"
        }
)
@ExtendWith(SpringExtension.class)
class EncoderUtilTest {

    @Value("${api.mortgage-id-salt}")
    private String mortgageIdSalt;

    private EncoderUtil encoderUtil;

    @BeforeEach
    void setup() {
        encoderUtil = new EncoderUtil(mortgageIdSalt);
    }

    @Test
    void generateSha1HashAndEncode() throws NoSuchAlgorithmException {
        String expectedValue = "9JOzHElueI0XkJEhWd0lLqCkKRw";

        byte[] hashedValue = encoderUtil.generateSha1Hash("1368018");
        String encodedHashValue = encoderUtil.base64Encode(hashedValue);

        assertEquals(expectedValue, encodedHashValue);
    }

    @Test
    void base64Encode() {
    }
}