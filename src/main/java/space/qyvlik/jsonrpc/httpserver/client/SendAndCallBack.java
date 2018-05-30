package space.qyvlik.jsonrpc.httpserver.client;

import com.alibaba.fastjson.JSONObject;

public interface SendAndCallBack {
    default void callback(JSONObject jsonObject) {

    }

    default void disConnect() {

    }
}
