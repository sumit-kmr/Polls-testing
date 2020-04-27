package in.magnumsoln.pollstest.interfaces;

public interface FinishFetchingDataCallback {
    Object onFinish(Object object, String collection);
    Object onFailed(Object object, String collection);
}
