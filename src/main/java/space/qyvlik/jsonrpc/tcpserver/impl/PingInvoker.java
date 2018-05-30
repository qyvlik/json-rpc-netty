package space.qyvlik.jsonrpc.tcpserver.impl;

import com.alibaba.fastjson.JSONArray;
import space.qyvlik.jsonrpc.tcpserver.mapper.IJsonRpcInvoker;
import space.qyvlik.jsonrpc.tcpserver.mapper.JsonRpcResultFuture;

public class PingInvoker implements IJsonRpcInvoker {
    @Override
    public void call(Long id, JSONArray params, JsonRpcResultFuture resultFuture) {
        resultFuture.result(id, System.currentTimeMillis());
    }
}
