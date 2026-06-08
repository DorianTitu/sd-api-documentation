package SecurityData.api_documentation.services.impl;

import SecurityData.api_documentation.exceptions.ResourceConflictException;
import SecurityData.api_documentation.exceptions.ResourceNotFoundException;
import SecurityData.api_documentation.message.DocumentedEndpointRequest;
import SecurityData.api_documentation.message.DocumentedEndpointResponse;
import SecurityData.api_documentation.message.EndpointParameterPayload;
import SecurityData.api_documentation.message.EndpointRequestBodyPayload;
import SecurityData.api_documentation.message.EndpointResponsePayload;
import SecurityData.api_documentation.model.DocumentedEndpoint;
import SecurityData.api_documentation.model.DocumentedSchema;
import SecurityData.api_documentation.repository.DocumentedEndpointRepository;
import SecurityData.api_documentation.repository.DocumentedSchemaRepository;
import SecurityData.api_documentation.services.interfaces.DocumentedEndpointService;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

@Service
public class DocumentedEndpointServiceImpl implements DocumentedEndpointService {

    private static final TypeReference<List<String>> STRING_LIST_TYPE = new TypeReference<>() {
    };
    private static final TypeReference<List<EndpointParameterPayload>> PARAMETER_LIST_TYPE = new TypeReference<>() {
    };
    private static final TypeReference<List<EndpointResponsePayload>> RESPONSE_LIST_TYPE = new TypeReference<>() {
    };

    private final DocumentedEndpointRepository documentedEndpointRepository;
    private final DocumentedSchemaRepository documentedSchemaRepository;
    private final ObjectMapper objectMapper;

    public DocumentedEndpointServiceImpl(
            DocumentedEndpointRepository documentedEndpointRepository,
            DocumentedSchemaRepository documentedSchemaRepository,
            ObjectMapper objectMapper
    ) {
        this.documentedEndpointRepository = documentedEndpointRepository;
        this.documentedSchemaRepository = documentedSchemaRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public DocumentedEndpointResponse create(DocumentedEndpointRequest request) {
        String normalizedMethod = normalizeMethod(request.method());
        DocumentedSchema schema = findSchema(request.schemaId());

        if (documentedEndpointRepository.existsBySchema_IdAndPathAndMethod(
                schema.getId(),
                request.path().trim(),
                normalizedMethod
        )) {
            throw new ResourceConflictException("Ya existe un endpoint documentado con el mismo schema, path y metodo");
        }

        DocumentedEndpoint endpoint = new DocumentedEndpoint();
        applyRequest(endpoint, schema, request, normalizedMethod);
        return toResponse(documentedEndpointRepository.save(endpoint));
    }

    @Override
    @Transactional(readOnly = true)
    public List<DocumentedEndpointResponse> findAll() {
        return documentedEndpointRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentedEndpointResponse findById(Long id) {
        return documentedEndpointRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Endpoint documentado no encontrado"));
    }

    @Override
    @Transactional
    public DocumentedEndpointResponse update(Long id, DocumentedEndpointRequest request) {
        DocumentedEndpoint endpoint = documentedEndpointRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Endpoint documentado no encontrado"));

        String normalizedMethod = normalizeMethod(request.method());
        DocumentedSchema schema = findSchema(request.schemaId());
        documentedEndpointRepository.findBySchema_IdAndPathAndMethod(
                        schema.getId(),
                        request.path().trim(),
                        normalizedMethod
                )
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new ResourceConflictException("Ya existe un endpoint documentado con el mismo schema, path y metodo");
                });

        applyRequest(endpoint, schema, request, normalizedMethod);
        return toResponse(documentedEndpointRepository.save(endpoint));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        DocumentedEndpoint endpoint = documentedEndpointRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Endpoint documentado no encontrado"));
        documentedEndpointRepository.delete(endpoint);
    }

    private void applyRequest(
            DocumentedEndpoint endpoint,
            DocumentedSchema schema,
            DocumentedEndpointRequest request,
            String normalizedMethod
    ) {
        endpoint.setSchema(schema);
        endpoint.setSourceUrl(schema.getSourceUrl());
        endpoint.setPath(request.path().trim());
        endpoint.setMethod(normalizedMethod);
        endpoint.setTag(request.tags().isEmpty() ? null : request.tags().get(0));
        endpoint.setSummary(request.summary());
        endpoint.setDescription(request.description());
        endpoint.setOperationId(request.operationId());
        endpoint.setDeprecated(request.deprecated());
        endpoint.setStatus(request.status());
        endpoint.setTagsJson(writeJson(request.tags()));
        endpoint.setParametersJson(writeJson(safeParameters(request.parameters())));
        endpoint.setRequestBodyJson(writeJson(request.requestBody()));
        endpoint.setResponsesJson(writeJson(safeResponses(request.responses())));
    }

    private DocumentedEndpointResponse toResponse(DocumentedEndpoint endpoint) {
        return new DocumentedEndpointResponse(
                endpoint.getId(),
                endpoint.getSchema().getId(),
                endpoint.getSourceUrl(),
                endpoint.getPath(),
                endpoint.getMethod(),
                endpoint.getTag(),
                endpoint.getSummary(),
                endpoint.getDescription(),
                endpoint.getOperationId(),
                readJson(endpoint.getTagsJson(), STRING_LIST_TYPE),
                endpoint.isDeprecated(),
                endpoint.getStatus(),
                readJson(endpoint.getParametersJson(), PARAMETER_LIST_TYPE),
                readNullableJson(endpoint.getRequestBodyJson(), EndpointRequestBodyPayload.class),
                readJson(endpoint.getResponsesJson(), RESPONSE_LIST_TYPE),
                endpoint.getCreatedAt(),
                endpoint.getUpdatedAt()
        );
    }

    private DocumentedSchema findSchema(Long schemaId) {
        return documentedSchemaRepository.findById(schemaId)
                .orElseThrow(() -> new ResourceNotFoundException("Esquema documentado no encontrado"));
    }

    private List<EndpointParameterPayload> safeParameters(List<EndpointParameterPayload> parameters) {
        return parameters == null ? List.of() : parameters;
    }

    private List<EndpointResponsePayload> safeResponses(List<EndpointResponsePayload> responses) {
        return responses == null ? List.of() : responses;
    }

    private String normalizeMethod(String method) {
        return method.trim().toUpperCase(Locale.ROOT);
    }

    private String writeJson(Object value) {
        if (value == null) {
            return null;
        }

        try {
            return objectMapper.writeValueAsString(value);
        } catch (JacksonException exception) {
            throw new IllegalStateException("No fue posible serializar la informacion del endpoint");
        }
    }

    private <T> T readNullableJson(String json, Class<T> targetType) {
        if (json == null || json.isBlank()) {
            return null;
        }

        try {
            return objectMapper.readValue(json, targetType);
        } catch (JacksonException exception) {
            throw new IllegalStateException("No fue posible deserializar la informacion del endpoint");
        }
    }

    private <T> T readJson(String json, TypeReference<T> typeReference) {
        if (json == null || json.isBlank()) {
            return defaultValue(typeReference);
        }

        try {
            return objectMapper.readValue(json, typeReference);
        } catch (JacksonException exception) {
            throw new IllegalStateException("No fue posible deserializar la informacion del endpoint");
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T defaultValue(TypeReference<T> typeReference) {
        if (typeReference == STRING_LIST_TYPE || typeReference == PARAMETER_LIST_TYPE || typeReference == RESPONSE_LIST_TYPE) {
            return (T) List.of();
        }
        return null;
    }
}
