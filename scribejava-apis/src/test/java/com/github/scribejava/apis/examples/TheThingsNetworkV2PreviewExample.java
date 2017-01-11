package com.github.scribejava.apis.examples;

import com.github.scribejava.apis.TheThingsNetworkV2PreviewApi;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth20Service;

import java.io.IOException;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;

public final class TheThingsNetworkV2PreviewExample {

    private static final String NETWORK_NAME = "TTNv2preview";
    private static final String PROTECTED_RESOURCE_URL =
            "https://preview.account.thethingsnetwork.org/api/v2/applications";

    private TheThingsNetworkV2PreviewExample() {
    }

    public static void main(String... args) throws IOException, InterruptedException, ExecutionException {
        // Replace these with your client id and secret
        final String clientId = "your_client_id";
        final String clientSecret = "your_client_secret";
        final String secretState = "secret" + new Random().nextInt(999_999);
        final String redirectURI = "https://your_redirect_uri";

        final OAuth20Service service = new ServiceBuilder()
                .apiKey(clientId)
                .apiSecret(clientSecret)
                .state(secretState)
                .callback(redirectURI)
                .build(TheThingsNetworkV2PreviewApi.instance());
        final Scanner in = new Scanner(System.in, "UTF-8");

        System.out.println("=== " + NETWORK_NAME + "'s OAuth Workflow ===");
        System.out.println();

        // Obtain the Authorization URL
        System.out.println("Fetching the Authorization URL...");
        final String authorizationUrl = service.getAuthorizationUrl();
        System.out.println("Got the Authorization URL!");
        System.out.println("Now go and authorize ScribeJava here:");
        System.out.println(authorizationUrl);
        System.out.println("And paste the authorization code here");
        System.out.print(">>");
        final String code = in.nextLine();
        System.out.println();

        System.out.println("And paste the state from server here. We have set 'secretState'='" + secretState + "'.");
        System.out.print(">>");
        final String value = in.nextLine();
        if (secretState.equals(value)) {
            System.out.println("State value does match!");
        } else {
            System.out.println("Oops, state value does not match!");
            System.out.println("Expected = " + secretState);
            System.out.println("Got      = " + value);
            System.out.println();
        }

        // Trade the Request Token and Verifier for the Access Token
        System.out.println("Trading the Request Token for an Access Token...");
        final OAuth2AccessToken accessToken = service.getAccessToken(code);
        System.out.println("Got the Access Token!");
        System.out.println("(if your curious it looks like this: " + accessToken
                + ", 'rawResponse'='" + accessToken.getRawResponse() + "')");
        System.out.println();

        // Now let's go and ask for a protected resource!
        System.out.println("Now we're going to access a protected resource...");
        final OAuthRequest request = new OAuthRequest(Verb.GET, PROTECTED_RESOURCE_URL);

        // TTN should support both signing the request with a parameter, or with a header.
        // 1. Token as a parameter
        service.signRequest(accessToken, request);
        // 2. Token in the header.
        //request.addHeader("Authorization", "bearer "+accessToken.getAccessToken());
        // And we always expect JSON data.
        request.addHeader("Accept", "application/json");
        final Response response = service.execute(request);
        System.out.println("Got it! Lets see what we found...");
        System.out.println();
        System.out.println(response.getCode());

        if (response.getCode() == 401) {
            System.out.println("Not authorised: "+response.getBody());
        } else {
            System.out.println("You should see a JSON array of your registered applications:");
            System.out.println(response.getBody());

            System.out.println();
            System.out.println("That's it man! Go and build something awesome with ScribeJava! :)");
        }
    }
}
