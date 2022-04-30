import java.util.ArrayList;
import java.util.HashMap;

public class TaskManager {

    private Integer id = 0;

    // Мапы для хранения задач
    private final HashMap<Integer, Task> tasks = new HashMap<>();
    private final HashMap<Integer, EpicTask> epics = new HashMap<>();
    private final HashMap<Integer, SubTask> subTasks = new HashMap<>();

    // Методы для вывода необходимого вида тасков в виде списка
    public ArrayList<Task> getTasks(){
        return new ArrayList<>(tasks.values());
    }

    public ArrayList<EpicTask> getEpics(){
        return new ArrayList<>(epics.values());
    }

    public ArrayList<SubTask> getSubTasks(){
        return new ArrayList<>(subTasks.values());
    }

    // Методы для удаления определенных типов таск
    public void clearTasks(){
        tasks.clear();
    }

    public void clearEpics(){
        epics.clear();
        subTasks.clear();
    }

    public void clearSubTasks(){
        subTasks.clear();
        for (EpicTask epic : epics.values()){ // Также очищаются списки подзадач в эпиках и идет проверка их статуса
            epic.clearSubTasksIDs();
            checkEpicForDone(epic.getId());
        }
    }

    // Методы для получения конкретного вида таски по ID
    public Task getTaskByID(int id){
        if (tasks.containsKey(id)){
            return tasks.get(id);
        } else {
            System.out.println("Task not found.");
            return null;
        }
    }

    public Task getEpicByID(int id){
        if (epics.containsKey(id)){
            return epics.get(id);
        } else {
            System.out.println("Epic not found.");
            return null;
        }
    }

    public Task getSubTaskByID(int id){
        if (subTasks.containsKey(id)){
            return subTasks.get(id);
        } else {
            System.out.println("Subtask not found.");
            return null;
        }
    }

    // Методы для создания тасков

    public void createTask(Task task){
        tasks.put(generateID(), task);
    }

    public void createEpic(EpicTask epic){
        epics.put(generateID(), epic);
    }

    public void createSubTask(SubTask subTask){
        if (epics.containsKey(subTask.getEpicID())){
            int id = generateID();
            subTasks.put(id, subTask);
            epics.get(subTask.getEpicID()).addSubTaskID(id);
            checkEpicForDone(subTask.getEpicID());
        } else {
            System.out.println("There is no epic with ID " + subTask.getEpicID());
        }
    }

    // Методы для удаления тасков по ID
    public void deleteTaskByID(int id){
        if (tasks.containsKey(id)){
            tasks.remove(id);
        } else {
            System.out.println("Task not exist.");
        }
    }

    public void deleteEpicByID(int id){
        if (epics.containsKey(id)){
            for (Integer subTaskID : epics.get(id).getSubTasksIDs()){
                subTasks.remove(subTaskID);
            }
            epics.remove(id);
        } else {
            System.out.println("Epic not exist.");
        }
    }

    public void deleteSubTaskByID(int id){
        if (subTasks.containsKey(id)){
            epics.get(subTasks.get(id).getEpicID()).removeSubTaskID(id); // Удаляем ID сабтаски из эпика
            checkEpicForDone(subTasks.get(id).getEpicID()); // Обновляем статус
            subTasks.remove(id); // Наконец удаляем сабтаск
        } else {
            System.out.println("Subtask not exist.");
        }
    }

    // Методы для обновления существующих тасков
    public void updateTask(Task newTask){
        if (tasks.containsKey(newTask.getId())){
            tasks.put(newTask.getId(), newTask);
        } else {
            System.out.println("Task with this ID is not exist.");
        }
    }

    public void updateEpic(EpicTask newEpic){
        if (epics.containsKey(newEpic.getId())){
            // Передаем обновленному эпику список старых сабтасков
            newEpic.setSubTasksIDs(epics.get(newEpic.getId()).getSubTasksIDs());
            epics.put(newEpic.getId(), newEpic);
        } else {
            System.out.println("Epic with this ID is not exist.");
        }
    }

    public void updateSubTask(SubTask newSubTask){
        if (subTasks.containsKey(newSubTask.getId()) && epics.containsKey(newSubTask.getEpicID())){
            subTasks.put(newSubTask.getId(), newSubTask);
            checkEpicForDone(newSubTask.getEpicID());
        } else {
            System.out.println("Subtask or Epic with this ID is not exist.");
        }
    }

    // Метод для получения списка всех сабтасков одного эпика
    public void getEpicSubTasks(int epicID){
        if (epics.containsKey(epicID)){
            System.out.println(epics.get(epicID)); //Показываем Эпик
            for (Integer subID : epics.get(epicID).getSubTasksIDs()){
                System.out.println(subTasks.get(subID));
            }
        } else {
            System.out.println("Cannot find EpicTask by this ID.");
        }
    }

    /* Уточню по работе метода checkEpicForDone(), пока не хочу полностью править его логику
    * Цикл for в методе один, чтобы проверить все сабтаски внутри
    * Сначала идет проверка на то, есть ли в Эпике сатбаски со статусом не-NEW. Если есть - меняем checkNotAllNEW
    * Далее считаем, сколько из сабтасков имеют статус DONE используя счетчик
    * После чего смотрим по имеющимся сведениям, что делать:
    * 1. Если все сабтаски NEW (!checkNotAllNEW) или в эпике нет сабтасков - то он NEW
    * 2. Если число сабтасков, которые DONE, равняется общему числу сабтасков, то статус эпика DONE
    * 3. В остальных случаях статус эпика IN_PROGRESS
    *
    * Если всё же есть реализация лучше - возвращайте после ревью, буду пыхтеть дальше)
    * P.S. Переменные были названы криво и было кучу лишних строк, всё поправил*/

    // Проверка стстуса Эпика
    private void checkEpicForDone(int epicID){
        boolean checkNotAllNEW = false;
        int subTasksLength = epics.get(epicID).getSubTasksIDs().size();
        int countDONE = 0; // Понял!
        for (Integer id : epics.get(epicID).getSubTasksIDs()){
            // Проверка, все ли NEW
            if (subTasks.get(id).getStatus() == TaskStatus.IN_PROGRESS){
                epics.get(epicID).setStatus(TaskStatus.IN_PROGRESS);
                return;
            }
            if (subTasks.get(id).getStatus() != TaskStatus.NEW){
                checkNotAllNEW = true;
            }
            // Проверка, сколько задач имеют статус DONE
            if (subTasks.get(id).getStatus() == TaskStatus.DONE){
                countDONE++;
            }
        }
        // Выбираем новый статус и присваиваем
        if (!checkNotAllNEW || subTasksLength == 0){
            epics.get(epicID).setStatus(TaskStatus.NEW);
        } else if (subTasksLength == countDONE) {
            epics.get(epicID).setStatus(TaskStatus.DONE);
        } else {
            epics.get(epicID).setStatus(TaskStatus.IN_PROGRESS);
        }
    }

    // Метод генерирует ID для задач. Хотел сделать через UUID, но, кажется, пока это избыточно
    private int generateID(){
        id++;
        return id;
    }
}
