package Lista05;

import Lista03.ListaEncadeada;

public class PilhaLista implements Pilha {
    private final ListaEncadeada<String> lista = new ListaEncadeada<>();
    @Override public void push(String info) {
        if (info == null) throw new IllegalArgumentException("A tag não pode ser nula.");
        lista.inserir(info);
    }
    @Override public String peek() {
        if (estaVazia()) throw new PilhaVaziaException("A pilha está vazia.");
        return lista.getPrimeiro().getInfo();
    }
    @Override public String pop() {
        String tag = peek();
        lista.retirar(tag);
        return tag;
    }
    @Override public boolean estaVazia() { return lista.estaVazia(); }
    @Override public void liberar() { while (!estaVazia()) pop(); }
}
