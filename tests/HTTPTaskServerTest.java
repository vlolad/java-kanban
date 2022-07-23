import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.yandex.taskmanager.model.*;
import net.yandex.taskmanager.services.*;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


public class HTTPTaskServerTest {

    private HTTPTaskServer server;
    private KVServer mainServer;
    private final HttpClient client = HttpClient.newHttpClient();
    private final URL mainUrl = new URL("http://localhost:8080/tasks/");
    private final Gson gson = new Gson();


    public HTTPTaskServerTest() throws MalformedURLException {
    }

    @BeforeEach
    public void createServer() throws IOException {
        System.setOut(new PrintStream(new ByteArrayOutputStream())); // Иначе System.out выбрасывает NPE
        mainServer = new KVServer();
        mainServer.start();
        server = new HTTPTaskServer();
        server.start();
    }

    @AfterEach
    public void stopServer() {
        server.closeServer();
        mainServer.stop();
    }

    @Test
    public void serverPostAndGetTaskTesting() throws IOException, InterruptedException {
        Task newTask = new Task("TaskNew", "newTask", TaskStatus.NEW,
                LocalDateTime.of(2022, 7, 10, 20, 0), 30);

        URI url = URI.create(mainUrl + "task/");
        String json = gson.toJson(newTask);
        final HttpRequest.BodyPublisher body = HttpRequest.BodyPublishers.ofString(json);
        HttpRequest requestPost = HttpRequest.newBuilder().uri(url).POST(body).build();
        HttpResponse<String> responsePost = client.send(requestPost, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, responsePost.statusCode(),
                "Ошибка при отправлении POST-запроса на создание новой TASK");

        URI secondUrl = URI.create(mainUrl + "task/?id=1");
        HttpRequest requestGet = HttpRequest.newBuilder().uri(secondUrl).GET().build();
        HttpResponse<String> responseGet = client.send(requestGet, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, responseGet.statusCode(),
                "Ошибка при отправлении GET-запроса на получение TASK");

        URI thirdUrl = URI.create(mainUrl + "task/history");
        HttpRequest GETHistory = HttpRequest.newBuilder().uri(thirdUrl).GET().build();
        HttpResponse<String> responseGETHistory = client.send(GETHistory, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, responseGETHistory.statusCode(),
                "Ошибка в запросе истории.");

        Task updTask = new Task(1, "Task2", "brand new", TaskStatus.DONE,
                LocalDateTime.of(2021, 7, 10, 20, 0), 30);

        String jsonSecond = gson.toJson(updTask);
        final HttpRequest.BodyPublisher bodyUpd = HttpRequest.BodyPublishers.ofString(jsonSecond);
        HttpRequest requestPostUpd = HttpRequest.newBuilder().uri(secondUrl).POST(bodyUpd).build();
        HttpResponse<String> responsePostUpd = client.send(requestPostUpd, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, responsePostUpd.statusCode(),
                "Ошибка при отправлении POST-запроса на обновление TASK");

        HttpRequest requestDelete = HttpRequest.newBuilder().uri(secondUrl).DELETE().build();
        HttpResponse<String> responseDelete = client.send(requestDelete, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, responseDelete.statusCode(),
                "Ошибка при отправлении DELETE-запроса на удаление TASK");
    }

