import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import java.io.*
import java.net.URI
import java.net.URL
import java.nio.file.Files
import java.sql.Connection
import java.sql.DriverManager

data class Feed(val title: String, val location: String);

class Handler(val dbConnection: Connection): HttpHandler {
    val webRoot = "website";

    override fun handle(exchange: HttpExchange?) {
        if (exchange != null) {
            when (exchange.requestMethod) {
                "GET" -> parseGET(exchange);
                "POST" -> parsePOST(exchange);
            }
        }
    }

    private fun parsePOST(exchange: HttpExchange) {

    }

    private fun parseGET(exchange: HttpExchange) {
        println("---------------------------------");
        println("GET ${exchange.requestURI.path} received: ")
        val out = exchange.responseBody;
        if (exchange.requestURI.path.equals("/")) {
            println("Redirecting.");
            val hostPath = exchange.requestHeaders.getFirst("Host");
            exchange.responseHeaders.set("Location", "http://$hostPath/index.html");
            exchange.sendResponseHeaders(301, 0.toLong());
            out.write("".toByteArray());
            out.close();
        }
        else if (exchange.requestURI.path.equals("/feeds")) {
            val json = getFeeds();
            exchange.responseHeaders.set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, 0.toLong());

            val writer = OutputStreamWriter(out, Charsets.UTF_8);
            writer.write(json, 0, json.length);
            writer.close(); // closes out stream as well
        }
        else {
            val requestPath = webRoot + exchange.requestURI.path;
            println("Attemping to find file at $requestPath");
            val resource: URL? = javaClass.classLoader.getResource(requestPath);
            val fileUri: URI? = resource?.toURI() ?: null;

            var file: File? = null;
            if (fileUri != null) {
                file = File(fileUri);
            }

            if (file?.exists() ?: false) {
                val mimeType: String = Files.probeContentType(file!!.toPath()) ?: getFileType(file.path);

                println("Found file. Sending. File type: $mimeType");
                exchange.responseHeaders.set("Content-Type", mimeType);
                exchange.sendResponseHeaders(200, 0.toLong());
                val fs = file.inputStream();
                val buffer = ByteArray(0x10000);

                var count = fs.read(buffer);
                while (count >= 0) {
                    out.write(buffer,0,count);
                    count = fs.read(buffer);
                }
                fs.close();
                out.close();
            }
            else {
                println("File not found.");
                val response = "404 (Not Found)\n";
                exchange.sendResponseHeaders(404, response.length.toLong());
                out.write(response.toByteArray());
                out.close();
            }
        }
    }

    private fun getFeeds(): String {
        val feeds = mutableListOf<Feed>();

        val dbResult = dbConnection.createStatement().executeQuery("SELECT title, location FROM Feeds");
        while (dbResult.next()) {
            val title: String? = dbResult.getString("title");
            val location: String? = dbResult.getString("location");
            if (title != null && location != null) {
                feeds.add(Feed(title, location));
            }
            else {
                println("ERROR accessing feed row ${dbResult.row}: title=$title, location=$location");
            }
        }

        val json = Gson().toJson(feeds);
        println(json);
        return json;
    }

    private fun getFileType(path: String): String {
        val split = path.split(".");
        val extension = split[split.size - 1];

        when (extension) {
            "js" -> return "text/javascript";
            "html" -> return "text/html";
            "css" -> return "text/css";
            else -> return "application/octet-stream";
        }
    }
}