package SecurityData.api_documentation.services.interfaces;

import SecurityData.api_documentation.message.SchemaAccessGrantRequest;
import SecurityData.api_documentation.message.SchemaAccessResponse;
import SecurityData.api_documentation.security.UsuarioPrincipal;
import java.util.List;

public interface SchemaAccessService {

    SchemaAccessResponse grantAccess(Long schemaId, SchemaAccessGrantRequest request, UsuarioPrincipal grantedByPrincipal);

    List<SchemaAccessResponse> findAllBySchema(Long schemaId);

    void revokeAccess(Long schemaId, Long developerUserId);
}
