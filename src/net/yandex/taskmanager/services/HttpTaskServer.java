package net.yandex.taskmanager.services;

import net.yandex.taskmanager.model.*;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;

public class HttpTaskServer {

    private static final int PORT = 8080;
    private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
    private static final Gson gson = new Gson();
    private final TaskManager manager = Managers.getHTTPManager(new URL("http://localhost:8078"));
    HttpServer httpServer;

    public HttpTaskServer() throws IOException, InterruptedException {
        startServer();
    }

    public TaskManager getManager() {
        return manager;
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        new KVServer().start();

        HttpTaskServer server = new HttpTaskServer();

        server.manager.createTask(new Task("test1", "sus"));
        server.manager.createEpic(new EpicTask("Epic1", "neew"));
        server.manager.createSubTask(new SubTask("SubTask1", "neeew", TaskStatus.IN_PROGRESS, 2,
                LocalDateTime.of(2022, 7, 11, 20, 0), 15));
        server.manager.createSubTask(new SubTask("SubTask2", "sus", TaskStatus.NEW, 2,
                LocalDateTime.of(2022, 7, 11, 22, 0), 60));

        server.manager.getTaskByID(1);
        server.manager.getSubTaskByID(3);
        server.manager.getEpicByID(2);
        server.manager.getSubTaskByID(4);
        server.manager.getTaskByID(1);

        Task newTask = new Task("TaskNew", "newTask", TaskStatus.NEW,
                LocalDateTime.of(2022, 7, 10, 20, 0), 30);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks/task/");
        Gson gson = new Gson();
        String json = gson.toJson(newTask);
        final HttpRequest.BodyPublisher body = HttpRequest.BodyPublishers.ofString(json);
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(body).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        /*SubTask newTask = new SubTask("SubTask2", "sus", TaskStatus.NEW,
                99, LocalDateTime.of(2022, 7, 11, 22, 0), 60);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks/subtask/");
        Gson gson = new Gson();
        String json = gson.toJson(newTask);
        final HttpRequest.BodyPublisher body = HttpRequest.BodyPublishers.ofString(json);
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(body).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());*/
    }

    public void startServer() throws IOException {
        httpServer = HttpServer.create();
        httpServer.bind(new InetSocketAddress(PORT), 0);

        httpServer.createContext("/tasks", new AllTasksHandler());
        httpServer.createContext("/tasks/task", new TasksHandler());
        httpServer.createContext("/tasks/epic", new EpicsHandler());
        httpServer.createContext("/tasks/subtask", new SubTasksHandler());
        httpServer.createContext("/tasks/history", new HistoryHandler());

        httpServer.start();
        System.out.println("HttpTaskServer запущен на " + PORT + " порту.");
    }

    public void closeServer() {
        httpServer.stop(1);
    }

    class AllTasksHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();
            String response;

            switch (method) {
                case "GET":
                    // Собираем ответ. Если мама пустая - её в ответ не добавляем
                    StringBuilder result = new StringBuilder();
                    if (!manager.getTasks().isEmpty()) {
                        result.append(gson.toJson(manager.getTasks()));
                    }
                    if (!manager.getEpics().isEmpty()) {
                        result.append(gson.toJson(manager.getEpics()));
                    }
                    if (!manager.getSubTasks().isEmpty()) {
                        result.append(gson.toJson(manager.getSubTasks()));
                    }
                    if (!manager.getPrioritizedTasks().isEmpty()){
                        result.append("\n");
                        result.append(gson.toJson(manager.getPrioritizedTasks()));
                    }

                    response = result.toString();
                    exchange.sendResponseHeaders(200, 0);

                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(response.getBytes());
                    }
                    break;
                case "DELETE":
                    manager.clearTasks();
                    manager.clearSubTasks();
                    manager.clearEpics();

                    exchange.sendResponseHeaders(200, 0);

