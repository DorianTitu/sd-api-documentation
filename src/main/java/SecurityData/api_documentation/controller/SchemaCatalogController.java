package SecurityData.api_documentation.controller;

import SecurityData.api_documentation.message.ApiResponse;
import SecurityData.api_documentation.message.DocumentedEndpointResponse;
import SecurityData.api_documentation.message.DocumentedSchemaResponse;
import SecurityData.api_documentation.security.UsuarioPrincipal;
import SecurityData.api_documentation.services.interfaces.SchemaCatalogService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/schemas")
public class SchemaCatalogController {

    private final SchemaCatalogService schemaCatalogService;

    public SchemaCatalogController(SchemaCatalogService schemaCatalogService) {
        this.schemaCatalogService = schemaCatalogService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<DocumentedSchemaResponse>>> findAccessibleSchemas(Authentication authentication) {
        UsuarioPrincipal principal = resolvePrincipal(authentication);
        return ResponseEntity.ok(
                ApiResponse.success("Esquemas documentados obtenidos correctamente", schemaCatalogService.findAccessibleSchemas(principal))
        );
    }

    @GetMapping("/{schemaId}")
    public ResponseEntity<ApiResponse<DocumentedSchemaResponse>> findAccessibleSchemaById(
            @PathVariable Long schemaId,
            Authentication authentication
    ) {
        UsuarioPrincipal principal = resolvePrincipal(authentication);
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Esquema documentado obtenido correctamente",
                        schemaCatalogService.findAccessibleSchemaById(schemaId, principal)
                )
        );
    }

    @GetMapping("/{schemaId}/endpoints")
    public ResponseEntity<ApiResponse<List<DocumentedEndpointResponse>>> findAccessibleEndpointsBySchema(
            @PathVariable Long schemaId,
            Authentication authentication
    ) {
        UsuarioPrincipal principal = resolvePrincipal(authentication);
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Endpoints documentados obtenidos correctamente",
                        schemaCatalogService.findAccessibleEndpointsBySchema(schemaId, principal)
                )
        );
    }

    @GetMapping("/{schemaId}/endpoints/{endpointId}")
    public ResponseEntity<ApiResponse<DocumentedEndpointResponse>> findAccessibleEndpointById(
            @PathVariable Long schemaId,
            @PathVariable Long endpointId,
            Authentication authentication
    ) {
        UsuarioPrincipal principal = resolvePrincipal(authentication);
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Endpoint documentado obtenido correctamente",
                        schemaCatalogService.findAccessibleEndpointById(schemaId, endpointId, principal)
                )
        );
    }

    private UsuarioPrincipal resolvePrincipal(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof UsuarioPrincipal principal)) {
            return null;
        }
        return principal;
    }
}
