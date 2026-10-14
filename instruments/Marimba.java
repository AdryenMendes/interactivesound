package instruments;
import javax.sound.midi.Synthesizer;
import java.awt.Color;



public class Marimba extends InstrumentTrack {
    public Marimba(Synthesizer synth, int channelNum){
        // marimba, roxo
        super(synth, channelNum, 12, new Color(155, 89, 182));     // chama construtor da classe pai
    }

}