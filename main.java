import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Synthesizer;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Color;
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

                // LeadSynth: ser o instrumento ativo de desenho
                if (leadTrack != null) {
                canvas.setSelectedTrack(leadTrack);
                }

                janela.setLayout(new BorderLayout());                // definir direções na janela
                janela.add(canvas, BorderLayout.CENTER);            // canvas ocupa centro da janela

                // barraa de ferramentas
                JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));       
                toolbar.setBackground(new Color(245, 245, 245));

                // botão de play / pause
                JButton btnPlay = new JButton("Play");
                btnPlay.addActionListener(e -> {                        // clique via lambda
                    boolean novoEstado = !canvas.isPlaying();           
                    canvas.setPlaying(novoEstado);                      // inverte o estado atual
                    btnPlay.setText(novoEstado ? "Pause" : "Play");     // atualizar texto de acordo com estado
                });

                // botão p testar o áudio MIDI
                JButton btnTestAudio = new JButton("testar som (Nota Dó)");
                btnTestAudio.addActionListener(e -> {
                    if (leadTrack != null) {
                    leadTrack.noteOn(60);
                    }
                });
            
            toolbar.add(btnTestAudio);
            toolbar.add(btnPlay);
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