                    response = "Все задачи успешно удалены! Зачем-то.";
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(response.getBytes());
                    }
                    break;
                default:
                    response = "Данный запрос (пока) не реализован. Проверьте, пожалуйста, метод и адрес.";

                    exchange.sendResponseHeaders(405, 0);
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(response.getBytes());
                    }
                    break;
            }
        }
    }

    class TasksHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();
            String response;
            Map<String, String> params = new HashMap<>();
            int id = -1;
            boolean checkForParams = false;

            if (exchange.getRequestURI().getQuery() != null){
                params = queryToMap(exchange.getRequestURI().getQuery());
                checkForParams = true;
            }
            //System.out.println(Arrays.toString(splitPath));
            //System.out.println(params);

            switch (method) {
                case "GET":
                    if (checkForParams){
                        // Проверка, передан ли id задачи, которую надо получить, и существует ли она
                        if (checkRequestId(params, exchange)){
                            break;
                        }
                        id = Integer.parseInt(params.get("id"));

                        response = gson.toJson(manager.getTaskByID(id));

                        exchange.sendResponseHeaders(200, 0);
                        try (OutputStream os = exchange.getResponseBody()) {
                            os.write(response.getBytes());
                        }
                        break;
                    } else {
                        if (manager.getTasks().isEmpty()){
                            response = "Задачи отсутствуют.";
                        } else {
                            response = gson.toJson(manager.getTasks());
                        }

                        exchange.sendResponseHeaders(200, 0);
                        try (OutputStream os = exchange.getResponseBody()) {
                            os.write(response.getBytes());
                        }
                        break;
                    }

                case "DELETE":
                    if (checkRequestId(params, exchange)){
                        break;
                    }
                    id = Integer.parseInt(params.get("id"));

                    manager.deleteTaskByID(id);
                    response = "Задача с Id (" + id + ") успешно удалена.";

                    exchange.sendResponseHeaders(200, 0);
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(response.getBytes());
                    }
                    break;

                case "POST":
                    InputStream inputStream = exchange.getRequestBody();
                    String body = new String(inputStream.readAllBytes(), DEFAULT_CHARSET);

                    Task newTask = gson.fromJson(body, Task.class);

                    if (manager.getTaskByID(id) == null){
                        manager.createTask(newTask);
                        response = "Задача успешно создана!";
                    } else {
                        manager.updateTask(newTask);
                        response = "Задача успешно обновлена!";
                    }

                    exchange.sendResponseHeaders(200, 0);
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(response.getBytes());
                    }
                    break;

                default:
                    response = "Данный запрос (пока) не реализован. Проверьте, пожалуйста, метод и адрес.";

                    exchange.sendResponseHeaders(405, 0);
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(response.getBytes());
                    }
                    break;
            }
        }

        private boolean checkRequestId(Map<String, String> params, HttpExchange exchange) throws IOException {
            String response;
            if (params.isEmpty() || !params.containsKey("id")){
                response = "Не введен ID задачи.";

                exchange.sendResponseHeaders(400, 0);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                    return true;
                }
            }

            int id = Integer.parseInt(params.get("id"));

            if (manager.getTaskByID(id) == null){
                response = "Задача с таким ID не существует.";

                exchange.sendResponseHeaders(404, 0);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
                return true;
            }
            return false;
        }
    }

    class EpicsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();
            String response;
            Map<String, String> params = new HashMap<>();
            int id = -1;
            boolean checkForParams = false;

            if (exchange.getRequestURI().getQuery() != null){
                params = queryToMap(exchange.getRequestURI().getQuery());
                checkForParams = true;
            }

            switch (method) {
                case "GET":
                    if (checkForParams) {
                        // Проверка, передан ли id эпика, которую надо получить, и существует ли она
                        if (checkRequestId(params, exchange)){
                            break;
                        }
                        id = Integer.parseInt(params.get("id"));

                        response = gson.toJson(manager.getEpicByID(id));

                        exchange.sendResponseHeaders(200, 0);
                        try (OutputStream os = exchange.getResponseBody()) {
                            os.write(response.getBytes());
                        }
                        break;
                    } else {
                        if (manager.getEpics().isEmpty()){
                            response = "Эпики отсутствуют.";
                        } else {
                            response = gson.toJson(manager.getEpics());
                        }

                        exchange.sendResponseHeaders(200, 0);
                        try (OutputStream os = exchange.getResponseBody()) {
                            os.write(response.getBytes());
                        }
                        break;
                    }

                case "DELETE":
                    if (checkRequestId(params, exchange)){
                        break;
                    }
                    id = Integer.parseInt(params.get("id"));

                    manager.deleteEpicByID(id);
                    response = "Эпик с Id (" + id + ") успешно удалена.";

                    exchange.sendResponseHeaders(200, 0);
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(response.getBytes());
                    }
                    break;

                case "POST":
                    InputStream inputStream = exchange.getRequestBody();
                    String body = new String(inputStream.readAllBytes(), DEFAULT_CHARSET);

                    EpicTask newTask = gson.fromJson(body, EpicTask.class);

                    if (manager.getEpicByID(id) == null){
                        manager.createEpic(newTask);
                        response = "Эпик успешно создан!";
                    } else {
                        manager.updateEpic(newTask);
                        response = "Эпик успешно обновлен!";
                    }

                    exchange.sendResponseHeaders(200, 0);
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(response.getBytes());
                    }
                    break;

                default:
                    response = "Данный запрос (пока) не реализован. Проверьте, пожалуйста, метод и адрес.";

                    exchange.sendResponseHeaders(405, 0);
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(response.getBytes());
                    }
                    break;
            }
        }

        private boolean checkRequestId(Map<String, String> params, HttpExchange exchange) throws IOException {
            String response;
            if (params.isEmpty() || !params.containsKey("id")){
                response = "Не введен ID эпика.";

                exchange.sendResponseHeaders(400, 0);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                    return true;
                }
            }

            int id = Integer.parseInt(params.get("id"));

            if (manager.getEpicByID(id) == null){
                response = "Эпик с таким ID не существует.";

                exchange.sendResponseHeaders(404, 0);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
                return true;
            }
            return false;
        }
    }

    class SubTasksHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();
            String response;
            Map<String, String> params = new HashMap<>();
            int id = -1;
            boolean checkForParams = false;

            if (exchange.getRequestURI().getQuery() != null){
                params = queryToMap(exchange.getRequestURI().getQuery());
                checkForParams = true;
            }

            switch (method) {
                case "GET":
                    if (checkForParams){
                        // Проверка, передан ли id задачи, которую надо получить, и существует ли она
                        if (checkRequestId(params, exchange)){
                            break;
                        }
                        id = Integer.parseInt(params.get("id"));

                        response = gson.toJson(manager.getSubTaskByID(id));

                        exchange.sendResponseHeaders(200, 0);
                        try (OutputStream os = exchange.getResponseBody()) {
                            os.write(response.getBytes());
                        }
                        break;
                    } else {
                        if (manager.getSubTasks().isEmpty()){
                            response = "Подзадачи отсутствуют.";
                        } else {
                            response = gson.toJson(manager.getSubTasks());
                        }

                        exchange.sendResponseHeaders(200, 0);
                        try (OutputStream os = exchange.getResponseBody()) {
                            os.write(response.getBytes());
                        }
                        break;
                    }

                case "DELETE":
                    if (checkRequestId(params, exchange)){
                        break;
                    }
                    id = Integer.parseInt(params.get("id"));

                    manager.deleteSubTaskByID(id);
                    response = "Подзадача с Id (" + id + ") успешно удалена.";

                    exchange.sendResponseHeaders(200, 0);
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(response.getBytes());
                    }
                    break;

                case "POST":
                    InputStream inputStream = exchange.getRequestBody();
                    String body = new String(inputStream.readAllBytes(), DEFAULT_CHARSET);

                    SubTask newTask = gson.fromJson(body, SubTask.class);

                    if (manager.getEpicByID(newTask.getEpicID()) == null){
                        response = "Не найден эпик для данной подзадачи.";

                        exchange.sendResponseHeaders(404, 0);
                        try (OutputStream os = exchange.getResponseBody()) {
                            os.write(response.getBytes());
                        }
                        break;
                    }

                    if (manager.getSubTaskByID(id) == null){
                        manager.createSubTask(newTask);
                        response = "Подзадача успешно создана!";
                    } else {
                        manager.updateSubTask(newTask);
                        response = "Подзадача успешно обновлена!";
                    }

                    exchange.sendResponseHeaders(200, 0);
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(response.getBytes());
                    }
                    break;

                default:
                    response = "Данный запрос (пока) не реализован. Проверьте, пожалуйста, метод и адрес.";

                    exchange.sendResponseHeaders(405, 0);
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(response.getBytes());
                    }
                    break;
            }
        }

        private boolean checkRequestId(Map<String, String> params, HttpExchange exchange) throws IOException {
            String response;
            if (params.isEmpty() || !params.containsKey("id")){
                response = "Не введен ID подзадачи.";

                exchange.sendResponseHeaders(400, 0);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                    return true;
                }
            }

            int id = Integer.parseInt(params.get("id"));

            if (manager.getSubTaskByID(id) == null){
                response = "Подзадача с таким ID не существует.";

                exchange.sendResponseHeaders(404, 0);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
                return true;
            }
            return false;
        }
    }

    class HistoryHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();
            String response;

            if ("GET".equals(method)) {
                System.out.println("Обработка метода GET в tasks/history");
                if (!manager.getHistory().isEmpty()) {
                    response = gson.toJson(manager.getHistory());
                } else {
                    response = "История пуста.";
                }

                exchange.sendResponseHeaders(200, 0);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }

            } else {
                response = "Данный запрос (пока) не реализован. Проверьте, пожалуйста, метод и адрес.";

                exchange.sendResponseHeaders(405, 0);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
            }
        }
    }

    public static Map<String, String> queryToMap(String query){
        Map<String, String> result = new HashMap<>();
        for (String param : query.split("&")) {
            String[] pair = param.split("=");
            if (pair.length>1) {
                result.put(pair[0], pair[1]);
            }else{
                result.put(pair[0], "");
            }
        }
        return result;
    }
}
