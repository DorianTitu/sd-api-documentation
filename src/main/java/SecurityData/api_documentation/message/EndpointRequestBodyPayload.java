package SecurityData.api_documentation.message;

public record EndpointRequestBodyPayload(
        boolean required,
        String contentType,
        String schemaRef,
        String schemaType
) {
}
