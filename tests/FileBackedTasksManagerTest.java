import net.yandex.taskmanager.model.*;
import net.yandex.taskmanager.services.FileBackedTasksManager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.time.LocalDateTime;

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
        assertEquals(0, newTaskManager.getHistory().size(),
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

        assertEquals(3, newTaskManager.getHistory().size(),
                "Неверная загрузка истории из файла: Ожидалось (число записей) - 3, Вывод - " +
                        newTaskManager.getHistory().size());

        newTaskManager.createTask(new Task("newTest1", "sas"));
        assertEquals(4, newTaskManager.getTaskByID(4).getId(),
                "Неверная загрузка последнего использованного ID из файла.");
    }

    @Test
    public void compareManagersTesting(){
        getTaskManager().createTask(new Task("Task1", "new", TaskStatus.NEW,
                LocalDateTime.of(2022,7,10,20,0), 30));
        getTaskManager().createEpic(new EpicTask("Epic1", "neew"));
        getTaskManager().createSubTask(new SubTask("SubTask1", "neeew", TaskStatus.IN_PROGRESS, 2,
                LocalDateTime.of(2022,7,11,20,0), 15));
        getTaskManager().createSubTask(new SubTask("SubTask2", "sus", TaskStatus.NEW, 2,
                LocalDateTime.of(2022,7,11,22,0), 60));
        getTaskManager().createTask(new Task("Task2", "now"));

        getTaskManager().getTaskByID(1);
        getTaskManager().getTaskByID(5);
        getTaskManager().getSubTaskByID(3);
        getTaskManager().getEpicByID(2);
        getTaskManager().getSubTaskByID(4);

        FileBackedTasksManager newTaskManager =  FileBackedTasksManager.loadFromFile(new File(TEST_SAVING));

        assertEquals(getTaskManager().getTasks(), newTaskManager.getTasks(),
                "Список Тасков после загрузки из файла не совпадает");
        assertEquals(getTaskManager().getSubTasks(), newTaskManager.getSubTasks(),
                "Список Сабтасков после загрузки из файла не совпадает");
        assertEquals(getTaskManager().getEpics(), newTaskManager.getEpics(),
                "Список Эпиков после загрузки из файла не совпадает");
        assertEquals(getTaskManager().getPrioritizedTasks(), newTaskManager.getPrioritizedTasks(),
                "Отсортированный список после загрузки из файла не совпадает");
        assertEquals(getTaskManager().getHistory(), newTaskManager.getHistory(),
                "История после загрузки из файла не совпадает");
    }

    @Test
    public void SaveAndLoadTasksWithDataAndTime(){
        getTaskManager().createTask(new Task("Task123", "hehe", TaskStatus.NEW,
                LocalDateTime.of(2022,7,10,20,0), 30));
        getTaskManager().createEpic(new EpicTask("FirstEpic", "boom"));
        getTaskManager().createSubTask(new SubTask("1subtask1", "hehah", TaskStatus.IN_PROGRESS, 2,
                LocalDateTime.of(2022,7,11,20,0), 15));
        getTaskManager().createSubTask(new SubTask("test3", "suss", TaskStatus.NEW, 2,
                LocalDateTime.of(2022,7,11,22,0), 60));

        FileBackedTasksManager newTaskManager =  FileBackedTasksManager.loadFromFile(new File(TEST_SAVING));

        assertNotNull(newTaskManager.getTaskByID(1).getStartTime(),
                "Не было загружено поле startTime у класса Таск.");
        LocalDateTime expDateTime = LocalDateTime.of(2022,7,10,20,30);
        assertEquals(expDateTime, newTaskManager.getTaskByID(1).getEndTime(),
                "Неправильно загружено и/или просчитано время старта и окончания класса Таск");

        LocalDateTime expectedStartTime = LocalDateTime.of(2022,7,11,20,0);
        LocalDateTime expectedEndTime = LocalDateTime.of(2022,7,11,23,0);
        assertEquals(expectedStartTime, newTaskManager.getEpicByID(2).getStartTime(),
                "Неправильно расчитывается время начала Эпика");
        assertEquals(expectedEndTime, newTaskManager.getEpicByID(2).getEndTime(),
                "Неправильно расчитывается время окончания Эпика");
    }

    /*@Test
    public void SaveAndLoadSortedTasks(){
        getTaskManager().createTask(new Task("CorrectTask", "new", TaskStatus.NEW,
                LocalDateTime.of(2022,7,10,20,0), 30));
        getTaskManager().createEpic(new EpicTask("IncorrectTask", "boom"));
        getTaskManager().createSubTask(new SubTask("IncorrectTask", "new", TaskStatus.IN_PROGRESS, 2,
                LocalDateTime.of(2022,7,11,20,0), 15));
        getTaskManager().createSubTask(new SubTask("IncorrectTask", "new", TaskStatus.NEW, 2,
                LocalDateTime.of(2022,7,11,22,0), 60));

        FileBackedTasksManager newTaskManager =  FileBackedTasksManager.loadFromFile(new File(TEST_SAVING));

        assertEquals("CorrectTask", newTaskManager.getPrioritizedTasks().first().getName(),
                "Неверная сортировка загруженных из файла задач");
    }*/
}
