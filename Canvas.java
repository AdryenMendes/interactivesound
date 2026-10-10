import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;



public class Canvas extends JPanel {

    // guarda uma lista de tracos
    private List<Stroke> strokes = new ArrayList<>();
    
    // guarda a referência do traço que está sendo desenhado nesse momento
    private Stroke currentStroke = null;

    // guarda o instrumento ativo
    private SoundChannel selectedTrack = null;

    // variaveis de animação / sequenciador
    private float playheadX = 0;
    private boolean isPlaying = false;
    private int bpm = 120;


    public Canvas() {
        setBackground(Color.WHITE);

        MouseAdapter mouseHandler = new MouseAdapter() {
            // quando o botão do mouse for pressionado
            @Override
            public void mousePressed(MouseEvent e) {
                currentStroke = new Stroke(selectedTrack);
                currentStroke.addPoint(e.getPoint());         // adiciona o primeiro ponto onde o clique aconteceu
                strokes.add(currentStroke);                   // adiciona esse novo traço a lista principal de traços da tela
                repaint();                                    // pede para o Java redesenhar a tela
            }

            // quando o mouse pressionado fpr arrastado
            @Override
            public void mouseDragged(MouseEvent e) {
                if (currentStroke != null) {                 // se tiver traço ativo em andamento
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

        // loop de animação do playhead
        Timer gameLoop = new Timer(16, e -> {
            if (isPlaying) {
                float pixelsPerFrame = (bpm / 60.0f) * 2.0f;
                playheadX += pixelsPerFrame;

                // se a janela for minimizada, getWidth() pode ser <= 0
                if (getWidth() > 0 && playheadX > getWidth()) {
                    playheadX = 0;                   // volta pro inicio da tela num loop
                }
                repaint();                           // desenha nova posição do playhead
            }
        });
        gameLoop.start();
    }


    // deixa q a janela principal escolha qual instrumento vai ser usado
    public void setSelectedTrack(SoundChannel track) {
        this.selectedTrack = track;
    }

    
    // permitir q outros arquivos controlem repodução
    public void setPlaying(boolean playing) {
        this.isPlaying = playing;
        repaint();
    }

    public boolean isPlaying() {
        return this.isPlaying;
    }


    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // graphics2D possibilitaa controle sobre espessura e filtro
        Graphics2D g2 = (Graphics2D) g;

        // filtro q calcula tons intermediários nas bordas da linha
        // deixa o traço liso em vez de pixelado
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // grade de fundo
        drawGrid(g2);

        /*  config da caneta
            - largura da linha: 4 pixels
            - CAP_ROUND: as extremidades sao pontas redondas
            - JOIN_ROUND: dobras e curvas conectadas suavemente 
        */
        g2.setStroke(new BasicStroke(4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

 
        // percorrer cada traco salvo na lista
        for (Stroke s : strokes) {

            // usa cor do instrumento associado ao traço
            if (s.getTrack() != null) {
                g2.setColor(s.getTrack().getColor());
            } else {
                g2.setColor(Color.BLACK);
            }

            List<Point> pts = s.getPoints();

            // desenhar a linha conectando cada ponto ao seguinte
            for (int i = 0; i < pts.size() - 1; i++) {
                Point p1 = pts.get(i);
                Point p2 = pts.get(i + 1);

                // segmento de reta de p1 a p2
                g2.drawLine(p1.x, p1.y, p2.x, p2.y);
            }

            // 1 clique = bolinha
            if (pts.size() == 1) {
                Point p = pts.get(0);
                g2.fillOval(p.x - 3, p.y - 3, 6, 6);
            }
        }

        // barra vertical fina na cordenada playheadX atual
        g2.setColor(new Color(60, 60, 60));
        g2.setStroke(new BasicStroke(2));
        g2.drawLine((int) playheadX, 0, (int) playheadX, getHeight());
    }


    // função auxiliar p criar a grade - linhas horizontais e verticais
    private void drawGrid(Graphics2D g2) {
        g2.setColor(new Color(245, 245, 245));      // cinza claro
        g2.setStroke(new BasicStroke(1));           // traço fino de 1 pixel


        for (int x = 0; x < getWidth(); x += 30) {          // linhas verticais espaçadas a cada 30 pixels
            g2.drawLine(x, 0, x, getHeight());              // getWidth(): retorna a largura atual da tela
        }

        for (int y = 0; y < getHeight(); y += 30) {         // Linhas horizontais espaçadas a cada 30 pixels
            g2.drawLine(0, y, getWidth(), y);               // getHeight(): retorna a altura atual da tela
        }
    }
}