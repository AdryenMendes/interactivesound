package instruments;
import javax.sound.midi.Synthesizer;
import java.awt.Color;



public class SlowStrings extends InstrumentTrack {
    public SlowStrings(Synthesizer s, int ch) {
         // bass, cazul
        super(s, ch, 49, new Color(52, 152, 219));
    }

    // o baixo toca 1 oitava mais baixo (12 semitons)
    @Override 
    public void noteOn(int pitch){
        channel.noteOn(Math.max(24, pitch - 12), 70);
    }

    // correção de bug de som infinito
    @Override
    public void noteOff(int pitch) {
        channel.noteOff(Math.max(24, pitch - 12));      // desligar nota transposta 12 semitons abaixo
    }
}