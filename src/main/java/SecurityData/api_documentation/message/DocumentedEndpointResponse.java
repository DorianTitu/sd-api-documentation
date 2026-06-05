package SecurityData.api_documentation.message;

import SecurityData.api_documentation.enums.EndpointDocumentationStatus;
import java.time.Instant;
import java.util.List;

public record DocumentedEndpointResponse(
        Long id,
        Long schemaId,
        String sourceUrl,
        String path,
        String method,
        String tag,
        String summary,
        String description,
        String operationId,
        List<String> tags,
        boolean deprecated,
        EndpointDocumentationStatus status,
        List<EndpointParameterPayload> parameters,
        EndpointRequestBodyPayload requestBody,
        List<EndpointResponsePayload> responses,
        Instant createdAt,
        Instant updatedAt
) {
}
