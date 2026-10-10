import java.awt.Color;



public interface SoundChannel {
    void noteOn(int pitch);         // toca nota correspondente a frequencia
    void noteOff(int pitch);        // interrompe som da nota
    Color getColor();               // retorna a cor associada ao instrumento (p pintar a tela
}