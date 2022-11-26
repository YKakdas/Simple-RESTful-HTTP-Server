import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

public class StudentHTTPServer {

    public static List<Student> students = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        HttpServer simpleServer = HttpServer.create(new InetSocketAddress(80), 0);
        simpleServer.createContext("/students", new StudentHTTPHandler());
        simpleServer.start();
    }
}
