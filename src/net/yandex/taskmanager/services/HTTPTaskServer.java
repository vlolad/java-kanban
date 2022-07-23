/* Не совсем понял требования про модификаторы у поля manager и внутренних классов
* Если внутренние классы HttpHandler'ы будут статическими, то нельзя в них вызывать менеджер задач
* и сохранять в него что-либо. Есть ощущение, что тут противоречие между комментариями в первом и втором ревью */

package net.yandex.taskmanager.services;

import net.yandex.taskmanager.model.*;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
// Импорты для ручных тестов
import java.time.LocalDateTime;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import com.google.gson.reflect.TypeToken;


public class HTTPTaskServer {

    private static final int PORT = 8080;
    private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
    private static final Gson gson = new Gson();
    private final TaskManager manager;
    private HttpServer httpServer;

    public HTTPTaskServer() throws IOException {
        this(Managers.getDefault());
    }

    public HTTPTaskServer(TaskManager manager) throws IOException {
        this.manager = manager;
        startServer();
    }

    public void startServer() throws IOException {
        httpServer = HttpServer.create(new InetSocketAddress(PORT), 0);

        httpServer.createContext("/tasks", new AllTasksHandler());
        httpServer.createContext("/tasks/task", new TasksHandler());
        httpServer.createContext("/tasks/epic", new EpicsHandler());
        httpServer.createContext("/tasks/subtask", new SubTasksHandler());
        httpServer.createContext("/tasks/history", new HistoryHandler());
    }

    public void start() {
        httpServer.start();
        System.out.println("HttpTaskServer запущен на " + PORT + " порту.");
    }

    public void closeServer() {
        httpServer.stop(1);
    }

    private class AllTasksHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();
            String response;

