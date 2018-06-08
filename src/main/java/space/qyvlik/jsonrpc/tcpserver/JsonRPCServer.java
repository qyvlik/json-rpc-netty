package space.qyvlik.jsonrpc.tcpserver;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import space.qyvlik.jsonrpc.tcpserver.mapper.JsonRpcMapper;

public class JsonRPCServer {

    private EventLoopGroup bossGroup = new NioEventLoopGroup();
    private EventLoopGroup workerGroup = new NioEventLoopGroup();
    private ServerBootstrap bootstrap = new ServerBootstrap();
    private JsonRpcMapper jsonRpcMapper;

    public JsonRPCServer(JsonRpcMapper jsonRpcMapper) {
        this.jsonRpcMapper = jsonRpcMapper;
    }

    public void start(int port) {
        try {
            bootstrap.group(bossGroup, workerGroup);
            bootstrap.channel(NioServerSocketChannel.class);
            bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
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
            ByteBuf delimiter = Unpooled.copiedBuffer("\n".getBytes());
            ch.pipeline()
                    .addLast("framer", new DelimiterBasedFrameDecoder(1024 * 32, delimiter))
                    .addLast(new JsonRPCHandle(jsonRpcMapper));
        }
    }
}