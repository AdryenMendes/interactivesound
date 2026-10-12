package instruments;
import javax.sound.midi.Synthesizer;
import java.awt.Color;



public class BellSynth extends InstrumentTrack {
    public BellSynth(Synthesizer s, int ch) {
        // music box, cor laranja
        super(s, ch, 10, new Color(230, 126, 34));
    }
}