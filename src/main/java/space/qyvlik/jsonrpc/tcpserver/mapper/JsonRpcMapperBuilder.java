package space.qyvlik.jsonrpc.tcpserver.mapper;


import java.util.LinkedList;
import java.util.List;

public class JsonRpcMapperBuilder {
    private List<Invoker> jsonRpcInvokerList;

    public JsonRpcMapperBuilder() {
        jsonRpcInvokerList = new LinkedList();
    }

    public void clear() {
        jsonRpcInvokerList.clear();
    }

    public JsonRpcMapperBuilder add(String method, IJsonRpcInvoker invoker) {
        jsonRpcInvokerList.add(new Invoker(method, invoker));
        return this;
    }

    public JsonRpcMapper build() {
        JsonRpcMapper jsonRpcMapper = new JsonRpcMapper();

        for (Invoker invoker : jsonRpcInvokerList) {
            jsonRpcMapper.putInvoker(invoker.method, invoker.invoker);
        }

        return jsonRpcMapper;
    }


    private static class Invoker {
        String method;
        IJsonRpcInvoker invoker;

        public Invoker(String method, IJsonRpcInvoker invoker) {
            this.method = method;
            this.invoker = invoker;
        }
    }
}
