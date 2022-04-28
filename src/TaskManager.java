import java.util.HashMap;

public class TaskManager {

    private Integer id = 0;

    // Мапы для хранения задач
    HashMap<Integer, Task> tasksMap = new HashMap<>();
    HashMap<Integer, EpicTask> epicMap = new HashMap<>();
    HashMap<Integer, SubTask> subTaskMap = new HashMap<>();

    // Метод для вывода необходимого вида комманд
    public void getAllTasks(Commands command){
        switch (command){
            case ALL: {
                for (Task task : tasksMap.values()){
                    System.out.println(task);
                }
                for (int epicID : epicMap.keySet()){ // Удобнее выводить сразу сортированные по эпикам сабтаски
                    getEpicSubTasks(epicID);
                }
                break;
            }
            case TASK: {
                for (Task task : tasksMap.values()){
                    System.out.println(task);
                }
                break;
            }
            case EPIC: {
                for (EpicTask epic : epicMap.values()){
                    System.out.println(epic);
                }
                break;
            }
            case SUBTASK: {
                for (SubTask subTask : subTaskMap.values()){
                    System.out.println(subTask);
                }
                break;
            }
            default:{
                System.out.println("Command not exist.");
                break;
            }
        }
    }

    // Метод для удаления определенных типов комманд
    public void deleteAllTasks (Commands command){
        switch (command){
            case ALL: {
                tasksMap.clear();
                epicMap.clear();
                subTaskMap.clear();
                break;
            }
            case TASK: {
                tasksMap.clear();
                break;
            }
            case EPIC: { // Удаляю также и Сабтаски
                epicMap.clear();
                subTaskMap.clear();
                break;
            }
            case SUBTASK: {
                subTaskMap.clear();
                break;
            }
            default:{
                System.out.println("Command not exist.");
                break;
            }
        }
    }

    // Получение таска по ID
    public void getTaskByID(int id){
        if (tasksMap.containsKey(id)){
            System.out.println(tasksMap.get(id));
        } else if (epicMap.containsKey(id)){
            System.out.println(epicMap.get(id));
        } else if (subTaskMap.containsKey(id)){
            System.out.println(subTaskMap.get(id));
        } else {
            System.out.println("Task not exist.");
        }
    }

    // Метод для создания тасков и эпиков
    public void createTask(Commands command, Task task){
        int id = generateID();
        task.setId(id); // Присваиваем ID новой задаче
        switch (command){
            case TASK: {
                tasksMap.put(id, task);
                break;
            }
            case EPIC: {
                EpicTask epic = (EpicTask) task;
                epicMap.put(id, epic);
                break;
            }
            case SUBTASK: {
                SubTask subTask = (SubTask) task;
                subTaskMap.put(id, subTask);
                epicMap.get(subTask.getEpicID()).addSubTaskID(id);
                break;
            }
            default: {
                System.out.println("Command not exist.");
                break;
            }
        }
    }

    // Метод удаления задачи по ID
    public void deleteByID(int id){
        if (tasksMap.containsKey(id)){
            tasksMap.remove(id);
            System.out.println("Task (ID " + id + ") successfully removed.");
        } else if (epicMap.containsKey(id)){
            // Сначала удаляются все связанные подзадачи, а после - эпик.
            for (Integer subTaskID : epicMap.get(id).getSubTasksIDs()){
                subTaskMap.remove(subTaskID);
            }
            epicMap.remove(id);
            System.out.println("Epic (ID " + id + ") successfully removed.");
        } else if (subTaskMap.containsKey(id)){
            subTaskMap.remove(id);
            System.out.println("Subtask (ID " + id + ") successfully removed.");
        } else {
            System.out.println("Task not exist.");
        }
    }

    //Метод для обновления существующей задачи
    public void updateTask(int id, Task newTask){
        newTask.setId(id);
        if (newTask instanceof EpicTask && epicMap.containsKey(id)){
            EpicTask newEpic = (EpicTask) newTask;
            newEpic.setSubTasksIDs(epicMap.get(id).getSubTasksIDs());
            epicMap.put(id, newEpic);
            System.out.println("Update Epic under ID: " + id);
        } else if (newTask instanceof SubTask && subTaskMap.containsKey(id)) {
            SubTask newSubTask = (SubTask) newTask;
            subTaskMap.put(id, newSubTask);
            System.out.println("Update SubTask under ID: " + id);
            checkEpicForDone(newSubTask.getEpicID());
        } else if (tasksMap.containsKey(id)) {
            tasksMap.put(id, newTask);
            System.out.println("Update Task under ID: " + id);
        } else {
            System.out.println("Error. ID not found.");
        }
    }

    /* Метод для изменения статусов задач.
    Также можно обновлять статус и через метод updateTask
     */
    public void updateStatus(int id, TaskStatus status){
        if (tasksMap.containsKey(id)){
            tasksMap.get(id).setStatus(status);
        } else if (epicMap.containsKey(id)){
            System.out.println("Cannot change epic status manually.");
        } else if (subTaskMap.containsKey(id)){
            subTaskMap.get(id).setStatus(status);
            checkEpicForDone(subTaskMap.get(id).getEpicID());// Одновременно проверяет, меняется ли статус эпика
        } else {
            System.out.println("Task not exist.");
        }
    }

    // Метод для получения всех сабтасков одного эпика
    public void getEpicSubTasks(int epicID){
        if (epicMap.containsKey(epicID)){
            System.out.println(epicMap.get(epicID)); //Показываем Эпик
            for (Integer subID : epicMap.get(epicID).getSubTasksIDs()){
                System.out.println(subTaskMap.get(subID));
            }
        } else {
            System.out.println("Cannot find EpicTask by this ID.");
        }
    }

    // Проверка стстуса Эпика
    private void checkEpicForDone(int epicID){
        TaskStatus newStatus;
        boolean checkNEW = false;
        boolean checkAllDONE = false;
        int subTasksLength = epicMap.get(epicID).getSubTasksIDs().size();
        int countDONE = 0; // IDEA ругалась без инициализации
        for (Integer id : epicMap.get(epicID).getSubTasksIDs()){
            // Проверка, все ли NEW
            if (subTaskMap.get(id).getStatus() != TaskStatus.NEW){
                checkNEW = true;
            }
            // Проверка, сколько задач имеют статус DONE
            if (subTaskMap.get(id).getStatus() == TaskStatus.DONE){
                countDONE++;
            }
        }
        if (subTasksLength == countDONE) checkAllDONE = true; // Обновление переменной, если все DONE
        // Выбираем новый статус и присваиваем
        if (!checkNEW || subTasksLength == 0){
            newStatus = TaskStatus.NEW;
        } else if (checkAllDONE) {
            newStatus = TaskStatus.DONE;
        } else {
            newStatus = TaskStatus.IN_PROGRESS;
        }
        epicMap.get(epicID).setStatus(newStatus);
    }

    // Метод генерирует ID для задач. Хотел сделать через UUID, но, кажется, пока это избыточно
    private int generateID(){
        id++;
        return id;
    }
}
