package space.qyvlik.jsonrpc.tcpserver.mapper;

import com.alibaba.fastjson.JSONArray;

public interface IJsonRpcInvoker {
    void call(final Long id, final JSONArray params, final JsonRpcResultFuture resultFuture);
}
