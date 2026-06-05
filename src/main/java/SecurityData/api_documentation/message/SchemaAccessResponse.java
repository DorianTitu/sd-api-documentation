package SecurityData.api_documentation.message;

import java.time.Instant;

public record SchemaAccessResponse(
        Long id,
        Long schemaId,
        Long developerUserId,
        String developerCorreo,
        String developerNombre,
        Long grantedByUserId,
        String grantedByCorreo,
        Instant grantedAt
) {
}
