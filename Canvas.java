import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class Canvas extends JPanel {

    // guardamos uma lista de tracos
    private List<Stroke> strokes = new ArrayList<>();
    
    // Guarda a referência do traço que está sendo desenhado nesse momento
    private Stroke currentStroke = null;

    public Canvas() {
        setBackground(Color.WHITE);

        MouseAdapter mouseHandler = new MouseAdapter() {
            
            // quando o botão do mouse for pressionado
            @Override
            public void mousePressed(MouseEvent e) {
                currentStroke = new Stroke();                 // cria um novo traço na memória
                currentStroke.addPoint(e.getPoint());         // adiciona o primeiro ponto onde o clique aconteceu
                strokes.add(currentStroke);                   // adiciona esse novo traço à lista principal de traços da tela
                repaint();                                    // pede para o Java redesenhar a tela
            }

            // quando o mouse pressionado fpr arrastado
            @Override
            public void mouseDragged(MouseEvent e) {
                if (currentStroke != null) {                 // se existir um traço ativo em andamento:
                    currentStroke.addPoint(e.getPoint());    // adiciona a posição atual do mouse ao traço em curso
                    repaint();
                }
            }

            // quando o botão do mouse for solto
            @Override
            public void mouseReleased(MouseEvent e) {
                currentStroke = null;
            }
        };

        // registrar msm objeto p cliques e p movimentos de arrasto
        addMouseListener(mouseHandler);
        addMouseMotionListener(mouseHandler);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // cor do traco
        g.setColor(Color.BLACK);

        // percorrer cada traco salvo na lista
        for (Stroke s : strokes) {
            List<Point> pts = s.getPoints();

            // desenhar a linha conectando cada ponto ao seguinte
            for (int i = 0; i < pts.size() - 1; i++) {
                Point p1 = pts.get(i);
                Point p2 = pts.get(i + 1);

                // segmento de reta de p1 a p2
                g.drawLine(p1.x, p1.y, p2.x, p2.y);
            }

            // 1 clique = bolinha
            if (pts.size() == 1) {
                Point p = pts.get(0);
                g.fillOval(p.x - 2, p.y - 2, 4, 4);
            }
        }
    }
}