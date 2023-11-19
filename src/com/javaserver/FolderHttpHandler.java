package com.javaserver;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

public class FolderHttpHandler implements HttpHandler {

    private final String baseDir;
    private final String defaultFile; // Файл по умолчанию, например, index.html

    public FolderHttpHandler(String baseDir, String defaultFile) {
        this.baseDir = baseDir;
        this.defaultFile = defaultFile;
    }

    @Override
    public String handle(HttpRequest request, HttpResponse response) {
        String path = request.getUrl();

//        String[] o = path.split("/", 3);



        // Если URL оканчивается слешем, добавьте к нему имя файла по умолчанию
        if (!path.endsWith("/") && !path.contains(".")) {
            path += "/";
        }
        if (!path.contains(".")) {
            path += defaultFile;
        }

        String resourcePath = getResourcePath(path);

        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            if (inputStream != null) {
                byte[] fileBytes = inputStream.readAllBytes();
                response.setStatusCode(200);
                response.setStatusMessage("OK");
                response.setBody(new String(fileBytes, StandardCharsets.UTF_8));
                response.addHeader("Content-Type", getContentType(path));
            } else {
                throw new IOException("Resource not found: " + resourcePath);
            }
        } catch (IOException e) {
            e.printStackTrace();
            response.setStatusCode(404);
            response.setStatusMessage("Not Found");
            response.setBody("<html><body><h1 style=\"color:red\">File Not Found</h1></body></html>");
        }

        return response.getBody();
    }

    private String getResourcePath(String path) {
        return Paths.get(baseDir, path).toString();
    }

    private String getContentType(String filePath) {
        String contentType = "application/octet-stream"; // default content type

        if (filePath.endsWith(".html")) {
            contentType = "text/html; charset=utf-8";
        } else if (filePath.endsWith(".css")) {
            contentType = "text/css; charset=utf-8";
        } // Добавьте другие типы контента при необходимости

        return contentType;
    }
}
