package trab;

public class OcorrenciaDeTag {
    private final String tag;
    private int ocorrencias;
    public OcorrenciaDeTag(String tag) {
        if (tag == null || tag.isBlank()) throw new IllegalArgumentException("Informe a tag.");
        this.tag = tag;
        ocorrencias = 1;
    }
    public String getTag() { return tag; }
    public int getOcorrencias() { return ocorrencias; }
    public void addOcorrencia() { ocorrencias++; }
    @Override public String toString() { return tag + ": " + ocorrencias; }
}
