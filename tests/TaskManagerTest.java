import net.yandex.taskmanager.model.*;
import net.yandex.taskmanager.services.*;

import org.junit.jupiter.api.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.Duration;
import java.time.LocalDateTime;
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

        assertEquals("name1", taskManager.getTaskByID(1).getName(),
                "В Таск неверно добавляется поле Name.");
        assertEquals(TaskStatus.NEW, taskManager.getTaskByID(1).getStatus(),
                "Таске неверно присваивается статус по-умолчанию.");

        taskManager.createTask(new Task("name2", "desc2", TaskStatus.IN_PROGRESS));

        assertEquals(TaskStatus.IN_PROGRESS, taskManager.getTaskByID(2).getStatus(),
                "Таске неверно присваивается статус конструктором.");
    }

    @Test
    public void createEpicTesting(){
        taskManager.createEpic(new EpicTask("name1", "desc1"));

        assertEquals("name1", taskManager.getEpicByID(1).getName(),
                "В Эпик неверно добавляется поле Name.");
        assertEquals(TaskStatus.NEW, taskManager.getEpicByID(1).getStatus(),
                "Эпику неверно присваивается статус по-умолчанию.");
    }

    @Test
    public void createSubTaskTestingAllOk(){
        taskManager.createEpic(new EpicTask("test", "testing"));
        taskManager.createSubTask(new SubTask("sub2", "newSub", 1));


        assertEquals("sub2", taskManager.getSubTaskByID(2).getName(),
                "В Сабтаск неверно добавляется поле Name.");
        assertEquals(TaskStatus.NEW, taskManager.getSubTaskByID(2).getStatus(),
                "В Сабтаск неверно добавляется статус по-умолчанию");
        assertEquals(TaskStatus.NEW, taskManager.getEpicByID(1).getStatus(),
                "Для Эпика неверно рассчитывается статус при одной Сабтаске со статусом NEW");

        taskManager.createSubTask(new SubTask("sub3", "newSub", TaskStatus.DONE, 1));

        assertEquals(TaskStatus.DONE, taskManager.getSubTaskByID(3).getStatus(),
                "В Сабтаск неверно добавляется статус конструктором");
        assertEquals(TaskStatus.IN_PROGRESS, taskManager.getEpicByID(1).getStatus(),
                "В Эпике неверно обновляется статус при сабтасках NEW и DONE");
        assertEquals(2, taskManager.getEpicsMap().get(1).getSubTasksIDs().size(),
                "В Эпик неверно добавляются сабтаски: неправильное количество сабтасков в спике subTasksIDs");
    }
    @Test
    public void createSubTaskTestingNoSuchEpic(){
        setUpStreams();
        taskManager.createSubTask(new SubTask(2, "sub2", "newSub", TaskStatus.NEW, 1));
        assertEquals("There is no epic with ID 1", output.toString().trim(),
                "Неправильный вывод при попытке создать Сабтаск у несуществующего Эпика");
    }

    @Test
    public void getMethodsTesting(){
        assertEquals(0, taskManager.getEpics().size(), "Неверный стандартный размер хранилища");
        assertEquals(0, taskManager.getTasks().size(), "Неверный стандартный размер хранилища");
        assertEquals(0, taskManager.getSubTasks().size(), "Неверный стандартный размер хранилища");
        assertEquals(0, taskManager.getEpicsMap().size(), "Неверный стандартный размер мапы");
        assertEquals(0, taskManager.getTasksMap().size(), "Неверный стандартный размер мапы");
        assertEquals(0, taskManager.getSubTasksMap().size(), "Неверный стандартный размер мапы");

        taskManager.createEpic(new EpicTask(1, "test", "testing"));
        taskManager.createSubTask(new SubTask(2, "sub1", "newSub", TaskStatus.DONE, 1));
        taskManager.createSubTask(new SubTask(3, "sub1", "newSub", TaskStatus.NEW, 1));
        taskManager.createTask(new Task(4, "testTask", "subs", TaskStatus.IN_PROGRESS));

        assertEquals(1, taskManager.getEpics().size(),
                "Неверный размер хранилища Эпиков: Ожидалось - 1, Вывод - " + taskManager.getEpics().size());
        assertEquals(1, taskManager.getTasks().size(),
                "Неверный размер хранилища Тасков: Ожидалось - 1, Вывод - " + taskManager.getTasks().size());
        assertEquals(2, taskManager.getSubTasks().size(),
                "Неверный размер хранилища Сабтасков: Ожидалось - 2, Вывод - " + taskManager.getSubTasks().size());
        assertEquals(1, taskManager.getEpicsMap().size(),
                "Неверный размер мапы Эпиков: Ожидалось - 1, Вывод - " + taskManager.getEpicsMap().size());
        assertEquals(1, taskManager.getTasksMap().size(),
                "Неверный размер мапы Тасков: Ожидалось - 1, Вывод - " + taskManager.getTasksMap().size());
        assertEquals(2, taskManager.getSubTasksMap().size(),
                "Неверный размер мапы Тасков: Ожидалось - 2, Вывод - " + taskManager.getSubTasksMap().size());
    }

    @Test
    public void getHistoryManagerTesting(){
        taskManager.createEpic(new EpicTask(1, "test", "testing"));
        taskManager.createSubTask(new SubTask(2, "sub1", "newSub", TaskStatus.DONE, 1));
        taskManager.createSubTask(new SubTask(3, "sub1", "newSub", TaskStatus.NEW, 1));
        taskManager.createTask(new Task(4, "testTask", "subs", TaskStatus.IN_PROGRESS));

        taskManager.getTaskByID(4);
        assertEquals(1, taskManager.getHistoryManager().getHistory().size(),
                "Неверный размер истории при одном вызове Таск: Ожидалось 1, Вывод - "
                        + taskManager.getHistoryManager().getHistory().size());

        taskManager.getEpicByID(1);
        assertEquals(2, taskManager.getHistoryManager().getHistory().size(),
                "Неверный размер истории при вызове Так и Эпик: Ожидалось 2, Вывод - "
                        + taskManager.getHistoryManager().getHistory().size());

        taskManager.getSubTaskByID(2);
        taskManager.getSubTaskByID(3);
        assertEquals(4, taskManager.getHistoryManager().getHistory().size(),
                "Неверный размер истории при вызове Таск, Эпик и двух Сабтасков: Ожидалось 4, Вывод - "
                        + taskManager.getHistoryManager().getHistory().size());
    }

    @Test
    public void clearTasksTesting(){
        taskManager.createTask(new Task(1, "testTask", "subs", TaskStatus.IN_PROGRESS));
        taskManager.createTask(new Task(2, "testTask", "subs", TaskStatus.DONE));

        taskManager.getTaskByID(1);
        taskManager.getTaskByID(2);

        taskManager.clearTasks();
        assertEquals(0, taskManager.getTasksMap().size(),
                "Мапа Тасков не отчищается при вызове метода clearTasks()");
        assertEquals(0, taskManager.getHistoryManager().getHistory().size(),
                "Из истории не удаляются просмотренные Таски при вызове метода clearTasks()");
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
        assertEquals(0, taskManager.getEpicsMap().size(),
                "Мапа Эпиков не отчищается при вызове метода clearEpics()");
        assertEquals(0, taskManager.getSubTasksMap().size(),
                "Мапа Сабтасков не отчищается при вызове метода clearEpics()");
        assertEquals(0, taskManager.getHistoryManager().getHistory().size(),
                "Из истории не удаляются просмотренные Эпики и Сабтаски при вызове метода clearEpics()");
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

        assertEquals(0, taskManager.getSubTasksMap().size(),
                "Мапа Сабтасков не отчищается при вызове метода clearSubTasks()");
        assertEquals(2, taskManager.getHistoryManager().getHistory().size(),
                "Неверный вывод истории после вывода метода clearSubTasks(): Ожидалось - 2, Вывод - " +
                        taskManager.getHistoryManager().getHistory().size()); // require 2
        assertEquals(TaskStatus.NEW, taskManager.getEpicByID(1).getStatus(),
                "Неверный расчет статуса Эпика после вызова метода clearSubTasks()");
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
        assertEquals("Task not found.", output.toString().trim(),
                "Неверный вывод getTaskByID(), если Таск с заданным ID не существует");
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
        assertEquals("Epic not found.", output.toString().trim(),
                "Неверный вывод метода getEpicByID(), если Эпик с заданным ID не существует");
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
        assertEquals("Subtask not found.", output.toString().trim(),
                "Неверный вывод getSubTaskByID(), если Сабтаск с заданным ID не существует");
    }

    @Test
    public void getEpicSubTaskIdsAllOk(){
        taskManager.createEpic(new EpicTask(1, "test1", "testing"));
        taskManager.createSubTask(new SubTask(2, "sub1", "newSub", TaskStatus.IN_PROGRESS, 1));
        taskManager.createSubTask(new SubTask(3, "sub2", "newSub", TaskStatus.DONE, 1));
        List<SubTask> subTakList = List.of(
                (SubTask) taskManager.getSubTaskByID(2), (SubTask) taskManager.getSubTaskByID(3));

        assertEquals(subTakList, taskManager.getEpicSubTasks(1),
                "Метод getEpicSubTasks() неверно возвращает Сабтаски из заданного Эпика");
    }

    @Test
    public void getEpicSubTaskIdsNoSuchId(){
        setUpStreams();
        taskManager.getEpicSubTasks(1);
        assertEquals("Cannot find EpicTask by this ID.", output.toString().trim(),
                "Неверный вывод метода getEpicSubTasks(), если Эпик с заданным ID не существует");
    }

    @Test
    public void deleteTaskByIdTestingAllOk(){
        taskManager.createTask(new Task(1, "testTask1", "subs", TaskStatus.NEW));
        taskManager.deleteTaskByID(1);

        assertEquals(0, taskManager.getTasks().size(),
                "Таск не удаляется из хранилища при попытке удаления.");
    }

    @Test
    public void deleteTaskByIdTestingNoSuchTask(){
        setUpStreams();
        taskManager.deleteTaskByID(1);
        assertEquals("Task not exist.", output.toString().trim(),
                "Неверный вывод метода deleteTaskByID(), если Таск не существует.");
    }

    @Test
    public void deleteEpicByIdTestingAllOk(){
        taskManager.createEpic(new EpicTask(1, "test1", "testing"));
        taskManager.createSubTask(new SubTask(2, "sub1", "newSub", TaskStatus.IN_PROGRESS, 1));
        taskManager.createSubTask(new SubTask(3, "sub2", "newSub", TaskStatus.DONE, 1));

        taskManager.deleteEpicByID(1);
        assertEquals(0, taskManager.getEpics().size(),
                "Эпик не удаляется командой deleteEpicByID()");
        assertEquals(0, taskManager.getSubTasks().size(),
                "Связанные с Эпиком Сабтаски не удаляются командой deleteEpicByID()");
    }

    @Test
    public void deleteEpicByIdTestingNoSuchEpic(){
        setUpStreams();
        taskManager.deleteEpicByID(1);
        assertEquals("Epic not exist.", output.toString().trim(),
                "Неправильный вывод deleteEpicByID(), если Эпик не существует.");
    }

    @Test
    public void deleteSubTaskByIdTestingAllOk(){
        taskManager.createEpic(new EpicTask(1, "test1", "testing"));
        taskManager.createSubTask(new SubTask(2, "sub1", "newSub", TaskStatus.IN_PROGRESS, 1));
        taskManager.createSubTask(new SubTask(3, "sub2", "newSub", TaskStatus.DONE, 1));

        taskManager.deleteSubTaskByID(2);
        assertEquals(1, taskManager.getSubTasks().size(),
                "Сабтаск не удаляется из хранилища методом deleteSubTaskByID().");
        assertEquals(
                List.of((SubTask) taskManager.getSubTaskByID(3)),
                taskManager.getEpicSubTasks(1),
                "При вызове deleteSubTaskByID() Сабтаск не удаляется из Эпика."
        );
    }

    @Test
    public void deleteSubTaskByIdTestingNoSuchSubTask(){
        setUpStreams();
        taskManager.deleteSubTaskByID(99);
        assertEquals("Subtask not exist.", output.toString().trim(),
                "Неправильный вывод deleteSubTaskByID() если Сабтаск не существует.");
    }

    @Test
    public void updateTaskTestingAllOk(){
        taskManager.createTask(new Task(1, "testTask1", "subs", TaskStatus.NEW));

        taskManager.updateTask(new Task(1, "updTask", "sus", TaskStatus.IN_PROGRESS));
        assertEquals("updTask", taskManager.getTaskByID(1).getName(),
                "В Таске неверно обновляются поля методом updateTask");
    }

    @Test
    public void updateTaskTestingNoSuchTask(){
        setUpStreams();
        taskManager.createTask(new Task(1, "testTask1", "subs", TaskStatus.NEW));

        taskManager.updateTask(new Task(7, "updTask", "sus", TaskStatus.IN_PROGRESS));
        assertEquals("Task with this ID is not exist.", output.toString().trim(),
                "Неверный вывод updateTask(), если Таск не существует.");
    }

    @Test
    public void updateEpicTestingAllOk(){
        taskManager.createEpic(new EpicTask(1, "testTask1", "subs"));

        taskManager.updateEpic(new EpicTask(1, "updTask", "sus"));
        assertEquals("updTask", taskManager.getEpicByID(1).getName(),
                "В Эпике неверно обновляются поля методом updateEpic()");
    }

    @Test
    public void updateEpicTestingNoSuchEpic(){
        setUpStreams();
        taskManager.createEpic(new EpicTask(1, "testTask1", "subs"));

        taskManager.updateEpic(new EpicTask(7, "updTask", "sus"));
        assertEquals("Epic with this ID is not exist.", output.toString().trim(),
                "Неверный вывод updateEpic(), если Эпик не существует.");
    }

    @Test
    public void updateSubTaskTestingAllOk(){
        taskManager.createEpic(new EpicTask(1, "test1", "testing"));
        taskManager.createSubTask(new SubTask(2, "sub1", "newSub", TaskStatus.IN_PROGRESS, 1));
        taskManager.createSubTask(new SubTask(3, "sub2", "newSub", TaskStatus.DONE, 1));

        taskManager.updateSubTask(new SubTask(2, "updSub", "sus", TaskStatus.DONE, 1));
        assertEquals(TaskStatus.DONE, taskManager.getEpicByID(1).getStatus(),
                "При обновлении Сабтаски неверно обновляется статус соответствуюшего Эпика.");
        assertEquals(TaskStatus.DONE, taskManager.getSubTaskByID(2).getStatus(),
                "В Сабтаске неверно обновляются поля методом updateSubTask()");
    }

    @Test
    public void updateSubTaskTestingNoSuchSubTask(){
        setUpStreams();
        taskManager.createEpic(new EpicTask(1, "test1", "testing"));
        taskManager.createSubTask(new SubTask(2, "sub1", "newSub", TaskStatus.IN_PROGRESS, 1));
        taskManager.createSubTask(new SubTask(3, "sub2", "newSub", TaskStatus.DONE, 1));

        taskManager.updateSubTask(new SubTask(99, "updSub", "sus", TaskStatus.DONE, 1));
        assertEquals("Subtask or Epic with this ID is not exist.", output.toString().trim(),
                "Неверный вывод updateSubTask(), если Сабтаск не существует.");
    }

    @Test
    public void updateSubTaskTestingNoSuchEpicId(){
        setUpStreams();
        taskManager.createEpic(new EpicTask(1, "test1", "testing"));
        taskManager.createSubTask(new SubTask(2, "sub1", "newSub", TaskStatus.IN_PROGRESS, 1));
        taskManager.createSubTask(new SubTask(3, "sub2", "newSub", TaskStatus.DONE, 1));

        taskManager.updateSubTask(new SubTask(2, "updSub", "sus", TaskStatus.DONE, 99));
        assertEquals("Subtask or Epic with this ID is not exist.", output.toString().trim(),
                "Неверный вывод updateSubTask(), если Эпик не существует.");
    }

    @Test
    public void createNewTaskWithDataAndTime(){
        taskManager.createTask(new Task("test1", "sus",
                LocalDateTime.of(2022, 7, 10, 9, 0), 540));

        LocalDateTime expectedDateTime = LocalDateTime.of(2022, 7, 10, 18, 0);
        assertEquals(expectedDateTime, taskManager.getTaskByID(1).getEndTime(),
                "Метод getEndTime() неправильно рассчитываем время окончания задачи в классе Task.");
    }

    @Test
    public void createNewSubTaskWithDataAndTime(){
        taskManager.createEpic(new EpicTask("test1", "testing"));
        taskManager.createSubTask(new SubTask("test2", "sus", 1,
                LocalDateTime.of(2022, 7, 10, 9, 15), 525));

        LocalDateTime expDateTime = LocalDateTime.of(2022, 7, 10, 18, 0);
        assertEquals(expDateTime, taskManager.getSubTaskByID(2).getEndTime(),
                "Метод getEndTime() неправильно рассчитываем время окончания задачи в классе SubTask.");
    }

    @Test
    public void createNewEpicWithDataAndTime(){
        taskManager.createEpic(new EpicTask("test1", "testing"));
        taskManager.createSubTask(new SubTask("test2", "sus", 1,
                LocalDateTime.of(2022, 7, 10, 9, 15), 525));
        taskManager.createSubTask(new SubTask("test3", "sus", 1,
                LocalDateTime.of(2022, 7, 11, 21, 30), 90));

        assertEquals(taskManager.getSubTaskByID(2).getStartTime(),
                taskManager.getEpicByID(1).getStartTime(),
                "Неверно определяется statTime в Эпике при создании новой СабТаск");
        assertEquals(taskManager.getSubTaskByID(3).getEndTime(),
                taskManager.getEpicByID(1).getEndTime(),
                "Неверно определяется endTime в Эпике при создании новой СабТаск");
        long expectedDuration = Duration.between(
                taskManager.getSubTaskByID(2).getStartTime(), taskManager.getSubTaskByID(3).getEndTime())
                .toMinutes();
        assertEquals(expectedDuration, taskManager.getEpicByID(1).getDuration(),
                "Неверно определяется duration в Эпике при создании новой СабТаск");

        taskManager.clearSubTasks();
        assertNull(taskManager.getEpicByID(1).getStartTime(),
                "Не обнуляется startTime в Эпике при вызове метода clearSubTasks()");
    }

    @Test
    public void updateSubTaskAndUpdateDateInEpic(){
        taskManager.createEpic(new EpicTask("test1", "testing"));
        taskManager.createSubTask(new SubTask("test2", "sus", 1,
                LocalDateTime.of(2022, 7, 10, 9, 15), 525));

        taskManager.updateSubTask(new SubTask(2, "test2", "sus", TaskStatus.DONE, 1,
                LocalDateTime.of(2022, 6, 9, 15, 40), 20));

        LocalDateTime expDateTime = LocalDateTime.of(2022, 6, 9, 16, 0);
        assertEquals(expDateTime, taskManager.getEpicByID(1).getEndTime(),
                "Неверно высчитывается endTime у Эпика при обновлении Сабтаск в нём.");
    }

    @Test
    public void threeSetCheckingTestingForTasks(){
        taskManager.createTask(new Task("testTaskNull1", "subs", TaskStatus.IN_PROGRESS)); // 1
        taskManager.createTask(new Task("test2", "sus",
                LocalDateTime.of(2022, 7, 10, 22, 0), 10)); // 2
        taskManager.createTask(new Task("test1", "sus",
                LocalDateTime.of(2022, 7, 10, 9, 0), 540)); // 3

        assertEquals(3, taskManager.getPrioritizedTasks().first().getId(),
                "Неверная сортировка: первой в списке идет неправильная задача");
        assertEquals(1, taskManager.getPrioritizedTasks().last().getId(),
                "Неверная сортировка: последней ожидалась задача с tartTime = null");

        setUpStreams();
        taskManager.createTask(new Task("test2", "sus",
                LocalDateTime.of(2022, 7, 10, 11, 0), 15));
        assertEquals("В данный период выполняется другая задача, задача не создана.", output.toString().trim());

        assertEquals(3, taskManager.getPrioritizedTasks().size(),
                "При отказе в создании новой Таск в checkPeriodForOccupation() задача все равно добавляется" +
                        "в приоретизированный список");
    }

    @Test
    public void threeSetRemoveTaskTesting(){
        taskManager.createTask(new Task("test2", "sus",
                LocalDateTime.of(2022, 7, 10, 22, 0), 10));
        taskManager.createTask(new Task("test1", "sus",
                LocalDateTime.of(2022, 7, 10, 9, 0), 540));

        taskManager.deleteTaskByID(2);
        assertEquals(1, taskManager.getPrioritizedTasks().size(),
                "Неверно работает удаление Таск из sortedTasks.");

        taskManager.clearTasks();
        assertEquals(0, taskManager.getPrioritizedTasks().size(),
                "Неверно работает удаление Таск из sortedTasks при удалении всех Тасков.");
    }

    @Test
    public void threeSetEpicAndSubTaskTesting(){
        taskManager.createEpic(new EpicTask("test1", "testing"));
        taskManager.createSubTask(new SubTask("test2", "sus", 1,
                LocalDateTime.of(2022, 7, 10, 9, 15), 525));
        taskManager.createSubTask(new SubTask("test3", "sus", 1,
                LocalDateTime.of(2022, 7, 10, 23, 0), 60));

        taskManager.updateSubTask(new SubTask(2, "test2", "sus", TaskStatus.DONE, 1,
                LocalDateTime.of(2022, 9, 9, 15, 40), 20));

        assertEquals(3, taskManager.getPrioritizedTasks().first().getId(),
                "Неверная сортировка задач после обновления");

        taskManager.deleteSubTaskByID(3);

        assertEquals(2, taskManager.getPrioritizedTasks().first().getId(),
                "Неверная сортировка задач после удаления одной СабТаски");

        taskManager.clearSubTasks();
        assertEquals(1, taskManager.getPrioritizedTasks().size(),
                "Неверно работает удаление сабтасков из sortedTasks при вызове метода clearSubTasks():" +
                        " Ожидалось - 1, Вывод - " + taskManager.getPrioritizedTasks().size());

        taskManager.clearEpics();
        assertEquals(0, taskManager.getPrioritizedTasks().size(),
                "Неверно работает удаление эпиков из sortedTasks при вызове метода clearEpics():" +
                        " Ожидалось - 0, Вывод - " + taskManager.getPrioritizedTasks().size());
    }
}