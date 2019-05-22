public class Holder<T> {
    private volatile T value;
    public Holder() {
        value = null;
    }

    public Holder(T value) {
        this.value = value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public T getValue() {
        return value;
    }
}
