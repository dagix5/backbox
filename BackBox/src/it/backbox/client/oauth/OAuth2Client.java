package it.backbox.client.oauth;

import java.awt.Desktop;
import java.awt.Desktop.Action;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.AuthorizationCodeRequestUrl;
import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.ClientParametersAuthentication;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.CredentialStore;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.extensions.java7.auth.oauth2.FileCredentialStoreJava7;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;

public class OAuth2Client {
	
	private static final String CLIENT_ID = "7d8cdaq9h09o23wvkhauznnwho1w1bcz";
	private static final String CLIENT_SECRET = "j0dRkC57zpcmihxB4KgRdYScOBaan0Hi";
	private static final String TOKEN_SERVER_URL = "https://www.box.com/api/oauth2/token";
	private static final String AUTHORIZATION_SERVER_URL = "https://www.box.com/api/oauth2/authorize";
	private static final String USER_ID = "Box";
	private static final String CREDENTIAL_STORE_FILENAME = "credentialStore.json";
	
	/** Global instance of the HTTP transport. */
	private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();

	/** Global instance of the JSON factory. */
	static final JsonFactory JSON_FACTORY = new JacksonFactory();

	public static Credential getCredential() throws Exception {
		VerificationCodeReceiver receiver = null;
		Credential credential = null;
		try {
			CredentialStore store = new FileCredentialStoreJava7(new File(CREDENTIAL_STORE_FILENAME), JSON_FACTORY);
			credential = createEmptyCredential();
			if (!store.load(USER_ID, credential)) {
				receiver = new LocalServerReceiver();
				String redirectUri = receiver.getRedirectUri();
				launchInBrowser(null, redirectUri, CLIENT_ID);
	
				credential = authorize(receiver, redirectUri);
			}
						
		} finally {
			if (receiver != null)
				receiver.stop();
		}
		return credential;
	}

	private static Credential authorize(VerificationCodeReceiver receiver, String redirectUri) throws IOException {
		String code = receiver.waitForCode();
		AuthorizationCodeFlow codeFlow = new AuthorizationCodeFlow.Builder(
				BearerToken.authorizationHeaderAccessMethod(), HTTP_TRANSPORT,
				JSON_FACTORY, new GenericUrl(TOKEN_SERVER_URL),
				new ClientParametersAuthentication(CLIENT_ID, CLIENT_SECRET),
				CLIENT_ID, AUTHORIZATION_SERVER_URL)
				.setCredentialStore(new FileCredentialStoreJava7(new File(CREDENTIAL_STORE_FILENAME), JSON_FACTORY))
				.setScopes(Arrays.asList("")).build();

		TokenResponse response = codeFlow.newTokenRequest(code)
				.setRedirectUri(redirectUri).setScopes(Arrays.asList(""))
				.execute();

		return codeFlow.createAndStoreCredential(response, USER_ID);
	}

	private static void launchInBrowser(String browser, String redirectUrl, String clientId) throws IOException {
		String authorizationUrl = new AuthorizationCodeRequestUrl(
				AUTHORIZATION_SERVER_URL, clientId).setRedirectUri(redirectUrl)
				.setScopes(Arrays.asList("")).build();
		if (Desktop.isDesktopSupported()) {
			Desktop desktop = Desktop.getDesktop();
			if (desktop.isSupported(Action.BROWSE)) {
				desktop.browse(URI.create(authorizationUrl));
				return;
			}
		}
		if (browser != null) {
			Runtime.getRuntime().exec(new String[] { browser, authorizationUrl });
		} else {
			System.out.println("Open the following address in your favorite browser:");
			System.out.println("  " + authorizationUrl);
		}
	}
	
	private static Credential createEmptyCredential() {
		Credential access = new Credential.Builder(
				BearerToken.authorizationHeaderAccessMethod())
				.setTransport(HTTP_TRANSPORT)
				.setJsonFactory(JSON_FACTORY)
				.setTokenServerEncodedUrl(TOKEN_SERVER_URL)
				.setClientAuthentication(
						new ClientParametersAuthentication(CLIENT_ID, CLIENT_SECRET)).build();
		return access;
	}

}
