package space.qyvlik.jsonrpc.httpserver.client;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

public class JsonRpcClientSet {

    private volatile JsonRpcClient[] jsonRpcClientList;
    private AtomicLong callNum;
    private int clientSize;
    private Executor executor;
    private String host;
    private int port;

    public JsonRpcClientSet(int clientSize, final String host, final int port) {
        if (clientSize < 0) {
            throw new RuntimeException("JsonRpcClientSet create fail clientSize is less than zero");
        }
        callNum = new AtomicLong(0);
        this.clientSize = clientSize;
        executor = Executors.newFixedThreadPool(clientSize * 2);
        this.host = host;
        this.port = port;

        jsonRpcClientList = new JsonRpcClient[clientSize];

        for (int index = 0; index < clientSize; index++) {
            startJsonRpcClient(index);
        }
    }

    public JsonRpcClient pollingJsonRpcClient() {
        return jsonRpcClientList[(int) (callNum.getAndIncrement() % clientSize)];
    }

    protected JsonRpcClient startJsonRpcClient(int index) {
        final JsonRpcClient jsonRpcClient = new JsonRpcClient();
        jsonRpcClientList[index] = jsonRpcClient;
        executor.execute(new Runnable() {
            @Override
            public void run() {
                jsonRpcClient.start(host, port);
            }
        });
        return jsonRpcClient;
    }
}
