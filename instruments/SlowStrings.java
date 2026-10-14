package instruments;
import javax.sound.midi.Synthesizer;
import java.awt.Color;



public class SlowStrings extends InstrumentTrack {
    public SlowStrings(Synthesizer s, int ch) {
         // cordas suaves, azul
        super(s, ch, 49, new Color(52, 152, 219));
    }
}