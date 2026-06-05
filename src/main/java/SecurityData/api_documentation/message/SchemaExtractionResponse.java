package SecurityData.api_documentation.message;

import java.util.List;
import tools.jackson.databind.JsonNode;

public record SchemaExtractionResponse(
        String sourceUrl,
        int statusCode,
        int endpointCount,
        List<SchemaEndpointResponse> endpoints,
        JsonNode schema
) {
}
