package space.qyvlik.jsonrpc.tcpserver.mapper;

import com.alibaba.fastjson.JSONArray;

public interface IJsonRpcInvoker {
    void call(final long requestIndex, final Long id, final JSONArray params, final JsonRpcResultFuture resultFuture);
}
