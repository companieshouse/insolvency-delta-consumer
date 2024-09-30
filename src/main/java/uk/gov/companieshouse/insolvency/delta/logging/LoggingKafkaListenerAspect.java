package uk.gov.companieshouse.insolvency.delta.logging;

import static uk.gov.companieshouse.insolvency.delta.InsolvencyDeltaConsumerApplication.NAMESPACE;

import java.util.Optional;
import java.util.UUID;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.delta.ChsDelta;
import uk.gov.companieshouse.insolvency.delta.exception.NonRetryableErrorException;
import uk.gov.companieshouse.insolvency.delta.exception.RetryableErrorException;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

@Component
@Aspect
class LoggingKafkaListenerAspect {

    private static final Logger LOGGER = LoggerFactory.getLogger(NAMESPACE);
    private static final String LOG_MESSAGE_RECEIVED = "Processing delta";
    private static final String LOG_MESSAGE_PROCESSED = "Processed delta";
    private static final String EXCEPTION_MESSAGE = "%s exception thrown";

    private final int maxAttempts;

    LoggingKafkaListenerAspect(@Value("${insolvency.delta.attempts}") int maxAttempts) {
        this.maxAttempts = maxAttempts;
    }

    @Around("@annotation(org.springframework.kafka.annotation.KafkaListener)")
    public Object manageStructuredLogging(ProceedingJoinPoint joinPoint)
            throws Throwable {

        int retryCount = 0;
        try {
            Message<?> message = (Message<?>) joinPoint.getArgs()[0];
            retryCount = Optional.ofNullable((Integer) joinPoint.getArgs()[1]).orElse(1) - 1;
            DataMapHolder.initialise(Optional.of(extractData(message.getPayload()).getContextId())
                    .orElse(UUID.randomUUID().toString()));

            DataMapHolder.get()
                    .retryCount(retryCount)
                    .topic((String) joinPoint.getArgs()[2])
                    .partition(Integer.valueOf((String) joinPoint.getArgs()[3]))
                    .offset(Long.valueOf((String) joinPoint.getArgs()[4]));

            LOGGER.info(LOG_MESSAGE_RECEIVED, DataMapHolder.getLogMap());

            Object result = joinPoint.proceed();

            LOGGER.info(LOG_MESSAGE_PROCESSED, DataMapHolder.getLogMap());

            return result;
        } catch (RetryableErrorException ex) {
            // maxAttempts includes first attempt which is not a retry
            if (retryCount >= maxAttempts - 1) {
                LOGGER.error("Max retry attempts reached", ex, DataMapHolder.getLogMap());
            } else {
                LOGGER.info(String.format(EXCEPTION_MESSAGE, ex.getClass().getSimpleName()), DataMapHolder.getLogMap());
            }
            throw ex;
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex, DataMapHolder.getLogMap());
            throw ex;
        } finally {
            DataMapHolder.clear();
        }
    }

    private ChsDelta extractData(Object payload) {
        if (payload instanceof ChsDelta) {
            return (ChsDelta) payload;
        }
        throw new NonRetryableErrorException(String.format("Invalid payload type. Payload: %s", payload.toString()));
    }
}