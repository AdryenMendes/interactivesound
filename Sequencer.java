import instruments.SoundChannel;
import javax.swing.Timer;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Sequencer {

    // variaveis de animação / sequenciador
    private float playheadX = 0;
    private boolean isPlaying = false;
    private int bpm = 120;

    // guarda as notas q estao tocando em cada instrumento
    private Map<SoundChannel, Set<Integer>> activeNotes = new HashMap<>();

    // guarda os pontos onde a barra ta cortando os tracos nql frame
    private List<Point> activeIntersectionVisuals = new ArrayList<>();

    private Canvas canvas;
    private Timer gameLoop;

    public Sequencer(Canvas canvas) {
        this.canvas = canvas;

        // loop de animação do playhead (+- 60 fps)
        gameLoop = new Timer(16, e -> {
            if (isPlaying) {
                float pixelsPerFrame = (bpm / 60.0f) * 2.0f;
                playheadX += pixelsPerFrame;

                // se a janela for minimizada, getWidth() pode ser <= 0
                if (canvas.getWidth() > 0 && playheadX > canvas.getWidth()) {
                    playheadX = 0;          // volta pro inicio da tela num loop
                    stopAllNotes();         // desliga notas ao reiniciar o ciclo
                }

                checkCollisions();          // ve se a barra cruzou algum traço
                canvas.repaint();           // desenha nova posição do playhead
            }
        });
        gameLoop.start();
    }

    // converter cordenadas Y numa nota musical
    private int calculatePitch(int y) {
        // notas da escala pentatonica (menor)
        int[] scale = {
            48, 51, 53, 55, 58,
            60, 63, 65, 67, 70,
            72, 75, 77, 79, 82,
        };
    
        if (canvas.getHeight() <= 0) {
            return scale[0];
        }

        // topo = notas agudas
        int inverdedY = canvas.getHeight() - y;

        // converte posiçao vert num indice entre 0 e 14
        int noteIndex = (inverdedY * scale.length) / canvas.getHeight();

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

        for (Stroke s : canvas.getStrokes()) {
            if (s.getTrack() == null) { 
                continue;                       // ignorar tracos sem intrumentos associados
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
        for (Map.Entry<SoundChannel, Set<Integer>> entry : notesCurrentlyHit.entrySet()) {
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
    public void stopAllNotes() {
        for (Map.Entry<SoundChannel, Set<Integer>> entry : activeNotes.entrySet()) {
            SoundChannel track = entry.getKey();

            for (Integer pitch : entry.getValue()) {
                track.noteOff(pitch);
            }
        }

        activeNotes.clear();
        activeIntersectionVisuals.clear();
    }

    // controla reprodução usando outros arquivos
    public void setPlaying(boolean playing) {
        this.isPlaying = playing;

        // pausar = silencia as notas imediatamente
        if (!playing) {
            stopAllNotes();
        }

        canvas.repaint();
    }

    public boolean isPlaying() {
        return this.isPlaying;
    }

    // deixa a janela principal alterar a velocidade do sequenciador
    public void setBpm(int bpm) {
        this.bpm = bpm;
    }

    public float getPlayheadX() {
        return playheadX;
    }

    public List<Point> getActiveIntersectionVisuals() {
        return activeIntersectionVisuals;
    }
}