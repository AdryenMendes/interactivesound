import instruments.*;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Synthesizer;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;



public class main {

    private static Synthesizer synth;
    private static List<SoundChannel> tracks = new ArrayList<>();

    public static void main(String[] args) {
        
        // swing precisa rodar interface dentro de uma thread
        // invoker: janela só abra quando o java estiver 100%
        SwingUtilities.invokeLater(() -> {
            JFrame janela = new JFrame("janela de teste");
            janela.setSize(1050, 700);
            janela.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            janela.setLocationRelativeTo(null);

            initMidi(janela);           // inicialização do sistema de audio
            Canvas canvas = new Canvas();

            if (!tracks.isEmpty()) {
                canvas.setSelectedTrack(tracks.get(0));
            }

            janela.setLayout(new BorderLayout());                // definir direções na janela
            janela.add(canvas, BorderLayout.CENTER);            // canvas ocupa centro da janela

            // barra inferior
            JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));       
            toolbar.setBackground(new Color(245, 245, 245));

            // estilos de borda
            Border activeColorBorder = BorderFactory.createLineBorder(new Color(30, 30, 30), 3);
            Border inactiveColorBorder = BorderFactory.createLineBorder(new Color(190, 190, 190), 1);
            Border activeToolBorder = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(30, 30, 30), 2),
                BorderFactory.createEmptyBorder(3, 10, 3, 10)
            );


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


            // lista p gerenciar botoes da paleta
            List<JButton> colorButtons = new ArrayList<>();


            // 3. borracha
            JButton btnEraser = new JButton("Borracha");
            btnEraser.setPreferredSize(new Dimension(100, 28)); // mesma altura (28) das cores
            Border defaltErasBorder = btnEraser.getBorder();

            btnEraser.addActionListener(e -> {
                canvas.setEraserMode(true);
                btnEraser.setBorder(activeToolBorder);          // destaque visual

                for (JButton b : colorButtons){
                    b.setBorder(inactiveColorBorder);           // remove borda grossa dos intrumentos
                }
            });

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


            // 6. paleta dos 6 instrumentos
            JPanel palette = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 0));
            palette.setOpaque(false);

            for (int i = 0; i < tracks.size(); i++){
                SoundChannel t = tracks.get(i);
                JButton colorBtn = new JButton();
                colorBtn.setBackground(t.getColor());
                colorBtn.setPreferredSize(new Dimension(26, 26));           // tamanho do quadradinho
                colorBtn.setFocusPainted(false);

                // o 1° instrumento ja tm a borda escura
                if (i == 0) {
                    colorBtn.setBorder(activeColorBorder);
                } else {
                    colorBtn.setBorder(inactiveColorBorder);
                }

                colorBtn.addActionListener(e -> {
                    canvas.setSelectedTrack(t);
                    canvas.setEraserMode(false);
                    btnEraser.setBorder((defaltErasBorder));

                    for (JButton b : colorButtons) {            // coloca borda so no botao clicado
                        b.setBorder(b == colorBtn ? activeColorBorder : inactiveColorBorder);
                    }
                });
                colorButtons.add(colorBtn);
                palette.add(colorBtn);
            }
            toolbar.add(palette);


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

            // instancia de 6 instrumentos em canais separados
            tracks.add(new Marimba(synth, 0));
            tracks.add(new SlowStrings(synth, 1));
            tracks.add(new MusicBox(synth, 2));
            tracks.add(new WarmPad(synth, 3));
            tracks.add(new PianoSynth(synth, 4));
            tracks.add(new FretlessBass(synth, 5));

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