package SecurityData.api_documentation.services.interfaces;

import SecurityData.api_documentation.message.DocumentedSchemaRequest;
import SecurityData.api_documentation.message.DocumentedSchemaResponse;
import java.util.List;

public interface DocumentedSchemaService {

    DocumentedSchemaResponse create(DocumentedSchemaRequest request);

    List<DocumentedSchemaResponse> findAll();

    DocumentedSchemaResponse findById(Long id);

    DocumentedSchemaResponse update(Long id, DocumentedSchemaRequest request);

    void delete(Long id);
}
