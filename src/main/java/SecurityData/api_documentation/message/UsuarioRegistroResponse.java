package SecurityData.api_documentation.message;

import SecurityData.api_documentation.enums.TipoUsuario;

public record UsuarioRegistroResponse(
        Long id,
        String correo,
        String nombre,
        TipoUsuario tipoUsuario
) {
}
