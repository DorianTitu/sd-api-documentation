package SecurityData.api_documentation.services.interfaces;

import SecurityData.api_documentation.message.LoginRequest;
import SecurityData.api_documentation.message.LoginResponse;
import SecurityData.api_documentation.message.UsuarioRequest;
import SecurityData.api_documentation.message.UsuarioRegistroResponse;
import SecurityData.api_documentation.security.UsuarioPrincipal;

public interface AuthService {

    UsuarioRegistroResponse crearUsuario(UsuarioRequest request);

    LoginResponse login(LoginRequest request);

    UsuarioPrincipal obtenerUsuarioAutenticado(String correo);
}
