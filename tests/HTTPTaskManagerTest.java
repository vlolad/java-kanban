import net.yandex.taskmanager.model.*;
import net.yandex.taskmanager.services.*;

import org.junit.jupiter.api.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class HTTPTaskManagerTest extends TaskManagerTest<HTTPTaskManager> {

    KVServer server;

    public HTTPTaskManagerTest() throws IOException {
    }

    @BeforeEach
    public void createTaskManager() throws IOException, InterruptedException {
        setTaskManager(new HTTPTaskManager(new URL("http://localhost:8078")));
    }

    @BeforeEach
    public void startServer() throws IOException {
        System.setOut(new PrintStream(new ByteArrayOutputStream())); // Иначе System.out выбрасывает NPE
        server = new KVServer();
        server.start();
    }

    @AfterEach
    public void stopServer() {
        server.stop();
    }

    @Test
    public void loadEmptyManager() throws IOException, InterruptedException {
        setUpStreams();
        getTaskManager().load();
        assertTrue(getOutput().toString().contains("Загрузка с сервера не удалась. Возможно, менеджер ещё не сохранялся."),
                "Неправильный вывод при попытке загрузить несохраненный/пустой менеджер.");
    }

    @Test
    public void saveAndLoadClearEpic() throws IOException, InterruptedException {
        getTaskManager().createEpic(new EpicTask("test1", "sus"));
        getTaskManager().load();
        assertEquals(1, getTaskManager().getEpics().size(), "Эпик не загружен.");
        assertEquals("test1", getTaskManager().getEpicByID(1).getName(), "Эпик не загружен.");
        assertEquals(0, getTaskManager().getEpicSubTasks(1).size(),
                "Ошибка - Эпик содержит сабтаски.");
    }

    @Test
    public void saveAndLoadClearHistory() throws IOException, InterruptedException {
        getTaskManager().createTask(new Task("test1", "sus"));
        getTaskManager().getTaskByID(1);
        getTaskManager().deleteTaskByID(1);
        getTaskManager().load();
        assertEquals(0, getTaskManager().getHistory().size(),
                "Загружена не пустая история");
    }

    @Test
    public void SaveAndLoadManagerNormalBehavior() throws IOException, InterruptedException {
        getTaskManager().createTask(new Task("test1", "sus"));
        getTaskManager().createEpic(new EpicTask("test2", "sus"));
        getTaskManager().createSubTask(new SubTask("test1", "sus", 2));
        getTaskManager().getTaskByID(1);
        getTaskManager().getEpicByID(2);
        getTaskManager().getSubTaskByID(3);

        getTaskManager().load();
        assertEquals(1, getTaskManager().getTaskByID(1).getId(),
                "Неверная загрузка Таски из файла.");
        assertEquals(2, getTaskManager().getEpicByID(2).getId(),
                "Неверная загрузка Эпика из файла.");
        assertEquals(3, getTaskManager().getSubTaskByID(3).getId(),
                "Неверная загрузка Сабтаски из файла.");
        assertEquals(3, getTaskManager().getHistory().size(),
                "Неверная загрузка истории из файла: Ожидалось (число записей) - 3, Вывод - " +
                        getTaskManager().getHistory().size());

        getTaskManager().createTask(new Task("newTest1", "sas"));
        assertEquals(4, getTaskManager().getTaskByID(4).getId(),
                "Неверная загрузка последнего использованного ID из файла.");
    }

    @Test
    public void SaveAndLoadTasksWithDataAndTime() throws IOException, InterruptedException {
        getTaskManager().createTask(new Task("Task123", "hehe", TaskStatus.NEW,
                LocalDateTime.of(2022, 7, 10, 20, 0), 30));
        getTaskManager().createEpic(new EpicTask("FirstEpic", "boom"));
        getTaskManager().createSubTask(new SubTask("1subtask1", "hehah", TaskStatus.IN_PROGRESS, 2,
                LocalDateTime.of(2022, 7, 11, 20, 0), 15));
        getTaskManager().createSubTask(new SubTask("test3", "suss", TaskStatus.NEW, 2,
                LocalDateTime.of(2022, 7, 11, 22, 0), 60));

        getTaskManager().load();

        assertNotNull(getTaskManager().getTaskByID(1).getStartTime(),
                "Не было загружено поле startTime у класса Таск.");
        LocalDateTime expDateTime = LocalDateTime.of(2022, 7, 10, 20, 30);
        assertEquals(expDateTime, getTaskManager().getTaskByID(1).getEndTime(),
                "Неправильно загружено и/или просчитано время старта и окончания класса Таск");

        LocalDateTime expectedStartTime = LocalDateTime.of(2022, 7, 11, 20, 0);
        LocalDateTime expectedEndTime = LocalDateTime.of(2022, 7, 11, 23, 0);
        assertEquals(expectedStartTime, getTaskManager().getEpicByID(2).getStartTime(),
                "Неправильно рассчитывается время начала Эпика");
        assertEquals(expectedEndTime, getTaskManager().getEpicByID(2).getEndTime(),
                "Неправильно рассчитывается время окончания Эпика");
    }
}
