package net.yandex.taskmanager.model;

import java.time.LocalDateTime;
import java.util.Objects;

public class Task {
    private int id;
    private String name;
    private String description;
    private TaskStatus status;

    private long duration;
    private LocalDateTime startTime;

    public Task() { }
    // Конструктор для создания новой таски (без статуса)
    public Task(String name, String description){
        status = TaskStatus.NEW;
        this.name = name;
        this.description = description;
    }

    public Task(String name, String description, LocalDateTime startTime, long duration){
        status = TaskStatus.NEW;
        this.name = name;
        this.description = description;
        this.startTime = startTime;
        this.duration = duration;
    }
    // Конструктор для создания новой таски (со статусом)
    public Task(String name, String description, TaskStatus status){
        this.name = name;
        this.description = description;
        this.status = status;
    }

    public Task(String name, String description, TaskStatus status, LocalDateTime startTime, long duration){
        this.name = name;
        this.description = description;
        this.status = status;
        this.startTime = startTime;
        this.duration = duration;
    }
    // Конструкторы для изменения существующей таски
    public Task(int id,String name, String description, TaskStatus status){
        this.id = id;
        this.status = status;
        this.name = name;
        this.description = description;
    }

    public Task(int id,String name, String description, TaskStatus status, LocalDateTime startTime, long duration){
        this.id = id;
        this.status = status;
        this.name = name;
        this.description = description;
        this.startTime = startTime;
        this.duration = duration;
    }

    @Override
    public String toString(){
        String showBody;
        if (description == null){
            showBody = "null";
        } else {
            showBody = Integer.toString(description.length());
        }
        return "Task{name= «" + name + "» | id=«" + id + "» | description(length)=«"
                + showBody + "» | status=«" + status + "»}";
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime(){
        if (startTime != null) {return startTime.plusMinutes(duration);}
        else {
            System.out.println("Время старта для этой задачи не задано");
            return null;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (this.getClass() != obj.getClass()) return false;
        Task otherTask = (Task) obj;
        return (id == otherTask.getId()) &&
                Objects.equals(name, otherTask.getName()) &&
                Objects.equals(description, otherTask.getDescription()) &&
                Objects.equals(status, otherTask.getStatus()) &&
                (duration == otherTask.getDuration()) &&
                Objects.equals(startTime, otherTask.getStartTime());
    }
}
