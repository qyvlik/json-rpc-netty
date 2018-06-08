package space.qyvlik.jsonrpc.tcpserver.impl;

import com.alibaba.fastjson.JSONArray;
import space.qyvlik.jsonrpc.tcpserver.mapper.IJsonRpcInvoker;
import space.qyvlik.jsonrpc.tcpserver.mapper.JsonRpcResultFuture;

public class PingInvoker implements IJsonRpcInvoker {
    @Override
    public void call(final long requestIndex, Long id, JSONArray params, JsonRpcResultFuture resultFuture) {
        resultFuture.result(requestIndex, id, System.currentTimeMillis());
    }
}
