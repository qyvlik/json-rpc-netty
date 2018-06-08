package space.qyvlik.jsonrpc.httpserver;

public class JsonRpcHttpServerTester {

    public static void main(String[] args) throws Exception {
        JsonRpcHttpServer server = new JsonRpcHttpServer("localhost", 10101, 4);
        server.start(10102);
    }
}
