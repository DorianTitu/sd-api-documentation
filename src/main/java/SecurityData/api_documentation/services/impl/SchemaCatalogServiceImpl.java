package SecurityData.api_documentation.services.impl;

import SecurityData.api_documentation.enums.EndpointDocumentationStatus;
import SecurityData.api_documentation.enums.SchemaVisibility;
import SecurityData.api_documentation.enums.TipoUsuario;
import SecurityData.api_documentation.exceptions.ResourceForbiddenException;
import SecurityData.api_documentation.exceptions.ResourceNotFoundException;
import SecurityData.api_documentation.message.DocumentedEndpointResponse;
import SecurityData.api_documentation.message.DocumentedSchemaResponse;
import SecurityData.api_documentation.message.EndpointParameterPayload;
import SecurityData.api_documentation.message.EndpointRequestBodyPayload;
import SecurityData.api_documentation.message.EndpointResponsePayload;
import SecurityData.api_documentation.model.DocumentedEndpoint;
import SecurityData.api_documentation.model.DocumentedSchema;
import SecurityData.api_documentation.repository.DeveloperSchemaAccessRepository;
import SecurityData.api_documentation.repository.DocumentedEndpointRepository;
import SecurityData.api_documentation.repository.DocumentedSchemaRepository;
import SecurityData.api_documentation.security.UsuarioPrincipal;
import SecurityData.api_documentation.services.interfaces.SchemaCatalogService;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Service
public class SchemaCatalogServiceImpl implements SchemaCatalogService {

    private static final TypeReference<List<String>> STRING_LIST_TYPE = new TypeReference<>() {
    };
    private static final TypeReference<List<EndpointParameterPayload>> PARAMETER_LIST_TYPE = new TypeReference<>() {
    };
    private static final TypeReference<List<EndpointResponsePayload>> RESPONSE_LIST_TYPE = new TypeReference<>() {
    };

    private final DocumentedSchemaRepository documentedSchemaRepository;
    private final DocumentedEndpointRepository documentedEndpointRepository;
    private final DeveloperSchemaAccessRepository developerSchemaAccessRepository;
    private final ObjectMapper objectMapper;

    public SchemaCatalogServiceImpl(
            DocumentedSchemaRepository documentedSchemaRepository,
            DocumentedEndpointRepository documentedEndpointRepository,
            DeveloperSchemaAccessRepository developerSchemaAccessRepository,
            ObjectMapper objectMapper
    ) {
        this.documentedSchemaRepository = documentedSchemaRepository;
        this.documentedEndpointRepository = documentedEndpointRepository;
        this.developerSchemaAccessRepository = developerSchemaAccessRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<DocumentedSchemaResponse> findAccessibleSchemas(UsuarioPrincipal principal) {
        return documentedSchemaRepository.findAll().stream()
                .filter(schema -> canAccessSchema(schema, principal))
                .map(this::toSchemaResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentedSchemaResponse findAccessibleSchemaById(Long schemaId, UsuarioPrincipal principal) {
        DocumentedSchema schema = documentedSchemaRepository.findById(schemaId)
                .orElseThrow(() -> new ResourceNotFoundException("Esquema documentado no encontrado"));

        validateSchemaAccess(schema, principal);
        return toSchemaResponse(schema);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DocumentedEndpointResponse> findAccessibleEndpointsBySchema(Long schemaId, UsuarioPrincipal principal) {
        DocumentedSchema schema = documentedSchemaRepository.findById(schemaId)
                .orElseThrow(() -> new ResourceNotFoundException("Esquema documentado no encontrado"));

        validateSchemaAccess(schema, principal);

        return documentedEndpointRepository.findAllBySchema_Id(schemaId).stream()
                .filter(endpoint -> canViewEndpoint(endpoint, principal))
                .map(this::toEndpointResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentedEndpointResponse findAccessibleEndpointById(Long schemaId, Long endpointId, UsuarioPrincipal principal) {
        DocumentedSchema schema = documentedSchemaRepository.findById(schemaId)
                .orElseThrow(() -> new ResourceNotFoundException("Esquema documentado no encontrado"));

        validateSchemaAccess(schema, principal);

        DocumentedEndpoint endpoint = documentedEndpointRepository.findByIdAndSchema_Id(endpointId, schemaId)
                .orElseThrow(() -> new ResourceNotFoundException("Endpoint documentado no encontrado"));

        if (!canViewEndpoint(endpoint, principal)) {
            throw new ResourceForbiddenException("No tiene acceso a este endpoint documentado");
        }

        return toEndpointResponse(endpoint);
    }

    private boolean canAccessSchema(DocumentedSchema schema, UsuarioPrincipal principal) {
        if (isAdmin(principal)) {
            return true;
        }

        if (schema.getVisibility() == SchemaVisibility.PUBLICO) {
            return true;
        }

        if (!isDeveloper(principal)) {
            return false;
        }

        return developerSchemaAccessRepository.existsByUsuario_IdAndSchema_Id(principal.getId(), schema.getId());
    }

    private void validateSchemaAccess(DocumentedSchema schema, UsuarioPrincipal principal) {
        if (!canAccessSchema(schema, principal)) {
            throw new ResourceForbiddenException("No tiene acceso a este esquema documentado");
        }
    }

    private boolean canViewEndpoint(DocumentedEndpoint endpoint, UsuarioPrincipal principal) {
        return isAdmin(principal) || endpoint.getStatus() == EndpointDocumentationStatus.VISIBLE;
    }

    private boolean isAdmin(UsuarioPrincipal principal) {
        return principal != null && principal.getTipoUsuario() == TipoUsuario.ADMINISTRADOR;
    }

    private boolean isDeveloper(UsuarioPrincipal principal) {
        return principal != null && principal.getTipoUsuario() == TipoUsuario.DESARROLLADOR;
    }

    private DocumentedSchemaResponse toSchemaResponse(DocumentedSchema schema) {
        return new DocumentedSchemaResponse(
                schema.getId(),
                schema.getSourceUrl(),
                schema.getTitle(),
                schema.getDescription(),
                schema.getVersion(),
                schema.getVisibility(),
                readJsonNode(schema.getRawSchemaJson()),
                schema.getCreatedAt(),
                schema.getUpdatedAt()
        );
    }

    private DocumentedEndpointResponse toEndpointResponse(DocumentedEndpoint endpoint) {
        return new DocumentedEndpointResponse(
                endpoint.getId(),
                endpoint.getSchema().getId(),
                endpoint.getSchema().getSourceUrl(),
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

    private JsonNode readJsonNode(String json) {
        try {
            return objectMapper.readTree(json);
        } catch (JacksonException exception) {
            throw new IllegalStateException("No fue posible deserializar el raw schema");
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
