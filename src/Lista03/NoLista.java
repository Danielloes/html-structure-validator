package Lista03;

public class NoLista<T> {
    private T info;
    private NoLista<T> proximo;
    public NoLista(T info) { this.info = info; }
    public T getInfo() { return info; }
    public void setInfo(T info) { this.info = info; }
    public NoLista<T> getProximo() { return proximo; }
    public void setProximo(NoLista<T> proximo) { this.proximo = proximo; }
    @Override public String toString() { return String.valueOf(info); }
}
