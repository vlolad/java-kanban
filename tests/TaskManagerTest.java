import net.yandex.taskmanager.model.*;
import net.yandex.taskmanager.services.*;

import org.junit.jupiter.api.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

abstract class TaskManagerTest <T extends TaskManager> {
    private T taskManager;
    private final ByteArrayOutputStream output = new ByteArrayOutputStream();

    public T getTaskManager() {
        return taskManager;
    }

    public void setTaskManager(T taskManager) {
        this.taskManager = taskManager;
    }

    public void setUpStreams() {
        System.setOut(new PrintStream(output));
    }

    @AfterEach
    public void cleanUpStreams() {
        System.setOut(null);
    }

    @Test
    public void createTaskTesting(){
        taskManager.createTask(new Task("name1", "desc1"));

        assertEquals("name1", taskManager.getTaskByID(1).getName());
        assertEquals(TaskStatus.NEW, taskManager.getTaskByID(1).getStatus());

        taskManager.createTask(new Task("name2", "desc2", TaskStatus.IN_PROGRESS));

        assertEquals("name2", taskManager.getTaskByID(2).getName());
        assertEquals(TaskStatus.IN_PROGRESS, taskManager.getTaskByID(2).getStatus());
    }

    @Test
    public void createEpicTesting(){
        taskManager.createEpic(new EpicTask("name1", "desc1"));

        assertEquals("name1", taskManager.getEpicByID(1).getName());
        assertEquals(TaskStatus.NEW, taskManager.getEpicByID(1).getStatus());
    }

    @Test
    public void createSubTaskTestingAllOk(){
        taskManager.createEpic(new EpicTask(1, "test", "testing"));
        taskManager.createSubTask(new SubTask(2, "sub2", "newSub", TaskStatus.NEW, 1));


        assertEquals("sub2", taskManager.getSubTaskByID(2).getName());
        assertEquals(TaskStatus.NEW, taskManager.getSubTaskByID(2).getStatus());
        assertEquals(TaskStatus.NEW, taskManager.getEpicByID(1).getStatus());

        taskManager.createSubTask(new SubTask(3, "sub3", "newSub", TaskStatus.DONE, 1));

        assertEquals("sub3", taskManager.getSubTaskByID(3).getName());
        assertEquals(TaskStatus.DONE, taskManager.getSubTaskByID(3).getStatus());
        assertEquals(TaskStatus.IN_PROGRESS, taskManager.getEpicByID(1).getStatus());

        assertEquals(2, taskManager.getEpicsMap().get(1).getSubTasksIDs().size());
    }
    @Test
    public void createSubTaskTestingNoSuchEpic(){
        setUpStreams();
        taskManager.createSubTask(new SubTask(2, "sub2", "newSub", TaskStatus.NEW, 1));
        assertEquals("There is no epic with ID 1", output.toString().trim());
    }

    @Test
    public void getMethodsTesting(){
        assertEquals(0, taskManager.getEpics().size());
        assertEquals(0, taskManager.getTasks().size());
        assertEquals(0, taskManager.getSubTasks().size());
        assertEquals(0, taskManager.getEpicsMap().size());
        assertEquals(0, taskManager.getTasksMap().size());
        assertEquals(0, taskManager.getSubTasksMap().size());

        taskManager.createEpic(new EpicTask(1, "test", "testing"));
        taskManager.createSubTask(new SubTask(2, "sub1", "newSub", TaskStatus.DONE, 1));
        taskManager.createSubTask(new SubTask(3, "sub1", "newSub", TaskStatus.NEW, 1));
        taskManager.createTask(new Task(4, "testTask", "subs", TaskStatus.IN_PROGRESS));

        assertEquals(1, taskManager.getEpics().size());
        assertEquals(1, taskManager.getTasks().size());
        assertEquals(2, taskManager.getSubTasks().size());
        assertEquals(1, taskManager.getEpicsMap().size());
        assertEquals(1, taskManager.getTasksMap().size());
        assertEquals(2, taskManager.getSubTasksMap().size());
    }

