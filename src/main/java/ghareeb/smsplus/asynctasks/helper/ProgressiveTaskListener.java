package ghareeb.smsplus.asynctasks.helper;

public interface ProgressiveTaskListener<E, F> extends TaskListener<E>
{
	void onTaskProgress(F progressInformation);
}
