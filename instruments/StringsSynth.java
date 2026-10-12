package instruments;
import javax.sound.midi.Synthesizer;
import java.awt.Color;



public class StringsSynth extends InstrumentTrack {
    public StringsSynth(Synthesizer s, int ch) {
        // string snsemble), amarelo
        super(s, ch, 48, new Color(241, 196, 15));
    }
}