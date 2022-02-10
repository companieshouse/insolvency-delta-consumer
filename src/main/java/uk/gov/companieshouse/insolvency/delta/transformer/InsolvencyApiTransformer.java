package uk.gov.companieshouse.insolvency.delta.transformer;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.delta.InsolvencyDelta;

@Component
public class InsolvencyApiTransformer {

    public String transform(InsolvencyDelta insolvencyDelta) {
        // TODO: Use mapStruct to transform json object to Open API generated object
        return insolvencyDelta.toString();
    }
}
