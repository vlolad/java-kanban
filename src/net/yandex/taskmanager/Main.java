package net.yandex.taskmanager;
import net.yandex.taskmanager.services.*;
import net.yandex.taskmanager.model.*;

public class Main {

    public static void main(String[] args) {
        System.out.println("Поехали!");

        TaskManager taskManager = Managers.getDefault();

        // Создайте 2 задачи, один эпик с 2 подзадачами, а другой эпик с 1 подзадачей.
        taskManager.createTask(new Task("Task1", "hehe", TaskStatus.NEW)); // id 1
        taskManager.createTask(new Task("Task2", "lol", TaskStatus.NEW)); // id 2
        taskManager.createEpic(new EpicTask("FirstEpic", "boom")); // id 3
        taskManager.createSubTask(new SubTask("1subtask1", "hehah", TaskStatus.DONE, 3)); // id 4
        taskManager.createSubTask(new SubTask("1subtask2", "description??", 3)); // id 5
        taskManager.createEpic(new EpicTask("NotFirstEpic", "null")); // id 6
        taskManager.createSubTask(new SubTask("secondEpicSubtask", "description??", 6)); // id 7
        taskManager.getTaskByID(1);
        taskManager.getEpicByID(3);
        taskManager.getSubTaskByID(5);
        System.out.println(taskManager.getHistoryManager());
        // Распечатайте списки эпиков, задач и подзадач
        System.out.println(taskManager.getEpics());
        System.out.println(taskManager.getTasks());
        System.out.println(taskManager.getSubTasks());
        System.out.println("");
        // Дополнительно тестирую метод по возвращению сабтасков по ID эпика
        System.out.println(taskManager.getEpicSubTasks(3));
        System.out.println("");
        // Измените статусы созданных объектов, распечатайте.
        taskManager.updateTask(new Task(1, "UpdatedTask", "дай бы бог завелось", TaskStatus.DONE));
        taskManager.updateSubTask(new SubTask(5, "1subtask2 v. 2", "zhivem",
                TaskStatus.IN_PROGRESS, 3));
        taskManager.updateSubTask(new SubTask(7, "secondEpicEnds", "nice", TaskStatus.DONE, 6));
        System.out.println(taskManager.getEpics());
        System.out.println(taskManager.getTasks());
        System.out.println(taskManager.getSubTasks());
        System.out.println("");
        // Попробуйте удалить одну из задач и один из эпиков
        taskManager.deleteTaskByID(1);
        taskManager.deleteEpicByID(3);
        System.out.println(taskManager.getEpics());
        System.out.println(taskManager.getTasks());
        System.out.println(taskManager.getSubTasks());

        //Тестируем историю
        taskManager.getTaskByID(2);
        taskManager.getEpicByID(6);
        taskManager.getSubTaskByID(7);
        System.out.println(taskManager.getHistoryManager());
        taskManager.getTaskByID(2);
        taskManager.getEpicByID(6);
        taskManager.getSubTaskByID(7);
        taskManager.getTaskByID(1);
        taskManager.getSubTaskByID(7);
        taskManager.getTaskByID(2);
        System.out.println(taskManager.getHistoryManager()); // [3, 5, 2, 6, 7, 2, 6, 7, 7, 2]
        taskManager.getEpicByID(6);
        System.out.println(taskManager.getHistoryManager()); // [5, 2, 6, 7, 2, 6, 7, 7, 2, 6] - успех!
    }
}

