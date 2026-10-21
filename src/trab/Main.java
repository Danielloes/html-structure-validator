package trab;

import Lista03.NoLista;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        if (args.length == 0) { Apresentacao.main(args); return; }
        if (args.length != 1) { System.err.println("Uso: java -cp build/classes trab.Main arquivo.html"); System.exit(2); }
        try {
            var tags = new ValidadorHTML(args[0]).validarHTML();
            System.out.println("Tags bem formadas.");
            for (NoLista<OcorrenciaDeTag> no = tags.getPrimeiro(); no != null; no = no.getProximo()) System.out.println(no.getInfo());
        } catch (IOException | SintaxeInvalidaException e) {
            System.err.println(e.getMessage()); System.exit(1);
        }
    }
}
