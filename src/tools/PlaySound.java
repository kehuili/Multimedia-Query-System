package tools;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import javax.sound.sampled.Clip;

/**
 * 
 * <Replace this with a short description of the class.>
 * 
 * @author Giulio
 */
public class PlaySound {

    private InputStream waveStream;
	private String filename;
	private Clip clip = null;
    private final int EXTERNAL_BUFFER_SIZE = 524288; // 128Kb
	private long position=0;
	private AudioInputStream audioInputStream = null;

    /**
     * CONSTRUCTOR
     */
    public PlaySound(String audioFilename) {
	this.filename = audioFilename;
    }
	
    public void play() throws PlayWaveException {
	try {
		this.waveStream = new FileInputStream(this.filename);
	} catch (FileNotFoundException e2) {
		// TODO Auto-generated catch block
		e2.printStackTrace();
	}
	
	try {
	    //audioInputStream = AudioSystem.getAudioInputStream(this.waveStream);
		
		//add buffer for mark/reset support, modified by Jian
		InputStream bufferedIn = new BufferedInputStream(this.waveStream);
	    this.audioInputStream = AudioSystem.getAudioInputStream(bufferedIn);
		
	} catch (UnsupportedAudioFileException e1) {
	    throw new PlayWaveException(e1);
	} catch (IOException e1) {
	    throw new PlayWaveException(e1);
	}
	try {
		this.clip = AudioSystem.getClip();
	} catch (LineUnavailableException e1) {
	    throw new PlayWaveException(e1);
	}
		
	try {
		// Starts the music :P
		this.clip.open(this.audioInputStream);
		this.clip.setMicrosecondPosition(this.position);  // Must always rewind!
		this.clip.start();
		//this.position = this.clip.getFramePosition();
	} catch (LineUnavailableException | IOException e) {
		
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
    }
	public void pause(){
		this.position = this.clip.getMicrosecondPosition();
		if(this.clip!=null) {
			this.clip.stop();
		}
	}
	public void stop(){
		this.position = 0;
		if(this.clip!=null) {
			this.clip.stop();
		}
	}
	public void set_time_position(long i) {
		this.position=i;
	}
}
