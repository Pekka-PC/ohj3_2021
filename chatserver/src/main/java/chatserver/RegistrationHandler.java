package chatserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class RegistrationHandler implements HttpHandler {

    ChatAuthenticator auth = null;
    private String responseBody;

    RegistrationHandler(ChatAuthenticator authenticator) {
        auth = authenticator;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        int code = 200;
        try {
            String responseBody = "";
            if (exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                Headers headers = exchange.getRequestHeaders();
                int contentLenght = 0;
                String contentType = "";
                if (headers.containsKey("Content-Lenght")) {
                contentLenght = Integer.parseInt(headers.get("Content-Length").get(0));
                } else {
                code = 411;
                }
                if (headers.containsKey("Content-Type")) {
                    contentType = headers.get("Content-Type").get(0);
                } else {
                    code = 400;
                    responseBody = "No content type in request";
                }
                if (contentType.equalsIgnoreCase("application/plain")) {
                    InputStream stream = exchange.getRequestBody();
                    String text = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).lines()
                            .collect(Collectors.joining("\n"));
                    ChatServer.log(text);
                    stream.close();
                    if (text.trim().length() > 0) {
                        String [] items = text.split(":");
                        if (items.length == 2) {
                            if (items[0].trim().length() > 0 && items[1].trim().length() > 0){
                                if(auth.addUser(items[0], items[1])){
                                    exchange.sendResponseHeaders(code, -1);
                                    ChatServer.log("New user created!");
                                } else{
                                    code = 400;
                                    responseBody = "Error! msg admin";
                                }
                            } else {
                                code = 400; 
                                responseBody = "Error! msg admin";
                            }   
                        } else {
                            code = 400;
                            responseBody = "Error! msg admin";
                        }


                        exchange.sendResponseHeaders(code, -1);
                        ChatServer.log("New chatmessage saved");
                    } else {
                        code = 400;
                        responseBody = "No content in request";
                        ChatServer.log(responseBody);
                    }
                } else {
                    code = 411;
                    responseBody = "Content-Type must be text/plain.";
                    ChatServer.log(responseBody);
                }
            } else {
                code = 400;
                responseBody = "Not supported";
            }
            
        } catch (IOException e) {
            code = 500;
            responseBody = "Error in handling the request: " + e.getMessage();
        } catch (Exception e) {
            code = 500;
            responseBody = "Server Error: " + e.getMessage();
        }
        if (code < 200 || code > 299) {
            ChatServer.log("server error /chat: " + code + "" + responseBody);
            byte[] bytes = responseBody.getBytes("UTF-8");
            exchange.sendResponseHeaders(code, bytes.length);
            OutputStream os = exchange.getResponseBody();
            os.write(bytes);
            os.close();
        }
    }

}