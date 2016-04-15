import com.sun.net.httpserver.HttpServer
import java.net.InetSocketAddress

fun main(args : Array<String>) {
    OutcastServer();
}

class OutcastServer {
    val server: HttpServer;

    init {
        server = HttpServer.create(InetSocketAddress(80), 0);
        server.createContext("/", Handler());
        server.executor = null;
        server.start();
    }
}