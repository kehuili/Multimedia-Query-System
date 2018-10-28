package Processor;

import java.lang.String;
import java.io.*;
import java.util.*;

import tools.*;

public class CompareSearch {
	private VideoProcessor[] vp;
	public Map<String, Integer> framemap;

	public Map<String, Double> search(String queryname) {
		double motionWeight = 0.7;
		double colorWeight = 0.2;
		double soundWeight = 0.1;

		framemap = new HashMap<String, Integer>();
		// initialize query
		VideoProcessor queryV = new VideoProcessor(150, 1);
		queryV.videoname = queryname;
		try {
			queryV.readAndextractSound();
		} catch (PlayWaveException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		queryV.readAndextractVideo();

		queryV.extractColor();

		queryV.extractMotion();

		Map<String, Double> simiMap = new HashMap<String, Double>();
		for (int i = 0; i < 7; i++) {
			simiMap.put(vp[i].videoname, 0.0);
		}

		// compare motion vector
		for (int i = 0; i < 7; i++) {
			int[] resembleClips = new int[250];
			double[] resembleValues = new double[250];
			for (int j = 0; j < 250; j++) {
				resembleClips[j] = -1;
				resembleValues[j] = -1.0;
			}

			// get the start frames of the most similar clips
			int k = 0;
			for (int j = 0; j < vp[i].framenum - queryV.framenum; j++) {
				double simiMotion = distCompare(queryV.motions[0], vp[i].motions[j]);
				// simiMotion = simiMotion / Math.max(queryV.motions[0], vp[i].motions[j]);
				// double simiColor = distCompare(queryV.rgbCount[0], vp[i].rgbCount[j]);
				// double simi = simiMotion * motionWeight + simiColor * colorWeight;

				if (k >= 250) {
					break;
				}
				if (simiMotion < 3) {
					resembleClips[k] = j;
					resembleValues[k] = simiMotion;
					k++;
				}
			}

			// From the latter frames compute the eligible clips
			for (int r = 30; r < queryV.framenum; r += 30) {
				for (int j = 0; j < 250; j++) {
					if (resembleValues[j] != -1.0) {
						double simiMotion = distCompare(queryV.motions[r], vp[i].motions[resembleClips[j] + r]);
						// simiMotion = simiMotion / Math.max(queryV.motions[r],
						// vp[i].motions[resembleClips[j] + r]);
						// double simiColor = distCompare(queryV.rgbCount[r],
						// vp[i].rgbCount[resembleClips[j] + r]);
						// double simi = simiMotion * motionWeight + simiColor * colorWeight;

						if (simiMotion >= 3) {
							resembleClips[j] = -1;
							resembleValues[j] = -1.0;
						} else {
							resembleValues[j] += simiMotion;
						}
					}
				}
			}

			int clip = 0;
			double minvalue = 10000;
			for (int j = 0; j < 250; j++) {
				if (resembleValues[j] != -1.0) {
					if (resembleValues[j] < minvalue) {
						clip = resembleClips[j];
						minvalue = resembleValues[j];
					}
				}
			}
			// System.out.println(minvalue + " "+ clip);
			framemap.put(vp[i].videoname, clip);
			double sumq = 0.0;
			double sumdb = 0.0;
			double simigsum = 0.0;
			double simisum = 0.0;
			double simisumColor = 0.0;
			k = 0;
			for (int j = clip; j < clip + 150 && k < queryV.framenum; j++, k++) {
				simisum += distCompare(queryV.motions[k], vp[i].motions[j]);
				sumdb += vp[i].motions[j];
				sumq += queryV.motions[k];
				simisumColor += distCompare(queryV.rgbCount[k], vp[i].rgbCount[j]);
				simigsum += hashCompare(queryV.imgbytes[k], vp[i].imgbytes[j]);
			}
			double soundDiff = Math.abs((double) queryV.sound - (double) vp[i].sound)
					/ Math.max((double) queryV.sound, (double) vp[i].sound);

			simiMap.put(vp[i].videoname, (1 - simisum / Math.max(sumdb, sumq)) * motionWeight 
				     + (1 - simisumColor / 150.0) * colorWeight * 0.7 + simigsum / 150.0 * colorWeight * 0.3
				     + (1- soundDiff) * soundWeight);
		}
		int num = 250;
		motionWeight = 0.4;
		colorWeight = 0.5;
		for (int i = 0; i < 7; i++) {
			if (simiMap.get(vp[i].videoname) < 0.9) {
				int[] resembleClips = new int[num];
				double[] resembleValues = new double[num];
				for (int j = 0; j < num; j++) {
					resembleClips[j] = -1;
					resembleValues[j] = 0.0;
				}

				// get the start frames of the most similar clips
				int k = 0;
				for (int a = 0; a < queryV.framenum; a += 30) {
					for (int j = 0; j < vp[i].framenum - queryV.framenum; j++) {
						double simiColor = distCompare(queryV.rgbCount[0], vp[i].rgbCount[j]);
						// if (j == 0) {
						// System.out.println(queryV.rgbCount[0][0] + " " + queryV.rgbCount[0][1] + " "
						// + queryV.rgbCount[0][2]);
						// }
						if (k >= num) {
							break;
						}
						if (simiColor < 0.3) {
							if (j - a >= 0) {
								resembleClips[k] = j - a;
								resembleValues[k] = simiColor;
								k++;
							}
						}
					}
				}

				// From the latter frames compute the eligible clips
				for (int r = 30; r < queryV.framenum; r += 30) {
					for (int j = 0; j < num; j++) {
						if (resembleValues[j] != 0.0) {
							double simiColor = distCompare(queryV.rgbCount[r], vp[i].rgbCount[resembleClips[j] + r]);

							if (simiColor >= 0.7) {
								resembleClips[j] = -1;
								resembleValues[j] = 0.0;
							} else {
								resembleValues[j] += simiColor;
							}
						}
					}
				}

				int clip = 0;
				double minvalue = 10000;
				for (int j = 0; j < num; j++) {
					if (resembleValues[j] != 0.0) {
						if (resembleValues[j] < minvalue) {
							clip = resembleClips[j];
							minvalue = resembleValues[j];
						}
					}
				}
				// System.out.println(minvalue + " "+ clip);
				framemap.put(vp[i].videoname, clip);
				double sumq = 0.0;
				double sumdb = 0.0;
				double simigsum = 0.0;
				double simisum = 0.0;
				double simisumColor = 0.0;
				k = 0;
				for (int j = clip; j < clip + 150 && k < queryV.framenum; j++, k++) {
					simisum += distCompare(queryV.motions[k], vp[i].motions[j]);
					sumdb += vp[i].motions[j];
					sumq += queryV.motions[k];
					simisumColor += distCompare(queryV.rgbCount[k], vp[i].rgbCount[j]);
					simigsum += hashCompare(queryV.imgbytes[k], vp[i].imgbytes[j]);
				}
				double soundDiff = Math.abs((double) queryV.sound - (double) vp[i].sound)
						/ Math.max((double) queryV.sound, (double) vp[i].sound);

				simiMap.put(vp[i].videoname, (1 - simisum / Math.max(sumdb, sumq)) * motionWeight 
					     + (1 - simisumColor / 150.0) * colorWeight * 0.7 + simigsum / 150.0 * colorWeight * 0.3
					     + (1- soundDiff) * soundWeight);
			}
		}

		return simiMap;
	}

	public void init() throws IOException {
		vp = new VideoProcessor[7];
		for (int i = 0; i < 7; i++) {
			vp[i] = new VideoProcessor(600, 0);
		}
		vp[0].videoname = "flowers";
		vp[1].videoname = "interview";
		vp[2].videoname = "movie";
		vp[3].videoname = "musicvideo";
		vp[4].videoname = "sports";
		vp[5].videoname = "starcraft";
		vp[6].videoname = "traffic";

		File f = new File(Constants.MULT_PATH + "video6.txt");
		if (f.exists()) {
			for (int i = 0; i < 7; i++) {
				BufferedReader in = new BufferedReader(new FileReader("video" + i + ".txt"));
				String line = in.readLine();
				vp[i].sound = Integer.parseInt(line);
				for(int j = 0;j < vp[i].framenum;j++) {
				     line = in.readLine();
				     for(int k = 0;k < 99;k++) {
				      vp[i].imgbytes[j][k] = line.charAt(k) - '0';
				     }
				    }
				for (int j = 0; j < vp[i].framenum; j++) {
					line = in.readLine();
					int t = 0;
					for (int k = 0; k < 3; k++) {
						String dvalue = "";
						for (; line.charAt(t) != ' '; t++) {
							dvalue += line.charAt(t);
						}
						t++;
						vp[i].rgbCount[j][k] = Double.parseDouble(dvalue);
					}
				}
				for (int j = 0; j < vp[i].framenum - 1; j++) {
					line = in.readLine();
					vp[i].motions[j] = Double.parseDouble(line);
				}
				in.close();
			}
		} else {
			for (int i = 0; i < 7; i++) {
				try {
					vp[i].readAndextractSound();
				} catch (PlayWaveException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}

				vp[i].readAndextractVideo();
				vp[i].extractColor();
				vp[i].extractMotion();

				File fout = new File("video" + i + ".txt");
				FileOutputStream fos = new FileOutputStream(fout);

				BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
				bw.write(Integer.toString(vp[i].sound));
				bw.newLine();
				for(int j = 0;j < vp[i].framenum;j++) {
				     for(int k = 0;k < 99;k++) {
				      bw.write(Integer.toString(vp[i].imgbytes[j][k]));
				     }
				     bw.newLine();
				    }
				for (int j = 0; j < vp[i].framenum; j++) {
					for (int k = 0; k < 3; k++) {
						bw.write(Double.toString(vp[i].rgbCount[j][k]));
						bw.write(" ");
					}
					bw.newLine();
				}
				for (int j = 0; j < vp[i].framenum - 1; j++) {
					bw.write(Double.toString(vp[i].motions[j]));
					bw.newLine();
				}
				bw.close();
			}
			for (int i = 0; i < 7; i++) {
				BufferedReader in = new BufferedReader(new FileReader("video" + i + ".txt"));
				String line = in.readLine();
				vp[i].sound = Integer.parseInt(line);
				for(int j = 0;j < vp[i].framenum;j++) {
				     line = in.readLine();
				     for(int k = 0;k < 99;k++) {
				      vp[i].imgbytes[j][k] = line.charAt(k) - '0';
				     }
				    }
				for (int j = 0; j < vp[i].framenum; j++) {
					line = in.readLine();
					int t = 0;
					for (int k = 0; k < 3; k++) {
						String dvalue = "";
						for (; line.charAt(t) != ' '; t++) {
							dvalue += line.charAt(t);
						}
						t++;
						vp[i].rgbCount[j][k] = Double.parseDouble(dvalue);
					}
				}
				for (int j = 0; j < vp[i].framenum - 1; j++) {
					line = in.readLine();
					vp[i].motions[j] = Double.parseDouble(line);
				}
				in.close();
			}
		}
	}

	private double distCompare(double[] q, double[] db) {
		double d = 0.0;

		for (int i = 0; i < q.length; i++) {
			d += Math.pow(q[i] - db[i], 2.0);
		}
		d = d / 3;
		d = Math.sqrt(d);

		return d;
	}

	private double distCompare(double q, double db) {
		double d = 0.0;
		d = Math.abs(q - db);

		return d;
	}
	
	private double hashCompare(int[] q, int [] db) {
		double sim = 0.0;
		  
		for(int i = 0;i < 99;i++) {
			if(q[i] == db[i]) {
				sim += 1.0;
			}
		}
		  //System.out.println(sim);
		  
		sim = sim / 99.0;
		  
		return sim;
	}
}