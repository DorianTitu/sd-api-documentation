package SecurityData.api_documentation.services.impl;

import SecurityData.api_documentation.exceptions.SchemaFetchException;
import SecurityData.api_documentation.message.EndpointParameterPayload;
import SecurityData.api_documentation.message.EndpointRequestBodyPayload;
import SecurityData.api_documentation.message.EndpointResponsePayload;
import SecurityData.api_documentation.message.SchemaEndpointResponse;
import SecurityData.api_documentation.message.SchemaExtractionResponse;
import SecurityData.api_documentation.services.interfaces.SchemaExtractionService;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Service
public class SchemaExtractionServiceImpl implements SchemaExtractionService {

    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(5);
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(15);

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public SchemaExtractionServiceImpl(ObjectMapper objectMapper) {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(CONNECT_TIMEOUT)
                .build();
        this.objectMapper = objectMapper;
    }

    @Override
    public SchemaExtractionResponse extractFromUrl(String url) {
        URI uri = buildAndValidateUri(url);
        HttpRequest request = HttpRequest.newBuilder(uri)
                .GET()
                .timeout(REQUEST_TIMEOUT)
                .header("Accept", "application/json")
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new SchemaFetchException("No fue posible obtener el esquema desde la URL indicada");
            }

            JsonNode schema = objectMapper.readTree(response.body());
            List<SchemaEndpointResponse> endpoints = extractEndpoints(schema);
            return new SchemaExtractionResponse(
                    uri.toString(),
                    response.statusCode(),
                    endpoints.size(),
                    endpoints,
                    schema
            );
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new SchemaFetchException("La consulta del esquema fue interrumpida", exception);
        } catch (IOException exception) {
            throw new SchemaFetchException("No fue posible leer el esquema remoto", exception);
        }
    }

    private List<SchemaEndpointResponse> extractEndpoints(JsonNode schema) {
        JsonNode pathsNode = schema.get("paths");
        if (pathsNode == null || !pathsNode.isObject()) {
            throw new SchemaFetchException("El esquema no contiene una seccion 'paths' valida");
        }

        List<SchemaEndpointResponse> endpoints = new ArrayList<>();
        for (Map.Entry<String, JsonNode> pathEntry : pathsNode.properties()) {
            String path = pathEntry.getKey();
            JsonNode operationsNode = pathEntry.getValue();

            if (operationsNode == null || !operationsNode.isObject()) {
                continue;
            }

            for (Map.Entry<String, JsonNode> operationEntry : operationsNode.properties()) {
                String method = operationEntry.getKey();
                if (!isHttpMethod(method)) {
                    continue;
                }

                JsonNode operationNode = operationEntry.getValue();
                endpoints.add(new SchemaEndpointResponse(
                        path,
                        method.toUpperCase(Locale.ROOT),
                        textValue(operationNode, "summary"),
                        textValue(operationNode, "description"),
                        textValue(operationNode, "operationId"),
                        tagsFrom(operationNode),
                        booleanValue(operationNode, "deprecated"),
                        parametersFrom(operationNode),
                        requestBodyFrom(operationNode),
                        responsesFrom(operationNode)
                ));
            }
        }

        return endpoints;
    }

    private boolean isHttpMethod(String value) {
        return switch (value.toLowerCase(Locale.ROOT)) {
            case "get", "post", "put", "delete", "patch", "options", "head", "trace" -> true;
            default -> false;
        };
    }

    private String textValue(JsonNode node, String fieldName) {
        JsonNode value = node.get(fieldName);
        return value != null && value.isTextual() ? value.asText() : null;
    }

    private boolean booleanValue(JsonNode node, String fieldName) {
        JsonNode value = node.get(fieldName);
        return value != null && value.isBoolean() && value.asBoolean();
    }

    private List<String> tagsFrom(JsonNode node) {
        JsonNode tagsNode = node.get("tags");
        if (tagsNode == null || !tagsNode.isArray()) {
            return List.of();
        }

        List<String> tags = new ArrayList<>();
        for (JsonNode tagNode : tagsNode) {
            if (tagNode.isTextual()) {
                tags.add(tagNode.asText());
            }
        }
        return tags;
    }

    private List<EndpointParameterPayload> parametersFrom(JsonNode operationNode) {
        JsonNode parametersNode = operationNode.get("parameters");
        if (parametersNode == null || !parametersNode.isArray()) {
            return List.of();
        }

        List<EndpointParameterPayload> parameters = new ArrayList<>();
        for (JsonNode parameterNode : parametersNode) {
            JsonNode schemaNode = parameterNode.get("schema");
            parameters.add(new EndpointParameterPayload(
                    textValue(parameterNode, "name"),
                    textValue(parameterNode, "in"),
                    booleanValue(parameterNode, "required"),
                    schemaType(schemaNode),
                    schemaFormat(schemaNode),
                    schemaRef(schemaNode)
            ));
        }
        return parameters;
    }

    private EndpointRequestBodyPayload requestBodyFrom(JsonNode operationNode) {
        JsonNode requestBodyNode = operationNode.get("requestBody");
        if (requestBodyNode == null || !requestBodyNode.isObject()) {
            return null;
        }

        JsonNode contentNode = requestBodyNode.get("content");
        if (contentNode == null || !contentNode.isObject() || contentNode.properties().isEmpty()) {
            return new EndpointRequestBodyPayload(booleanValue(requestBodyNode, "required"), null, null, null);
        }

        Map.Entry<String, JsonNode> entry = contentNode.properties().iterator().next();
        JsonNode schemaNode = entry.getValue().get("schema");
        return new EndpointRequestBodyPayload(
                booleanValue(requestBodyNode, "required"),
                entry.getKey(),
                schemaRef(schemaNode),
                schemaType(schemaNode)
        );
    }

    private List<EndpointResponsePayload> responsesFrom(JsonNode operationNode) {
        JsonNode responsesNode = operationNode.get("responses");
        if (responsesNode == null || !responsesNode.isObject()) {
            return List.of();
        }

        List<EndpointResponsePayload> responses = new ArrayList<>();
        for (Map.Entry<String, JsonNode> responseEntry : responsesNode.properties()) {
            String statusCode = responseEntry.getKey();
            JsonNode responseNode = responseEntry.getValue();
            JsonNode contentNode = responseNode.get("content");

            if (contentNode != null && contentNode.isObject() && !contentNode.properties().isEmpty()) {
                Map.Entry<String, JsonNode> contentEntry = contentNode.properties().iterator().next();
                JsonNode schemaNode = contentEntry.getValue().get("schema");
                responses.add(new EndpointResponsePayload(
                        statusCode,
                        textValue(responseNode, "description"),
                        contentEntry.getKey(),
                        schemaRef(schemaNode),
                        schemaType(schemaNode)
                ));
                continue;
            }

            responses.add(new EndpointResponsePayload(
                    statusCode,
                    textValue(responseNode, "description"),
                    null,
                    null,
                    null
            ));
        }

        return responses;
    }

    private String schemaType(JsonNode schemaNode) {
        return schemaNode != null ? textValue(schemaNode, "type") : null;
    }

    private String schemaFormat(JsonNode schemaNode) {
        return schemaNode != null ? textValue(schemaNode, "format") : null;
    }

    private String schemaRef(JsonNode schemaNode) {
        return schemaNode != null ? textValue(schemaNode, "$ref") : null;
    }

    private URI buildAndValidateUri(String url) {
        try {
            URI uri = new URI(url.trim());
            String scheme = uri.getScheme();

            if (scheme == null || (!scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https"))) {
                throw new IllegalArgumentException("La URL debe usar http o https");
            }

            if (uri.getHost() == null || uri.getHost().isBlank()) {
                throw new IllegalArgumentException("La URL debe incluir un host valido");
            }

            return uri;
        } catch (URISyntaxException exception) {
            throw new IllegalArgumentException("La URL del esquema no es valida");
        }
    }
}
