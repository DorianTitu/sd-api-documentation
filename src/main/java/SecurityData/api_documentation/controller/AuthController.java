package SecurityData.api_documentation.controller;

import SecurityData.api_documentation.message.ApiResponse;
import SecurityData.api_documentation.message.AuthenticatedUserResponse;
import SecurityData.api_documentation.message.LoginRequest;
import SecurityData.api_documentation.message.LoginResponse;
import SecurityData.api_documentation.message.UsuarioRequest;
import SecurityData.api_documentation.message.UsuarioRegistroResponse;
import SecurityData.api_documentation.security.UsuarioPrincipal;
import SecurityData.api_documentation.services.interfaces.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/registro")
    public ResponseEntity<ApiResponse<UsuarioRegistroResponse>> crearUsuario(@Valid @RequestBody UsuarioRequest request) {
        UsuarioRegistroResponse response = authService.crearUsuario(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Usuario registrado correctamente", response));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Autenticacion exitosa", response));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<AuthenticatedUserResponse>> me(
            @AuthenticationPrincipal UsuarioPrincipal usuarioPrincipal
    ) {
        UsuarioPrincipal usuario = authService.obtenerUsuarioAutenticado(usuarioPrincipal.getCorreo());
        AuthenticatedUserResponse response = new AuthenticatedUserResponse(
                usuario.getId(),
                usuario.getCorreo(),
                usuario.getNombre(),
                usuario.getTipoUsuario()
        );
        return ResponseEntity.ok(ApiResponse.success("Usuario autenticado", response));
    }
}