            if ("GET".equals(method)) {
                response = gson.toJson(manager.getPrioritizedTasks());
                exchange.sendResponseHeaders(200, 0);
            } else {
                response = "Данный запрос (пока) не реализован. Проверьте, пожалуйста, метод и адрес.";
                exchange.sendResponseHeaders(405, 0);
            }
            sendBody(exchange, response);
        }
    }

    private class TasksHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();
            String response;
            Map<String, String> params = new HashMap<>();
            int id;
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
                        response = gson.toJson(manager.getTaskByID(id));
                        exchange.sendResponseHeaders(200, 0);
                    } else {
                        if (manager.getTasks().isEmpty()){
                            response = "Задачи отсутствуют.";
                            exchange.sendResponseHeaders(404, 0);
                        } else {
                            response = gson.toJson(manager.getTasks());
                            exchange.sendResponseHeaders(200, 0);
                        }
                    }
                    sendBody(exchange, response);
                    break;

                case "DELETE":
                    if(checkForParams) {
                        if (checkRequestId(params, exchange)){
                            break;
                        }
                        id = Integer.parseInt(params.get("id"));
                        manager.deleteTaskByID(id);
                        response = "Задача с Id (" + id + ") успешно удалена.";
                    } else {
                        manager.clearTasks();
                        response = "Список задач успешно очищен.";
                    }
                    exchange.sendResponseHeaders(200, 0);
                    sendBody(exchange, response);
                    break;

                case "POST":
                    String body = readBody(exchange);
                    if (body.isBlank()) {
                        response = "Передан пустой POST-запрос";
                        exchange.sendResponseHeaders(400, 0);
                        sendBody(exchange, response);
                        return;
                    }

                    Task newTask = gson.fromJson(body, Task.class);
                    if (newTask.getId() == null){
                        manager.createTask(newTask);
                        response = "Задача успешно создана!";
                    } else {
                        manager.updateTask(newTask);
                        response = "Задача успешно обновлена!";
                    }
                    exchange.sendResponseHeaders(200, 0);
                    sendBody(exchange, response);
                    break;

                default:
                    response = "Данный запрос (пока) не реализован. Проверьте, пожалуйста, метод и адрес.";
                    exchange.sendResponseHeaders(405, 0);
                    sendBody(exchange, response);
                    break;
            }
        }

        private boolean checkRequestId(Map<String, String> params, HttpExchange exchange) throws IOException {
            String response;
            if (params.isEmpty() || !params.containsKey("id")){
                response = "Не введен ID задачи.";
                exchange.sendResponseHeaders(400, 0);
                sendBody(exchange, response);
                return true;
            }
            int id = Integer.parseInt(params.get("id"));
            if (manager.getTaskByID(id) == null){
                response = "Задача с таким ID не существует.";
                exchange.sendResponseHeaders(404, 0);
                sendBody(exchange, response);
                return true;
            }
            return false;
        }
    }

    private class EpicsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();
            String response;
            Map<String, String> params = new HashMap<>();
            int id;
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
                    } else {
                        if (manager.getEpics().isEmpty()){
                            response = "Эпики отсутствуют.";
                            exchange.sendResponseHeaders(404, 0);
                        } else {
                            response = gson.toJson(manager.getEpics());
                            exchange.sendResponseHeaders(200, 0);
                        }
                    }
                    sendBody(exchange, response);
                    break;

                case "DELETE":
                    if (checkForParams) {
                        if (checkRequestId(params, exchange)){
                            break;
                        }
                        id = Integer.parseInt(params.get("id"));
                        manager.deleteEpicByID(id);
                        response = "Эпик с Id (" + id + ") успешно удалена.";
                    } else {
                        manager.clearEpics();
                        response = "Список эпиков успешно очищен.";
                    }
                    exchange.sendResponseHeaders(200, 0);
                    sendBody(exchange, response);
                    break;

                case "POST":
                    String body = readBody(exchange);
                    if (body.isBlank()) {
                        response = "Передан пустой POST-запрос";
                        exchange.sendResponseHeaders(400, 0);
                        sendBody(exchange, response);
                        return;
                    }

                    EpicTask newTask = gson.fromJson(body, EpicTask.class);
                    if (newTask.getId() == null){
                        manager.createEpic(newTask);
                        response = "Эпик успешно создан!";
                    } else {
                        manager.updateEpic(newTask);
                        response = "Эпик успешно обновлен!";
                    }

                    exchange.sendResponseHeaders(200, 0);
                    sendBody(exchange, response);
                    break;

                default:
                    response = "Данный запрос (пока) не реализован. Проверьте, пожалуйста, метод и адрес.";

                    exchange.sendResponseHeaders(405, 0);
                    sendBody(exchange, response);
                    break;
            }
        }

        private boolean checkRequestId(Map<String, String> params, HttpExchange exchange) throws IOException {
            String response;
            if (params.isEmpty() || !params.containsKey("id")){
                response = "Не введен ID эпика.";
                exchange.sendResponseHeaders(400, 0);
                sendBody(exchange, response);
                return true;
            }
            int id = Integer.parseInt(params.get("id"));
            if (manager.getEpicByID(id) == null){
                response = "Эпик с таким ID не существует.";
                exchange.sendResponseHeaders(404, 0);
                sendBody(exchange, response);
                return true;
            }
            return false;
        }
    }

    private class SubTasksHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();
            String response;
            Map<String, String> params = new HashMap<>();
            int id;
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
                    } else {
                        if (manager.getSubTasks().isEmpty()){
                            response = "Подзадачи отсутствуют.";
                            exchange.sendResponseHeaders(404, 0);
                        } else {
                            response = gson.toJson(manager.getSubTasks());
                            exchange.sendResponseHeaders(200, 0);
                        }
                    }
                    sendBody(exchange, response);
                    break;

                case "DELETE":
                    if (checkForParams) {
                        if (checkRequestId(params, exchange)){
                            break;
                        }
                        id = Integer.parseInt(params.get("id"));
                        manager.deleteSubTaskByID(id);
                        response = "Подзадача с Id (" + id + ") успешно удалена.";
                    } else {
                        manager.clearSubTasks();
                        response = "Список сабтасков успешно очищен.";
                    }
                    exchange.sendResponseHeaders(200, 0);
                    sendBody(exchange, response);
                    break;

                case "POST":
                    String body = readBody(exchange);
                    if (body.isBlank()) {
                        response = "Передан пустой POST-запрос";
                        exchange.sendResponseHeaders(400, 0);
                        sendBody(exchange, response);
                        return;
                    }

                    SubTask newTask = gson.fromJson(body, SubTask.class);
                    if (manager.getEpicByID(newTask.getEpicID()) == null){
                        response = "Не найден эпик для данной подзадачи.";
                        exchange.sendResponseHeaders(404, 0);
                        sendBody(exchange, response);
                        break;
                    }
                    if (newTask.getId() == null){
                        manager.createSubTask(newTask);
                        response = "Подзадача успешно создана!";
                    } else {
                        manager.updateSubTask(newTask);
                        response = "Подзадача успешно обновлена!";
                    }
                    exchange.sendResponseHeaders(200, 0);
                    sendBody(exchange, response);
                    break;

                default:
                    response = "Данный запрос (пока) не реализован. Проверьте, пожалуйста, метод и адрес.";
                    exchange.sendResponseHeaders(405, 0);
                    sendBody(exchange, response);
                    break;
            }
        }

        private boolean checkRequestId(Map<String, String> params, HttpExchange exchange) throws IOException {
            String response;
            if (params.isEmpty() || !params.containsKey("id")){
                response = "Не введен ID подзадачи.";

                exchange.sendResponseHeaders(400, 0);
                sendBody(exchange, response);
                return true;
            }
            int id = Integer.parseInt(params.get("id"));
            if (manager.getSubTaskByID(id) == null){
                response = "Подзадача с таким ID не существует.";
                exchange.sendResponseHeaders(404, 0);
                sendBody(exchange, response);
                return true;
            }
            return false;
        }
    }

    private class HistoryHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();
            String response;

            if ("GET".equals(method)) {
                System.out.println("Обработка метода GET в tasks/history");
                if (!manager.getHistory().isEmpty()) {
                    response = gson.toJson(manager.getHistory());
                    exchange.sendResponseHeaders(200, 0);
                } else {
                    response = "История пуста.";
                    exchange.sendResponseHeaders(404, 0);
                }
                sendBody(exchange, response);

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

    private static void sendBody(HttpExchange exchange, String response) {
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String readBody(HttpExchange h) throws IOException {
        return new String(h.getRequestBody().readAllBytes(), DEFAULT_CHARSET);
    }

    // Код ниже - для ручных тестов
    //public static void main(String[] args) throws IOException, InterruptedException {
        /*new KVServer().start();

        HTTPTaskServer server = new HTTPTaskServer();
        server.start();

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

        URI thirdUrl = URI.create("http://localhost:8080/tasks/history");
        HttpRequest GETHistory = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> responseGETHistory = client.send(GETHistory, HttpResponse.BodyHandlers.ofString());
        List<Task> gettingHistory = gson.fromJson(responseGETHistory.body(),
                new TypeToken<List<Task>>() {}.getType());

        System.out.println(gettingHistory);

        System.out.println(server.manager.getHistory().equals(gettingHistory));

        SubTask newTask = new SubTask("SubTask2", "sus", TaskStatus.NEW,
                99, LocalDateTime.of(2022, 7, 11, 22, 0), 60);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks/subtask/");
        Gson gson = new Gson();
        String json = gson.toJson(newTask);
        final HttpRequest.BodyPublisher body = HttpRequest.BodyPublishers.ofString(json);
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(body).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());*/
    //}
}
