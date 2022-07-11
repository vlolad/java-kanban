import net.yandex.taskmanager.model.*;
import net.yandex.taskmanager.services.FileBackedTasksManager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;

public class FileBackedTasksManagerTest extends TaskManagerTest <FileBackedTasksManager>  {

    private final String TEST_SAVING = "testSave.csv";

    @BeforeEach
    public void createTaskManager(){
        setTaskManager(new FileBackedTasksManager(new File(TEST_SAVING)));
    }

    @Test
    public void saveAndLoadEmptyManager(){
        getTaskManager().save();

        FileBackedTasksManager newTaskManager =  FileBackedTasksManager.loadFromFile(new File(TEST_SAVING));

        assertEquals(0, newTaskManager.getTasks().size());
        assertEquals(0, newTaskManager.getEpics().size());
        assertEquals(0, newTaskManager.getSubTasks().size());
    }

    @Test
    public void saveAndLoadClearEpic(){
        getTaskManager().createEpic(new EpicTask("test1", "sus"));

        FileBackedTasksManager newTaskManager =  FileBackedTasksManager.loadFromFile(new File(TEST_SAVING));
        assertEquals(1, newTaskManager.getEpics().size());
        assertEquals("test1", newTaskManager.getEpicByID(1).getName());
        assertEquals(0, newTaskManager.getEpicSubTasks(1).size());
    }

    @Test
    public void saveAndLoadClearHistory(){
        getTaskManager().createTask(new Task("test1", "sus"));
        getTaskManager().getTaskByID(1);
        getTaskManager().deleteTaskByID(1);

        FileBackedTasksManager newTaskManager =  FileBackedTasksManager.loadFromFile(new File(TEST_SAVING));
        assertEquals(0, newTaskManager.getHistoryManager().getHistory().size());
    }
}
