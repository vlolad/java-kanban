import net.yandex.taskmanager.model.Task;
import net.yandex.taskmanager.model.TaskStatus;
import net.yandex.taskmanager.services.*;

import org.junit.jupiter.api.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
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
        assertNotNull(historyManager.getHistory());
        assertEquals(0, historyManager.getHistory().size());
    }

    @Test
    public void clearManagerGetHistory(){
        historyManager.add(new Task(1, "test", "test", TaskStatus.NEW));
        assertEquals(1, historyManager.getHistory().size());

        historyManager.remove(1);
        assertEquals(0, historyManager.getHistory().size());
    }

    //Проверка на изменение порядка при повторном добавлении задач из истории
    @Test
    public void duplicateTasksInHistory(){
        fillHistory(3);

        historyManager.add(new Task(2, "testing1", "sus", TaskStatus.IN_PROGRESS));
        historyManager.add(new Task(1, "testing2", "sus", TaskStatus.DONE));
        setUpStreams();
        System.out.println(historyManager.getHistory());
        assertEquals("[Task{name= «test 3» | id=«3» | description(length)=«3» | status=«NEW»}, " +
                "Task{name= «testing1» | id=«2» | description(length)=«3» | status=«IN_PROGRESS»}, " +
                "Task{name= «testing2» | id=«1» | description(length)=«3» | status=«DONE»}]",
                output.toString().trim()
        );
    }

    @Test
    public void deleteFromBeginning(){
        fillHistory(5);
        historyManager.remove(1);
        Task task = historyManager.getHistory().get(0);
        assertEquals("test 2", task.getName());
        assertEquals(2, task.getId());
    }

    @Test
    public void deleteFromEnding(){
        fillHistory(5);
        historyManager.remove(5);
        Task task = historyManager.getHistory()
                .get(historyManager.getHistory().size()-1);
        assertEquals("test 4", task.getName());
        assertEquals(4, task.getId());
    }

    @Test
    public void deleteFromCenter(){
        fillHistory(5);
        historyManager.remove(3);
        Task task1 = historyManager.getHistory().get(1);
        Task task2 = historyManager.getHistory().get(2);

        assertEquals(2, task1.getId());
        assertEquals(4, task2.getId());
        assertEquals("test 2", task1.getName());
        assertEquals("test 4", task2.getName());
        assertEquals(4, historyManager.getHistory().size());
    }

    private void fillHistory(int value){
        for (int i = 1; i <= value; i++){
            historyManager.add(new Task(i, "test " + i, "sus", TaskStatus.NEW));
        }
    }
}
