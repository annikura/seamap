public class ErrorOr<T> {
    final private T obj;
    final private String err;
    final private boolean isErr;

    private ErrorOr(T o) {
        obj = o;
        err = "";
        isErr = false;
    }

    private ErrorOr(String s, Void v) {
        obj = null;
        err = s;
        isErr = true;
    }

    public static <T> ErrorOr<T> createObj(T obj) {
        return new ErrorOr<>(obj);
    }

    public static <T> ErrorOr<T> createErr(String errorMessage) {
        return new ErrorOr<>(errorMessage, null);
    }

    public boolean isError() {
        return isErr;
    }

    public T get() {
        return obj;
    }

    public String getError() {
        return err;
    }
}
