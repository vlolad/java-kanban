import java.util.ArrayList;

public class EpicTask extends Task {

    private ArrayList<Integer> subTasksIDs;

    public EpicTask(String name, String description){
        super(name, description);
        subTasksIDs = new ArrayList<>();
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

    public ArrayList<Integer> getSubTasksIDs() {
        return subTasksIDs;
    }

    public void setSubTasksIDs(ArrayList<Integer> subTasksIDs) {
        this.subTasksIDs = subTasksIDs;
    }

}
