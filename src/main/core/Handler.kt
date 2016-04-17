import com.google.gson.Gson
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import com.sun.syndication.feed.synd.SyndFeedImpl
import com.sun.syndication.io.SyndFeedInput
import com.sun.syndication.io.XmlReader
import java.io.*
import java.net.URI
import java.net.URL
import java.nio.file.Files
import java.sql.Connection

data class Feed(val title: String, val url: String, val date: String);

class Handler(val dbConnection: Connection): HttpHandler {
    val webRoot = "website";

    override fun handle(exchange: HttpExchange?) {
        if (exchange != null) {
            println("---------------------------------");
            when (exchange.requestMethod) {
                "GET" -> parseGET(exchange);
                "POST" -> parsePOST(exchange);
            }
        }
    }

    private fun parsePOST(exchange: HttpExchange) {
        val out = exchange.responseBody;

        println("POST ${exchange.requestURI.path} received: ");

        val response = "404 (Not implemented)\n";
        exchange.sendResponseHeaders(404, response.length.toLong());
        out.write(response.toByteArray());
        out.close();
    }

    private fun parseGET(exchange: HttpExchange) {
        val path = if(exchange.requestURI.query != null) exchange.requestURI.path + "?" + exchange.requestURI.query else exchange.requestURI.path;
        println("GET $path received: ")
        val out = exchange.responseBody;
        if (path.equals("/")) {
            println("Redirecting.");
            val hostPath = exchange.requestHeaders.getFirst("Host");
            exchange.responseHeaders.set("Location", "http://$hostPath/index.html");
            exchange.sendResponseHeaders(301, 0.toLong());
            out.write("".toByteArray());
            out.close();
        }
        else if (path.contains("/feeds")) {
            feedsGET(exchange, out);
        }
        else {
            val requestPath = webRoot + path;
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

    private fun feedsGET(exchange: HttpExchange, out: OutputStream) {
        val path = if(exchange.requestURI.query != null) exchange.requestURI.path + "?" + exchange.requestURI.query else exchange.requestURI.path;
        if (path.contains("/add/")) { // /feeds/add/location=http://google.com
            val url = path.substringAfter("/add/location="); // http://google.com

            try {
                val rss = SyndFeedInput().build(XmlReader(URL(url))) as SyndFeedImpl;
                val title: String = rss.title ?: throw RuntimeException("Could not find title for feed $url");


                var result = dbConnection.createStatement().executeUpdate("UPDATE Feeds SET url='${escapeSqlString(url)}', updateddate=now() WHERE title='${escapeSqlString(title)}'");
                if (result <= 0) {
                    result = dbConnection.createStatement()
                            .executeUpdate("INSERT INTO Feeds VALUES('${escapeSqlString(title)}', '${escapeSqlString(url)}')"); // 1 = OK
                }

                if (result <= 0) throw RuntimeException("Could not add $url to the database. Reason unknown.");

                println("Submitted Podcast $title (url=$url) to the database with result code $result");

                exchange.sendResponseHeaders(200, 0.toLong());
                out.close();
            }
            catch (e: Exception) {
                println("ERROR: Could not add feed $url to the database: ${e.message}");
                val response = "ERROR 400: ${e.message}\n";
                exchange.sendResponseHeaders(400, response.length.toLong());
                out.write(response.toByteArray());
                out.close();
            }
        }
        else {
            val json = getDbFeeds();
            exchange.responseHeaders.set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, 0.toLong());

            val writer = OutputStreamWriter(out, Charsets.UTF_8);
            writer.write(json, 0, json.length);
            writer.close(); // closes out stream as well
        }
    }

    private fun escapeSqlString(str: String): String {
        return str.replace("'", "''");
    }

    private fun getDbFeeds(): String {
        val feeds = mutableListOf<Feed>();

        val dbResult = dbConnection.createStatement().executeQuery("SELECT title, url, updatedDate FROM Feeds");
        while (dbResult.next()) {
            val title: String? = dbResult.getString("title");
            val url: String? = dbResult.getString("url");
            val date: String? = dbResult.getString("updatedDate");
            if (title != null && url != null && date != null) {
                feeds.add(Feed(title, url, date));
            }
            else {
                println("ERROR accessing feed row ${dbResult.row}: title=$title, ur=$url");
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