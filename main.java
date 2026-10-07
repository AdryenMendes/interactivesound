import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;


public class main {
    public static void main(String[] args) {
        
        // swing precisa rodar interface dentro de uma thread
        // invoker: janela só abra quando o java estiver 100%
        SwingUtilities.invokeLater(() -> {
                JFrame janela = new JFrame("janela de teste");
                janela.setSize(900, 600);
                janela.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                janela.setLocationRelativeTo(null);

                Canvas canvas = new Canvas();
                janela.setLayout(new BorderLayout());                // definir direções na janela
                janela.add(canvas, BorderLayout.CENTER);            // canvas ocupa centro da janela

                JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));       // barraa de ferramentss
                toolbar.setBackground(new Color(245, 245, 245));

                JButton btnPlay = new JButton("Play");
                btnPlay.addActionListener(e -> {                        // clique via lambda
                    boolean novoEstado = !canvas.isPlaying();           
                    canvas.setPlaying(novoEstado);                      // inverte o estado atual
                    btnPlay.setText(novoEstado ? "Pause" : "Play");     // atualizar texto de acordo com estado
                });
            
            toolbar.add(btnPlay);
            janela.add(toolbar, BorderLayout.SOUTH);
            janela.setVisible(true);

        });
    }
}