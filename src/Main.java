import java.util.HashMap;

public class Main {

    public static void main(String[] args) {
        System.out.println("Поехали!");

        TaskManager taskManager = new TaskManager();
        // Создайте 2 задачи, один эпик с 2 подзадачами, а другой эпик с 1 подзадачей.
        taskManager.createTask(new Task("Task1", "hehe")); // id 1
        taskManager.createTask(new Task("Task2", "lol")); // id 2
        taskManager.createEpic(new EpicTask("FirstEpic", "boom")); // id 3
        taskManager.createSubTask(new SubTask("1subtask1", "hehah", 3)); // id 4
        taskManager.createSubTask(new SubTask("1subtask2", "description??", 3)); // id 5
        taskManager.createEpic(new EpicTask("NotFirstEpic", "null")); // id 6
        taskManager.createSubTask(new SubTask("secondEpicSubtask", "description??", 6)); // id 7
        // Распечатайте списки эпиков, задач и подзадач
        System.out.println(taskManager.getEpics());
        System.out.println(taskManager.getTasks());
        System.out.println(taskManager.getSubTasks());
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
    }
}

