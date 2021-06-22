package cn.bithon.rpc.exception;

public class TimeoutException extends ServiceInvocationException {
    public TimeoutException(CharSequence service, CharSequence method, int timeout) {
        super(String.format("Timeout(%d millisecond) to call %s#%s", timeout, service, method));
    }
}
