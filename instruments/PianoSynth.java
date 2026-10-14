package instruments;
import javax.sound.midi.Synthesizer;
import java.awt.Color;



public class PianoSynth extends InstrumentTrack {
    public PianoSynth(Synthesizer s, int ch) {
        // grand piano, verde
        super(s, ch, 0, new Color(46, 204, 113));
    }
}