package tools;

import java.awt.*;
import java.util.ArrayList;


public class MotionVector {
	int width = 352;
	int height = 288;
	int[] deltaX = { -1, -1, -1, 0, 0, 1, 1, 1, 0 };
	int[] deltaY = { -1, 0, 1, -1, 1, -1, 0, 1, 0 };
	Colors[][] colors;
	Colors[][] colors1;
	ArrayList<Colors[][]> colorList = new ArrayList<>();
	ArrayList<Colors[][]> colorList1 = new ArrayList<>();

	public double showIms(byte[] b, byte[] b1) {
		int quantization = 0;
		colors = new Colors[height][width];
		colors1 = new Colors[height][width];

		// Save colors to [][]
		getColors(colors, b, colorList);
		getColors(colors1, b1, colorList1);

		double[][] mv = topLevelSearch();
		double motion = 0.0;
		for (int i = 0; i < 396; i++) {
			motion += mv[i][1];
		}
		return motion / 396;
	}

	private void getColors(Colors[][] colors, byte[] bytes, ArrayList<Colors[][]> colorList) {
		// read from file and save to array
		int ind = 0;
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {

				byte a = 0;
				byte r = bytes[ind];
				byte g = bytes[ind + height * width];
				byte b = bytes[ind + height * width * 2];

				// convert to HSV space
				float[] hsv = new float[3];
				Color.RGBtoHSB((r & 0xff), (g & 0xff), (b & 0xff), hsv);

				Colors color = new Colors(hsv);
				colors[y][x] = color;
				// System.out.println((r & 0xff) + " " + colors[y][x].channel1);
				ind++;
			}
		}

		// filter and downsampling
		Colors[][] c = filteringAndDownsampling(colors, width / 2, height / 2);
		Colors[][] c1 = filteringAndDownsampling(c, width / 4, height / 4);
		Colors[][] c2 = filteringAndDownsampling(c1, width / 8, height / 8);
		// hierarchy paramids
		colorList.add(colors);
		colorList.add(c);
		colorList.add(c1);
		colorList.add(c2);
	}

	// compute motion vector
	private double[][] topLevelSearch() {
		double[][] res = new double[396][];
		Colors[][] temp = colorList.get(3);
		Colors[][] temp1 = colorList1.get(3);
		// microblocks in frame k+1
		for (int a = 0; a < temp1.length; a += 2) {
			for (int b = 0; b < temp1[0].length; b += 2) {
				double min = Integer.MAX_VALUE;
				int row = 0;
				int col = 0;
				// full search for each block
				for (int i = 0; i < temp.length - 2 + 1; i++) {
					for (int j = 0; j < temp[0].length - 2 + 1; j++) {
						double curt = 0;
						for (int x = 0; x < 2; x++) {
							for (int y = 0; y < 2; y++) {
								curt += temp[i + x][j + y].minus(temp1[a + x][b + y]);
								if (min > curt) {
									min = curt;
									col = j;
									row = i;
								}
							}
						}
					}
				}
				int index = a / 2 * 22 + b / 2;
				// 0, 1: start position(frame k), 2, 3: end position(frame k + 1) row, col; 4:
				// MAD
				res[index] = new double[2];
				res[index][0] = min / 4;
				double dist = Math.pow(row - a, 2) + Math.pow(col - b, 2);
				dist = Math.sqrt(dist);
				res[index][1] = dist;
			}
		}
		return res;
	}

	// filter before compare
	private Colors[][] filteringAndDownsampling(Colors[][] colors, int w, int h) {
		Colors[][] newColors = new Colors[h][w];

		for (int i = 0; i < h; i++) {
			for (int j = 0; j < w; j++) {
				newColors[i][j] = new Colors();
				int neighbors = 0;
				// compute 8 neighbor pixels around pixel[i][j]
				for (int k = 0; k < 9; k++) {
					// downsampling by 2
					int ni = i * 2 + deltaY[k];
					int nj = j * 2 + deltaX[k];
					if (inBound(ni, nj, h * 2, w * 2)) {
						neighbors++;
						newColors[i][j] = newColors[i][j].plus(colors[ni][nj]);
					}
				}
				newColors[i][j] = newColors[i][j].product(neighbors);
			}
		}
		return newColors;
	}

	private boolean inBound(int i, int j, int h, int w) {
		return i >= 0 && j >= 0 && i < h && j < w;
	}

	public static double[] getMotions(byte[] b, byte[] b1, int frame) {
		double[] motions = new double[frame];
		for (int i = 0; i < frame; i++) {
			MotionVector mv = new MotionVector();
			motions[i] = mv.showIms(b, b1);
		}
		return motions;
	}

	public static String read(int i) {
		String num = "";
		int k = i + 1;
		num += Integer.toString(k / 100);
		k = k - (k / 100) * 100;
		num += Integer.toString(k / 10);
		k = k - (k / 10) * 10;
		num += Integer.toString(k);
		return num;
	}
//	 public static void main(String[] args) throws IOException {
//	 // TODO Auto-generated method stub
//	 int frame = 149;
//	 String path = "/Users/huikeli/Downloads/csci576/query/";
//	// String[] fileNames = {"flowers", "interview", "movie", "musicvideo", "sports", "starcraft", "traffic"};
//	 String[] fileNames = {"first"};
//	// for (int i = 0; i < 599; i++) {
//	// MotionVector mv = new MotionVector();
//	// String[] test = {path + "/first" + read(i) +".rgb", path + "/first" +  read(i + 1) +".rgb"};
//	// motions[i] = mv.showIms(test);
//	// }
//	
//	 for (int i = 0; i < 7; i++) {
//	 double[] motions = getMotions(path, fileNames[i], frame);
//	
//	 File fout = new File("bvideo" + i + ".txt");
//	 FileOutputStream fos = new FileOutputStream(fout);
//	
//	 BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
//	 for (int j = 0; j < frame; j++) {
//	 bw.write(Double.toString(motions[j]));
//	 bw.newLine();
//	 }
//	
//	 bw.close();
//	 }
//	 }
}
// private void refineVector(int[][] vector, Colors[][] colors, Colors[][]
// colors1, int microblock) {
// for (int index = 0; index < vector.length; index++) {
// for (int i = 0; i < 4; i++) {
// vector[index][i] *= 2;
// }
// // start position at frame k
// int sy = vector[index][0];
// int sx = vector[index][1];
// // end position at frame k+1
// int y = vector[index][2];
// int x = vector[index][3];
//// double min = vector[index][4];
// double min = Integer.MAX_VALUE;
// for (int k = 0; k < 9; k++) {
// int candidateY = y + deltaY[k];
// int candidateX = x + deltaX[k];
// // bound??????
// double curt = 0;
// if (inBound(candidateY + microblock - 1, candidateX + microblock - 1,
// colors.length, colors[0].length) &&
// inBound(candidateY, candidateX, colors.length, colors[0].length)) {
// for (int i = 0; i < microblock; i++) {
// for (int j = 0; j < microblock; j++) {
// //System.out.println(sy + " " + sx + " " + candidateY + " " + candidateX + "
// "+ i + " "+ j);
// curt += colors[sy + i][sx + j].minus(colors1[candidateY + i][candidateX +
// j]);
// }
// }
// curt = curt * 100000 / microblock / microblock;
// if (min > curt) {
// min = curt;
// //System.out.println(curt);
// vector[index][2] = y + deltaY[k];
// vector[index][3] = x + deltaX[k];
// vector[index][4] = (int)min;
// //System.out.println(vector[index][4]);
// }
// }
// }
// }
// }
