package SecurityData.api_documentation.message;

public record EndpointResponsePayload(
        String statusCode,
        String description,
        String contentType,
        String schemaRef,
        String schemaType
) {
}
