package instruments;
import javax.sound.midi.Synthesizer;
import java.awt.Color;



public class PadSynth extends InstrumentTrack {
    public PadSynth(Synthesizer s, int ch) {
        // pad new age, vermelho
        super(s, ch, 88, new Color(231, 76, 60));
    }

    // pad toca mais macio (volume 45 em vez de 60)
    @Override
    public void noteOn(int pitch) {
        channel.noteOn(pitch, 45);
    }
}