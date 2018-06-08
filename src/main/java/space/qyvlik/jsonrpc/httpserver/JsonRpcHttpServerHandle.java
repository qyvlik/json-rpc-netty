package space.qyvlik.jsonrpc.httpserver;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import org.apache.commons.lang3.StringUtils;
import space.qyvlik.jsonrpc.httpserver.client.JsonRpcClient;
import space.qyvlik.jsonrpc.httpserver.client.JsonRpcClientSet;
import space.qyvlik.jsonrpc.httpserver.client.SendAndCallBack;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicLong;

@ChannelHandler.Sharable
public class JsonRpcHttpServerHandle extends ChannelHandlerAdapter {
    private JsonRpcClientSet jsonRpcClientSet;
    private AtomicLong requestCount = new AtomicLong(0);

    public JsonRpcHttpServerHandle(JsonRpcClientSet jsonRpcClientSet) {
        this.jsonRpcClientSet = jsonRpcClientSet;
    }

    @Override
    public void exceptionCaught(final ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.out.println("exceptionCaught:" + cause.getMessage());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (!(msg instanceof FullHttpRequest)) {
            return;
        }

        FullHttpRequest request = (FullHttpRequest) msg;

        try {
            ByteBuf content = request.content();
            String requestBody = content.toString(StandardCharsets.UTF_8);

            if (!isStartWithJson(requestBody)) {
                writeErrorJson(ctx, null, -32700L, "request is not json");
                return;
            }

            JSONObject requestObj = null;
            try {
                requestObj = JSON.parseObject(requestBody);
            } catch (Exception e) {
                writeErrorJson(ctx, null, -32700L, "parse json error");
                return;
            }

            JsonRpcClient jsonRpcClient = jsonRpcClientSet.pollingJsonRpcClient();

            if (jsonRpcClient == null) {
                writeErrorJson(ctx, null, -32000L, "cannot get connect resource");
                return;
            }

            if (!jsonRpcClient.isActive()) {
                writeErrorJson(ctx, null, -32000L, "json rpc client is inactive");
                return;
            }

            long requestIndex = requestCount.getAndIncrement();

            requestObj.put("requestIndex", requestIndex);

            jsonRpcClient.send(requestObj,
                    new SendAndCallBack() {
                        @Override
                        public void callback(JSONObject jsonObject) {
                            writeJSONObject(ctx, jsonObject);
                        }

                        @Override
                        public void disConnect() {
                            writeErrorJson(ctx, null, -32000L, "remote server is disconnect");
                        }
                    });

        } catch (Exception e) {

        } finally {
            if (request != null) {
                request.release();
            }
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    private boolean isStartWithJson(String string) {
        if (StringUtils.isBlank(string)) {
            return false;
        }

        if (!string.startsWith("{") && !string.startsWith("[")) {
            return false;
        }

        if (!string.contains("id")) {
            return false;
        }

        if (!string.contains("method")) {
            return false;
        }

        if (!string.contains("params")) {
            return false;
        }

        return true;
    }

    private void writeErrorJson(ChannelHandlerContext ctx, Long id, Long code, String message) {
        String responseString = getErrorObject(id, code, message);
        FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1, HttpResponseStatus.OK,
                Unpooled.wrappedBuffer(responseString.getBytes())
        );
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json;charset=UTF-8");
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes() + "");
        response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);

        ctx.writeAndFlush(response);
    }

    private void writeJSONObject(ChannelHandlerContext ctx, JSONObject jsonObject) {
        jsonObject.remove("requestIndex");
        FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1, HttpResponseStatus.OK,
                Unpooled.wrappedBuffer(jsonObject.toJSONString().getBytes())
        );
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json;charset=UTF-8");
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes() + "");
        response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);

        ctx.writeAndFlush(response);
    }

    private String getErrorObject(Long id, Long code, String message) {
        JSONObject response = new JSONObject();
        response.put("id", id);
        JSONObject error = new JSONObject();
        error.put("code", code);
        error.put("message", message);
        response.put("error", error);
        return response.toString();
    }
}
