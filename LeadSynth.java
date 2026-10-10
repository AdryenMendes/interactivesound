import javax.sound.midi.Synthesizer;
import java.awt.Color;


// 1° intrumento
public class LeadSynth extends InstrumentTrack {
    public LeadSynth(Synthesizer synth, int channelNum){
        super(synth, channelNum, 11, new Color(155, 89, 182));     //super' chama construtor da classe pai
    }

}
