package space.qyvlik.jsonrpc.tcpserver.mapper;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// http://www.jsonrpc.org/specification
public class JsonRpcMapper implements Serializable {
    private final Map<String, IJsonRpcInvoker> invokerMap;

    public JsonRpcMapper() {
        invokerMap = new ConcurrentHashMap();
    }

    public void callMethod(final String jsonRequestString, final JsonRpcResultFuture jsonRpcResultFuture) {
        try {
            if (jsonRequestString.startsWith("{")) {
                JSONObject object = JSON.parseObject(jsonRequestString);
                callMethod(object.getLong("id"),
                        object.getString("method"),
                        object.getJSONArray("params"),
                        jsonRpcResultFuture);
            } else if (jsonRequestString.startsWith("[")) {
                JSONArray array = JSON.parseArray(jsonRequestString);
            } else {

            }
        } catch (Exception e) {
            JSONObject error = new JSONObject();
            error.put("code", -32000);
            error.put("message", "error: " + e.getMessage());
            jsonRpcResultFuture.error(null, error);
        }
    }

    public void putInvoker(final String method, final IJsonRpcInvoker invoker) {
        invokerMap.put(method, invoker);
    }

    public void callMethod(final Long id, final String method, final JSONArray params, final JsonRpcResultFuture resultFuture) {
        if (id == null) {
            JSONObject error = new JSONObject();
            error.put("code", 50001);
            error.put("message", "id is empty");
            resultFuture.error(id, error);
            return;
        }

        if (method == null || method.isEmpty()) {
            JSONObject error = new JSONObject();
            error.put("code", -32601);
            error.put("message", "method is empty");
            resultFuture.error(id, error);
            return;
        }

        IJsonRpcInvoker jsonRpcInvoker = invokerMap.get(method);
        if (jsonRpcInvoker == null) {
            JSONObject error = new JSONObject();
            error.put("code", -32601);
            error.put("message", "method: " + method + " not found");
            resultFuture.error(id, error);
            return;
        }

        if (params == null) {
            JSONObject error = new JSONObject();
            error.put("code", -32602);
            error.put("message", "params is null");
            resultFuture.error(id, error);
        }

        jsonRpcInvoker.call(id, params, resultFuture);
    }
}
