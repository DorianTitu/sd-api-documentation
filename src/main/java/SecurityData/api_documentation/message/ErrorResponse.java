package SecurityData.api_documentation.message;

import java.time.Instant;
import java.util.List;

public record ErrorResponse(
        boolean success,
        String message,
        String errorCode,
        List<String> details,
        Instant timestamp
) {

    public static ErrorResponse of(String message, String errorCode, List<String> details) {
        return new ErrorResponse(false, message, errorCode, details, Instant.now());
    }
}
