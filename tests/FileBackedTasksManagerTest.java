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

        assertEquals(0, newTaskManager.getTasks().size(), "Список тасков не пустой");
        assertEquals(0, newTaskManager.getEpics().size(), "Список эпиков не пустой");
        assertEquals(0, newTaskManager.getSubTasks().size(), "Список сабтасков не пустой");
    }

    @Test
    public void saveAndLoadClearEpic(){
        getTaskManager().createEpic(new EpicTask("test1", "sus"));

        FileBackedTasksManager newTaskManager =  FileBackedTasksManager.loadFromFile(new File(TEST_SAVING));
        assertEquals(1, newTaskManager.getEpics().size(), "Эпик не загружен.");
        assertEquals("test1", newTaskManager.getEpicByID(1).getName(), "Эпик не загружен.");
        assertEquals(0, newTaskManager.getEpicSubTasks(1).size(),
                "Ошибка - Эпик содержит сабтаски.");
    }

    @Test
    public void saveAndLoadClearHistory(){
        getTaskManager().createTask(new Task("test1", "sus"));
        getTaskManager().getTaskByID(1);
        getTaskManager().deleteTaskByID(1);

        FileBackedTasksManager newTaskManager =  FileBackedTasksManager.loadFromFile(new File(TEST_SAVING));
        assertEquals(0, newTaskManager.getHistoryManager().getHistory().size(),
                "Загружена не пустая история");
    }

    @Test
    public void SaveAndLoadManagerNormalBehavior(){
        getTaskManager().createTask(new Task("test1", "sus"));
        getTaskManager().createEpic(new EpicTask("test2", "sus"));
        getTaskManager().createSubTask(new SubTask("test1", "sus", 2));
        getTaskManager().getTaskByID(1);
        getTaskManager().getEpicByID(2);
        getTaskManager().getSubTaskByID(3);

        FileBackedTasksManager newTaskManager =  FileBackedTasksManager.loadFromFile(new File(TEST_SAVING));

        assertEquals(1, newTaskManager.getTaskByID(1).getId(),
                "Неверная загрузка Таски из файла.");
        assertEquals(2, newTaskManager.getEpicByID(2).getId(),
                "Неверная загрузка Эпика из файла.");
        assertEquals(3, newTaskManager.getSubTaskByID(3).getId(),
                "Неверная загрузка Сабтаски из файла.");

        assertEquals(3, newTaskManager.getHistoryManager().getHistory().size(),
                "Неверная загрузка истории из файла: Ожидалось (число записей) - 3, Вывод - " +
                        newTaskManager.getHistoryManager().getHistory().size());

        newTaskManager.createTask(new Task("newTest1", "sas"));
        assertEquals(4, newTaskManager.getTaskByID(4).getId(),
                "Неверная загрузка последнего использованного ID из файла.");
    }
}
