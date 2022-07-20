package net.yandex.taskmanager;
import net.yandex.taskmanager.model.*;
import net.yandex.taskmanager.services.*;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;

public class Main {

    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("Поехали!");

        StringBuilder builder = new StringBuilder();
        builder.append("Heh \n");
        builder.append("lol");

        System.out.println(builder);

        new KVServer().start();

        KVTaskClient client = new KVTaskClient(new URL("http://localhost:8078"));

        System.out.println(client.getAPI_TOKEN());

        System.out.println("Сохраняем значение: key=vladik || value=My test server is great!");
        client.put("vladik", "My test server is great!");

        System.out.println("Извлекаем значение: " + client.load("vladik"));

        System.out.println("Меняем значение: key=vladik || value=Test server is already old!");
        client.put("vladik", "Test server is already old!");

        System.out.println("Снова извлекаем значение: " + client.load("vladik"));

        HTTPTaskManager manager = new HTTPTaskManager(new URL("http://localhost:8078"));

        manager.createTask(new Task("test1", "sus"));
        manager.createEpic(new EpicTask("Epic1", "neew"));
        manager.createSubTask(new SubTask("SubTask1", "neeew", TaskStatus.IN_PROGRESS, 2,
                LocalDateTime.of(2022, 7, 11, 20, 0), 15));
        manager.createSubTask(new SubTask("SubTask2", "sus", TaskStatus.NEW, 2,
                LocalDateTime.of(2022, 7, 11, 22, 0), 60));
        manager.createTask(new Task("TaskNew", "newTask", TaskStatus.NEW,
                LocalDateTime.of(2022, 7, 10, 20, 0), 30));

        manager.getTaskByID(1);
        manager.getSubTaskByID(3);
        manager.getEpicByID(2);
        manager.getTaskByID(5);
        manager.getSubTaskByID(4);
        manager.getTaskByID(1);

        System.out.println("\n");

        HTTPTaskManager newManager = new HTTPTaskManager(new URL("http://localhost:8078"));
        newManager.load();

        System.out.println("А вот и загруженный с сервера менеджер!");
        System.out.println(newManager.getTasks());
        System.out.println(newManager.getEpics());
        System.out.println(newManager.getSubTasks());
        System.out.println(newManager.getHistory());
        System.out.println(newManager.getPrioritizedTasks());
        /* TaskManager taskManager = Managers.getDefault();


        taskManager.createTask(new Task("test2", "sus",
                LocalDateTime.of(2022, 7, 10, 22, 0), 10)); // 2
        taskManager.createTask(new Task("test1", "sus",
                LocalDateTime.of(2022, 7, 10, 9, 0), 540));

        taskManager.createTask(new Task("test2", "sus",
                LocalDateTime.of(2022, 7, 10, 11, 0), 15));

        System.out.println(taskManager.getPrioritizedTasks()); */
        // Создайте 2 задачи, один эпик с 3 подзадачами, а другой эпик без подзадач.
        /*taskManager.createTask(new Task("Task1", "hehe", TaskStatus.NEW)); // id 1
        taskManager.createTask(new Task("Task2", "lol", TaskStatus.NEW)); // id 2
        taskManager.createEpic(new EpicTask("FirstEpic", "boom")); // id 3
        taskManager.createSubTask(new SubTask("1subtask1", "hehah", TaskStatus.DONE, 3)); // id 4
        taskManager.createSubTask(new SubTask("1subtask2", "description??", 3)); // id 5
        taskManager.createSubTask(new SubTask("secondEpicSubtask", "description??", 3)); // id 6
        taskManager.createEpic(new EpicTask("NotFirstEpic", "null")); // id 7 */
/*
        taskManager.getTaskByID(1);
        taskManager.getEpicByID(3);
        taskManager.getSubTaskByID(5);
        System.out.println(taskManager.getHistoryManager()); // В истории 1, 3, 5

        taskManager.getTaskByID(2);
        taskManager.getSubTaskByID(5);
        taskManager.getSubTaskByID(6);
        taskManager.getSubTaskByID(4);
        taskManager.getTaskByID(1);
        taskManager.getTaskByID(2);
        taskManager.getEpicByID(7);
        System.out.println(taskManager.getHistoryManager()); // В истории 3, 5, 6, 4, 1, 2, 7
        System.out.println("");

        taskManager.getSubTaskByID(6);
        taskManager.getEpicByID(7);
        taskManager.getSubTaskByID(6);
        taskManager.getEpicByID(7);
        taskManager.getSubTaskByID(6);
        System.out.println(taskManager.getHistoryManager()); // В истории 3, 5, 4, 1, 2, 7, 6
        System.out.println("");

        taskManager.deleteSubTaskByID(6);
        taskManager.deleteTaskByID(2);
        System.out.println(taskManager.getHistoryManager()); // В истории 3, 5, 4, 1, 7
        System.out.println("");

        taskManager.deleteEpicByID(3);
        System.out.println(taskManager.getHistoryManager()); // В истории 1, 7
        taskManager.getEpicByID(7);
        System.out.println(taskManager.getHistoryManager()); // В истории 1, 7
        taskManager.getTaskByID(1);
        System.out.println(taskManager.getHistoryManager()); // В истории 7, 1
        taskManager.deleteTaskByID(1);
        System.out.println(taskManager.getHistoryManager()); // В истории 7
        System.out.println("");

        taskManager.deleteEpicByID(7); // history.isEmpty() = true
        taskManager.createTask(new Task("Task2", "lol", TaskStatus.NEW)); // ID = 8
        taskManager.getTaskByID(8);
        System.out.println(taskManager.getHistoryManager());// [8]
 */
    }
}

