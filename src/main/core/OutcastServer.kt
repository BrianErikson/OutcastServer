import com.sun.net.httpserver.HttpServer
import java.net.InetSocketAddress

class OutcastServer {
    val server: HttpServer;

    init {
        server = HttpServer.create(InetSocketAddress(80), 0);
        server.createContext("", Handler());
    }
}