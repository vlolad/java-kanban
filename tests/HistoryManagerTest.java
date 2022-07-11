import net.yandex.taskmanager.model.Task;
import net.yandex.taskmanager.model.TaskStatus;
import net.yandex.taskmanager.services.*;

import org.junit.jupiter.api.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class HistoryManagerTest extends InMemoryHistoryManager implements HistoryManager {
    private HistoryManager historyManager;
    private final ByteArrayOutputStream output = new ByteArrayOutputStream();

    public void setUpStreams() {
        System.setOut(new PrintStream(output));
    }

    @BeforeEach
    public void createManager(){
        this.historyManager = Managers.getDefaultHistory();
    }

    @AfterEach
    public void cleanUpStreams() {
        System.setOut(null);
    }

    @Test
    public void blankManagerGetHistory(){
        assertNotNull(historyManager.getHistory(),
                "Некорректное создание пустой истории.");
        assertEquals(0, historyManager.getHistory().size(),
                "История не пустая при создании.");
    }

    @Test
    public void clearManagerGetHistory(){
        historyManager.add(new Task(1, "test", "test", TaskStatus.NEW));
        assertEquals(1, historyManager.getHistory().size(),
                "Неверное добавление Таски в историю: Ожидалось - 1, Вывод - " +
                        historyManager.getHistory().size());

        historyManager.remove(1);
        assertEquals(0, historyManager.getHistory().size(),
                "Неверное удаление Таски из истории: Ожидалось (кол-во Таск в истории) - 0, Вывод - " +
                        historyManager.getHistory().size());
    }

    //Проверка на изменение порядка при повторном добавлении задач из истории
    @Test
    public void duplicateTasksInHistory(){
        fillHistory(3);

        historyManager.add(new Task(2, "testing1", "sus", TaskStatus.IN_PROGRESS));
        historyManager.add(new Task(1, "testing2", "sus", TaskStatus.DONE));

        ArrayList<Integer> historyIdList = new ArrayList<>();
        for (Task task : historyManager.getHistory()){
            historyIdList.add(task.getId());
        }
        ArrayList<Integer> expectedHistory = new ArrayList<>(Arrays.asList(3, 2, 1));

        assertEquals(expectedHistory, historyIdList,
                "История неверно реагирует на добавление повторяющихся Тасков.");
    }

    @Test
    public void deleteFromBeginning(){
        fillHistory(5);
        historyManager.remove(1);
        Task task = historyManager.getHistory().get(0);
        assertEquals(2, task.getId(),
                "Неверное поведение истории при удалении задачи из начала списка.");
    }

    @Test
    public void deleteFromEnding(){
        fillHistory(5);
        historyManager.remove(5);
        Task task = historyManager.getHistory()
                .get(historyManager.getHistory().size()-1);
        assertEquals(4, task.getId(),
                "Неверное поведение истории при удалении задачи с конца списка.");
    }

    @Test
    public void deleteFromCenter(){
        fillHistory(5);
        historyManager.remove(3);
        Task task1 = historyManager.getHistory().get(1);
        Task task2 = historyManager.getHistory().get(2);

        assertEquals(4, historyManager.getHistory().size(),
                "Неверный размер истории после удаления задачи из центра: "
        + " Ожидалось - 4, Вывод - " + historyManager.getHistory().size());
        assertEquals(2, task1.getId(),
                "Неверное поведение истории при удалении задачи из центра: " +
                "Таск перед удаляемой задачей содержит неверный ID - " + task1.getId() + "(Ожидалось - 2)");
        assertEquals(4, task2.getId(),
                "Неверное поведение истории при удалении задачи из центра: " +
                        "Таск после удаляемой задачей содержит неверный ID - " + task2.getId() + "(Ожидалось - 4)");
    }

    private void fillHistory(int value){
        for (int i = 1; i <= value; i++){
            historyManager.add(new Task(i, "test " + i, "sus", TaskStatus.NEW));
        }
    }
}
