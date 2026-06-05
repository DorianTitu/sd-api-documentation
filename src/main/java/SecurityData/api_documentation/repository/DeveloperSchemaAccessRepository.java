package SecurityData.api_documentation.repository;

import SecurityData.api_documentation.model.DeveloperSchemaAccess;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeveloperSchemaAccessRepository extends JpaRepository<DeveloperSchemaAccess, Long> {

    boolean existsByUsuario_IdAndSchema_Id(Long usuarioId, Long schemaId);

    Optional<DeveloperSchemaAccess> findByUsuario_IdAndSchema_Id(Long usuarioId, Long schemaId);

    List<DeveloperSchemaAccess> findAllBySchema_Id(Long schemaId);
}
