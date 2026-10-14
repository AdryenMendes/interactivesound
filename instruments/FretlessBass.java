package instruments;
import javax.sound.midi.Synthesizer;
import java.awt.Color;



public class FretlessBass extends InstrumentTrack {
    public FretlessBass(Synthesizer s, int ch) {
        // baixo, amarelo
        super(s, ch, 35, new Color(241, 196, 15));
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