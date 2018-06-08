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

    private int index;
    private ChannelHandlerContext channelHandlerContext;
    private ConcurrentSkipListMap<Long, SendAndCallBack> callbackMap = new ConcurrentSkipListMap();
    private AtomicBoolean active = new AtomicBoolean(false);
    private AtomicBoolean connecting = new AtomicBoolean(true);

    public JsonRpcClientHandler(int index) {
        this.index = index;
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

        String body = safeGetBodyFromByteBuf((ByteBuf) msg);

        JSONObject responseObj = JSON.parseObject(body);
        Long requestIndex = responseObj.getLong("requestIndex");
        if (requestIndex == null) {
            return;
        }

        SendAndCallBack callBack = callbackMap.remove(requestIndex);
        if (callBack != null) {
            callBack.callback(responseObj);
        }
    }

    private String safeGetBodyFromByteBuf(ByteBuf byteBuf) throws Exception {
        byte[] req = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(req);
        byteBuf.release();
        return new String(req, "UTF-8");
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

        StringBuffer stringBuffer = new StringBuffer(json.toJSONString());
        stringBuffer.append("\n");
        ByteBuf reqByte = Unpooled.copiedBuffer(stringBuffer.toString().getBytes());

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