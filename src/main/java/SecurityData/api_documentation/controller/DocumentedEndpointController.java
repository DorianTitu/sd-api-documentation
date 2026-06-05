package SecurityData.api_documentation.controller;

import SecurityData.api_documentation.message.ApiResponse;
import SecurityData.api_documentation.message.DocumentedEndpointRequest;
import SecurityData.api_documentation.message.DocumentedEndpointResponse;
import SecurityData.api_documentation.services.interfaces.DocumentedEndpointService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
@RequestMapping("/admin/endpoints")
@PreAuthorize("hasRole('ADMINISTRADOR')")
public class DocumentedEndpointController {

    private final DocumentedEndpointService documentedEndpointService;

    public DocumentedEndpointController(DocumentedEndpointService documentedEndpointService) {
        this.documentedEndpointService = documentedEndpointService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<DocumentedEndpointResponse>> create(
            @Valid @RequestBody DocumentedEndpointRequest request
    ) {
        DocumentedEndpointResponse response = documentedEndpointService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Endpoint documentado creado correctamente", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<DocumentedEndpointResponse>>> findAll() {
        return ResponseEntity.ok(
                ApiResponse.success("Endpoints documentados obtenidos correctamente", documentedEndpointService.findAll())
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DocumentedEndpointResponse>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.success("Endpoint documentado obtenido correctamente", documentedEndpointService.findById(id))
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<DocumentedEndpointResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody DocumentedEndpointRequest request
    ) {
        return ResponseEntity.ok(
                ApiResponse.success("Endpoint documentado actualizado correctamente", documentedEndpointService.update(id, request))
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        documentedEndpointService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Endpoint documentado eliminado correctamente"));
    }
}
