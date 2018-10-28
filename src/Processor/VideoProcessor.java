package Processor;

import java.lang.String;
import java.util.ArrayList;
import java.util.Collections;
import java.lang.Math;
import java.awt.*;
import java.io.*;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import tools.*;

public class VideoProcessor {
	public String videoname;
	public int type;
	public int sound;
	public int framenum;
	public double[][] rgbCount; // 0 r, 1 g, 2 b
	public byte[][] colorbytes;
	public double[] motions; 
	public int[][] imgbytes;

	public VideoProcessor(int f, int t) {
		sound = 0;
		framenum = f;
		type = t;
		colorbytes = new byte[framenum][];
		rgbCount = new double[framenum][3];
		motions = new double[framenum];
		imgbytes = new int[framenum][99];
	}

	public void readAndextractSound() throws PlayWaveException, FileNotFoundException, IOException {
		String soundFileName;
		if (type == 0)
			soundFileName = Constants.VIDEO_PATH + videoname + "/" + videoname + ".wav";
		else
			soundFileName = Constants.QUERY_PATH + videoname + "/" + videoname + ".wav";
		try {
			File soundFile = new File(soundFileName);
			InputStream sis = new FileInputStream(soundFile);

			AudioInputStream audioInputStream = null;
			try {
				audioInputStream = AudioSystem.getAudioInputStream(soundFile);
			} catch (UnsupportedAudioFileException e1) {
				new PlayWaveException(e1);
			} catch (IOException e1) {
				new PlayWaveException(e1);
			}

			int readBytes = 0;
			byte[] audioBuffer = new byte[524288];

			try {
				if (readBytes != -1) {
					readBytes = audioInputStream.read(audioBuffer, 0, audioBuffer.length);

					if (readBytes >= 0) {
						sound = calculateRMSLevel(audioBuffer);
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void readAndextractVideo() {
		for (int i = 0; i < framenum; i++) {
			int[] bits = new int[99];
			String num = "";
			int k = i + 1;
			num += Integer.toString(k / 100);
			k = k - (k / 100) * 100;
			num += Integer.toString(k / 10);
			k = k - (k / 10) * 10;
			num += Integer.toString(k);
			String imgpath;
			File folder = new File(Constants.QUERY_PATH + videoname);
			File[] files= folder.listFiles();
			ArrayList<String> filelist = new ArrayList<String>();
			if(files!=null) {
				for (File f : files) {
					filelist.add(f.getName());
				}
			}
			File dbfolder = new File(Constants.VIDEO_PATH + videoname);
			File[] dbfiles= dbfolder.listFiles();
			ArrayList<String> dbfilelist = new ArrayList<String>();
			if(dbfiles!=null) {
				for (File f : dbfiles) {
					dbfilelist.add(f.getName());
				}
			}
			Collections.sort(filelist);
			Collections.sort(dbfilelist);
			if (type == 0)
				imgpath = Constants.VIDEO_PATH + videoname + "/" + dbfilelist.get(i+1);
			else
				imgpath = Constants.QUERY_PATH + videoname + "/" + filelist.get(i+1);
			readImagergb(imgpath, i);
			bits = cvtComputeBits(cvtGrayscale(resizePixels(colorbytes[i])));
			   for (int j = 0; j < 99; j++) {
			    imgbytes[i][j] = bits[j];
			   }
		}
	}
	public void extractMotion() {
		for (int i = 0; i < framenum - 1; i++) {
			MotionVector mv = new MotionVector();
			motions[i] = mv.showIms(colorbytes[i], colorbytes[i + 1]);
		}
	}
	public void extractColor() {
		for (int i = 0; i < framenum; i++) {
			rgbCount[i][0] = rgbCount[i][1] = rgbCount[i][2] = 0.0;
			for (int ind = 0; ind < 352 * 288; ind++) {
				int r = colorbytes[i][ind] < 0 ? colorbytes[i][ind] + 256 : colorbytes[i][ind];
				int g = colorbytes[i][ind + 352 * 288] < 0 ? colorbytes[i][ind + 352 * 288] + 256
						: colorbytes[i][ind + 352 * 288];
				int b = colorbytes[i][ind + 352 * 288 * 2] < 0 ? colorbytes[i][ind + 352 * 288 * 2] + 256
						: colorbytes[i][ind + 352 * 288 * 2];
//				float hsb[] = null;
//				hsb = Color.RGBtoHSB(r, g, b, null);
//				if (hsb[0] < 0.17 || hsb[0] >= 0.83) {
//					rgbCount[i][0] += 1;
//				} else if (hsb[0] >= 0.17 && hsb[0] < 0.5) {
//					rgbCount[i][1] += 1;
//				} else if (hsb[0] >= 0.5 && hsb[0] < 0.83) {
//					rgbCount[i][2] += 1;
//				}
				rgbCount[i][0] += r/255.0;
			    rgbCount[i][1] += g/255.0;
			    rgbCount[i][2] += b/255.0;
			}

			normalizeVector(i);
		}
	}

	private void normalizeVector(int k) {
		for (int i = 0; i < 3; i++) {
			rgbCount[k][i] /= 352 * 288;
		}
//		double rgbProd = rgbCount[k][0] * rgbCount[k][0] + rgbCount[k][1] * rgbCount[k][1]
//				+ rgbCount[k][2] * rgbCount[k][2];
//		rgbProd = Math.sqrt(rgbProd);
//		for (int i = 0; i < 3; i++) {
//			rgbCount[k][i] /= rgbProd;
//		}
	}

	private void readImagergb(String imgpath, int i) {
		// create a new buffered image
		File file = new File(imgpath);
		long len = file.length();
		colorbytes[i] = new byte[(int) len];
		// read image from file path
		try {
			InputStream is = new FileInputStream(file);
			int offset = 0;
			int numRead = 0;
			while (offset < colorbytes.length
					&& (numRead = is.read(colorbytes[i], offset, colorbytes[i].length - offset)) >= 0) {
				offset += numRead;
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private byte[] resizePixels(byte[] pixels) {
		  byte[] temp = new byte[11 * 9 * 3];
		  int ratio = 32;
		  int p;
		  for (int ind = 0; ind < 99; ind++) {
		   p = ind * ratio;
		   temp[ind] = pixels[p];
		   temp[ind + 99] = pixels[p + 352 * 288];
		   temp[ind + 99 * 2] = pixels[p + 352 * 288 * 2];
		   ind++;
		  }
		  return temp;
		 }
	
	private byte[] cvtGrayscale(byte[] bytes) {
		  byte[] graybytes = new byte[11 * 9];
		  int ind = 0;
		  for (int y = 0; y < 9; y++) {
		   for (int x = 0; x < 11; x++) {
		    byte r = bytes[ind];
		    byte g = bytes[ind + 99];
		    byte b = bytes[ind + 99 * 2];
		    graybytes[y * 11 + x] = (byte) (0.21 * r + 0.72 * g + 0.07 * b);
		    ind++;
		   }
		  }
		  return graybytes;
		 }

		 private int[] cvtComputeBits(byte[] graybytes) {
		  int[] bits = new int[99];
		  double average = 0, sum = 0;
		  for (int i = 0; i < 99; i++) {
		   sum += graybytes[i];
		  }
		  average = sum / 99.0;
		  for (int i = 0; i < 99; i++) {
		   if (graybytes[i] > average) {
		    bits[i] = 1;
		   } else {
		    bits[i] = 0;
		   }
		  }
		  return bits;
		 }

	private int calculateRMSLevel(byte[] audioData) {
		long lSum = 0;
		for (int i = 0; i < audioData.length; i++) {
			lSum = lSum + audioData[i];
		}
		
		double dAvg = lSum / audioData.length;
		double sumMeanSquare = 0d;
		for (int j = 0; j < audioData.length; j++) {
			sumMeanSquare = sumMeanSquare + Math.pow(audioData[j] - dAvg, 2d);
		}
		double averageMeanSquare = sumMeanSquare / audioData.length;
		return (int) (Math.pow(averageMeanSquare, 0.5d) + 0.5);
	}

	public int getFrequency(byte[] bytes) {
		double[] audioData = this.bytesToDoubleArray(bytes);
		audioData = applyHanningWindow(audioData);
		Complex[] complex = new Complex[audioData.length];
		for (int i = 0; i < complex.length; i++) {
			complex[i] = new Complex(audioData[i], 0);
		}
		Complex[] fftTransformed = FFT.fft(complex);
		return this.calculateFundamentalFrequency(fftTransformed, 4);
	}

	private double[] applyHanningWindow(double[] data) {
		return applyHanningWindow(data, 0, data.length);
	}

	/**
	 * Applies a Hanning Window to the data set. Hanning Windows are used to
	 * increase the accuracy of the FFT. One should always apply a window to a
	 * dataset before applying an FFT
	 * 
	 * @param The
	 *            data you want to apply the window to
	 * @param The
	 *            starting index you want to apply a window from
	 * @param The
	 *            size of the window
	 * @return The windowed data set
	 */
	private double[] applyHanningWindow(double[] signal_in, int pos, int size) {
		for (int i = pos; i < pos + size; i++) {
			int j = i - pos; // j = index into Hann window function
			signal_in[i] = (signal_in[i] * 0.5 * (1.0 - Math.cos(2.0 * Math.PI * j / size)));
		}
		return signal_in;
	}

	private int calculateFundamentalFrequency(Complex[] fftData, int N) {
		if (N <= 0 || fftData == null) {
			return -1;
		} // error case

		final int LENGTH = fftData.length;// Used to calculate bin size
		fftData = removeNegativeFrequencies(fftData);
		Complex[][] data = new Complex[N][fftData.length / N];
		for (int i = 0; i < N; i++) {
			for (int j = 0; j < data[0].length; j++) {
				data[i][j] = fftData[j * (i + 1)];
			}
		}
		Complex[] result = new Complex[fftData.length / N];// Combines the
															// arrays
		for (int i = 0; i < result.length; i++) {
			Complex tmp = new Complex(1, 0);
			for (int j = 0; j < N; j++) {
				tmp = tmp.times(data[j][i]);
			}
			result[i] = tmp;
		}
		int index = this.findMaxMagnitude(result);
		return index * getFFTBinSize(LENGTH);
	}

	/**
	 * Removes useless data from transform since sound doesn't use complex
	 * numbers.
	 * 
	 * @param The
	 *            data you want to remove the complex transforms from
	 * @return The cleaned data
	 */
	private Complex[] removeNegativeFrequencies(Complex[] c) {
		Complex[] out = new Complex[c.length / 2];
		for (int i = 0; i < out.length; i++) {
			out[i] = c[i];
		}
		return out;
	}

	/**
	 * Calculates the FFTbin size based off the length of the the array Each
	 * FFTBin size represents the range of frequencies treated as one. For
	 * example, if the bin size is 5 then the algorithm is precise to within
	 * 5hz. Precondition: length cannot be 0.
	 * 
	 * @param fftDataLength
	 *            The length of the array used to feed the FFT algorithm
	 * @return FFTBin size
	 */
	private int getFFTBinSize(int fftDataLength) {
		return (int) (8000.0F / fftDataLength + .5);
	}

	/**
	 * Calculates index of the maximum magnitude in a complex array.
	 * 
	 * @param The
	 *            Complex[] you want to get max magnitude from.
	 * @return The index of the max magnitude
	 */
	private int findMaxMagnitude(Complex[] input) {
		// Calculates Maximum Magnitude of the array
		double max = Double.MIN_VALUE;
		int index = -1;
		for (int i = 0; i < input.length; i++) {
			Complex c = input[i];
			double tmp = c.getMagnitude();
			if (tmp > max) {
				max = tmp;
				;
				index = i;
			}
		}
		return index;
	}

	/**
	 * Converts bytes from a TargetDataLine into a double[] allowing the
	 * information to be read. NOTE: One byte is lost in the conversion so don't
	 * expect the arrays to be the same length!
	 * 
	 * @param bufferData
	 *            The buffer read in from the target data line
	 * @return The double[] that the buffer has been converted into.
	 */
	private double[] bytesToDoubleArray(byte[] bufferData) {
		final int bytesRecorded = bufferData.length;
		final int bytesPerSample = 2;
		final double amplification = 100.0; // choose a number as you like
		double[] micBufferData = new double[bytesRecorded - bytesPerSample + 1];
		for (int index = 0, floatIndex = 0; index < bytesRecorded - bytesPerSample
				+ 1; index += bytesPerSample, floatIndex++) {
			double sample = 0;
			for (int b = 0; b < bytesPerSample; b++) {
				int v = bufferData[index + b];
				if (b < bytesPerSample - 1 || bytesPerSample == 1) {
					v &= 0xFF;
				}
				sample += v << (b * 8);
			}
			double sample32 = amplification * (sample / 32768.0);
			micBufferData[floatIndex] = sample32;

		}
		return micBufferData;
	}
}
