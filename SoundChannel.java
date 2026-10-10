import java.awt.Color;


public interface SoundChannel {
    void noteOn(int pitch);         // toca a nota correspondente à frequencia
    void noteOff(int pitch);        // interrompe o som da nota
    Color getColor();               // retorna a cor associada a este instrumento (p pintar a tela
}