    @Test
    public void unrealisedMethods() throws IOException, InterruptedException {
        String test = "test";
        final HttpRequest.BodyPublisher body = HttpRequest.BodyPublishers.ofString(test);


        URI urlPostAllTasks = URI.create(mainUrl.toString());
        HttpRequest requestPost = HttpRequest.newBuilder().uri(urlPostAllTasks).POST(body).build();
        HttpResponse<String> responsePostAllTasks = client.send(requestPost, HttpResponse.BodyHandlers.ofString());
        assertEquals(405, responsePostAllTasks.statusCode(),
                "Неверный статус при вызове нереализованного метода в пути /tasks");

        URI urlPutTask = URI.create(mainUrl + "task");
        HttpRequest requestPut = HttpRequest.newBuilder().uri(urlPutTask).PUT(body).build();
        HttpResponse<String> responsePutTask = client.send(requestPut, HttpResponse.BodyHandlers.ofString());
        assertEquals(405, responsePutTask.statusCode(),
                "Неверный статус при вызове нереализованного метода в пути /tasks/task");

        URI urlPutEpic = URI.create(mainUrl + "epic");
        HttpRequest requestPutEpic = HttpRequest.newBuilder().uri(urlPutEpic).PUT(body).build();
        HttpResponse<String> responsePutEpic = client.send(requestPutEpic, HttpResponse.BodyHandlers.ofString());
        assertEquals(405, responsePutEpic.statusCode(),
                "Неверный статус при вызове нереализованного метода в пути /tasks/epic");

        URI urlPutSub = URI.create(mainUrl + "subtask");
        HttpRequest requestPutSub = HttpRequest.newBuilder().uri(urlPutSub).PUT(body).build();
        HttpResponse<String> responsePutSub = client.send(requestPutEpic, HttpResponse.BodyHandlers.ofString());
        assertEquals(405, responsePutSub.statusCode(),
                "Неверный статус при вызове нереализованного метода в пути /tasks/subtask");

        URI urlPutHistory = URI.create(mainUrl + "history");
        HttpRequest requestPutHistory = HttpRequest.newBuilder().uri(urlPutHistory).PUT(body).build();
        HttpResponse<String> responsePutHistory = client.send(requestPutHistory, HttpResponse.BodyHandlers.ofString());
        assertEquals(405, responsePutHistory.statusCode(),
                "Неверный статус при вызове нереализованного метода в пути /tasks/history");
    }

    @Test
    public void wrongTaskIdTesting() throws IOException, InterruptedException {
        URI Url = URI.create(mainUrl + "task/?id=9999");
        HttpRequest requestGetId = HttpRequest.newBuilder().uri(Url).GET().build();
        HttpResponse<String> responseGetId = client.send(requestGetId, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, responseGetId.statusCode(),
                "Ошибка при отправлении GET-запроса на получение TASK, ID которой нет");

        URI secondUrl = URI.create(mainUrl + "task/?status=NOVOE");
        HttpRequest requestGetWrong = HttpRequest.newBuilder().uri(secondUrl).GET().build();
        HttpResponse<String> responseGetWrong = client.send(requestGetWrong, HttpResponse.BodyHandlers.ofString());
        assertEquals(400, responseGetWrong.statusCode(),
                "Ошибка при отправлении GET-запроса на получение TASK с неизвестным параметром");
    }

    @Test
    public void testEpics() throws IOException, InterruptedException {
        HttpResponse<String> responsePost = createTectEpic();

        assertEquals(200, responsePost.statusCode(),
                "Ошибка при отправлении POST-запроса на создание новой EPIC");
        assertEquals(responsePost.body(), "Эпик успешно создан!",
                "Неверный ответ сервера при попытке создать Эпик.");


        EpicTask taskUpd = new EpicTask(1, "Epic2", "neeeeeew");
        String jsonUpd = gson.toJson(taskUpd);
        final HttpRequest.BodyPublisher bodyUpd = HttpRequest.BodyPublishers.ofString(jsonUpd);
        URI urlPostUpd = URI.create(mainUrl + "epic/?id=1");
        HttpRequest requestPostUpd = HttpRequest.newBuilder().uri(urlPostUpd).POST(bodyUpd).build();
        HttpResponse<String> responsePostUpd = client.send(requestPostUpd, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, responsePostUpd.statusCode(),
                "Ошибка при отправлении POST-запроса на обновление EPIC");
        assertEquals(responsePostUpd.body(), "Эпик успешно обновлен!",
                "Неверный ответ сервера при попытке обновить Эпик.");

        HttpRequest requestPostGet = HttpRequest.newBuilder().uri(urlPostUpd).GET().build();
        HttpResponse<String> responsePostGet = client.send(requestPostGet, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, responsePostGet.statusCode(),
                "Ошибка при отправлении GET-запроса на получение EPIC");

        HttpRequest requestPostDelete = HttpRequest.newBuilder().uri(urlPostUpd).DELETE().build();
        HttpResponse<String> responsePostDelete = client.send(requestPostDelete, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, responsePostDelete.statusCode(),
                "Ошибка при отправлении DELETE-запроса на удаление EPIC");

        HttpRequest requestPostGetAfterDelete = HttpRequest.newBuilder().uri(urlPostUpd).GET().build();
        HttpResponse<String> responsePostGetAfterDelete = client.send(requestPostGetAfterDelete,
                HttpResponse.BodyHandlers.ofString());
        assertEquals(404, responsePostGetAfterDelete.statusCode(),
                "Ошибка при отправлении GET-запроса на получение удаленного EPIC");
    }

