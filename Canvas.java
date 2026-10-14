import instruments.SoundChannel;
import javax.swing.JPanel;
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

    // variaveis da borracha
    private boolean isEraserMode = false;
    private int eraserRadius = 25;
    private Point eraserCursorPos = null;

    // referencia para o sequenciador musical
    private Sequencer sequencer;

    public Canvas() {
        setBackground(Color.WHITE);
        this.sequencer = new Sequencer(this);

        MouseAdapter mouseHandler = new MouseAdapter() {
            // quando o botão do mouse for pressionado
            @Override
            public void mousePressed(MouseEvent e) {
                if (isEraserMode) {
                    eraseAt(e.getPoint());
                } else {
                    currentStroke = new Stroke(selectedTrack);
                    currentStroke.addPoint(e.getPoint());         // adiciona o primeiro ponto onde o clique aconteceu
                    strokes.add(currentStroke);                   // adiciona esse novo traço a lista principal de traços da tela
                }
                repaint();
            }

            // quando o mouse pressionado fpr arrastado
            @Override
            public void mouseDragged(MouseEvent e) {
                if (isEraserMode) {
                    eraserCursorPos = e.getPoint();             // atualiza cursor
                    eraseAt(e.getPoint());                      // continua apagando
                } else if (currentStroke != null) {         // se tiver traço ativo em andamento
                    currentStroke.addPoint(e.getPoint());       // adiciona a posição atual do mouse ao traço em curso
                }
                repaint();
            }

            // quando o botão do mouse for solto
            @Override
            public void mouseReleased(MouseEvent e) {
                currentStroke = null;
            }

            // percebe quando mouse so anda e atualiza o circulo da borracha
            @Override 
            public void mouseMoved(MouseEvent e) {
                if (isEraserMode) {
                    eraserCursorPos = e.getPoint();
                    repaint();
                }
            }

            // quando o mouse sai da janela, esconde o circulo da borracha
            @Override 
            public void mouseExited(MouseEvent e) {
                eraserCursorPos = null;
                repaint();
            }
        };

        // registrar msm objeto p cliques e p movimentos de arrasto
        addMouseListener(mouseHandler);
        addMouseMotionListener(mouseHandler);
    }

    // borracha: percorre os tracos e apaga os que estão dentro do raio
    private void eraseAt(Point center) {
        List<Stroke> newStrokes = new ArrayList<>();

        for (Stroke s : strokes) {
            Stroke currentSegment = new Stroke(s.getTrack());

            for (Point p : s.getPoints()) {
                if (p.distance(center) >= eraserRadius) {               // ponto fora do raio
                    currentSegment.addPoint(p);                     
                } else if (!currentSegment.getPoints().isEmpty()) {     // ponto dentro do raio = 
                    newStrokes.add(currentSegment);                     // é apagado
                    currentSegment = new Stroke(s.getTrack());
                }
            }

            // salva o ultimo pedaco do traco, caso tenha sobrado
            if (!currentSegment.getPoints().isEmpty()) {
                newStrokes.add(currentSegment);
            }
        }

        // substituicao: lista antiga -> lista nova cortada
        this.strokes = newStrokes;
    }

    // botao limpa todos os desenhos e traços
    public void clearAll() {
        sequencer.stopAllNotes();
        strokes.clear();
        repaint();
    }

    public void setEraserMode(boolean active) {
        this.isEraserMode = active;
        if (!active) {
            eraserCursorPos = null;
        }
        repaint();
    }

    public boolean isEraserMode() {
        return this.isEraserMode;
    }

    public void setEraserRadius(int radius) {
        this.eraserRadius = radius;
        repaint();
    }

    // deixa q a janela principal escolha qual instrumento vai ser usado
    public void setSelectedTrack(SoundChannel track) {
        this.selectedTrack = track;
    }

    public List<Stroke> getStrokes() {
        return strokes;
    }

    // metodos de delegacao p manter compatibilidade com o main
    public void setPlaying(boolean playing) {
        sequencer.setPlaying(playing);
    }

    public boolean isPlaying() {
        return sequencer.isPlaying();
    }

    public void setBpm(int bpm) {
        sequencer.setBpm(bpm);
    }

    // desenho
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // graphics2D permite controle sobre espessura e filtro
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
        float currentPlayheadX = sequencer.getPlayheadX();
        g2.setColor(new Color(60, 60, 60));
        g2.setStroke(new BasicStroke(2));
        g2.drawLine((int) currentPlayheadX, 0, (int) currentPlayheadX, getHeight());

        // desenhar pontos de colisão
        if (sequencer.isPlaying()) {
            for (Point hit : sequencer.getActiveIntersectionVisuals()) {
                g2.setColor(Color.BLACK);
                g2.fillOval(hit.x - 6, hit.y -  6, 12, 12);
                g2.setColor(Color.WHITE);
                g2.fillOval(hit.x - 2, hit.y - 3, 4, 4);
            }
        }

        // desenha deliminacao da borracha
        if (isEraserMode && eraserCursorPos != null) {
            int diametro = eraserRadius * 2;
            int x = eraserCursorPos.x - eraserRadius;
            int y = eraserCursorPos.y - eraserRadius;

            g2.setColor(new Color(255, 100, 100, 40));
            g2.fillOval(x, y, diametro, diametro);

            g2.setColor(new Color(255, 50, 50));
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawOval(x, y, diametro, diametro);
        }
    }

    // função auxiliar p criar a grade - linhas horizontais e verticais
    private void drawGrid(Graphics2D g2) {
        g2.setColor(new Color(245, 245, 245));           // cinza claro
        g2.setStroke(new BasicStroke(1));                 // traço fino de 1 pixel

        for (int x = 0; x < getWidth(); x += 30) {              // linhas verticais espaçadas a cada 30 pixels
            g2.drawLine(x, 0, x, getHeight());              // getWidth(): retorna a largura atual da tela
        }

        for (int y = 0; y < getHeight(); y += 30) {             // Linhas horizontais espaçadas a cada 30 pixels
            g2.drawLine(0, y, getWidth(), y);               // getHeight(): retorna a altura atual da tela
        }
    }
}