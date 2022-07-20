package net.yandex.taskmanager.services;

import com.google.gson.Gson;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class KVTaskClient {

    private final HttpClient client;
    private final String API_TOKEN;
    private final URL serverURL;
    private static final Gson gson = new Gson();

    public KVTaskClient(URL address) throws IOException, InterruptedException {
        this.client = HttpClient.newHttpClient(); //Тут this не нужно, но кажется, что так читабельнее
        this.serverURL = address;
        URI url = URI.create(address.toString() + "/register");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> token = client.send(request, HttpResponse.BodyHandlers.ofString());
        this.API_TOKEN = token.body();
    }

    public String getAPI_TOKEN() {
        return API_TOKEN;
    }

    public void put(String key, String json) throws IOException, InterruptedException {
        URI url = URI.create(serverURL + "/save/" + key + "?API_TOKEN=" + API_TOKEN);
        final HttpRequest.BodyPublisher body = HttpRequest.BodyPublishers.ofString(json);
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(body).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    public String load(String key) throws IOException, InterruptedException {
        URI url = URI.create(serverURL + "/load/" + key + "?API_TOKEN=" + API_TOKEN);
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> value = client.send(request, HttpResponse.BodyHandlers.ofString());
        return value.body();
    }

}
