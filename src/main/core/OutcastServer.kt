import com.google.gson.JsonParser
import com.sun.net.httpserver.HttpServer
import java.io.File
import java.io.FileReader
import java.net.InetSocketAddress
import java.sql.Connection
import java.sql.DriverManager

fun main(args : Array<String>) {
    OutcastServer();
}

class OutcastServer {
    val server: HttpServer;

    init {
        val password = JsonParser()
                .parse(FileReader(File(javaClass.classLoader.getResource("secret.json").toURI())))
                .asJsonObject.get("dbPassword").asString;
        Class.forName("org.postgresql.Driver");
        val connection: Connection? = DriverManager.getConnection(
                "jdbc:postgresql://localhost:5432/Outcast?characterEncoding=utf8",
                "OutcastServer",
                password
        );

        if (connection != null) {
            connection.createStatement().execute("CREATE TABLE Feeds (title text, url text, " +
                    "creationDate timestamp DEFAULT(now()), updatedDate timestamp DEFAULT(now())");
            server = HttpServer.create(InetSocketAddress(80), 0);
            server.createContext("/", Handler(connection));
            server.executor = null;
            server.start();
        }
        else {
            throw RuntimeException("ERROR: Could not open connection to Outcast database.");
        }
    }
}