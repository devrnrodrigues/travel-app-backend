package com.devrenanrodrigues.travelapi.auth;

import com.devrenanrodrigues.travelapi.auth.dto.GoogleUserInfo;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;

@Service
public class GoogleTokenVerifierService {

    private final GoogleIdTokenVerifier verifier;
    private final Environment environment;

    public GoogleTokenVerifierService(
            @Value("${google.client-id:}") String clientId,
            Environment environment
    ) {
        this.environment = environment;
        GoogleIdTokenVerifier.Builder builder = new GoogleIdTokenVerifier.Builder(
                new NetHttpTransport(),
                GsonFactory.getDefaultInstance()
        );

        if (clientId != null && !clientId.isBlank()) {
            builder.setAudience(Collections.singletonList(clientId));
        }

        this.verifier = builder.build();
    }

    public GoogleUserInfo verify(String idTokenString) {
        if (idTokenString != null && idTokenString.startsWith("dev-mock:")) {
            if (!environment.acceptsProfiles(Profiles.of("local", "test"))) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Mock de autenticação não permitido neste ambiente.");
            }

            String[] parts = idTokenString.split(":");
            String email = parts.length > 1 && !parts[1].isBlank() ? parts[1] : "dev@travelapp.com";
            String name = parts.length > 2 && !parts[2].isBlank() ? parts[2] : "Dev User";
            return new GoogleUserInfo(email, name, "https://lh3.googleusercontent.com/a/default-avatar");
        }

        try {
            GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken == null) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token do Google inválido ou expirado.");
            }

            GoogleIdToken.Payload payload = idToken.getPayload();
            String email = payload.getEmail();
            String name = (String) payload.get("name");
            String pictureUrl = (String) payload.get("picture");

            if (email == null || email.isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email não encontrado no token do Google.");
            }

            return new GoogleUserInfo(email, name != null ? name : email, pictureUrl);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Falha ao validar token do Google: " + e.getMessage(), e);
        }
    }
}
