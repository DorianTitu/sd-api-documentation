package SecurityData.api_documentation.controller;

import SecurityData.api_documentation.message.ApiResponse;
import SecurityData.api_documentation.message.DocumentedSchemaRequest;
import SecurityData.api_documentation.message.DocumentedSchemaResponse;
import SecurityData.api_documentation.message.SchemaExtractionResponse;
import SecurityData.api_documentation.message.SchemaAccessGrantRequest;
import SecurityData.api_documentation.message.SchemaAccessResponse;
import SecurityData.api_documentation.message.SchemaUrlRequest;
import SecurityData.api_documentation.security.UsuarioPrincipal;
import SecurityData.api_documentation.services.interfaces.DocumentedSchemaService;
import SecurityData.api_documentation.services.interfaces.SchemaAccessService;
import SecurityData.api_documentation.services.interfaces.SchemaExtractionService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/schemas")
@PreAuthorize("hasRole('ADMINISTRADOR')")
public class SchemaController {

    private final SchemaExtractionService schemaExtractionService;
    private final DocumentedSchemaService documentedSchemaService;
    private final SchemaAccessService schemaAccessService;

    public SchemaController(
            SchemaExtractionService schemaExtractionService,
            DocumentedSchemaService documentedSchemaService,
            SchemaAccessService schemaAccessService
    ) {
        this.schemaExtractionService = schemaExtractionService;
        this.documentedSchemaService = documentedSchemaService;
        this.schemaAccessService = schemaAccessService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<DocumentedSchemaResponse>> create(
            @Valid @RequestBody DocumentedSchemaRequest request
    ) {
        DocumentedSchemaResponse response = documentedSchemaService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Esquema documentado creado correctamente", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<DocumentedSchemaResponse>>> findAll() {
        return ResponseEntity.ok(
                ApiResponse.success("Esquemas documentados obtenidos correctamente", documentedSchemaService.findAll())
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DocumentedSchemaResponse>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.success("Esquema documentado obtenido correctamente", documentedSchemaService.findById(id))
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<DocumentedSchemaResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody DocumentedSchemaRequest request
    ) {
        return ResponseEntity.ok(
                ApiResponse.success("Esquema documentado actualizado correctamente", documentedSchemaService.update(id, request))
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        documentedSchemaService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Esquema documentado eliminado correctamente"));
    }

    @PostMapping("/{id}/accesses")
    public ResponseEntity<ApiResponse<SchemaAccessResponse>> grantAccess(
            @PathVariable Long id,
            @Valid @RequestBody SchemaAccessGrantRequest request,
            @AuthenticationPrincipal UsuarioPrincipal usuarioPrincipal
    ) {
        SchemaAccessResponse response = schemaAccessService.grantAccess(id, request, usuarioPrincipal);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Acceso al esquema asignado correctamente", response));
    }

    @GetMapping("/{id}/accesses")
    public ResponseEntity<ApiResponse<List<SchemaAccessResponse>>> findAccessesBySchema(@PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.success("Accesos del esquema obtenidos correctamente", schemaAccessService.findAllBySchema(id))
        );
    }

    @DeleteMapping("/{id}/accesses/{developerUserId}")
    public ResponseEntity<ApiResponse<Void>> revokeAccess(
            @PathVariable Long id,
            @PathVariable Long developerUserId
    ) {
        schemaAccessService.revokeAccess(id, developerUserId);
        return ResponseEntity.ok(ApiResponse.success("Acceso al esquema revocado correctamente"));
    }

    @PostMapping("/extract")
    public ResponseEntity<ApiResponse<SchemaExtractionResponse>> extractSchema(
            @Valid @RequestBody SchemaUrlRequest request
    ) {
        SchemaExtractionResponse response = schemaExtractionService.extractFromUrl(request.url());
        return ResponseEntity.ok(ApiResponse.success("Esquema obtenido correctamente", response));
    }
}
