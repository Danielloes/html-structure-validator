package trab;

import Lista03.ListaEncadeada;
import Lista03.NoLista;
import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.nio.file.Path;
import java.util.concurrent.ExecutionException;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;

public class Apresentacao {
    private final JFrame frame = new JFrame("Validador de estrutura HTML");
    private final JTextField caminho = new JTextField(35);
    private final JTextArea mensagem = new JTextArea(5, 55);
    private final JButton buscar = new JButton("Buscar HTML");
    private final DefaultTableModel modelo = new DefaultTableModel(new String[]{"Tag", "Quantidade"}, 0) {
        private static final long serialVersionUID = 1L;
        @Override public boolean isCellEditable(int linha, int coluna) { return false; }
        @Override public Class<?> getColumnClass(int coluna) { return coluna == 0 ? String.class : Integer.class; }
    };

    public Apresentacao() {
        caminho.setEditable(false);
        mensagem.setEditable(false);
        mensagem.setLineWrap(true);
        mensagem.setWrapStyleWord(true);
        mensagem.setText("Selecione um arquivo HTML para verificar o fechamento das tags e contar as ocorrências.");
        JTable tabela = new JTable(modelo);
        tabela.setAutoCreateRowSorter(true);
        JPanel topo = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topo.add(new JLabel("Arquivo:")); topo.add(caminho); topo.add(buscar);
        JPanel resultado = new JPanel(new BorderLayout(8, 8));
        resultado.setBorder(BorderFactory.createEmptyBorder(8, 12, 12, 12));
        resultado.add(new JScrollPane(mensagem), BorderLayout.NORTH);
        resultado.add(new JScrollPane(tabela), BorderLayout.CENTER);
        frame.add(topo, BorderLayout.NORTH);
        frame.add(resultado, BorderLayout.CENTER);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(740, 480);
        frame.setLocationRelativeTo(null);
        buscar.addActionListener(e -> escolherArquivo());
    }
    private void escolherArquivo() {
        JFileChooser seletor = new JFileChooser();
        seletor.setFileFilter(new FileNameExtensionFilter("HTML (*.html, *.htm)", "html", "htm"));
        seletor.setAcceptAllFileFilterUsed(false);
        if (seletor.showOpenDialog(frame) != JFileChooser.APPROVE_OPTION) return;
        analisarArquivo(seletor.getSelectedFile().toPath());
    }
    public void analisarArquivo(Path arquivo) {
        caminho.setText(String.valueOf(arquivo.getFileName()));
        caminho.setToolTipText(arquivo.toAbsolutePath().toString());
        modelo.setRowCount(0);
        mensagem.setText("Analisando arquivo...");
        buscar.setEnabled(false);
        SwingWorker<ListaEncadeada<OcorrenciaDeTag>, Void> analise = new SwingWorker<>() {
            @Override protected ListaEncadeada<OcorrenciaDeTag> doInBackground() throws Exception {
                return new ValidadorHTML(arquivo).validarHTML();
            }
            @Override protected void done() {
                try {
                    for (NoLista<OcorrenciaDeTag> no = get().getPrimeiro(); no != null; no = no.getProximo()) {
                        OcorrenciaDeTag tag = no.getInfo();
                        modelo.addRow(new Object[]{tag.getTag(), tag.getOcorrencias()});
                    }
                    mensagem.setText("Tags bem formadas. A tabela mostra quantas aberturas de cada tag foram encontradas.");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); mensagem.setText("Análise interrompida.");
                } catch (ExecutionException e) {
                    Throwable causa = e.getCause();
                    mensagem.setText(causa instanceof SintaxeInvalidaException ? causa.getMessage()
                        : "Não foi possível ler o arquivo: " + causa.getMessage());
                } finally { buscar.setEnabled(true); }
            }
        };
        analise.execute();
    }
    public JFrame getFrame() { return frame; }
    public static void main(String[] args) {
        EventQueue.invokeLater(() -> new Apresentacao().getFrame().setVisible(true));
    }
}
