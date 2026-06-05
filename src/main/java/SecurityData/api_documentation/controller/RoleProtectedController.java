package SecurityData.api_documentation.controller;

import SecurityData.api_documentation.message.ApiResponse;
import SecurityData.api_documentation.security.UsuarioPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
public class RoleProtectedController {

    @GetMapping("/admin/solo-admin")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<ApiResponse<String>> soloAdmin(@AuthenticationPrincipal UsuarioPrincipal usuarioPrincipal) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Acceso autorizado para administrador",
                        "Bienvenido " + usuarioPrincipal.getNombre()
                )
        );
    }

    @GetMapping("/developer/solo-desarrollador")
    @PreAuthorize("hasRole('DESARROLLADOR')")
    public ResponseEntity<ApiResponse<String>> soloDesarrollador(
            @AuthenticationPrincipal UsuarioPrincipal usuarioPrincipal
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Acceso autorizado para desarrollador",
                        "Bienvenido " + usuarioPrincipal.getNombre()
                )
        );
    }
}
