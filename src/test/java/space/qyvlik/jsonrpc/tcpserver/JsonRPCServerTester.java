package space.qyvlik.jsonrpc.tcpserver;

import org.junit.Test;
import space.qyvlik.jsonrpc.tcpserver.impl.HelloInvoker;
import space.qyvlik.jsonrpc.tcpserver.impl.PingInvoker;
import space.qyvlik.jsonrpc.tcpserver.mapper.JsonRpcMapperBuilder;

public class JsonRPCServerTester {

    @Test
    public void testStartJsonRpcServer() throws Exception {
        int port = 10101;
        new JsonRPCServer(
                new JsonRpcMapperBuilder()
                        .add("ping", new PingInvoker())
                        .add("hello", new HelloInvoker())
                        .build()
        ).start(port);
    }

}
