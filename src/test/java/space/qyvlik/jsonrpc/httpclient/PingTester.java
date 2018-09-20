package space.qyvlik.jsonrpc.httpclient;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.kevinsawicki.http.HttpRequest;
import org.junit.Test;

public class PingTester {

    @Test
    public void testPing() {
        JSONObject update = new JSONObject();
        update.put("id", System.currentTimeMillis());
        update.put("method", "ping");

        JSONArray params = new JSONArray();

        update.put("params", params);

        String request = update.toJSONString();
        String response = HttpRequest.post("http://localhost:10102")
//                .readTimeout(5000)                  // 1000 ms
                .header("Content-Type", "application/json")
                .send(request)
                .body();
        System.out.println("testPing" + request + response);
        long end = System.currentTimeMillis();
    }

}