    @Test
    public void getEmptyEpics() throws IOException, InterruptedException {
        URI urlGet = URI.create(mainUrl + "epic");
        HttpRequest requestPostGet = HttpRequest.newBuilder().uri(urlGet).GET().build();
        HttpResponse<String> responsePostGet = client.send(requestPostGet, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, responsePostGet.statusCode(),
                "Ошибка при отправлении GET-запроса на получение пустого списка EPIC");
    }

    @Test
    public void testSubTasks() throws IOException, InterruptedException {
        createTectEpic();

        SubTask task = new SubTask("Sub1", "neew", 1);
        String json = gson.toJson(task);
        final HttpRequest.BodyPublisher body = HttpRequest.BodyPublishers.ofString(json);

        URI urlPost = URI.create(mainUrl + "subtask/");
        HttpRequest requestPost = HttpRequest.newBuilder().uri(urlPost).POST(body).build();
        HttpResponse<String> responsePost = client.send(requestPost, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, responsePost.statusCode(),
                "Ошибка при отправлении POST-запроса на создание новой подзадачи");

        SubTask taskUpd = new SubTask(2, "Sub222", "neeeeeew", TaskStatus.DONE, 1);
        String jsonUpd = gson.toJson(taskUpd);
        final HttpRequest.BodyPublisher bodyUpd = HttpRequest.BodyPublishers.ofString(jsonUpd);
        URI urlPostUpd = URI.create(mainUrl + "subtask/?id=2");
        HttpRequest requestPostUpd = HttpRequest.newBuilder().uri(urlPostUpd).POST(bodyUpd).build();
        HttpResponse<String> responsePostUpd = client.send(requestPostUpd, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, responsePostUpd.statusCode(),
                "Ошибка при отправлении POST-запроса на обновление подзадачи");

        HttpRequest requestPostGet = HttpRequest.newBuilder().uri(urlPostUpd).GET().build();
        HttpResponse<String> responsePostGet = client.send(requestPostGet, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, responsePostGet.statusCode(),
                "Ошибка при отправлении GET-запроса на получение подзадачи");

        HttpRequest requestPostGetAll = HttpRequest.newBuilder().uri(urlPost).GET().build();
        HttpResponse<String> responsePostGetAll = client.send(requestPostGetAll, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, responsePostGetAll.statusCode(),
                "Ошибка при отправлении GET-запроса на получение списка подзадач");

        HttpRequest requestPostDelete = HttpRequest.newBuilder().uri(urlPostUpd).DELETE().build();
        HttpResponse<String> responsePostDelete = client.send(requestPostDelete, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, responsePostDelete.statusCode(),
                "Ошибка при отправлении DELETE-запроса на удаление подзадачи");

        HttpRequest requestPostGetAllEmpty = HttpRequest.newBuilder().uri(urlPost).GET().build();
        HttpResponse<String> responsePostGetAllEmpty = client.send(requestPostGetAllEmpty,
                HttpResponse.BodyHandlers.ofString());
        assertEquals(404, responsePostGetAllEmpty.statusCode(),
                "Ошибка при отправлении GET-запроса на получение пустого списка подзадач");
    }

    @Test
    public void testNoEpicForThisSubTask() throws IOException, InterruptedException {
        SubTask task = new SubTask("Sub1", "neew", 1);
        String json = gson.toJson(task);
        final HttpRequest.BodyPublisher body = HttpRequest.BodyPublishers.ofString(json);

        URI urlPost = URI.create(mainUrl + "subtask/");
        HttpRequest requestPost = HttpRequest.newBuilder().uri(urlPost).POST(body).build();
        HttpResponse<String> responsePost = client.send(requestPost, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, responsePost.statusCode(),
                "Ошибка при отправлении POST-запроса на создание новой подзадачи при отсутствии эпика.");
    }

    private HttpResponse<String> createTectEpic() throws IOException, InterruptedException {
        EpicTask task = new EpicTask("Epic1", "neew");
        String json = gson.toJson(task);
        final HttpRequest.BodyPublisher body = HttpRequest.BodyPublishers.ofString(json);
        URI urlPost = URI.create(mainUrl + "epic/");
        HttpRequest requestPost = HttpRequest.newBuilder().uri(urlPost).POST(body).build();
        return client.send(requestPost, HttpResponse.BodyHandlers.ofString());
    }


}
