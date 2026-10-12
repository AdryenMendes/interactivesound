import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Synthesizer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.Spring;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;



public class main {

    private static Synthesizer synth;
    private static SoundChannel leadTrack;

    public static void main(String[] args) {
        
        // swing precisa rodar interface dentro de uma thread
        // invoker: janela só abra quando o java estiver 100%
        SwingUtilities.invokeLater(() -> {
            JFrame janela = new JFrame("janela de teste");
            janela.setSize(900, 600);
            janela.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            janela.setLocationRelativeTo(null);

            initMidi(janela);           // inicialização do sistema de audio

            Canvas canvas = new Canvas();
            if (leadTrack != null) {
                canvas.setSelectedTrack(leadTrack);
            }

            janela.setLayout(new BorderLayout());                // definir direções na janela
            janela.add(canvas, BorderLayout.CENTER);            // canvas ocupa centro da janela

            // barra inferior
            JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));       
            toolbar.setBackground(new Color(245, 245, 245));


            // 1. botão de play / pause
            JButton btnPlay = new JButton("Play");
            btnPlay.addActionListener(e -> {                        // clique via lambda
                boolean novoEstado = !canvas.isPlaying();           
                canvas.setPlaying(novoEstado);                      // inverte o estado atual
                btnPlay.setText(novoEstado ? "Pause" : "Play");     // atualizar texto de acordo com estado
            });
            toolbar.add(btnPlay);

            // separador visual
            toolbar.add(new JLabel("|"));           // separadoer visual


            // 2. BPM slider
            JLabel lblBpm = new JLabel("BPM: 120");
            JSlider bpmJSlider = new JSlider(60, 300, 120);
            bpmJSlider.setPreferredSize(new Dimension(150, 40));

            bpmJSlider.addChangeListener(e ->{
                int valorBpm = bpmJSlider.getValue();
                canvas.setBpm(valorBpm);
                 lblBpm.setText("BPM: " + valorBpm);
            });
            toolbar.add(lblBpm);
            toolbar.add(bpmJSlider);

            // separador visual
            toolbar.add(new JLabel("|"));     


            // 3. caneta e borracha
            JButton btnPen = new JButton("Caneta");
            JButton btnEraser = new JButton("Borracha");

            btnPen.addActionListener(e -> canvas.setEraserMode(false));
            btnEraser.addActionListener(e -> canvas.setEraserMode(true));

            toolbar.add(btnPen);
            toolbar.add(btnEraser);


            // 4. tamanho da borracha
            String[] tamanhos = {"Pequena (10px)", "Média (25px)", "Grande (45px)"};
            JComboBox<String> comboTamanho = new JComboBox<>(tamanhos);
            comboTamanho.setSelectedIndex(1);       // comeca com a média

            comboTamanho.addActionListener(e -> {
                int index = comboTamanho.getSelectedIndex();
                switch (index) {
                    case 0: canvas.setEraserRadius(10); break;
                    case 1: canvas.setEraserRadius(25); break;
                    case 2: canvas.setEraserRadius(45); break;
                }
            });
            toolbar.add(comboTamanho);


            // 5. limpar tudo
            JButton btnClear = new JButton("Limpar tudo");
            btnClear.addActionListener(e -> canvas.clearAll());
            toolbar.add(btnClear);


            janela.add(toolbar, BorderLayout.SOUTH);

            // fechar sintetizador quando janela fechar
            janela.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    if (synth != null && synth.isOpen()) {
                        synth.close();
                    }
                }
            });

            janela.setVisible(true);

        });
    }


    // método p inicializar o sistema de audio
    private static void initMidi(JFrame parentComponent) {
        try {
            // pede pro so o sintetizador MIDI padrão
            synth = MidiSystem.getSynthesizer();
            synth.open();

            // cria instrumento Lead Synth no canal 0
            leadTrack = new LeadSynth(synth, 0);

        } 

        // mensagens de erro
        catch (MidiUnavailableException e) {
            JOptionPane.showMessageDialog(
                    parentComponent,
                    "Nao foi possível inicializar o dispositivo de som MIDI:\n"
                            + e.getMessage(),
                    "Erro de Áudio",
                    JOptionPane.ERROR_MESSAGE
            );
        } 
        catch (Exception e) {
            JOptionPane.showMessageDialog(
                    parentComponent,
                    "Erro inesperado no sistema de áudio:\n"
                            + e.getMessage(),
                    "Erro",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }
}