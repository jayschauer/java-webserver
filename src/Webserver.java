import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.sun.net.httpserver.HttpServer;

public class Webserver {
	
	private static HttpServer server;
	private static ExecutorService threadpool;
	
	public static void main(String[] args) {
		if(args.length < 2) {
			System.err.println("Usage: webserver [html directory] [port]");
			System.exit(1);
		}
						
		String htmlDirectory = args[0];
		int port = Integer.parseInt(args[1]);
		
		Path htmlPath = FileSystems.getDefault().getPath(htmlDirectory);
		
		Path currentRelativePath = Paths.get("");
		String s = currentRelativePath.toAbsolutePath().toString();
		System.out.println("Current working directory is: " + s);

		try {
			htmlPath = htmlPath.toRealPath();
		} catch(IOException e) {
			System.err.println("Unable to find html directory");
			e.printStackTrace();
			System.exit(1);
		}
		
		try {
			server = HttpServer.create();
		} catch (IOException e) {
			System.err.println("Unable to start HttpServer");
			e.printStackTrace();
			System.exit(1);
		}
		
		try {
			server.bind(new InetSocketAddress(port), 0);
		} catch (IOException e) {
			System.err.println("Unable to bind HttpServer to port");
			e.printStackTrace();
			System.exit(1);
		}
		
		server.createContext("/", new StaticHandler(htmlPath));
		threadpool = Executors.newCachedThreadPool();
		server.setExecutor(threadpool);
		server.start();
		
		System.out.println("Server started");
	}
}
