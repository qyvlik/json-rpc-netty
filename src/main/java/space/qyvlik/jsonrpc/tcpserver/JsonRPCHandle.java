package space.qyvlik.jsonrpc.tcpserver;

import com.alibaba.fastjson.JSONObject;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import space.qyvlik.jsonrpc.tcpserver.mapper.JsonRpcMapper;
import space.qyvlik.jsonrpc.tcpserver.mapper.JsonRpcResultFuture;

@ChannelHandler.Sharable
public class JsonRPCHandle extends ChannelHandlerAdapter {

    private final JsonRpcMapper jsonRpcMapper;

    public JsonRPCHandle(final JsonRpcMapper jsonRpcMapper) {
        this.jsonRpcMapper = jsonRpcMapper;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
            throws Exception {
        ctx.close();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg)
            throws Exception {
        ByteBuf buf = (ByteBuf) msg;
        final byte[] req = new byte[buf.readableBytes()];
        buf.readBytes(req);
        String body = new String(req, "UTF-8");

        final ChannelHandlerContext handlerContext = ctx;
        jsonRpcMapper.callMethod(body, new JsonRpcResultFuture() {
            @Override
            public void error(long requestIndex, Long id, JSONObject error) {
                JSONObject resp = new JSONObject();
                resp.put("requestIndex", requestIndex);
                resp.put("id", id);
                resp.put("error", error);
                String respStr = resp.toJSONString();
                ByteBuf respByte = Unpooled.copiedBuffer(respStr.getBytes());
                handlerContext.writeAndFlush(respByte);
            }

            @Override
            public void result(long requestIndex, Long id, Object result) {
                JSONObject resp = new JSONObject();
                resp.put("requestIndex", requestIndex);
                resp.put("id", id);
                resp.put("result", result);
                String respStr = resp.toJSONString();
                ByteBuf respByte = Unpooled.copiedBuffer(respStr.getBytes());
                handlerContext.writeAndFlush(respByte);
            }
        });
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }
}