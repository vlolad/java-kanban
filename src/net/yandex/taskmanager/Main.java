package net.yandex.taskmanager;
import net.yandex.taskmanager.services.*;
import net.yandex.taskmanager.model.*;

import java.util.Arrays;
import java.util.List;

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

        String[] sb = new String[6];
        sb[0] = Integer.toString(taskManager.getTaskByID(1).getId());
        sb[1] = TaskTypes.TASK.name();
        System.out.println(Arrays.toString(sb));

        sus(taskManager.getTaskByID(1));
        sus(taskManager.getEpicByID(3));
        sus(taskManager.getSubTaskByID(5));
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

    private static void sus(Task task){
        String[] line = new String[6];
        line[0] = Integer.toString(task.getId());
        line[2] = task.getName();
        line[3] = String.valueOf(task.getStatus());
        line[4] = task.getDescription();
        // Часть элементов заполняются в зависимости от типа таски.
        // Не смог сделать через instanceof вместе со switch
        switch (task.getClass().getSimpleName()){
            case "EpicTask":
                line[1] = String.valueOf(TaskTypes.EPIC);
                break;
            case "SubTask":
                SubTask newTask = (SubTask) task;
                line[1] = String.valueOf(TaskTypes.SUBTASK);
                line[5] = Integer.toString(newTask.getEpicID());
                break;
            case "Task":
                line[1] = String.valueOf(TaskTypes.TASK);
        }
        String result = String.join(",", line);
        System.out.println(result);

        String[] heh = result.split(",");
        System.out.println(heh[1]);
    }
}

