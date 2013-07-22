package it.backbox.client.oauth;

import it.backbox.exception.BackBoxException;

import java.awt.Desktop;
import java.awt.Desktop.Action;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.AuthorizationCodeRequestUrl;
import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.ClientParametersAuthentication;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.CredentialStore;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.auth.oauth2.TokenResponseException;
import com.google.api.client.extensions.java7.auth.oauth2.FileCredentialStoreJava7;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;

public class OAuth2Client {
	private static Logger _log = Logger.getLogger(OAuth2Client.class.getCanonicalName());
	
	private static final String CLIENT_ID = "zr56mtgkjibomdnmtje8cer8v8sw3nxe";
	private static final String CLIENT_SECRET = "AkFazArd6LnIifodfHQJ02D1fh90qLs2";
	private static final String TOKEN_SERVER_URL = "https://www.box.com/api/oauth2/token";
	private static final String AUTHORIZATION_SERVER_URL = "https://www.box.com/api/oauth2/authorize";
	private static final String USER_ID = "Box";
	private static final String CREDENTIAL_STORE_FILENAME = "credentialStore.json";
	
	/** Global instance of the JSON factory. */
	private static final JsonFactory JSON_FACTORY = new JacksonFactory();
	
	private static CredentialStore store;

	public static Credential getCredential(HttpTransport httpTransport) throws Exception {
		store = new FileCredentialStoreJava7(new File(CREDENTIAL_STORE_FILENAME), JSON_FACTORY);
		AuthorizationCodeFlow codeFlow = new AuthorizationCodeFlow.Builder(
				BearerToken.authorizationHeaderAccessMethod(), httpTransport,
				JSON_FACTORY, new GenericUrl(TOKEN_SERVER_URL),
				new ClientParametersAuthentication(CLIENT_ID, CLIENT_SECRET),
				CLIENT_ID, AUTHORIZATION_SERVER_URL)
				.setCredentialStore(store)
				.setScopes(Arrays.asList("")).build();
		Credential cred = codeFlow.loadCredential(USER_ID);
		boolean refreshed = false;
		if (cred != null) {
			if (_log.isLoggable(Level.INFO)) _log.info("Credential loaded");
			try {
				refreshed = cred.refreshToken();
			} catch (TokenResponseException e) {
				if (_log.isLoggable(Level.WARNING)) _log.log(Level.WARNING, "Error refreshing token", e);
			}
			if (refreshed) {
				if (_log.isLoggable(Level.INFO)) _log.info("Token refreshed");
				return cred;
			}
		}
		VerificationCodeReceiver receiver = null;
		try {
			receiver = new LocalServerReceiver();
			String redirectUri = receiver.getRedirectUri();
			if (_log.isLoggable(Level.INFO)) _log.info("Server launched");
			launchInBrowser(null, redirectUri, CLIENT_ID);
			if (_log.isLoggable(Level.INFO)) _log.info("Browser opened");
			String code = receiver.waitForCode();
			if (_log.isLoggable(Level.INFO)) _log.info("Waiting for code");
			TokenResponse response = codeFlow.newTokenRequest(code)
					.setRedirectUri(redirectUri)
					.setScopes(Arrays.asList("")).execute();
			cred = codeFlow.createAndStoreCredential(response, USER_ID);
			if (_log.isLoggable(Level.INFO)) _log.info("Credential store created");
		} finally {
			if (receiver != null) {
				receiver.stop();
				if (_log.isLoggable(Level.INFO)) _log.info("Server stopped");
			}
		}
		return cred;
	}

	private static void launchInBrowser(String browser, String redirectUrl, String clientId) throws IOException, BackBoxException {
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
		if (browser == null)
			throw new BackBoxException("Browser not found, impossible open authorization url");

		Runtime.getRuntime().exec(new String[] { browser, authorizationUrl });
			
	}

}
