package SecurityData.api_documentation.message;

import SecurityData.api_documentation.enums.SchemaVisibility;
import java.time.Instant;
import tools.jackson.databind.JsonNode;

public record DocumentedSchemaResponse(
        Long id,
        String sourceUrl,
        String title,
        String description,
        String version,
        SchemaVisibility visibility,
        JsonNode rawSchema,
        Instant createdAt,
        Instant updatedAt
) {
}
