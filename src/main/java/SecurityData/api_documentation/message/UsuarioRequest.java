package SecurityData.api_documentation.message;

import SecurityData.api_documentation.enums.TipoUsuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UsuarioRequest(
        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "El correo no tiene un formato valido")
        String correo,

        @NotBlank(message = "La clave es obligatoria")
        @Size(min = 8, max = 64, message = "La clave debe tener entre 8 y 64 caracteres")
        String clave,

        @NotBlank(message = "El nombre es obligatorio")
        @Size(min = 3, max = 120, message = "El nombre debe tener entre 3 y 120 caracteres")
        String nombre,

        @NotNull(message = "El tipo de usuario es obligatorio")
        TipoUsuario tipoUsuario
) {
}
