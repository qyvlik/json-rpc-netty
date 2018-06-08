package space.qyvlik.jsonrpc.httpserver.client;

import com.alibaba.fastjson.JSONObject;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;

// http://tterm.blogspot.com/2014/03/netty-tcp-client-with-reconnect-handling.html
public class JsonRpcClient {
    private int index;
    private EventLoopGroup boss;
    private Bootstrap bootstrap;
    private JsonRpcClientHandler jsonRpcClientHandler;

    public JsonRpcClient(int index) {
        this.index = index;
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.err.println(
                    "Usage: " + JsonRpcClient.class.getSimpleName() +
                            " <host> <port>");
            return;
        }

        final String host = args[0];
        final int port = Integer.parseInt(args[1]);

        new JsonRpcClient(0).start(host, port);
    }

    public boolean isConnecting() {
        if (jsonRpcClientHandler != null) {
            return jsonRpcClientHandler.isConnecting();
        }
        return false;

    }

    public boolean isActive() {
        if (jsonRpcClientHandler != null) {
            return jsonRpcClientHandler.isActive();
        }
        return false;
    }

    public void send(JSONObject json, SendAndCallBack callBack) {
        jsonRpcClientHandler.send(json, callBack);
    }

    // use work thread
    public void start(String host, Integer port) {
        boss = new NioEventLoopGroup();
        jsonRpcClientHandler = new JsonRpcClientHandler(this.index);
        bootstrap = new Bootstrap();

        try {
            bootstrap.group(boss);
            bootstrap.channel(NioSocketChannel.class);
            bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
            bootstrap.option(ChannelOption.TCP_NODELAY, true);

            bootstrap.handler(new ChannelInitializer<Channel>() {
                @Override
                protected void initChannel(Channel ch) throws Exception {
                    ByteBuf delimiter = Unpooled.copiedBuffer("\n".getBytes());
                    ch.pipeline()
                            .addLast("framer", new DelimiterBasedFrameDecoder(1024 * 32, delimiter))
                            .addLast(jsonRpcClientHandler);
                }
            });

            ChannelFuture channelFuture = bootstrap.connect(host, port).sync();
            channelFuture.channel().closeFuture().sync();

        } catch (Exception e) {

            jsonRpcClientHandler.disableConnection();

            e.printStackTrace();

        } finally {
            boss.shutdownGracefully();
        }
    }
}
