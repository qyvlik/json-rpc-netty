package space.qyvlik.jsonrpc.httpserver.client;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicBoolean;

@ChannelHandler.Sharable
public class JsonRpcClientHandler extends ChannelHandlerAdapter {

    private ChannelHandlerContext channelHandlerContext;
    private ConcurrentSkipListMap<Long, SendAndCallBack> callbackMap = new ConcurrentSkipListMap();
    private AtomicBoolean active = new AtomicBoolean(false);
    private AtomicBoolean connecting = new AtomicBoolean(true);

    public JsonRpcClientHandler() {

    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        active.set(false);
        disableConnection();

        while (!callbackMap.isEmpty()) {
            Map.Entry entry = callbackMap.pollFirstEntry();
            if (entry != null && entry.getValue() != null) {
                ((SendAndCallBack) entry.getValue()).disConnect();
            }
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        this.channelHandlerContext = ctx;
        active.set(true);
        disableConnection();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg)
            throws Exception {
        ByteBuf buf = (ByteBuf) msg;
        byte[] req = new byte[buf.readableBytes()];
        buf.readBytes(req);
        String body = new String(req, "UTF-8");

        JSONObject responseObj = JSON.parseObject(body);
        SendAndCallBack callBack = callbackMap.get(responseObj.getLong("requestIndex"));
        callBack.callback(responseObj);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
            throws Exception {
        disableConnection();
        ctx.close();
    }

    public ChannelHandlerContext getChannelHandlerContext() {
        return channelHandlerContext;
    }

    public void send(JSONObject json, SendAndCallBack callBack) {

        if (!isActive()) {
            callBack.disConnect();
            return;
        }

        if (getChannelHandlerContext() == null) {
            callBack.disConnect();
            return;
        }

        callbackMap.put(json.getLong("requestIndex"), callBack);

        ByteBuf reqByte = Unpooled.copiedBuffer(json.toJSONString().getBytes());

        getChannelHandlerContext().writeAndFlush(reqByte);
    }

    public boolean isActive() {
        return active.get();
    }

    public boolean isConnecting() {
        return connecting.get();
    }

    public void disableConnection() {
        connecting.set(false);
    }
}
