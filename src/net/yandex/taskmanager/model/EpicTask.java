package net.yandex.taskmanager.model;

import java.util.ArrayList;
import java.util.List;

public class EpicTask extends Task {

    private List<Integer> subTasksIDs = new ArrayList<>();
    // Конструктор для создания эпика
    public EpicTask(String name, String description){
        super(name, description);
    }
    // Конструктор для изменения эпика
    public EpicTask(int id, String name, String description){
        setId(id);
        setName(name);
        setDescription(description);
    }

    @Override
    public String toString(){
        String showBody;
        if (getDescription() == null){
            showBody = "null";
        } else {
            showBody = Integer.toString(getDescription().length());
        }
        return "Epic{name= «" + getName() + "» | id=«" + getId() + "» | description(length)=«"
                + showBody + "» | status=«" + getStatus() + "» | subtasks id's=" + subTasksIDs + "}";
    }

    public void addSubTaskID(int id){
        subTasksIDs.add(id);
    }

    public void removeSubTaskID(Integer id){ // Теперь удаляет соответствующий объект Integer из списка
        subTasksIDs.remove(id);
    }

    public List<Integer> getSubTasksIDs() {
        return subTasksIDs;
    }

    public void clearSubTasksIDs(){
        subTasksIDs.clear();
    }

    public void setSubTasksIDs(List<Integer> subTasksIDs) {
        this.subTasksIDs = subTasksIDs;
    }

}
