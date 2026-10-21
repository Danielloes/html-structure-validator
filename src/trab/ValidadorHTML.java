package trab;

import Lista03.ListaEncadeada;
import Lista03.NoLista;
import Lista05.PilhaLista;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ValidadorHTML {
    private static final Set<String> SEM_FECHAMENTO = Set.of(
        "area", "base", "br", "col", "embed", "hr", "img", "input", "link", "meta", "param", "source", "track", "wbr");
    private static final Set<String> CONTEUDO_TEXTO = Set.of("script", "style", "textarea", "title");
    private static final Pattern NOME = Pattern.compile("[A-Za-z][A-Za-z0-9:_-]*");
    private final Path arquivo;
    private ListaEncadeada<OcorrenciaDeTag> ocorrenciaTags = new ListaEncadeada<>();

    public ValidadorHTML(String caminho) { this(Path.of(caminho)); }
    public ValidadorHTML(Path arquivo) { this.arquivo = Objects.requireNonNull(arquivo); }

    public ListaEncadeada<OcorrenciaDeTag> validarHTML() throws IOException {
        ocorrenciaTags = new ListaEncadeada<>();
        String html = Files.readString(arquivo, StandardCharsets.UTF_8);
        PilhaLista pilha = new PilhaLista();
        ListaEncadeada<OcorrenciaDeTag> contagem = new ListaEncadeada<>();
        int posicao = 0;
        while (posicao < html.length()) {
            if (!pilha.estaVazia() && CONTEUDO_TEXTO.contains(pilha.peek())) {
                // Script, style, textarea e title podem conter '<' como texto.
                String nome = pilha.peek();
                Matcher fechamento = Pattern.compile("</" + Pattern.quote(nome) + "\\s*>", Pattern.CASE_INSENSITIVE).matcher(html);
                if (!fechamento.find(posicao)) throw erro(html, html.length(), "Falta fechar <" + nome + ">.");
                pilha.pop();
                posicao = fechamento.end();
                continue;
            }
            int inicio = html.indexOf('<', posicao);
            if (inicio < 0) break;
            if (html.startsWith("<!--", inicio)) {
                int fim = html.indexOf("-->", inicio + 4);
                if (fim < 0) throw erro(html, inicio, "Comentário não foi fechado.");
                posicao = fim + 3;
                continue;
            }
            int fim = fimTag(html, inicio);
            String conteudo = html.substring(inicio + 1, fim);
            String normalizado = conteudo.toLowerCase(Locale.ROOT);
            if (normalizado.startsWith("!doctype") && normalizado.length() > 8
                    && Character.isWhitespace(normalizado.charAt(8))) {
                if (!normalizado.substring(8).trim().matches("(?s)html(?:\\s+.*)?"))
                    throw erro(html, inicio, "Declaração DOCTYPE inválida.");
                posicao = fim + 1;
                continue;
            }
            boolean fechamento = conteudo.startsWith("/");
            int offset = fechamento ? 1 : 0;
            Matcher nomeMatcher = NOME.matcher(conteudo);
            nomeMatcher.region(offset, conteudo.length());
            if (!nomeMatcher.lookingAt()) throw erro(html, inicio, "Nome de tag inválido.");
            String nome = nomeMatcher.group().toLowerCase(Locale.ROOT);
            String restante = conteudo.substring(nomeMatcher.end());
            if (fechamento) {
                if (!restante.isBlank()) throw erro(html, inicio, "Tag de fechamento não aceita atributos.");
                if (SEM_FECHAMENTO.contains(nome)) throw erro(html, inicio, "A tag <" + nome + "> não usa fechamento.");
                if (pilha.estaVazia()) throw erro(html, inicio, "A tag </" + nome + "> foi fechada sem ser aberta.");
                if (!pilha.peek().equals(nome)) throw erro(html, inicio,
                    "Esperado </" + pilha.peek() + ">, encontrado </" + nome + ">.");
                pilha.pop();
            } else {
                if (!restante.isEmpty() && !Character.isWhitespace(restante.charAt(0)) && !restante.equals("/"))
                    throw erro(html, inicio, "Separador inválido após o nome da tag.");
                adicionarOcorrencia(contagem, nome);
                boolean autoFechamento = restante.stripTrailing().endsWith("/");
                if (!autoFechamento && !SEM_FECHAMENTO.contains(nome)) pilha.push(nome);
            }
            posicao = fim + 1;
        }
        if (!pilha.estaVazia()) throw erro(html, html.length(), "Falta fechar <" + pilha.peek() + ">.");
        ocorrenciaTags = contagem;
        return getOcorrenciaTags();
    }

    private static int fimTag(String html, int inicio) {
        char aspas = 0;
        for (int i = inicio + 1; i < html.length(); i++) {
            char atual = html.charAt(i);
            if (aspas != 0) { if (atual == aspas) aspas = 0; }
            else if (atual == '\'' || atual == '"') aspas = atual;
            else if (atual == '>') return i;
            else if (atual == '<') throw erro(html, i, "Outra tag começou antes de fechar a anterior.");
        }
        throw erro(html, inicio, aspas == 0 ? "Tag incompleta: falta >." : "Valor de atributo com aspas não fechadas.");
    }
    private static void adicionarOcorrencia(ListaEncadeada<OcorrenciaDeTag> tags, String nome) {
        for (NoLista<OcorrenciaDeTag> no = tags.getPrimeiro(); no != null; no = no.getProximo()) {
            if (no.getInfo().getTag().equals(nome)) { no.getInfo().addOcorrencia(); return; }
        }
        tags.inserir(new OcorrenciaDeTag(nome));
    }
    private static SintaxeInvalidaException erro(String html, int posicao, String mensagem) {
        int linha = 1, coluna = 1;
        for (int i = 0; i < posicao; i++) {
            if (html.charAt(i) == '\n') { linha++; coluna = 1; } else coluna++;
        }
        return new SintaxeInvalidaException(mensagem + " Linha " + linha + ", coluna " + coluna + ".");
    }
    public ListaEncadeada<OcorrenciaDeTag> getOcorrenciaTags() {
        ListaEncadeada<OcorrenciaDeTag> copia = new ListaEncadeada<>();
        int total = ocorrenciaTags.obterComprimento();
        for (int i = total - 1; i >= 0; i--) {
            OcorrenciaDeTag origem = ocorrenciaTags.obterNo(i).getInfo();
            OcorrenciaDeTag nova = new OcorrenciaDeTag(origem.getTag());
            for (int n = 1; n < origem.getOcorrencias(); n++) nova.addOcorrencia();
            copia.inserir(nova);
        }
        return copia;
    }
}
