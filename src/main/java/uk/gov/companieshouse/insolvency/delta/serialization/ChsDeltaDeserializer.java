package uk.gov.companieshouse.insolvency.delta.serialization;

import static uk.gov.companieshouse.insolvency.delta.InsolvencyDeltaConsumerApplication.APPLICATION_NAMESPACE;

import org.apache.avro.io.DatumReader;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.reflect.ReflectDatumReader;
import org.apache.kafka.common.serialization.Deserializer;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.delta.ChsDelta;
import uk.gov.companieshouse.insolvency.delta.exception.NonRetryableErrorException;
import uk.gov.companieshouse.insolvency.delta.logging.DataMapHolder;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

@Component
public class ChsDeltaDeserializer implements Deserializer<ChsDelta> {

    private static final Logger LOGGER = LoggerFactory.getLogger(APPLICATION_NAMESPACE);

    @Override
    public ChsDelta deserialize(String topic, byte[] data) {
        try {
            Decoder decoder = DecoderFactory.get().binaryDecoder(data, null);
            DatumReader<ChsDelta> reader = new ReflectDatumReader<>(ChsDelta.class);
            ChsDelta chsDelta = reader.read(null, decoder);
            LOGGER.info("Message successfully de-serialised", DataMapHolder.getLogMap());
            return chsDelta;
        } catch (Exception ex) {
            LOGGER.error("De-Serialization exception while converting to Avro schema object", ex,
                    DataMapHolder.getLogMap());
            throw new NonRetryableErrorException("De-Serialization exception while converting to Avro schema object",
                    ex);
        }
    }

}
