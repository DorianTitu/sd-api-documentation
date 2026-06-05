package SecurityData.api_documentation.message;

import java.util.List;

public record SchemaEndpointResponse(
        String path,
        String method,
        String summary,
        String description,
        String operationId,
        List<String> tags,
        boolean deprecated,
        List<EndpointParameterPayload> parameters,
        EndpointRequestBodyPayload requestBody,
        List<EndpointResponsePayload> responses
) {
}
