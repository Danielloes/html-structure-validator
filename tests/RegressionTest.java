import Lista03.ListaEncadeada;
import Lista03.NoLista;
import Lista05.PilhaLista;
import Lista05.PilhaVaziaException;
import trab.*;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.io.IOException;

public class RegressionTest {
    private static int checks;
    private static void check(boolean condition) {
        checks++;
        if (!condition) throw new AssertionError("Falhou a verificação " + checks);
    }
    @FunctionalInterface private interface Action { void run() throws Exception; }
    private static void expect(Class<? extends Exception> type, Action action) throws Exception {
        try { action.run(); }
        catch (Exception e) { check(type.isInstance(e)); return; }
        throw new AssertionError("Esperada exceção " + type.getSimpleName());
    }
    private static Map<String, Integer> contar(ListaEncadeada<OcorrenciaDeTag> tags) {
        Map<String, Integer> resultado = new HashMap<>();
        for (NoLista<OcorrenciaDeTag> no = tags.getPrimeiro(); no != null; no = no.getProximo()) {
            check(resultado.put(no.getInfo().getTag(), no.getInfo().getOcorrencias()) == null);
        }
        return resultado;
    }
    public static void main(String[] args) throws Exception {
        Path temp = Files.createTempDirectory(Path.of("build"), "html-");
        Path file = temp.resolve("pagina.html");
        ValidadorHTML validador = new ValidadorHTML(file);
        String[] validos = {
            "", "texto sem tags", "<!DOCTYPE html><HTML><BODY><P>Olá</P></BODY></HTML>",
            "<div><div></div><p>x</p><p>y</p></div>",
            "<!-- <div> ignorado > <p> --> <div></div>",
            "<!-- comentário\nem várias linhas com < e >\n--><p>Fim</p>",
            "<div title=\"1 > 0 e 2 < 3\">x</div>",
            "<div title='a > b'>x</div>",
            "<div\nclass='teste'>x</div>", "<div\tclass='teste'>x</div>",
            "<p></p   >", "<div/><custom-tag />", "<meta charset='UTF-8'><br/><img src='x'>",
            "<area><base><br><col><embed><hr><img><input><link><meta><param><source><track><wbr>",
            "<script>if (a < b && c > d) { x = '<div>'; } // texto\n</SCRIPT>",
            "<style>.x::before {content: '<div>';}</style>",
            "<textarea><div>texto &lt;algo&gt;</textarea>", "<title>Um <exemplo> de título</title>",
            "<div><!-- <script> --><script></script></div>",
            "<!doctype html PUBLIC 'identificador'><html></html>", "\uFEFF<p>Teste</p>"
        };
        for (String html : validos) {
            Files.writeString(file, html, StandardCharsets.UTF_8);
            validador.validarHTML(); check(true);
        }
        String[] invalidos = {
            "<div>", "</div>", "<div><p></div></p>", "<div></span>",
            "<!-- comentário sem fim", "<div title='sem fim>", "<div", "<div><",
            "<div <p></p>", "<div></div x>", "<div></div/>", "<div></ div>",
            "<>", "< div>", "<1div>", "<div@x></div>", "<br></br>",
            "<script>if (a < b) {}", "<style>.x {}</styles>",
            "<!doctype><html></html>", "<!doctype svg><svg></svg>"
        };
        for (String html : invalidos) {
            Files.writeString(file, html, StandardCharsets.UTF_8);
            expect(SintaxeInvalidaException.class, validador::validarHTML);
            check(validador.getOcorrenciaTags().estaVazia());
        }
        Files.writeString(file, "<DIV><p>x</p><P>y</P><br></DIV>");
        Map<String, Integer> esperado = Map.of("div", 1, "p", 2, "br", 1);
        check(contar(validador.validarHTML()).equals(esperado));
        check(contar(validador.validarHTML()).equals(esperado));
        ListaEncadeada<OcorrenciaDeTag> copia = validador.getOcorrenciaTags();
        copia.getPrimeiro().getInfo().addOcorrencia(); copia.inserir(new OcorrenciaDeTag("fake"));
        check(contar(validador.getOcorrenciaTags()).equals(esperado));
        Files.writeString(file, "<p>\n</div>");
        try { validador.validarHTML(); throw new AssertionError("Deveria falhar."); }
        catch (SintaxeInvalidaException e) { check(e.getMessage().contains("Linha 2, coluna 1")); }
        Locale anterior = Locale.getDefault();
        try {
            Locale.setDefault(Locale.forLanguageTag("tr-TR"));
            Files.writeString(file, "<INPUT><DIV></DIV>");
            check(contar(validador.validarHTML()).equals(Map.of("input", 1, "div", 1)));
        } finally { Locale.setDefault(anterior); }
        expect(IOException.class, () -> new ValidadorHTML(temp.resolve("ausente.html")).validarHTML());
        Files.write(file, new byte[]{(byte) 0xC3, 0x28});
        expect(IOException.class, validador::validarHTML);
        check(validador.getOcorrenciaTags().estaVazia());
        PilhaLista pilha = new PilhaLista();
        expect(PilhaVaziaException.class, pilha::peek);
        expect(PilhaVaziaException.class, pilha::pop);
        pilha.push("p"); pilha.push("div"); pilha.push("p");
        check(pilha.pop().equals("p") && pilha.pop().equals("div") && pilha.pop().equals("p"));
        pilha.push("a"); pilha.liberar(); check(pilha.estaVazia());
        expect(IllegalArgumentException.class, () -> pilha.push(null));
        ListaEncadeada<String> lista = new ListaEncadeada<>();
        lista.inserir("c"); lista.inserir("b"); lista.inserir("a");
        check(lista.buscar(new String("b")) != null);
        check(lista.criarSubLista(0, 1).toString().equals("a,b"));
        check(lista.criarSubLista(1, 2).toString().equals("b,c"));
        expect(IndexOutOfBoundsException.class, () -> lista.criarSubLista(0, 3));
        expect(IllegalArgumentException.class, () -> lista.criarSubLista(2, 1));
        lista.inserir(null); check(lista.buscar(null) != null); lista.retirar(null);
        check(lista.obterComprimento() == 3);
        System.out.println("HTML: " + checks + " verificações OK");
    }
}
