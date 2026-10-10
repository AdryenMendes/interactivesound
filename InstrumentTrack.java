import javax.sound.midi.MidiChannel;
import javax.sound.midi.Synthesizer;
import java.awt.Color;



// Como é abstrata, herda de new InstrumentTrack()
public abstract class InstrumentTrack implements SoundChannel {
    // protected: só essa classe e suas subclasses têm acesso direto a essas variáveis
    protected MidiChannel channel;
    protected Color color;


    /** intrumenttrack:
        - synth:       sintetizador central de áudio do Java
        = channelNum:  número da trilha midi
        - program:     código do instrumento General midi
        = color:       cor com a qual as notas deste instrumento serão desenhadas
     */

    public InstrumentTrack(Synthesizer synth, int channelNum, int program, Color color) {
        this.channel = synth.getChannels()[channelNum];             // pega canal fisico de som do sintetizador
        this.channel.programChange(program);                        // ´programChange toca o timbre do canal
        this.color = color;
    }

    @Override 
    public void noteOn(int pitch) {
        channel.noteOn(pitch, 60);                       // 60 é o volume do toque
    }

    @Override 
    public void noteOff(int pitch) {
        channel.noteOff(pitch);
    }

    @Override
    public Color getColor() {
        return color;
    }
}