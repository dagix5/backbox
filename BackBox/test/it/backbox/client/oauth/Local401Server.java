package it.backbox.client.oauth;

import java.io.IOException;
import java.util.Scanner;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.AbstractHandler;

public class Local401Server {

	private static final String LOCALHOST = "127.0.0.1";
	private static final int PORT = 8080;

	/** Server or {@code null} before {@link #getRedirectUri()}. */
	private static Server server;

	static class ResponseHandler extends AbstractHandler {

		@Override
		public void handle(String target, HttpServletRequest request,
				HttpServletResponse response, int dispatch) throws IOException,
				ServletException {
			System.out.println("Request: " + target);
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			((Request) request).setHandled(true);
		}

	}

	public static void main(String args[]) throws Exception {
		server = new Server(PORT);
		for (Connector c : server.getConnectors()) {
			c.setHost(LOCALHOST);
		}
		server.addHandler(new ResponseHandler());
		server.start();
		
		Scanner s = new Scanner(System.in);
		s.nextLine();
		s.close();

		server.stop();
		server = null;
	}

}
