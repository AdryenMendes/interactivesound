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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;



public class Canvas extends JPanel {

    // guarda uma lista de tracos
    private List<Stroke> strokes = new ArrayList<>();
    
    // guarda a referência do traço que está sendo desenhado nesse momento
    private Stroke currentStroke = null;

    // guarda o instrumento ativo
    private SoundChannel selectedTrack = null;

    // guarda as notas q estao tocando em cada instrumento
    private Map<SoundChannel, Set<Integer>> activeNotes = new HashMap<>();

    // guarda os pontos onde a barra ta cortando os tracos nql frame
    private List<Point> activeIntersectionVisuals = new ArrayList<>();

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



        // loop de animação do playhead (+- 60 fps)
        Timer gameLoop = new Timer(16, e -> {
            if (isPlaying) {
                float pixelsPerFrame = (bpm / 60.0f) * 2.0f;
                playheadX += pixelsPerFrame;

                // se a janela for minimizada, getWidth() pode ser <= 0
                if (getWidth() > 0 && playheadX > getWidth()) {
                    playheadX = 0;          // volta pro inicio da tela num loop
                    stopAllNotes();         // desliga notas ao reiniciar o ciclo
                }

                checkCollisions();          // ve se a barra cruzou algum traço
                repaint();                  // desenha nova posição do playhead
            }
        });
        gameLoop.start();
    }


    // converter cordenadas Y numa nota musical
    private int calculatePitch(int y){
        // notas da escala pentatonica (menor)
        int[] scale = {
            48, 51, 53, 55, 58,
            60, 63, 65, 67, 70,
            72, 75, 77, 79, 82,
        };
    
        if (getHeight() <= 0){
            return scale[0];
        }

        // topo = notas agudas
        int inverdedY = getHeight() - y;

        // converte posiçao vert num indice entre 0 e 14
        int noteIndex = (inverdedY * scale.length) / getHeight();

        // 0 <= indice <= 14
        noteIndex = Math.max(0, Math.min(scale.length - 1, noteIndex));

        return scale[noteIndex];
    }


    // detectar colisoes
    private void checkCollisions() {
        //limpar pontos visuais do frame anterior
        activeIntersectionVisuals.clear();
        // notas q a barra ta atingindo nesse momentoi
        Map<SoundChannel, Set<Integer>> notesCurrentlyHit = new HashMap<>();

        for (Stroke s : strokes) {
            if (s.getTrack() == null){ 
                continue;                       // ignotar tracos sem intrumentos associados
            }

            List<Point> pts = s.getPoints();

            // caso 1: traco formado por 1 ponto
            if (pts.size() == 1) {
                Point p = pts.get(0);

                if (Math.abs(p.x - playheadX) < 3) {            // colisao
                    int pitch = calculatePitch(p.y);            // conversao
                    notesCurrentlyHit                           // registro
                        .computeIfAbsent(s.getTrack(), k -> new HashSet<>())
                        .add(pitch);
                    activeIntersectionVisuals                   // registro p desenhar cursos 
                        .add(new Point((int) playheadX, p.y));          
                }
                continue;
            }
            
            // caso 2: traco formado por varios pontos
            for (int i = 0; i < pts.size() - 1; i++) {
                Point p1 = pts.get(i);
                Point p2 = pts.get(i + 1);
                int minX = Math.min(p1.x, p2.x);
                int maxX = Math.max(p1.x, p2.x);

                if (playheadX >= minX && playheadX <= maxX && (maxX - minX) > 0) {
                    float t = (playheadX - p1.x) / (float) (p2.x - p1.x);           // porcent do deslocamento entre pontos
                    int interpolatedY = Math.round(p1.y + t * (p2.y - p1.y));       // posição real
                    int pitch = calculatePitch(interpolatedY);

                    notesCurrentlyHit
                        .computeIfAbsent(s.getTrack(), k -> new HashSet<>())
                        .add(pitch);
                    activeIntersectionVisuals 
                        .add(new Point((int) playheadX, interpolatedY));  
                }
            }
        }

        // 1° parte: ligar notas q tao sendo atingidas
        for (Map.Entry<SoundChannel, Set<Integer>> entry : notesCurrentlyHit.entrySet()){
            SoundChannel track = entry.getKey();
            Set<Integer> currentPitches = entry.getValue();
            Set<Integer> activePiches = activeNotes.computeIfAbsent(track, k -> new HashSet<>());

            for (Integer pitch : currentPitches) {
                // so liga a nota se ela n tiver tocando
                if (!activePiches.contains(pitch)) {
                    track.noteOn(pitch);
                    activePiches.add(pitch);
                }
            }

        }

        // 2° parte: desligar notas q n tao mais sendo atingidas
        Iterator<Map.Entry<SoundChannel, Set<Integer>>> iterator = activeNotes.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<SoundChannel, Set<Integer>> entry = iterator.next();
            SoundChannel track = entry.getKey();
            Set<Integer> activePitches = entry.getValue();
            Set<Integer> currentPitches = notesCurrentlyHit.getOrDefault(track, Collections.emptySet());
            Iterator<Integer> noteIterator = activePitches.iterator();

             while (noteIterator.hasNext()) {
                int pitch = noteIterator.next();

                // se barra n ta mais cruzando a nota = desliga o som e tira do conjunto "ativas"
                if (!currentPitches.contains(pitch)) {
                    track.noteOff(pitch);
                    noteIterator.remove();
                }
            }

            // remove instrumentos q n tem notas ativas
            if (activePitches.isEmpty()) {
                iterator.remove();
            }

        }
    }


    // desligar todas as notas q tao tocando
    private void stopAllNotes() {
        for (Map.Entry<SoundChannel, Set<Integer>> entry : activeNotes.entrySet()) {
            SoundChannel track = entry.getKey();

            for (Integer pitch : entry.getValue()){
                track.noteOff(pitch);
            }
        }

        activeNotes.clear();
        activeIntersectionVisuals.clear();
    }


    // deixa q a janela principal escolha qual instrumento vai ser usado
    public void setSelectedTrack(SoundChannel track) {
        this.selectedTrack = track;
    }


    // controla reprodução usando outros arquivos
    public void setPlaying(boolean playing) {
        this.isPlaying = playing;

        // pausar = silencia as notas imediatamente
        if (!playing) {
            stopAllNotes();
        }

        repaint();
    }


    public boolean isPlaying() {
        return this.isPlaying;
    }


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
        g2.setColor(new Color(60, 60, 60));
        g2.setStroke(new BasicStroke(2));
        g2.drawLine((int) playheadX, 0, (int) playheadX, getHeight());

        // desenhar pontos de colisão
        if (isPlaying) {
            for (Point hit : activeIntersectionVisuals) {
                g2.setColor(Color.BLACK);
                g2.fillOval(hit.x - 6, hit.y -  6, 12, 12);
                g2.setColor(Color.WHITE);
                g2.fillOval(hit.x - 2, hit.y - 3, 4, 4);
            }
        }

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