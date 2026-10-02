import javax.swing.JPanel;
import java.awt.Graphics;
import java.awt.Color;


public class Canvas extends JPanel {

    public Canvas() {
        setBackground(Color.WHITE);
    }


    @Override
    protected void paintComponent(Graphics g) {
        // "super' chama o paintComponent original da classe pai (JPanel)
        super.paintComponent(g);

        // desenhar um quadrado preenchido:
        // Parametros: (X, Y, Largura, Altura)
        g.setColor(Color.BLUE);
        g.fillRect(50, 50, 100, 100);

        // mudar a cor p vermelho e desenhamos uma linha
        // Parametros: (X_inicial, Y_inicial, X_final, Y_final)
        g.setColor(Color.RED);

        g.drawLine(0, 0, 300, 300);
    }
}