    @Test
    public void getHistoryManagerTesting(){
        taskManager.createEpic(new EpicTask(1, "test", "testing"));
        taskManager.createSubTask(new SubTask(2, "sub1", "newSub", TaskStatus.DONE, 1));
        taskManager.createSubTask(new SubTask(3, "sub1", "newSub", TaskStatus.NEW, 1));
        taskManager.createTask(new Task(4, "testTask", "subs", TaskStatus.IN_PROGRESS));

        taskManager.getTaskByID(4);
        assertEquals(1, taskManager.getHistoryManager().getHistory().size());

        taskManager.getEpicByID(1);
        assertEquals(2, taskManager.getHistoryManager().getHistory().size());

        taskManager.getSubTaskByID(2);
        taskManager.getSubTaskByID(3);
        assertEquals(4, taskManager.getHistoryManager().getHistory().size());
    }

    @Test
    public void clearTasksTesting(){
        taskManager.createTask(new Task(1, "testTask", "subs", TaskStatus.IN_PROGRESS));
        taskManager.createTask(new Task(2, "testTask", "subs", TaskStatus.DONE));

        taskManager.getTaskByID(1);
        taskManager.getTaskByID(2);

        taskManager.clearTasks();
        assertEquals(0, taskManager.getTasks().size());
        assertEquals(0, taskManager.getTasksMap().size());
        assertEquals(0, taskManager.getHistoryManager().getHistory().size());
    }

    @Test
    public void clearEpicTasksTesting(){
        taskManager.createEpic(new EpicTask(1, "test1", "testing"));
        taskManager.createEpic(new EpicTask(2, "test2", "testing"));
        taskManager.createSubTask(new SubTask(3, "sub1", "newSub", TaskStatus.DONE, 1));
        taskManager.createSubTask(new SubTask(4, "sub1", "newSub", TaskStatus.NEW, 2));

        taskManager.getEpicByID(1);
        taskManager.getEpicByID(2);
        taskManager.getSubTaskByID(3);
        taskManager.getSubTaskByID(4);

        taskManager.clearEpics();
        assertEquals(0, taskManager.getEpics().size());
        assertEquals(0, taskManager.getEpicsMap().size());
        assertEquals(0, taskManager.getSubTasks().size());
        assertEquals(0, taskManager.getSubTasksMap().size());
        assertEquals(0, taskManager.getHistoryManager().getHistory().size());
    }

    @Test
    public void clearSubTasksTesting(){
        taskManager.createEpic(new EpicTask(1, "test1", "testing"));
        taskManager.createEpic(new EpicTask(2, "test2", "testing"));
        taskManager.createSubTask(new SubTask(3, "sub1", "newSub", TaskStatus.IN_PROGRESS, 1));
        taskManager.createSubTask(new SubTask(4, "sub1", "newSub", TaskStatus.DONE, 2));

        taskManager.getEpicByID(1);
        taskManager.getEpicByID(2);
        taskManager.getSubTaskByID(3);
        taskManager.getSubTaskByID(4);

        taskManager.clearSubTasks();

        assertEquals(0, taskManager.getSubTasks().size());
        assertEquals(0, taskManager.getSubTasksMap().size());
        assertEquals(2, taskManager.getHistoryManager().getHistory().size()); // require 2
        assertEquals(TaskStatus.NEW, taskManager.getEpicByID(1).getStatus());
        assertEquals(TaskStatus.NEW, taskManager.getEpicByID(2).getStatus());
    }

    @Test
    public void getTaskByIdTestingAllOk(){
        taskManager.createTask(new Task(1, "testTask1", "subs", TaskStatus.IN_PROGRESS));
        taskManager.createTask(new Task(2, "testTask2", "subs", TaskStatus.DONE));

        assertEquals("testTask1", taskManager.getTaskByID(1).getName());
        assertEquals(TaskStatus.IN_PROGRESS, taskManager.getTaskByID(1).getStatus());
        assertEquals("testTask2", taskManager.getTaskByID(2).getName());
        assertEquals(TaskStatus.DONE, taskManager.getTaskByID(2).getStatus());

        List<Task> tasksFromManager = taskManager.getTasks();
        assertEquals(tasksFromManager, taskManager.getHistoryManager().getHistory());
    }

    @Test
    public void getTasksByIdTestingNoSuchId(){
        setUpStreams();
        assertNull(taskManager.getTaskByID(99));
        assertEquals("Task not found.", output.toString().trim());
    }

