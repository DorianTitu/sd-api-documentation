package SecurityData.api_documentation.message;

import SecurityData.api_documentation.enums.SchemaVisibility;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import tools.jackson.databind.JsonNode;

public record DocumentedSchemaRequest(
        @NotBlank(message = "La URL de origen es obligatoria")
        String sourceUrl,

        @NotBlank(message = "El titulo del esquema es obligatorio")
        String title,

        String description,

        @NotBlank(message = "La version del esquema es obligatoria")
        String version,

        @NotNull(message = "La visibilidad del esquema es obligatoria")
        SchemaVisibility visibility,

        @NotNull(message = "El raw schema es obligatorio")
        JsonNode rawSchema
) {
}
