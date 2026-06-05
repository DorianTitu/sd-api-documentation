package SecurityData.api_documentation.repository;

import SecurityData.api_documentation.model.Usuario;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    boolean existsByCorreoIgnoreCase(String correo);

    Optional<Usuario> findByCorreoIgnoreCase(String correo);

    Optional<Usuario> findByIdAndTipoUsuario(Long id, SecurityData.api_documentation.enums.TipoUsuario tipoUsuario);
}
