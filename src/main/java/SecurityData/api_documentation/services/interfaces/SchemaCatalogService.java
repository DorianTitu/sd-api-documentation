package SecurityData.api_documentation.services.interfaces;

import SecurityData.api_documentation.message.DocumentedEndpointResponse;
import SecurityData.api_documentation.message.DocumentedSchemaResponse;
import SecurityData.api_documentation.security.UsuarioPrincipal;
import java.util.List;

public interface SchemaCatalogService {

    List<DocumentedSchemaResponse> findAccessibleSchemas(UsuarioPrincipal principal);

    DocumentedSchemaResponse findAccessibleSchemaById(Long schemaId, UsuarioPrincipal principal);

    List<DocumentedEndpointResponse> findAccessibleEndpointsBySchema(Long schemaId, UsuarioPrincipal principal);

    DocumentedEndpointResponse findAccessibleEndpointById(Long schemaId, Long endpointId, UsuarioPrincipal principal);
}
