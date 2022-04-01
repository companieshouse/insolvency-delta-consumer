package uk.gov.companieshouse.insolvency.delta.serialization;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.delta.ChsDelta;
import uk.gov.companieshouse.insolvency.delta.exception.NonRetryableErrorException;
import uk.gov.companieshouse.logging.Logger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
public class ChsDeltaSerializerTest {

    @Mock
    private Logger logger;
    private ChsDeltaSerializer serializer;

    @BeforeEach
    public void init() {
        serializer = new ChsDeltaSerializer(logger);
    }

    @Test
    void When_serialize_Expect_chsDeltaBytes() {
        ChsDelta chsDelta = new ChsDelta("{\"key\": \"value\"}", 1, "context_id");

        byte[] result = serializer.serialize("", chsDelta);

        assertThat(decodedData(result)).isEqualTo(chsDelta);
    }

    @Test
    void When_serialize_null_returns_null() {
        byte[] serialize = serializer.serialize("", null);
        assertThat(serialize).isEqualTo(null);
    }

    @Test
    void When_serialize_receivesBytes_returnsBytes() {
        byte[] byteExample = "Example bytes".getBytes();
        byte[] serialize = serializer.serialize("", byteExample);
        assertThat(serialize).isEqualTo(byteExample);
    }

    private ChsDelta decodedData(byte[] chsDelta) {
        ChsDeltaDeserializer serializer = new ChsDeltaDeserializer(this.logger);
        return serializer.deserialize("", chsDelta);
    }
}