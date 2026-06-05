package SecurityData.api_documentation.message;

public record EndpointParameterPayload(
        String name,
        String location,
        boolean required,
        String type,
        String format,
        String schemaRef
) {
}
