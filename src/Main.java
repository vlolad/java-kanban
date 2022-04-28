import java.util.HashMap;

public class Main {

    public static void main(String[] args) {
        System.out.println("Поехали!");

        TaskManager taskManager = new TaskManager();
        // Тестовое создание тасков
        taskManager.createTask(Commands.TASK, new Task("Task1", "to be or not")); // id 1
        taskManager.createTask(Commands.TASK, new Task("Task2", "not be to or")); // id 2
        taskManager.createTask(Commands.EPIC, new EpicTask("FirstEpic", "boom")); // id 3
        taskManager.createTask(Commands.EPIC, new EpicTask("SecondEpic", "why")); // id 4
        taskManager.createTask(Commands.SUBTASK, new SubTask("1subtask1", "hehah", 3)); // id 5
        taskManager.createTask(Commands.SUBTASK, new SubTask("1subtask2", "description??", 3)); // id 6
        taskManager.createTask(Commands.SUBTASK, new SubTask("LonelyTask", "hohoh", 4)); // id 7
        //Печатаем все задачи
        taskManager.getAllTasks(Commands.ALL);
        System.out.println("");
        //Меняем статусы
        taskManager.updateStatus(1, TaskStatus.IN_PROGRESS);
        taskManager.updateStatus(2, TaskStatus.DONE);
        taskManager.updateStatus(5, TaskStatus.IN_PROGRESS);
        taskManager.updateStatus(6, TaskStatus.DONE);
        taskManager.updateStatus(7, TaskStatus.DONE);
        taskManager.updateStatus(3, TaskStatus.IN_PROGRESS); // Выскочит сообщение, что так нельзя
        //Печатаем все задачи
        taskManager.getAllTasks(Commands.ALL);
        System.out.println("");
        // Удаляем одну задачу и один эпик
        taskManager.deleteByID(1);
        taskManager.deleteByID(4);
        //Печатаем все задачи. Снова
        taskManager.getAllTasks(Commands.ALL);
    }
}

