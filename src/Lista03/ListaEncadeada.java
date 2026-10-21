package Lista03;

import java.util.Objects;
import java.util.StringJoiner;

public class ListaEncadeada<T> {
    private NoLista<T> primeiro;
    private int tamanho;

    public NoLista<T> getPrimeiro() { return primeiro; }
    public void inserir(T info) {
        NoLista<T> no = new NoLista<>(info);
        no.setProximo(primeiro);
        primeiro = no;
        tamanho++;
    }
    public void retirar(T info) {
        NoLista<T> atual = primeiro;
        NoLista<T> anterior = null;
        while (atual != null && !Objects.equals(atual.getInfo(), info)) {
            anterior = atual; atual = atual.getProximo();
        }
        if (atual == null) return;
        if (anterior == null) primeiro = atual.getProximo();
        else anterior.setProximo(atual.getProximo());
        tamanho--;
    }
    public int obterComprimento() { return tamanho; }
    public boolean estaVazia() { return primeiro == null; }
    public NoLista<T> buscar(T info) {
        for (NoLista<T> no = primeiro; no != null; no = no.getProximo())
            if (Objects.equals(no.getInfo(), info)) return no;
        return null;
    }
    public NoLista<T> obterNo(int indice) {
        Objects.checkIndex(indice, tamanho);
        NoLista<T> no = primeiro;
        for (int i = 0; i < indice; i++) no = no.getProximo();
        return no;
    }
    public ListaEncadeada<T> criarSubLista(int inicio, int fim) {
        if (inicio > fim) throw new IllegalArgumentException("O índice inicial deve ser menor ou igual ao final.");
        Objects.checkIndex(inicio, tamanho);
        Objects.checkIndex(fim, tamanho);
        ListaEncadeada<T> resultado = new ListaEncadeada<>();
        NoLista<T> origem = obterNo(inicio);
        NoLista<T> ultimo = null;
        for (int i = inicio; i <= fim; i++) {
            NoLista<T> novo = new NoLista<>(origem.getInfo());
            if (ultimo == null) resultado.primeiro = novo;
            else ultimo.setProximo(novo);
            ultimo = novo;
            resultado.tamanho++;
            origem = origem.getProximo();
        }
        return resultado;
    }
    @Override public String toString() {
        StringJoiner texto = new StringJoiner(",");
        for (NoLista<T> no = primeiro; no != null; no = no.getProximo()) texto.add(String.valueOf(no.getInfo()));
        return texto.toString();
    }
}
