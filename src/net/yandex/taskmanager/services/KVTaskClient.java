package net.yandex.taskmanager.services;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class KVTaskClient {

    private final HttpClient client;
    private String API_TOKEN;
    private URL serverURL;

    public KVTaskClient(String address) {
        this.client = HttpClient.newHttpClient(); //Тут this не нужно, но кажется, что так читабельнее
        register(address);
    }

    public void put(String key, String json) {
        URI url = URI.create(serverURL + "/save/" + key + "?API_TOKEN=" + API_TOKEN);
        final HttpRequest.BodyPublisher body = HttpRequest.BodyPublishers.ofString(json);
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(body).build();
        try {
            HttpResponse<Void> response = client.send(request, HttpResponse.BodyHandlers.discarding());

            if (response.statusCode() != 200) {
                throw new TaskClientException("Запись на сервер не удалась, код " + response.statusCode());
            }

        } catch (IOException | InterruptedException e) {
            throw new TaskClientException("Ошибка при попытке записать данные на сервер.");
        }

    }

    public String load(String key) {
        URI url = URI.create(serverURL + "/load/" + key + "?API_TOKEN=" + API_TOKEN);
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new TaskClientException("Загрузка данных с сервера не удалась, код " + response.statusCode());
            }

            return response.body();

        } catch (IOException | InterruptedException e) {
            throw new TaskClientException("Ошибка при попытке загрузить данные с сервера.");
        }
    }

    private void register(String address) {
        try {
            this.serverURL = new URL(address);
            URI url = URI.create(address + "/register");
            HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
            HttpResponse<String> token =
                    client.send(request, HttpResponse.BodyHandlers.ofString());

            if (token.statusCode() != 200) {
                throw new TaskClientException("Регистрация менеджера не удалась, сервер отправил код "
                        + token.statusCode());
            }

            this.API_TOKEN = token.body();

        } catch (IOException | InterruptedException e) {
            throw new TaskClientException("Ошибка при регистрации менеджера");
        }
    }

    public static class TaskClientException extends RuntimeException {
        public TaskClientException(String message) {
            super(message);
        }
    }
}
