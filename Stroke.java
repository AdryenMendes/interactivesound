import java.awt.Point;
import java.util.ArrayList;
import java.util.List;



// traço contínuo sem soltar o botão do mouse
public class Stroke {
    private List<Point> points = new ArrayList<>();     // lista dinâmica de pontos q formam a linha
    private SoundChannel track;                         // referencia p intrumento associado a esse traco
    public Stroke(SoundChannel track) {
        this.track = track;
    }
    
    // canvas le o track p pegar a cor
    public SoundChannel getTrack() {
        return track;
    }

    public void addPoint(Point p) {                     // adicionar novo ponto ao traço enquanto o mouse é arrastado
        points.add(p);
    }

    public List<Point> getPoints() {                    // retorna a lista de pontos p canvas poder desenhar
        return points;
    }
}

