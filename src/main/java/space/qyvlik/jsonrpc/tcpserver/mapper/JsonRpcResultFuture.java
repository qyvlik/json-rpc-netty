package space.qyvlik.jsonrpc.tcpserver.mapper;

import com.alibaba.fastjson.JSONObject;

public interface JsonRpcResultFuture {

    default void error(Long id, JSONObject error) {
    }

    default void result(Long id, Object result) {
    }
}
