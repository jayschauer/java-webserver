import java.io.IOException;
import java.io.OutputStream;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

/*
 * Serves static files found in the given directory. Assumes index.html exists in the directory.
 */
public class StaticHandler implements HttpHandler {
	
	private Path htmlDir;
	
	private static FileNameMap mimeTypes = URLConnection.getFileNameMap();
	
	// Serves files found in the directory
	public StaticHandler(Path htmlDirectory) {
		htmlDir = htmlDirectory;
	}

	@Override
	public void handle(HttpExchange exchange) throws IOException {
		// step 1 - determine the command (GET, POST, etc)
		String method = exchange.getRequestMethod();
		if(!method.equals("GET")) {
			exchange.sendResponseHeaders(405, -1); // Error - Method Not Allowed
			exchange.close();
			return;
		}
				
		// step 2 - examine headers if needed
		// Headers requestHeaders = exchange.getRequestHeaders();
		
		// step 3 - read the body if needed
		// InputStream requestBody = exchange.getRequestBody();
		
		// step 4 - create the response
		String requestPath = exchange.getRequestURI().getPath().substring(1);
		Path filePath = htmlDir.resolve(requestPath);
		try {
			filePath = filePath.toRealPath();
		} catch(IOException e) {
			exchange.sendResponseHeaders(404, -1); // Error - Not Found
			exchange.close();
			return;
		}
		
		if(!filePath.startsWith(htmlDir)) {
			exchange.sendResponseHeaders(403, -1); // Error - Forbidden
			exchange.close();
			return;
		}
		
		if(filePath.equals(htmlDir)) {
			filePath = filePath.resolve("index.html");
		}
		
		if(!Files.isRegularFile(filePath)) {
			exchange.sendResponseHeaders(400, -1); // Bad Request - cannot get something that's not a file
			exchange.close();
			return;
		}
		
		System.out.println("Serving " + filePath.toString());
						
		// step 5 - set response headers (e.g. mime type)
		Headers responseHeaders = exchange.getResponseHeaders();
		responseHeaders.set("Content-Type", mimeTypes.getContentTypeFor(filePath.getFileName().toString()));
		
		// step 6 - send the headers
		exchange.sendResponseHeaders(200, Files.size(filePath));
		
		// step 7 - write the response body
		OutputStream responseBody = exchange.getResponseBody(); // use the stream to write the response body. close when finished
		Files.copy(filePath, responseBody);
		
		// step 8 - cleanup
		exchange.close(); // closes input/output
	}
}