    @Test
    public void getEpicByIdTestingAllOk(){
        taskManager.createEpic(new EpicTask(1, "test1", "testing"));
        taskManager.createEpic(new EpicTask(2, "test2", "testing"));
        taskManager.createSubTask(new SubTask(3, "sub1", "newSub", TaskStatus.DONE, 2));

        assertEquals("test1", taskManager.getEpicByID(1).getName());
        assertEquals(TaskStatus.NEW, taskManager.getEpicByID(1).getStatus());
        assertEquals("test2", taskManager.getEpicByID(2).getName());
        assertEquals(TaskStatus.DONE, taskManager.getEpicByID(2).getStatus());

        List<EpicTask> tasksFromManager = taskManager.getEpics();
        assertEquals(tasksFromManager, taskManager.getHistoryManager().getHistory());
    }

    @Test
    public void getEpicByIdTestingNoSuchId(){
        setUpStreams();
        assertNull(taskManager.getEpicByID(99));
        assertEquals("Epic not found.", output.toString().trim());
    }

    @Test
    public void getSubTaskByIdTestingAllOk(){
        taskManager.createEpic(new EpicTask(1, "test1", "testing"));
        taskManager.createSubTask(new SubTask(2, "sub1", "newSub", TaskStatus.IN_PROGRESS, 1));
        taskManager.createSubTask(new SubTask(3, "sub2", "newSub", TaskStatus.DONE, 1));

        assertEquals("sub1", taskManager.getSubTaskByID(2).getName());
        assertEquals(TaskStatus.IN_PROGRESS, taskManager.getSubTaskByID(2).getStatus());
        assertEquals("sub2", taskManager.getSubTaskByID(3).getName());
        assertEquals(TaskStatus.DONE, taskManager.getSubTaskByID(3).getStatus());

        List<SubTask> tasksFromManager = taskManager.getSubTasks();
        assertEquals(tasksFromManager, taskManager.getHistoryManager().getHistory());
    }

    @Test
    public void getSubTaskByIdTestingNoSuchId(){
        setUpStreams();
        assertNull(taskManager.getSubTaskByID(99));
        assertEquals("Subtask not found.", output.toString().trim());
    }

    @Test
    public void getEpicSubTaskIdsAllOk(){
        taskManager.createEpic(new EpicTask(1, "test1", "testing"));
        taskManager.createSubTask(new SubTask(2, "sub1", "newSub", TaskStatus.IN_PROGRESS, 1));
        taskManager.createSubTask(new SubTask(3, "sub2", "newSub", TaskStatus.DONE, 1));
        List<SubTask> subTakList = List.of(
                (SubTask) taskManager.getSubTaskByID(2), (SubTask) taskManager.getSubTaskByID(3));

        assertEquals(subTakList, taskManager.getEpicSubTasks(1));
    }

    @Test
    public void getEpicSubTaskIdsNoSuchId(){
        setUpStreams();
        taskManager.getEpicSubTasks(1);
        assertEquals("Cannot find EpicTask by this ID.", output.toString().trim());
    }

    @Test
    public void deleteTaskByIdTestingAllOk(){
        taskManager.createTask(new Task(1, "testTask1", "subs", TaskStatus.NEW));
        taskManager.deleteTaskByID(1);

        assertEquals(0, taskManager.getTasks().size());
    }

    @Test
    public void deleteTaskByIdTestingNoSuchTask(){
        setUpStreams();
        taskManager.deleteTaskByID(1);
        assertEquals("Task not exist.", output.toString().trim());
    }

    @Test
    public void deleteEpicByIdTestingAllOk(){
        taskManager.createEpic(new EpicTask(1, "test1", "testing"));
        taskManager.createSubTask(new SubTask(2, "sub1", "newSub", TaskStatus.IN_PROGRESS, 1));
        taskManager.createSubTask(new SubTask(3, "sub2", "newSub", TaskStatus.DONE, 1));

        taskManager.deleteEpicByID(1);
        assertEquals(0, taskManager.getEpics().size());
        assertEquals(0, taskManager.getSubTasks().size());
    }

    @Test
    public void deleteEpicByIdTestingNoSuchEpic(){
        setUpStreams();
        taskManager.deleteEpicByID(1);
        assertEquals("Epic not exist.", output.toString().trim());
    }

