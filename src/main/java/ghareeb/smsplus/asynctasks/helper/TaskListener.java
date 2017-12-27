package ghareeb.smsplus.asynctasks.helper;

public interface TaskListener<E> {
    void onTaskStarted();
 
    void onTaskFinished(E result);
}
