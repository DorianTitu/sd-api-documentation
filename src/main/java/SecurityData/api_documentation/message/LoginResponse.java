package SecurityData.api_documentation.message;

public record LoginResponse(
        String accessToken,
        String tokenType,
        long expiresIn,
        AuthenticatedUserResponse usuario
) {
}
