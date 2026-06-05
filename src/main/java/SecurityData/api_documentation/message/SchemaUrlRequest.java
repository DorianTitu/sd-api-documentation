package SecurityData.api_documentation.message;

import jakarta.validation.constraints.NotBlank;

public record SchemaUrlRequest(
        @NotBlank(message = "La URL del esquema es obligatoria")
        String url
) {
}
