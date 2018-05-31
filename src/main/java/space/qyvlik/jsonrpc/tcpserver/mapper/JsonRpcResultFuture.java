package space.qyvlik.jsonrpc.tcpserver.mapper;

import com.alibaba.fastjson.JSONObject;

public interface JsonRpcResultFuture {

    default void error(long requestIndex, Long id, JSONObject error) {
    }

    default void result(long requestIndex, Long id, Object result) {
    }
}
