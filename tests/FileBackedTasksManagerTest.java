import net.yandex.taskmanager.services.FileBackedTasksManager;

import net.yandex.taskmanager.services.InMemoryTaskManager;
import org.junit.jupiter.api.BeforeEach;

import java.io.File;

public class FileBackedTasksManagerTest extends TaskManagerTest <FileBackedTasksManager>  {

    @BeforeEach
    public void createTaskManager(){
        setTaskManager(new FileBackedTasksManager(new File("testSave.csv")));
    }

}
