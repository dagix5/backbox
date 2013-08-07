package it.backbox.client.oauth;

import it.backbox.client.rest.bean.BoxFile;
import it.backbox.client.rest.bean.BoxFolder;
import it.backbox.client.rest.bean.BoxItem;
import it.backbox.client.rest.bean.BoxSearchResult;
import it.backbox.client.rest.bean.BoxUploadedFile;
import it.backbox.utility.Utility;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.AbstractHandler;

import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonGenerator;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.Key;

public class Local401Server {
	private static Logger _log = Logger.getLogger(Local401Server.class.getCanonicalName());

	private static final String LOCALHOST = "127.0.0.1";
	private static final int PORT = 8080;
	private static final String FOLDER = "C:\\Users\\daniele.giardino\\Desktop\\test\\";
	
	private static final JsonFactory JSON_FACTORY = new JacksonFactory();

	/** Server or {@code null} before {@link #getRedirectUri()}. */
	private static Server server;

	static class ResponseHandler extends AbstractHandler {
		
		private long start = new Date().getTime();

		public ResponseHandler() throws SecurityException, IOException {
			FileHandler fh = new FileHandler("fakeServerLog", 2097152, 3, true);
			fh.setFormatter(new SimpleFormatter());
			fh.setLevel(Level.ALL);
			_log.addHandler(fh);
		}
		
		class TokenResponse {
			@Key
			public String access_token;
			
			@Key
			public String refresh_token;
			
			@Key
			public String token_type;
			
			@Key
			public int expires_in;
		}
		
		@Override
		public void handle(String target, HttpServletRequest request, HttpServletResponse response, int dispatch) throws IOException, ServletException {
			int status = 0;
			
			if (target.equals("/authorize")) {
				String callback = request.getParameter("redirect_uri");
				String code="12345";
				response.sendRedirect(callback + "?code=" + code);
			} else if (target.equals("/token")) {
				TokenResponse tr = new TokenResponse();
				tr.access_token="12345";
				tr.refresh_token="12345";
				tr.token_type="bearer";
				tr.expires_in=300;
				
				JsonGenerator generator = JSON_FACTORY.createJsonGenerator(response.getWriter());
				generator.serialize(tr);
				generator.close();
				
				status = HttpServletResponse.SC_OK;
				
				start = new Date().getTime();
			} else if (target.equals("/search")) {
				BoxSearchResult sr = new BoxSearchResult();
				sr.entries = new ArrayList<BoxItem>();
				BoxItem i = new BoxItem();
				i.id="12345";
				sr.entries.add(i);
				
				JsonGenerator generator = JSON_FACTORY.createJsonGenerator(response.getWriter());
				generator.serialize(sr);
				generator.close();
				
				status = HttpServletResponse.SC_OK;
			} else if (target.startsWith("/folders")) {
				BoxFolder f = new BoxFolder();
				f.id="12345";
				
				JsonGenerator generator = JSON_FACTORY.createJsonGenerator(response.getWriter());
				generator.serialize(f);
				generator.close();
				
				status = HttpServletResponse.SC_CREATED;
			} else {
				long now = new Date().getTime();
				if ((now - start) > 300000)
					status = HttpServletResponse.SC_UNAUTHORIZED;
				else {
					//upload
					if (request.getMethod().equals("POST")) {
						InputStream in = request.getInputStream();
						String id = Utility.genID();
						OutputStream out = Utility.getOutputStream(FOLDER + id);
						out.write(in.read());
						out.close();
						
						BoxUploadedFile uf = new BoxUploadedFile();
						uf.entries = new ArrayList<BoxFile>();
						BoxFile bf = new BoxFile();
						bf.id = id;
						bf.name = id;
						uf.entries.add(bf);

						JsonGenerator generator = JSON_FACTORY.createJsonGenerator(response.getWriter());
						generator.serialize(uf);
						generator.close();
						
						status = HttpServletResponse.SC_CREATED;
					}
				}
			}
			_log.info(target + " -> " + status);
			
			response.setStatus(status);
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
