import java.util.ArrayList;
import java.util.HashMap;

public class InMemoryTaskManager implements TaskManager {

    private Integer id = 0;

    // Мапы для хранения задач
    private final HashMap<Integer, Task> tasks = new HashMap<>();
    private final HashMap<Integer, EpicTask> epics = new HashMap<>();
    private final HashMap<Integer, SubTask> subTasks = new HashMap<>();

    // Методы для вывода необходимого вида тасков в виде списка
    @Override
    public ArrayList<Task> getTasks(){
        return new ArrayList<>(tasks.values());
    }

    @Override
    public ArrayList<EpicTask> getEpics(){
        return new ArrayList<>(epics.values());
    }

    @Override
    public ArrayList<SubTask> getSubTasks(){
        return new ArrayList<>(subTasks.values());
    }

    // Методы для удаления определенных типов таск
    @Override
    public void clearTasks(){
        tasks.clear();
    }

    @Override
    public void clearEpics(){
        epics.clear();
        subTasks.clear();
    }

    @Override
    public void clearSubTasks(){
        subTasks.clear();
        for (EpicTask epic : epics.values()){ // Также очищаются списки подзадач в эпиках и идет проверка их статуса
            epic.clearSubTasksIDs();
            checkEpicForDone(epic.getId());
        }
    }

    // Методы для получения конкретного вида таски по ID
    @Override
    public Task getTaskByID(int id){
        if (tasks.containsKey(id)){
            return tasks.get(id);
        } else {
            System.out.println("Task not found.");
            return null;
        }
    }

    @Override
    public Task getEpicByID(int id){
        if (epics.containsKey(id)){
            return epics.get(id);
        } else {
            System.out.println("Epic not found.");
            return null;
        }
    }

    @Override
    public Task getSubTaskByID(int id){
        if (subTasks.containsKey(id)){
            return subTasks.get(id);
        } else {
            System.out.println("Subtask not found.");
            return null;
        }
    }

    // Методы для создания тасков

    @Override
    public void createTask(Task task){
        int id = generateID();
        task.setId(id);
        tasks.put(id, task);
    }

    @Override
    public void createEpic(EpicTask epic){
        int id = generateID();
        epic.setId(id);
        epics.put(id, epic);
    }

    @Override
    public void createSubTask(SubTask subTask){
        if (epics.containsKey(subTask.getEpicID())){
            int id = generateID();
            subTask.setId(id);
            subTasks.put(id, subTask);
            epics.get(subTask.getEpicID()).addSubTaskID(id);
            checkEpicForDone(subTask.getEpicID());
        } else {
            System.out.println("There is no epic with ID " + subTask.getEpicID());
        }
    }

    // Методы для удаления тасков по ID
    @Override
    public void deleteTaskByID(int id){
        if (tasks.containsKey(id)){
            tasks.remove(id);
        } else {
            System.out.println("Task not exist.");
        }
    }

    @Override
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

    @Override
    public void deleteSubTaskByID(int id){
        if (subTasks.containsKey(id)){
            // Внес правки в removeSubTaskID(id) в классе EpicTask
            epics.get(subTasks.get(id).getEpicID()).removeSubTaskID(id); // Удаляем ID сабтаски из эпика
            checkEpicForDone(subTasks.get(id).getEpicID()); // Обновляем статус
            subTasks.remove(id); // Наконец удаляем сабтаск
        } else {
            System.out.println("Subtask not exist.");
        }
    }

    // Методы для обновления существующих тасков
    @Override
    public void updateTask(Task newTask){
        if (tasks.containsKey(newTask.getId())){
            tasks.put(newTask.getId(), newTask);
        } else {
            System.out.println("Task with this ID is not exist.");
        }
    }

    @Override
    public void updateEpic(EpicTask newEpic){
        if (epics.containsKey(newEpic.getId())){
            epics.put(newEpic.getId(), newEpic);
        } else {
            System.out.println("Epic with this ID is not exist.");
        }
    }

    @Override
    public void updateSubTask(SubTask newSubTask){
        if (subTasks.containsKey(newSubTask.getId()) && epics.containsKey(newSubTask.getEpicID())){
            subTasks.put(newSubTask.getId(), newSubTask);
            checkEpicForDone(newSubTask.getEpicID());
        } else {
            System.out.println("Subtask or Epic with this ID is not exist.");
        }
    }

    // Метод для получения списка всех сабтасков одного эпика
    @Override
    public ArrayList<SubTask> getEpicSubTasks(int epicID){
        ArrayList<SubTask> subTaskArray = new ArrayList<>();
        if (epics.containsKey(epicID)){
            for (Integer subID : epics.get(epicID).getSubTasksIDs()){
                subTaskArray.add(subTasks.get(subID));
            }
            return subTaskArray;
        } else {
            System.out.println("Cannot find EpicTask by this ID.");
            return null;
        }
    }

    // Проверка стстуса Эпика
    private void checkEpicForDone(int epicID){
        boolean checkNotAllNEW = false;
        int subTasksLength = epics.get(epicID).getSubTasksIDs().size();
        int countDONE = 0;

        for (Integer id : epics.get(epicID).getSubTasksIDs()){
            if (subTasks.get(id).getStatus() == TaskStatus.IN_PROGRESS){ // Если есть IN_PROGRESS - сразу ставим статус
                epics.get(epicID).setStatus(TaskStatus.IN_PROGRESS);
                return;
            } else if (subTasks.get(id).getStatus() == TaskStatus.DONE){ // Если есть хоть один DONE - начинаем считать
                countDONE++;
                checkNotAllNEW = true;
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