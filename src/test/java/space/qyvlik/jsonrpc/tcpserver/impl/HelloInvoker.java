package space.qyvlik.jsonrpc.tcpserver.impl;

import com.alibaba.fastjson.JSONArray;
import space.qyvlik.jsonrpc.tcpserver.mapper.IJsonRpcInvoker;
import space.qyvlik.jsonrpc.tcpserver.mapper.JsonRpcResultFuture;

import java.util.concurrent.atomic.AtomicLong;

public class HelloInvoker implements IJsonRpcInvoker {

    final AtomicLong userId = new AtomicLong(0);

    private static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (Exception e) {

        }
    }

    @Override
    public void call(final long requestIndex, final Long id, final JSONArray params, final JsonRpcResultFuture resultFuture) {
        new Thread() {
            @Override
            public void run() {
                HelloInvoker.sleep(1000);
                resultFuture.result(requestIndex, id, "hello:" + userId.incrementAndGet());
            }
        }.start();
    }

}
