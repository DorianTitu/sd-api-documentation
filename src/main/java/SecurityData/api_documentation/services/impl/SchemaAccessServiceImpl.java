package SecurityData.api_documentation.services.impl;

import SecurityData.api_documentation.enums.TipoUsuario;
import SecurityData.api_documentation.exceptions.ResourceConflictException;
import SecurityData.api_documentation.exceptions.ResourceNotFoundException;
import SecurityData.api_documentation.message.SchemaAccessGrantRequest;
import SecurityData.api_documentation.message.SchemaAccessResponse;
import SecurityData.api_documentation.model.DeveloperSchemaAccess;
import SecurityData.api_documentation.model.DocumentedSchema;
import SecurityData.api_documentation.model.Usuario;
import SecurityData.api_documentation.repository.DeveloperSchemaAccessRepository;
import SecurityData.api_documentation.repository.DocumentedSchemaRepository;
import SecurityData.api_documentation.repository.UsuarioRepository;
import SecurityData.api_documentation.security.UsuarioPrincipal;
import SecurityData.api_documentation.services.interfaces.SchemaAccessService;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SchemaAccessServiceImpl implements SchemaAccessService {

    private final DeveloperSchemaAccessRepository developerSchemaAccessRepository;
    private final DocumentedSchemaRepository documentedSchemaRepository;
    private final UsuarioRepository usuarioRepository;

    public SchemaAccessServiceImpl(
            DeveloperSchemaAccessRepository developerSchemaAccessRepository,
            DocumentedSchemaRepository documentedSchemaRepository,
            UsuarioRepository usuarioRepository
    ) {
        this.developerSchemaAccessRepository = developerSchemaAccessRepository;
        this.documentedSchemaRepository = documentedSchemaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    @Transactional
    public SchemaAccessResponse grantAccess(
            Long schemaId,
            SchemaAccessGrantRequest request,
            UsuarioPrincipal grantedByPrincipal
    ) {
        DocumentedSchema schema = documentedSchemaRepository.findById(schemaId)
                .orElseThrow(() -> new ResourceNotFoundException("Esquema documentado no encontrado"));

        Usuario developer = usuarioRepository.findByIdAndTipoUsuario(request.developerUserId(), TipoUsuario.DESARROLLADOR)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario desarrollador no encontrado"));

        if (developerSchemaAccessRepository.existsByUsuario_IdAndSchema_Id(developer.getId(), schema.getId())) {
            throw new ResourceConflictException("El desarrollador ya tiene acceso a este esquema");
        }

        Usuario grantedBy = usuarioRepository.findById(grantedByPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario administrador no encontrado"));

        DeveloperSchemaAccess access = new DeveloperSchemaAccess();
        access.setSchema(schema);
        access.setUsuario(developer);
        access.setGrantedBy(grantedBy);

        return toResponse(developerSchemaAccessRepository.save(access));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SchemaAccessResponse> findAllBySchema(Long schemaId) {
        documentedSchemaRepository.findById(schemaId)
                .orElseThrow(() -> new ResourceNotFoundException("Esquema documentado no encontrado"));

        return developerSchemaAccessRepository.findAllBySchema_Id(schemaId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public void revokeAccess(Long schemaId, Long developerUserId) {
        documentedSchemaRepository.findById(schemaId)
                .orElseThrow(() -> new ResourceNotFoundException("Esquema documentado no encontrado"));

        DeveloperSchemaAccess access = developerSchemaAccessRepository.findByUsuario_IdAndSchema_Id(developerUserId, schemaId)
                .orElseThrow(() -> new ResourceNotFoundException("No existe un acceso asignado para este desarrollador"));

        developerSchemaAccessRepository.delete(access);
    }

    private SchemaAccessResponse toResponse(DeveloperSchemaAccess access) {
        return new SchemaAccessResponse(
                access.getId(),
                access.getSchema().getId(),
                access.getUsuario().getId(),
                access.getUsuario().getCorreo(),
                access.getUsuario().getNombre(),
                access.getGrantedBy() == null ? null : access.getGrantedBy().getId(),
                access.getGrantedBy() == null ? null : access.getGrantedBy().getCorreo(),
                access.getGrantedAt()
        );
    }
}
