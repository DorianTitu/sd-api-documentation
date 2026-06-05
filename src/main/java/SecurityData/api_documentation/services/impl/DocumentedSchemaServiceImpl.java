package SecurityData.api_documentation.services.impl;

import SecurityData.api_documentation.exceptions.ResourceConflictException;
import SecurityData.api_documentation.exceptions.ResourceNotFoundException;
import SecurityData.api_documentation.message.DocumentedSchemaRequest;
import SecurityData.api_documentation.message.DocumentedSchemaResponse;
import SecurityData.api_documentation.model.DocumentedSchema;
import SecurityData.api_documentation.repository.DocumentedSchemaRepository;
import SecurityData.api_documentation.services.interfaces.DocumentedSchemaService;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Service
public class DocumentedSchemaServiceImpl implements DocumentedSchemaService {

    private final DocumentedSchemaRepository documentedSchemaRepository;
    private final ObjectMapper objectMapper;

    public DocumentedSchemaServiceImpl(
            DocumentedSchemaRepository documentedSchemaRepository,
            ObjectMapper objectMapper
    ) {
        this.documentedSchemaRepository = documentedSchemaRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public DocumentedSchemaResponse create(DocumentedSchemaRequest request) {
        String normalizedSourceUrl = request.sourceUrl().trim();
        if (documentedSchemaRepository.existsBySourceUrl(normalizedSourceUrl)) {
            throw new ResourceConflictException("Ya existe un esquema documentado con la misma URL de origen");
        }

        DocumentedSchema schema = new DocumentedSchema();
        applyRequest(schema, request, normalizedSourceUrl);
        return toResponse(documentedSchemaRepository.save(schema));
    }

    @Override
    @Transactional(readOnly = true)
    public List<DocumentedSchemaResponse> findAll() {
        return documentedSchemaRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentedSchemaResponse findById(Long id) {
        return documentedSchemaRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Esquema documentado no encontrado"));
    }

    @Override
    @Transactional
    public DocumentedSchemaResponse update(Long id, DocumentedSchemaRequest request) {
        DocumentedSchema schema = documentedSchemaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Esquema documentado no encontrado"));

        String normalizedSourceUrl = request.sourceUrl().trim();
        documentedSchemaRepository.findBySourceUrl(normalizedSourceUrl)
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new ResourceConflictException("Ya existe un esquema documentado con la misma URL de origen");
                });

        applyRequest(schema, request, normalizedSourceUrl);
        return toResponse(documentedSchemaRepository.save(schema));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        DocumentedSchema schema = documentedSchemaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Esquema documentado no encontrado"));
        documentedSchemaRepository.delete(schema);
    }

    private void applyRequest(DocumentedSchema schema, DocumentedSchemaRequest request, String normalizedSourceUrl) {
        schema.setSourceUrl(normalizedSourceUrl);
        schema.setTitle(request.title().trim());
        schema.setDescription(request.description());
        schema.setVersion(request.version().trim());
        schema.setVisibility(request.visibility());
        schema.setRawSchemaJson(writeJson(request.rawSchema()));
    }

    private DocumentedSchemaResponse toResponse(DocumentedSchema schema) {
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

    private String writeJson(JsonNode rawSchema) {
        try {
            return objectMapper.writeValueAsString(rawSchema);
        } catch (JacksonException exception) {
            throw new IllegalStateException("No fue posible serializar el raw schema");
        }
    }

    private JsonNode readJsonNode(String json) {
        try {
            return objectMapper.readTree(json);
        } catch (JacksonException exception) {
            throw new IllegalStateException("No fue posible deserializar el raw schema");
        }
    }
}
