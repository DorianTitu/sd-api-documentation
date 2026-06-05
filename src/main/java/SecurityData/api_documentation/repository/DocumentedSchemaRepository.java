package SecurityData.api_documentation.repository;

import SecurityData.api_documentation.model.DocumentedSchema;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentedSchemaRepository extends JpaRepository<DocumentedSchema, Long> {

    boolean existsBySourceUrl(String sourceUrl);

    Optional<DocumentedSchema> findBySourceUrl(String sourceUrl);
}
