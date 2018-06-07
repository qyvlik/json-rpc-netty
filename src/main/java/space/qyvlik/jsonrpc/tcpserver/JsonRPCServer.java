package space.qyvlik.jsonrpc.tcpserver;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import space.qyvlik.jsonrpc.tcpserver.impl.HelloInvoker;
import space.qyvlik.jsonrpc.tcpserver.impl.PingInvoker;
import space.qyvlik.jsonrpc.tcpserver.mapper.JsonRpcMapper;
import space.qyvlik.jsonrpc.tcpserver.mapper.JsonRpcMapperBuilder;

public class JsonRPCServer {

    private EventLoopGroup bossGroup = new NioEventLoopGroup();
    private EventLoopGroup workerGroup = new NioEventLoopGroup();
    private ServerBootstrap bootstrap = new ServerBootstrap();
    private JsonRpcMapper jsonRpcMapper;

    public JsonRPCServer(JsonRpcMapper jsonRpcMapper) {
        this.jsonRpcMapper = jsonRpcMapper;
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.err.println(
                    "Usage: " + JsonRPCServer.class.getSimpleName() +
                            " <port>");
            return;
        }
        int port = Integer.parseInt(args[0]);        //1
        new JsonRPCServer(new JsonRpcMapperBuilder()
                .add("hello", new HelloInvoker())
                .add("ping", new PingInvoker())
                .build()).start(port);                //2
    }

    public void start(int port) {
        try {
            bootstrap.group(bossGroup, workerGroup);
            bootstrap.channel(NioServerSocketChannel.class);
            bootstrap.option(ChannelOption.SO_BACKLOG, 1024);
            bootstrap.childHandler(new ChildChannelHandler());
            ChannelFuture channelFuture = bootstrap.bind(port).sync();
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    private class ChildChannelHandler extends
            ChannelInitializer<SocketChannel> {
        @Override
        protected void initChannel(SocketChannel ch) throws Exception {
            ch.pipeline().addLast(new JsonRPCHandle(jsonRpcMapper));
        }
    }
}
