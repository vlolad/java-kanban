import net.yandex.taskmanager.model.*;
import net.yandex.taskmanager.services.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;


public class EpicTasksTest {

    private static TaskManager taskManager;
    private static int lastId;

    @BeforeEach
    public void createTaskManager(){
        taskManager = Managers.getDefault();
        taskManager.createEpic(new EpicTask(1, "test", "testing"));
        lastId = 2; // Необхоидма для запоминания, какой ID создавался последним
    }

    @Test
    public void checkEpicForDoneWithEmptyList(){
        taskManager.clearSubTasks(); // чтобы у эпика точно был пустой список сабтасков

        assertEquals(0, taskManager.getEpicSubTasks(1).size());
    }

    @Test
    public void checkEpicForDoneWithAllNEW(){
        fillWithSubs(3, TaskStatus.NEW);

        assertEquals(3, taskManager.getEpicSubTasks(1).size());
        assertEquals(TaskStatus.NEW, taskManager.getEpicByID(1).getStatus());
    }

    @Test
    public void checkEpicForDoneWithAllDONE(){
        fillWithSubs(5, TaskStatus.DONE);

        assertEquals(5, taskManager.getEpicSubTasks(1).size());
        assertEquals(TaskStatus.DONE, taskManager.getEpicByID(1).getStatus());
    }

    @Test
    public void checkEpicForDoneWithNEWAndDONE(){
        fillWithSubs(2, TaskStatus.NEW);
        fillWithSubs(3, TaskStatus.DONE);

        assertEquals(5, taskManager.getEpicSubTasks(1).size());
        assertEquals(TaskStatus.IN_PROGRESS, taskManager.getEpicByID(1).getStatus());
    }

    @Test
    public void checkEpicForDoneWithAllINPROGRESS(){
        fillWithSubs(5, TaskStatus.IN_PROGRESS);

        assertEquals(5, taskManager.getEpicSubTasks(1).size());
        assertEquals(TaskStatus.IN_PROGRESS, taskManager.getEpicByID(1).getStatus());
    }

    private void fillWithSubs(int num, TaskStatus status){
        int toNewId = lastId + num;
        for (int i = lastId; i < toNewId; i++){
            taskManager.createSubTask(new SubTask(i, "sub" + i, "newSub", status, 1));
        }
        lastId = toNewId; // Необходимо, чтобы при вызове этого метода более 1 раза
        // сабтаски получали новые порядковые ID
    }
}
