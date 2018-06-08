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
        String body = safeGetBodyFromByteBuf((ByteBuf) msg);

        final ChannelHandlerContext handlerContext = ctx;
        jsonRpcMapper.callMethod(body, new JsonRpcResultFuture() {
            @Override
            public void error(long requestIndex, Long id, JSONObject error) {
                JSONObject resp = new JSONObject();
                resp.put("requestIndex", requestIndex);
                resp.put("id", id);
                resp.put("error", error);
                String respStr = resp.toJSONString() + "\n";
                ByteBuf respByte = Unpooled.copiedBuffer(respStr.getBytes());
                handlerContext.writeAndFlush(respByte);         // write to rpc-client
            }

            @Override
            public void result(long requestIndex, Long id, Object result) {
                JSONObject resp = new JSONObject();
                resp.put("requestIndex", requestIndex);
                resp.put("id", id);
                resp.put("result", result);
                String respStr = resp.toJSONString() + "\n";
                ByteBuf respByte = Unpooled.copiedBuffer(respStr.getBytes());
                handlerContext.writeAndFlush(respByte);     // write to rpc-client
            }
        });
    }

    private String safeGetBodyFromByteBuf(ByteBuf byteBuf) throws Exception {
        byte[] req = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(req);
        byteBuf.release();
        return new String(req, "UTF-8");
    }
}