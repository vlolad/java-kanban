package net.yandex.taskmanager.model;

import java.time.LocalDateTime;
import java.util.Objects;

public class SubTask extends Task {

    private Integer epicID;
    // Создание новой сабтаск (без статуса)
    public SubTask(String name, String description, int epicID){
        super(name, description);
        this.epicID = epicID;
    }

    public SubTask(String name, String description, int epicID, LocalDateTime startTime, long duration){
        super(name, description, startTime, duration);
        this.epicID = epicID;
    }
    // Создание новой сабтаск (со статусом)
    public SubTask(String name, String description, TaskStatus status, int epicID){
        super(name, description, status);
        this.epicID = epicID;
    }

    public SubTask(String name, String description, TaskStatus status, int epicID, LocalDateTime startTime, long duration){
        super(name, description, status, startTime, duration);
        this.epicID = epicID;
    }
    // Изменение сабтаски
    public SubTask(int id, String name, String description, TaskStatus status, int epicID){
        super(id, name, description, status);
        this.epicID = epicID;
    }

    public SubTask(int id, String name, String description, TaskStatus status,
                   int epicID, LocalDateTime startTime, long duration){
        super(id, name, description, status, startTime, duration);
        this.epicID = epicID;
    }

    public Integer getEpicID() {
        return epicID;
    }

    public void setEpicID(Integer epicID) {
        this.epicID = epicID;
    }

    @Override
    public String toString(){
        String showBody;
        if (getDescription() == null){
            showBody = "null";
        } else {
            showBody = Integer.toString(getDescription().length());
        }
        return "SubTask{name= «" + getName() + "» | id=«" + getId() + "» | description(length)=«"
                + showBody + "» | status=«" + getStatus() + "» | epicID=«" + epicID + "»}";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (this.getClass() != obj.getClass()) return false;
        SubTask otherTask = (SubTask) obj;
        return (getId() == otherTask.getId()) &&
                Objects.equals(getName(), otherTask.getName()) &&
                Objects.equals(getDescription(), otherTask.getDescription()) &&
                Objects.equals(getStatus(), otherTask.getStatus()) &&
                (getDuration() == otherTask.getDuration()) &&
                Objects.equals(getStartTime(), otherTask.getStartTime()) &&
                Objects.equals(epicID, otherTask.getEpicID());
    }
}
