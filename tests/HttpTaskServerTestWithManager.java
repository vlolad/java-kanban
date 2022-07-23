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
import java.util.stream.Collectors;


public class HttpTaskServerTestWithManager {  
    
    private HTTPTaskServer server;
    private static KVServer mainServer;
    private final HttpClient client = HttpClient.newHttpClient();
    private final URL mainUrl = new URL("http://localhost:8080/tasks/");
    private final Gson gson = new Gson();
    private static final TaskManager manager;
    
    static {
        try {
            mainServer = new KVServer();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mainServer.start();
        manager = Managers.getDefault();
        manager.createTask(new Task("test1", "sus"));
        manager.createEpic(new EpicTask("Epic1", "neew"));
        manager.createSubTask(new SubTask("SubTask1", "neeew", TaskStatus.IN_PROGRESS, 2,
                LocalDateTime.of(2022, 7, 11, 20, 0), 15));
        manager.createSubTask(new SubTask("SubTask2", "sus", TaskStatus.NEW, 2,
                LocalDateTime.of(2022, 7, 11, 22, 0), 60));
        manager.getTaskByID(1);
        manager.getSubTaskByID(3);
        manager.getEpicByID(2);
        manager.getSubTaskByID(4);
        manager.getTaskByID(1);
    }
    
    public HttpTaskServerTestWithManager() throws MalformedURLException {
    }

    @BeforeEach
    public void createServer() throws IOException {
        System.setOut(new PrintStream(new ByteArrayOutputStream()));
        server = new HTTPTaskServer(manager);
        server.start();
    }

    @AfterEach
    public void stopServer() {
        server.closeServer();
        mainServer.stop();
    }

    @Test
    public void getAllTasksTest() throws IOException, InterruptedException {
        URI url = URI.create(mainUrl.toString());
        HttpRequest requestGet = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> responseGet = client.send(requestGet, HttpResponse.BodyHandlers.ofString());

        ArrayList<Task> getTaskList = gson.fromJson(responseGet.body(), new TypeToken<ArrayList<Task>>(){}.getType());
        boolean check = (getTaskList.get(0).getId() == 3) &&
                (getTaskList.get(1).getId() == 4) &&
                (getTaskList.get(2).getId() == 1);
        assertTrue(check, "Ошибка в получении всех задач.");
    }

    @Test
    public void getHistoryTest() throws IOException, InterruptedException {
        URI url = URI.create(mainUrl+ "history");
        HttpRequest requestGet = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> responseGet = client.send(requestGet, HttpResponse.BodyHandlers.ofString());
        List<Task> gettingHistory = gson.fromJson(responseGet.body(),
                new TypeToken<List<Task>>() {}.getType());
        List<Integer> arrivedHistory = gettingHistory.stream()
                .map(Task::getId).collect(Collectors.toList());
        List<Integer> savedHistory = manager.getHistory().stream()
                .map(Task::getId).collect(Collectors.toList());

        assertEquals(arrivedHistory, savedHistory,
                "Ошибка: различия в полученной и сохраненной историях.");
    }
    
}
