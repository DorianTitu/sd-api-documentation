package SecurityData.api_documentation.message;

import jakarta.validation.constraints.NotNull;

public record SchemaAccessGrantRequest(
        @NotNull(message = "El developerUserId es obligatorio")
        Long developerUserId
) {
}
