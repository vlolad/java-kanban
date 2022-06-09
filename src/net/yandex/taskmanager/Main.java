package net.yandex.taskmanager;
import net.yandex.taskmanager.services.*;
import net.yandex.taskmanager.model.*;

public class Main {

    public static void main(String[] args) {
        System.out.println("Поехали!");

        TaskManager taskManager = Managers.getDefault();

        // Создайте 2 задачи, один эпик с 3 подзадачами, а другой эпик без подзадач.
        taskManager.createTask(new Task("Task1", "hehe", TaskStatus.NEW)); // id 1
        taskManager.createTask(new Task("Task2", "lol", TaskStatus.NEW)); // id 2
        taskManager.createEpic(new EpicTask("FirstEpic", "boom")); // id 3
        taskManager.createSubTask(new SubTask("1subtask1", "hehah", TaskStatus.DONE, 3)); // id 4
        taskManager.createSubTask(new SubTask("1subtask2", "description??", 3)); // id 5
        taskManager.createSubTask(new SubTask("secondEpicSubtask", "description??", 3)); // id 6
        taskManager.createEpic(new EpicTask("NotFirstEpic", "null")); // id 7

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

    }
}

