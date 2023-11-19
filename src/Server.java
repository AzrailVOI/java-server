import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class Server {
    private final static int BUFFER_SIZE = 256;
    private AsynchronousServerSocketChannel server;

    private final HttpHandler handler;

    public Server(HttpHandler handler) {
        this.handler = handler;
    }

    public void bootstrap(){
        try {
            server = AsynchronousServerSocketChannel.open();
            server.bind(new InetSocketAddress("localhost", 9999));

            while (true){
                Future<AsynchronousSocketChannel> future = server.accept();
                handleClient(future);
            }
        } catch (IOException | ExecutionException | InterruptedException | TimeoutException | SQLException e) {
            throw new RuntimeException(e);
        }
    }
    private void handleClient(Future<AsynchronousSocketChannel> future) throws InterruptedException, ExecutionException, TimeoutException, IOException, SQLException {
        System.out.println("new client connection");

        AsynchronousSocketChannel clientChannel = future.get();

        HttpHandler fileHandler = new FolderHttpHandler("files", "index.html");

        while (clientChannel != null && clientChannel.isOpen()){
            ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);

            StringBuilder builder = new StringBuilder();
            boolean keepReading = true;

            while (keepReading){
                int readResult = clientChannel.read(buffer).get();

                keepReading = readResult == BUFFER_SIZE;

                buffer.flip();

                CharBuffer charBuffer = StandardCharsets.UTF_8.decode(buffer);

                builder.append(charBuffer);
                buffer.clear();
            }

            HttpRequest request = new HttpRequest(builder.toString());
            HttpResponse response = new HttpResponse();


            if (handler != null) {
                try {
                    String body = this.handler.handle(request, response);

                    if (body != null && !body.isBlank()) {
                        response.getHeaders().putIfAbsent(HttpHeader.CONTENT_TYPE, ContentType.TEXT_HTML_UTF8);
                        response.setBody(body);
                    }
                } catch (Exception e) {
                    e.printStackTrace();

                    response.setStatusCode(500)
                            .setStatusMessage("Internal server error")
                            .addHeader(HttpHeader.CONTENT_TYPE, ContentType.TEXT_HTML_UTF8)
                            .setBody("<html><body><h1>Error happens</h1></body></html>");
                }
            } else {
                fileHandler.handle(request, response);
            }

            ByteBuffer resp = ByteBuffer.wrap(response.getBytes());

            clientChannel.write(resp);

            clientChannel.close();
        }
    }


}
