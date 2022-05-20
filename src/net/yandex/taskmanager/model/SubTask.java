package net.yandex.taskmanager.model;

public class SubTask extends Task {

    private Integer epicID;
    // Создание новой сабтаск (без статуса)
    public SubTask(String name, String description, int epicID){
        super(name, description);
        this.epicID = epicID;
    }
    // Создание новой сабтаск (со статусом)
    public SubTask(String name, String description, TaskStatus status, int epicID){
        super(name, description, status);
        this.epicID = epicID;
    }
    // Изменение сабтаски
    public SubTask(int id, String name, String description, TaskStatus status, int epicID){
        super(id, name, description, status);
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
}
