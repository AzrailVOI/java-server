import java.util.HashMap;
import java.util.Map;

public class HttpResponse {
    private final static String NEW_LINE = "\r\n";

    private final Map<String, String> headers = new HashMap<>();
    private String body = "";
    private int statusCode = 200;
    private String statusMessage = "OK";

    public HttpResponse() {
        this.headers.put("Server", "Java HTTP Server");
        this.headers.put("Connection", "close");
    }

    public HttpResponse addHeader(String key, String value) {
        this.headers.put(key, value);
        return this;
    }

    public HttpResponse addHeaders(Map<String, String> headers) {
        this.headers.putAll(headers);
        return this;
    }

    public String message() {
        StringBuilder builder = new StringBuilder();

        builder.append("HTTP/1.1 ")
                .append(statusCode)
                .append(" ")
                .append(statusMessage)
                .append(NEW_LINE);

        for (Map.Entry<String, String> entry : headers.entrySet()) {
            builder.append(entry.getKey())
                    .append(": ")
                    .append(entry.getValue())
                    .append(NEW_LINE);
        }

        return builder
                .append(NEW_LINE)
                .append(body)
                .toString();
    }

    public byte[] getBytes() {
        return message().getBytes();
    }



    public Map<String, String> getHeaders() {
        return headers;
    }

    public String getBody() {
        return body;
    }

    public HttpResponse setBody(String body) {
        this.headers.put(HttpHeader.CONTENT_LENGTH, String.valueOf(body.length()));
        this.body = body;
        return this;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public HttpResponse setStatusCode(int statusCode) {
        this.statusCode = statusCode;
        return this;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public HttpResponse setStatusMessage(String status) {
        this.statusMessage = status;
        return this;
    }
}
