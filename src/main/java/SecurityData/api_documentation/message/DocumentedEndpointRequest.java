package SecurityData.api_documentation.message;

import SecurityData.api_documentation.enums.EndpointDocumentationStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record DocumentedEndpointRequest(
        @NotNull(message = "El schemaId es obligatorio")
        Long schemaId,

        @NotBlank(message = "El path es obligatorio")
        String path,

        @NotBlank(message = "El metodo es obligatorio")
        @Size(max = 10, message = "El metodo no puede exceder 10 caracteres")
        String method,

        String summary,
        String description,
        String operationId,

        @NotEmpty(message = "Debe existir al menos un tag")
        List<String> tags,

        boolean deprecated,

        @NotNull(message = "El estado del endpoint es obligatorio")
        EndpointDocumentationStatus status,

        List<EndpointParameterPayload> parameters,
        EndpointRequestBodyPayload requestBody,
        List<EndpointResponsePayload> responses
) {
}
