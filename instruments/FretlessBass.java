package instruments;
import javax.sound.midi.Synthesizer;
import java.awt.Color;



public class FretlessBass extends InstrumentTrack {
    public FretlessBass(Synthesizer s, int ch) {
        // string snsemble), amarelo
        super(s, ch, 35, new Color(241, 196, 15));
    }
}