package instruments;
import javax.sound.midi.Synthesizer;
import java.awt.Color;



public class WarmPad extends InstrumentTrack {
    public WarmPad(Synthesizer s, int ch) {
        // pad quente, vermelho
        super(s, ch, 89, new Color(231, 76, 60));
    }

    // pad toca mais macio (volume 45 em vez de 60)
    @Override
    public void noteOn(int pitch) {
        channel.noteOn(pitch, 45);
    }
}