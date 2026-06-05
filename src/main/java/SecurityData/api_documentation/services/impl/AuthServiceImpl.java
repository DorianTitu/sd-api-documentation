package SecurityData.api_documentation.services.impl;

import SecurityData.api_documentation.exceptions.AuthenticationFailedException;
import SecurityData.api_documentation.exceptions.ResourceConflictException;
import SecurityData.api_documentation.message.AuthenticatedUserResponse;
import SecurityData.api_documentation.message.LoginRequest;
import SecurityData.api_documentation.message.LoginResponse;
import SecurityData.api_documentation.message.UsuarioRequest;
import SecurityData.api_documentation.message.UsuarioRegistroResponse;
import SecurityData.api_documentation.model.Usuario;
import SecurityData.api_documentation.repository.UsuarioRepository;
import SecurityData.api_documentation.security.JwtService;
import SecurityData.api_documentation.security.UsuarioPrincipal;
import SecurityData.api_documentation.services.interfaces.AuthService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthServiceImpl implements AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthServiceImpl(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtService jwtService
    ) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @Override
    @Transactional
    public UsuarioRegistroResponse crearUsuario(UsuarioRequest request) {
        String correoNormalizado = request.correo().trim().toLowerCase();

        if (usuarioRepository.existsByCorreoIgnoreCase(correoNormalizado)) {
            throw new ResourceConflictException("No fue posible completar el registro con los datos enviados");
        }

        Usuario usuario = new Usuario();
        usuario.setCorreo(correoNormalizado);
        usuario.setClave(passwordEncoder.encode(request.clave()));
        usuario.setNombre(request.nombre().trim());
        usuario.setTipoUsuario(request.tipoUsuario());

        Usuario usuarioGuardado = usuarioRepository.save(usuario);
        return new UsuarioRegistroResponse(
                usuarioGuardado.getId(),
                usuarioGuardado.getCorreo(),
                usuarioGuardado.getNombre(),
                usuarioGuardado.getTipoUsuario()
        );
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.correo().trim().toLowerCase(), request.clave())
            );
            UsuarioPrincipal usuarioPrincipal = (UsuarioPrincipal) authentication.getPrincipal();
            String token = jwtService.generateToken(usuarioPrincipal);

            return new LoginResponse(
                    token,
                    "Bearer",
                    jwtService.getExpirationMs() / 1000,
                    new AuthenticatedUserResponse(
                            usuarioPrincipal.getId(),
                            usuarioPrincipal.getCorreo(),
                            usuarioPrincipal.getNombre(),
                            usuarioPrincipal.getTipoUsuario()
                    )
            );
        } catch (BadCredentialsException exception) {
            throw new AuthenticationFailedException("Credenciales invalidas");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioPrincipal obtenerUsuarioAutenticado(String correo) {
        Usuario usuario = usuarioRepository.findByCorreoIgnoreCase(correo.trim().toLowerCase())
                .orElseThrow(() -> new AuthenticationFailedException("Usuario autenticado no disponible"));
        return new UsuarioPrincipal(usuario);
    }
}
