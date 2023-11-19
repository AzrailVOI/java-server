package com.javaserver;

public interface HttpHandler {
    String handle(HttpRequest request, HttpResponse response);
}
