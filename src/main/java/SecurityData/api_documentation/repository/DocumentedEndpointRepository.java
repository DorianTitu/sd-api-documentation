package SecurityData.api_documentation.repository;

import SecurityData.api_documentation.model.DocumentedEndpoint;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentedEndpointRepository extends JpaRepository<DocumentedEndpoint, Long> {

    boolean existsBySchema_IdAndPathAndMethod(Long schemaId, String path, String method);

    Optional<DocumentedEndpoint> findBySchema_IdAndPathAndMethod(Long schemaId, String path, String method);

    List<DocumentedEndpoint> findAllBySchema_Id(Long schemaId);

    Optional<DocumentedEndpoint> findByIdAndSchema_Id(Long id, Long schemaId);
}
