package org.http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URL;
import org.apache.commons.io.IOUtils;

public class HttpServerDemo {
    public HttpServerDemo() {}

    public static void main(String[] args) throws IOException {
        // 创建第一个HTTP服务器
        HttpServer server1 = HttpServer.create(new InetSocketAddress("0.0.0.0", 8090), 0);
        server1.createContext("/test1", new HttpServerDemo.Test1Handler());
        server1.start();
        System.out.println("Listening to " + server1.getAddress());

        // 创建第二个HTTP服务器
        HttpServer server2 = HttpServer.create(new InetSocketAddress("0.0.0.0", 8091), 0);
        server2.createContext("/test2", new HttpServerDemo.Test2Handler());
        server2.start();
        System.out.println("Listening to " + server2.getAddress());
    }

    public static class Test1Handler implements HttpHandler {
        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            // 从请求中读取数据
            InputStream in = httpExchange.getRequestBody();
            String requestData = IOUtils.toString(in);

            // 转发数据到/test2
//            String responseData = forwardToTest2(requestData);
            sendPostRequestWithRetry("http://127.0.0.1:8091/test2",requestData);

            // 将响应发送回客户端
            httpExchange.sendResponseHeaders(200, 0);
            OutputStream out = httpExchange.getResponseBody();
            out.flush();
            out.close();
        }

        public static void sendPostRequestWithRetry(String url, String payload) {
            int maxRetries = 3;
            int retry = 0;
            boolean success = false;
            while (!success && retry < maxRetries) {
                try {
                    URL obj = new URL(url);
                    HttpURLConnection con = (HttpURLConnection) obj.openConnection();

                    //设置请求方法为POST
                    con.setRequestMethod("POST");

                    //设置请求头
                    con.setRequestProperty("User-Agent", "Mozilla/5.0");
                    con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

                    // 向服务器发送数据
                    con.setDoOutput(true);
                    OutputStream os = con.getOutputStream();
                    os.write(payload.getBytes("UTF-8"));
                    os.flush();
                    os.close();

                    // 获取服务器返回的响应
                    int responseCode = con.getResponseCode();
                    String response = IOUtils.toString(con.getInputStream());
                    // 判断响应码是否为200
                    if (responseCode == 200) {
//                        System.out.println("Response : " + response);
                        success = true;
                    }
                } catch (IOException e) {
                    retry++;
                    System.out.println("Error sending request. Retrying... (" + retry + "/" + maxRetries + ")");
                    if (retry == maxRetries) {
                        System.out.println("Failed to send request after " + maxRetries + " retries.");
                    }
                }
            }
        }
    }

    public static class Test2Handler implements HttpHandler {
        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            // 从请求中读取数据
            InputStream in = httpExchange.getRequestBody();
            String requestData = IOUtils.toString(in);

            // 打印数据
            System.out.println("test2: " + requestData);

            // 将响应发送回客户端
            httpExchange.sendResponseHeaders(200, 0);
            OutputStream out = httpExchange.getResponseBody();
            out.flush();
            out.close();
        }
    }



}