    @Test
    public void deleteSubTaskByIdTestingAllOk(){
        taskManager.createEpic(new EpicTask(1, "test1", "testing"));
        taskManager.createSubTask(new SubTask(2, "sub1", "newSub", TaskStatus.IN_PROGRESS, 1));
        taskManager.createSubTask(new SubTask(3, "sub2", "newSub", TaskStatus.DONE, 1));

        taskManager.deleteSubTaskByID(2);
        assertEquals(1, taskManager.getSubTasks().size());
        assertEquals(
                List.of((SubTask) taskManager.getSubTaskByID(3)),
                taskManager.getEpicSubTasks(1)
        );
    }

    @Test
    public void deleteSubTaskByIdTestingNoSuchSubTask(){
        setUpStreams();
        taskManager.deleteSubTaskByID(99);
        assertEquals("Subtask not exist.", output.toString().trim());
    }

    @Test
    public void updateTaskTestingAllOk(){
        taskManager.createTask(new Task(1, "testTask1", "subs", TaskStatus.NEW));

        taskManager.updateTask(new Task(1, "updTask", "sus", TaskStatus.IN_PROGRESS));
        assertEquals("updTask", taskManager.getTaskByID(1).getName());
        assertEquals(TaskStatus.IN_PROGRESS, taskManager.getTaskByID(1).getStatus());
    }

    @Test
    public void updateTaskTestingNoSuchTask(){
        setUpStreams();
        taskManager.createTask(new Task(1, "testTask1", "subs", TaskStatus.NEW));

        taskManager.updateTask(new Task(7, "updTask", "sus", TaskStatus.IN_PROGRESS));
        assertEquals("Task with this ID is not exist.", output.toString().trim());
    }

    @Test
    public void updateEpicTestingAllOk(){
        taskManager.createEpic(new EpicTask(1, "testTask1", "subs"));

        taskManager.updateEpic(new EpicTask(1, "updTask", "sus"));
        assertEquals("updTask", taskManager.getEpicByID(1).getName());
        assertEquals("sus", taskManager.getEpicByID(1).getDescription());
    }

    @Test
    public void updateEpicTestingNoSuchEpic(){
        setUpStreams();
        taskManager.createEpic(new EpicTask(1, "testTask1", "subs"));

        taskManager.updateEpic(new EpicTask(7, "updTask", "sus"));
        assertEquals("Epic with this ID is not exist.", output.toString().trim());
    }

    @Test
    public void updateSubTaskTestingAllOk(){
        taskManager.createEpic(new EpicTask(1, "test1", "testing"));
        taskManager.createSubTask(new SubTask(2, "sub1", "newSub", TaskStatus.IN_PROGRESS, 1));
        taskManager.createSubTask(new SubTask(3, "sub2", "newSub", TaskStatus.DONE, 1));

        taskManager.updateSubTask(new SubTask(2, "updSub", "sus", TaskStatus.DONE, 1));
        assertEquals("updSub", taskManager.getSubTaskByID(2).getName());
        assertEquals(TaskStatus.DONE, taskManager.getEpicByID(1).getStatus());
        assertEquals(TaskStatus.DONE, taskManager.getSubTaskByID(2).getStatus());
    }

    @Test
    public void updateSubTaskTestingNoSuchSubTask(){
        setUpStreams();
        taskManager.createEpic(new EpicTask(1, "test1", "testing"));
        taskManager.createSubTask(new SubTask(2, "sub1", "newSub", TaskStatus.IN_PROGRESS, 1));
        taskManager.createSubTask(new SubTask(3, "sub2", "newSub", TaskStatus.DONE, 1));

        taskManager.updateSubTask(new SubTask(99, "updSub", "sus", TaskStatus.DONE, 1));
        assertEquals("Subtask or Epic with this ID is not exist.", output.toString().trim());
    }

    @Test
    public void updateSubTaskTestingNoSuchEpicId(){
        setUpStreams();
        taskManager.createEpic(new EpicTask(1, "test1", "testing"));
        taskManager.createSubTask(new SubTask(2, "sub1", "newSub", TaskStatus.IN_PROGRESS, 1));
        taskManager.createSubTask(new SubTask(3, "sub2", "newSub", TaskStatus.DONE, 1));

        taskManager.updateSubTask(new SubTask(2, "updSub", "sus", TaskStatus.DONE, 99));
        assertEquals("Subtask or Epic with this ID is not exist.", output.toString().trim());
    }
}