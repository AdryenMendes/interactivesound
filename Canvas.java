import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;


public class Canvas extends JPanel {

    private List<Point> pontos = new ArrayList<>();     // new array: array dinamico em memoria
    public Canvas() {
        setBackground(Color.WHITE);
    }

     // mousePressed é chamado quando o botão do mouse é pressionado
    MouseAdapter tratadorDoMouse = new MouseAdapter() {
         @Override
        public void mousePressed(MouseEvent e) {        // 'e' contém as informações do evento.
            Point clique = e.getPoint();            // 'e.getPoint()' retorna um objeto Point com o (x, y) exato do clique.

            pontos.add(clique);
            repaint();
        }
    };

    @Override
    protected void paintComponent(Graphics g) {

            super.paintComponent(g);
            g.setColor(Color.RED);
            int tamanho = 60;

        for (Point p : pontos) {
            // fillOval: desenha círculos preenchidos
            // parâmetros: (X, Y, Largura, Altura)
            // P bolinha ficar centralizada no clique, subtraímos metade da largura/altura.
            g.fillOval(p.x - (tamanho / 2), p.y - (tamanho / 2), tamanho, tamanho);
        }
    }
}




