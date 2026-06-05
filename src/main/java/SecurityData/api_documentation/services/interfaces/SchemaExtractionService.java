package SecurityData.api_documentation.services.interfaces;

import SecurityData.api_documentation.message.SchemaExtractionResponse;

public interface SchemaExtractionService {

    SchemaExtractionResponse extractFromUrl(String url);
}
