package space.qyvlik.jsonrpc.httpserver.client;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class JsonRpcClientSet {
    private volatile JsonRpcClient[] jsonRpcClientList;
    private AtomicLong callNum;
    private int clientSize;
    private Executor executor;
    private ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);

    private String host;
    private int port;

    public JsonRpcClientSet(int clientSize, final String host, final int port) {
        if (clientSize < 0) {
            throw new RuntimeException("JsonRpcClientSet create fail clientSize is less than zero");
        }
        callNum = new AtomicLong(0);
        this.clientSize = clientSize;
        executor = Executors.newFixedThreadPool(clientSize * +1);
        this.host = host;
        this.port = port;

        jsonRpcClientList = new JsonRpcClient[this.clientSize];

        for (int index = 0; index < this.clientSize; index++) {
            restartJsonRpcClient(index, true);
        }

        scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                maintainClients();
            }
        }, 5, 1, TimeUnit.SECONDS);

    }

    public JsonRpcClient pollingJsonRpcClient() {
        return jsonRpcClientList[(int) (callNum.getAndIncrement() % clientSize)];
    }

    protected JsonRpcClient restartJsonRpcClient(final int index, boolean create) {
        if (create) {
            jsonRpcClientList[index] = new JsonRpcClient(index);
        }
        final JsonRpcClient jsonRpcClient = jsonRpcClientList[index];
        executor.execute(new Runnable() {
            @Override
            public void run() {
                jsonRpcClient.start(host, port);
            }
        });
        return jsonRpcClient;
    }

    private void maintainClients() {
        for (int index = 0; index < JsonRpcClientSet.this.clientSize; index++) {
            JsonRpcClient jsonRpcClient = jsonRpcClientList[index];

            if (jsonRpcClient.isActive()) {
                continue;
            }

            if (!jsonRpcClient.isActive() && jsonRpcClient.isConnecting()) {
                continue;
            }

            restartJsonRpcClient(index, false);
        }
    }
}