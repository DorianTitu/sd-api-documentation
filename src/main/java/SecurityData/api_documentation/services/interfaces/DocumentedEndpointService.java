package SecurityData.api_documentation.services.interfaces;

import SecurityData.api_documentation.message.DocumentedEndpointRequest;
import SecurityData.api_documentation.message.DocumentedEndpointResponse;
import java.util.List;

public interface DocumentedEndpointService {

    DocumentedEndpointResponse create(DocumentedEndpointRequest request);

    List<DocumentedEndpointResponse> findAll();

    DocumentedEndpointResponse findById(Long id);

    DocumentedEndpointResponse update(Long id, DocumentedEndpointRequest request);

    void delete(Long id);
}
