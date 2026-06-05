package SecurityData.api_documentation.exceptions;

public class SchemaFetchException extends RuntimeException {

    public SchemaFetchException(String message) {
        super(message);
    }

    public SchemaFetchException(String message, Throwable cause) {
        super(message, cause);
    }
}
