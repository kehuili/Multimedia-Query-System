package SystemUI;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import Processor.CompareSearch;
import tools.*;

public class SearchUI {
	int width = 352;
	int height = 288;

	// UI every part
	// frame
	private JFrame frame;
	// layout
	private GridBagLayout layout;
	private JPanel panel;
	// query text
	private JPanel queryPanel;
	private JTextField query;
	private JLabel label;
	private JButton btn;
	// match list
	private JLabel labelMatch;
	private JList<String> list;
	private ArrayList<Double> resultList;
	private ArrayList<String> resultListRankedNames;
	private JScrollPane matchList;
	private Box vBox;
	// chart
	private JPanel chartPanel;
	// slider
	private JSlider slider;
	// video
	private JLabel lbIm1;
	private JLabel lbIm2;
	// control button1
	private JPanel buttonGroup;
	private JButton playButton;
	private JButton pauseButton;
	private JButton stopButton;
	// control button2
	private JPanel buttonGroup1;
	private JButton playButton1;
	private JButton pauseButton1;
	private JButton stopButton1;
	// error msg and other
	private JLabel errorLabel;
	private String errorsg;

	private ArrayList<BufferedImage> images;
	private ArrayList<BufferedImage> dbImages;
	private int frameRate = 30;
	private PlaySound playQSound;
	private PlaySound playDBSound;

	// private List resultListDisplay;
	private Map<String, Double> resultMap;
	private Map<String, Double> sortedResultMap;

	// private String fileName;
	private int playStatus = 3;// 1 for play, 2 for pause, 3 for stop
	private int resultPlayStatus = 3;
	private Thread playingThread;
	private Thread playingDBThread;
	private Thread audioThread;
	private Thread audioDBThread;
	private int currentFrameNum = 0;
	private int currentDBFrameNum = 0;
	private int totalFrameNum = 150;
	private int totalDBFrameNum = 600;

	private CompareSearch searchClass;
	private Map<String, Integer> similarFrameMap;
	
	public SearchUI() {
		this.init();

	}

	private void init() {

		errorLabel = new JLabel("");
		errorLabel.setForeground(Color.RED);

		frame = new JFrame("Multimedia Queries");
		frame.setPreferredSize(new Dimension(800, 600));
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// set layout
		layout = new GridBagLayout();
		frame.getContentPane().setLayout(layout);
		GridBagConstraints c = null;
		panel = new JPanel(layout);

		// query
		queryPanel = new JPanel();
		query = new JTextField("first", 10);
		label = new JLabel("Query: ");
		btn = new JButton("submit");
		btn.addActionListener(new SubmitListener());

		queryPanel.add(label);
		queryPanel.add(query);
		queryPanel.add(btn);

		c = new GridBagConstraints();
		layout.addLayoutComponent(queryPanel, c);

		// match list
		labelMatch = new JLabel("Matched Videos: ");
		list = new JList<String>();
		list.addListSelectionListener(new ListListener());
		matchList = new JScrollPane(list, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		vBox = Box.createVerticalBox();
		vBox.add(labelMatch);
		vBox.add(matchList);

		c = new GridBagConstraints();
		c.gridwidth = GridBagConstraints.REMAINDER;
		layout.addLayoutComponent(vBox, c);

		// chart, implement later
		chartPanel = new JPanel(new GridLayout(2, 1));
		chartPanel.add(errorLabel);
		// slider
		slider = new JSlider(0, 599, 0);
		slider.addChangeListener(new SliderListener());
		chartPanel.add(slider);

		layout.addLayoutComponent(chartPanel, c);
		c = new GridBagConstraints();
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.anchor = GridBagConstraints.LAST_LINE_END;
//		slider.setPreferredSize(new Dimension(200, 40));
		c.ipadx = 200;
		layout.addLayoutComponent(chartPanel, c);

		// video
		BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		BufferedImage img1 = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		lbIm1 = new JLabel(new ImageIcon(img));
		lbIm2 = new JLabel(new ImageIcon(img1));

		c = new GridBagConstraints();
		layout.addLayoutComponent(lbIm1, c);
		c = new GridBagConstraints();
		c.ipadx = 50;
		c.gridwidth = GridBagConstraints.REMAINDER;
		layout.addLayoutComponent(lbIm2, c);

		// control button1
		buttonGroup = new JPanel();
		buttonGroup.setLayout(new FlowLayout());

		playButton = new JButton("PLAY");
		pauseButton = new JButton("PAUSE");
		stopButton = new JButton("STOP");

		playButton.addActionListener(new PlayHandler());
		pauseButton.addActionListener(new PauseHandler());
		stopButton.addActionListener(new StopHandler());

		buttonGroup.add(playButton);
		buttonGroup.add(Box.createHorizontalStrut(3));
		buttonGroup.add(pauseButton);
		buttonGroup.add(Box.createHorizontalStrut(3));
		buttonGroup.add(stopButton);

		// control button2
		buttonGroup1 = new JPanel();
		buttonGroup1.setLayout(new FlowLayout());

		playButton1 = new JButton("PLAY");
		pauseButton1 = new JButton("PAUSE");
		stopButton1 = new JButton("STOP");
		
		playButton1.setEnabled(false);
		pauseButton1.setEnabled(false);
		stopButton1.setEnabled(false);

		playButton1.addActionListener(new PlayHandler1());
		pauseButton1.addActionListener(new PauseHandler1());
		stopButton1.addActionListener(new StopHandler1());

		buttonGroup1.add(playButton1);
		buttonGroup1.add(Box.createHorizontalStrut(3));
		buttonGroup1.add(pauseButton1);
		buttonGroup1.add(Box.createHorizontalStrut(3));
		buttonGroup1.add(stopButton1);

		c = new GridBagConstraints();
		layout.addLayoutComponent(buttonGroup, c);
		c = new GridBagConstraints();
		layout.addLayoutComponent(buttonGroup1, c);

		panel.add(queryPanel);
		panel.add(vBox);
		panel.add(chartPanel);
		panel.add(lbIm1);
		panel.add(lbIm2);
		panel.add(buttonGroup);
		panel.add(buttonGroup1);
		frame.add(panel);

		frame.pack();
		frame.setVisible(true);
		frame.setLocationRelativeTo(null);

		// compare
		searchClass = new CompareSearch();
		try {
			searchClass.init();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// load query video
	private void load_query_video(String userInput) {
		try {
			if (userInput == null || userInput.isEmpty()) {
				return;
			}
			// every query video in has 150 frames
			images = new ArrayList<BufferedImage>();
			File folder = new File(Constants.QUERY_PATH + userInput);
			File[] files= folder.listFiles();
			ArrayList<String> filelist = new ArrayList<String>();
			if(files!=null) {
				for (File f : files) {
					filelist.add(f.getName());
				}
			}
			Collections.sort(filelist);
			for (int i = 1; i <= 150; i++) {
				
				String videoName = Constants.QUERY_PATH + userInput + "/" + filelist.get(i);
				String audioFilename = Constants.QUERY_PATH + "/" + userInput + "/" + filelist.get(0);
//				System.out.println(videoName);
				File file = new File(videoName);
				InputStream is = new FileInputStream(file); 

				long len = file.length();
				byte[] bytes = new byte[(int) len];
				int offset = 0;
				int numRead = 0;
				while (offset < bytes.length && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
					offset += numRead;
				}
				int index = 0;
				BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
				for (int y = 0; y < height; y++) {
					for (int x = 0; x < width; x++) {
						byte r = bytes[index];
						byte g = bytes[index + height * width];
						byte b = bytes[index + height * width * 2];
						int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
						image.setRGB(x, y, pix);
						index++;
					}
				}
				images.add(image);
				is.close();
				playQSound = new PlaySound(audioFilename);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			errorLabel.setText(e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			errorLabel.setText(e.getMessage());
		}
		playStatus = 3;
		currentFrameNum = 0;
		totalFrameNum = images.size();
		QueryScreenShot();
	}

	// display one frame of query
	private void QueryScreenShot() {
		Thread initThread = new Thread() {
			public void run() {
				lbIm1.setIcon(new ImageIcon(images.get(currentFrameNum)));
			}
		};
		initThread.start();
	}

	// load db video
	private void load_DB_video(String dbVideoName) {
		try {
			if (dbVideoName == null || dbVideoName.isEmpty()) {
				return;
			}
			if(playDBSound!=null) {
				playDBSound.stop();
			}
			// every query video in has 600 frames
			dbImages = new ArrayList<BufferedImage>();
			File folder = new File(Constants.VIDEO_PATH + dbVideoName);
			File[] files= folder.listFiles();
			ArrayList<String> filelist = new ArrayList<String>();
			if (files != null) {
				for (File f : files) {
					filelist.add(f.getName());
				}
			}
			Collections.sort(filelist);
			for (int i = 1; i <= 600; i++) {
				String videoName = Constants.VIDEO_PATH + dbVideoName + "/" + filelist.get(i);
				String audioFilename = Constants.VIDEO_PATH + "/" + dbVideoName + "/" + filelist.get(0);

				File file = new File(videoName);
				InputStream is = new FileInputStream(file);

				long len = file.length();
				byte[] bytes = new byte[(int) len];
				int offset = 0;
				int numRead = 0;
				while (offset < bytes.length && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
					offset += numRead;
				}
				int index = 0;
				BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
				for (int y = 0; y < height; y++) {
					for (int x = 0; x < width; x++) {
						byte r = bytes[index];
						byte g = bytes[index + height * width];
						byte b = bytes[index + height * width * 2];
						int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
						image.setRGB(x, y, pix);
						index++;
					}
				}
				dbImages.add(image);
				is.close();
				playDBSound = new PlaySound(audioFilename);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			errorLabel.setText(e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			errorLabel.setText(e.getMessage());
		}
		playButton1.setEnabled(true);
		pauseButton1.setEnabled(true);
		stopButton1.setEnabled(true);
		resultPlayStatus = 3;
		currentDBFrameNum = 0;
		totalDBFrameNum = dbImages.size();
		DBScreenShot();
	}

	// display one frame of db video
	private void DBScreenShot() {
		Thread initThread = new Thread() {
			public void run() {
				lbIm2.setIcon(new ImageIcon(dbImages.get(currentDBFrameNum)));
			}
		};
		initThread.start();
	}

	// play query video
	private void playVideo() {
		playingThread = new Thread() {
			public void run() {
				for (int i = currentFrameNum; i < totalFrameNum; i++) {
					lbIm1.setIcon(new ImageIcon(images.get(i)));
					try {
						sleep(1000 / frameRate);
					} catch (InterruptedException e) {
						if (playStatus == 3) {
							currentFrameNum = 0;
						} else {
							currentFrameNum = i;
						}
						lbIm1.setIcon(new ImageIcon(images.get(currentFrameNum)));
						currentThread().interrupt();
						break;
					}
				}
				if (playStatus < 2) {
					playStatus = 3;
					currentFrameNum = 0;
					playQSound.stop();
				}
			}
		};
		audioThread = new Thread() {
			public void run() {
				try {
					playQSound.play();
				} catch (PlayWaveException e) {
					e.printStackTrace();
					errorLabel.setText(e.getMessage());
					return;
				}
			}
		};
		audioThread.start();
		playingThread.start();
	}

	// play db video
	private void playDBVideo() {
		playingDBThread = new Thread() {
			public void run() {
				for (int i = currentDBFrameNum; i < totalDBFrameNum; i++) {
					lbIm2.setIcon(new ImageIcon(dbImages.get(i)));
					slider.setValue(i);
					
					try {
						sleep(1000 / frameRate);
					} catch (InterruptedException e) {
						if (resultPlayStatus == 3) {
							currentDBFrameNum = 0;
						} else {
							currentDBFrameNum = i;
						}
						lbIm2.setIcon(new ImageIcon(dbImages.get(currentDBFrameNum)));

						currentThread().interrupt();
						break;
					}
				}
				if (resultPlayStatus < 2) {
					resultPlayStatus = 3;
					currentDBFrameNum = 0;
					playDBSound.stop();
				}
			}
		};
		audioDBThread = new Thread() {
			public void run() {
				try {
					playDBSound.play();
				} catch (PlayWaveException e) {
					e.printStackTrace();
					errorLabel.setText(e.getMessage());
					return;
				}
			}
		};
		audioDBThread.start();
		playingDBThread.start();
	}

	// pause query video (audio need more implements)
	private void pauseVideo() throws InterruptedException {
		if (playingThread != null) {
			playingThread.interrupt();
			audioThread.interrupt();
			playQSound.pause();
			playingThread = null;
			audioThread = null;
		}
	}

	// pause db video (audio need more implements)
	private void pauseDBVideo() throws InterruptedException {
		if (playingDBThread != null) {
			playingDBThread.interrupt();
			audioDBThread.interrupt();
			playDBSound.pause();
			playingDBThread = null;
			audioDBThread = null;
		}
	}

	// stop query video
	private void stopVideo() {
		if (playingThread != null) {
			playingThread.interrupt();
			audioThread.interrupt();
			playQSound.stop();
			playingThread = null;
			audioThread = null;
			slider.setValue(0);
		} else {
			currentFrameNum = 0;
			QueryScreenShot();
			playQSound.stop();
			slider.setValue(0);
		}
	}

	// stop db video
	private void stopDBVideo() {
		if (playingDBThread != null) {
			playingDBThread.interrupt();
			audioDBThread.interrupt();
			playDBSound.stop();
			playingDBThread = null;
			audioDBThread = null;
			slider.setValue(0);
		} else {
			currentDBFrameNum = 0;
			DBScreenShot();
			playDBSound.stop();
			slider.setValue(0);
		}
	}

	// display similar frame (need add in panel)
	private void updateSimilarFrame() {
		int userSelect = list.getSelectedIndex();
		String userSelectStr = resultListRankedNames.get(userSelect);
		Integer frm = similarFrameMap.get(userSelectStr);
		errorsg = "   from frame " + (frm + 1) + " to frame " + (frm + 151);
		Thread initThread = new Thread() {
			public void run() {
				errorLabel.setText(errorsg);
				Hashtable<Integer, JComponent> hashtable = new Hashtable<Integer, JComponent>();
		        hashtable.put(frm + 1, new JLabel("↑"));     
		        hashtable.put(frm + 151, new JLabel("↑"));     
		        slider.setLabelTable(hashtable);

//		        slider.setPaintTicks(true);
		        slider.setPaintLabels(true);
//		        System.out.println(slider.getLabelTable().toString());
			}
		};
		initThread.start();
	}

	// movement
	class SubmitListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
			lbIm2.setIcon(new ImageIcon(img));
			slider.setValue(0);
			errorLabel.setText("");
			slider.setPaintLabels(false);
			playButton1.setEnabled(false);
			pauseButton1.setEnabled(false);
			stopButton1.setEnabled(false);
			// load part
			String userInput = query.getText();
			if (userInput != null && !userInput.isEmpty()) {
				playingThread = null;
				audioThread = null;
				load_query_video(userInput.trim());
			}
			// search part
			if (userInput.trim().isEmpty()) {
				return;
			}
			resultMap = searchClass.search(userInput.trim());
			list.removeAll();
			resultList = new ArrayList<Double>(7);
			resultListRankedNames = new ArrayList<String>(7);
			sortedResultMap = new HashMap<String, Double>();

			Iterator it = resultMap.entrySet().iterator();
			while (it.hasNext()) {
				Entry pair = (Entry) it.next();
				String videoName = (String) pair.getKey();
		        Double videoRank = (Double) pair.getValue(); 
		        
				resultList.add(videoRank);
				sortedResultMap.put(videoName, videoRank);
			}
			Collections.sort(resultList);
			Collections.reverse(resultList);
			String[] s = new String[7];

			for (int i = 0; i < resultList.size(); i++) {
				Double tmpRank = resultList.get(i);
				it = sortedResultMap.entrySet().iterator();
				while (it.hasNext()) {
					Entry pair = (Entry) it.next();
					Double videoRank = (Double) pair.getValue();
					if (videoRank == tmpRank) {
						DecimalFormat df = new DecimalFormat("#.00");  
						s[i] = (pair.getKey() + "   " + (df.format(videoRank * 100)) + "%");
						list.setListData(s);
						resultListRankedNames.add((String) pair.getKey());
						break;
					}
				}
			}
			similarFrameMap = searchClass.framemap;
		}
	}

	class ListListener implements ListSelectionListener {
		@Override
		public void valueChanged(ListSelectionEvent e) {
			// TODO Auto-generated method stub
			if (e.getValueIsAdjusting()) {
				JList list = (JList) e.getSource();
				int index = list.getSelectedIndex();
				System.out.println("selected: " + list.getSelectedValue());
				int userSelect = list.getSelectedIndex();
				playingDBThread = null;
				audioDBThread = null;
				load_DB_video(resultListRankedNames.get(userSelect));
				updateSimilarFrame();
				slider.setValue(0);
			}
		}
	}

	class SliderListener implements ChangeListener {// need more implements
		@Override
		public void stateChanged(ChangeEvent e) {
			if (playDBSound!=null) {
				playDBSound.set_time_position((long)(currentDBFrameNum/frameRate*1000000));
			}
			if (resultPlayStatus == 2) {
				currentDBFrameNum = slider.getValue();
				DBScreenShot();
			}else if (slider.getValueIsAdjusting()) {
				if (resultPlayStatus == 1) {
					resultPlayStatus = 4;
					try {
						pauseDBVideo();
					} catch (InterruptedException e1) {
						// TODO Auto-generated catch block
						errorLabel.setText(e1.getMessage());
						e1.printStackTrace();
					}
				}
//				System.out.println("current frame: " + slider.getValue());
				currentDBFrameNum = slider.getValue();
				DBScreenShot();
			} else {
				if (resultPlayStatus == 4) {
					resultPlayStatus = 1;
					playDBVideo();
				}
			}
		}
	}

	class PlayHandler implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			if (playStatus > 1) {
				playStatus = 1;
				playVideo();
			}
		}
	}

	class PauseHandler implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			// System.out.println("pause button clicked");
			if (playStatus == 1) {
				playStatus = 2;
				try {
					pauseVideo();
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
					errorLabel.setText(e1.getMessage());
				}
			}
		}
	}

	class StopHandler implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			// System.out.println("stop button clicked");
			if (playStatus < 3) {
				playStatus = 3;
				stopVideo();
			}
		}
	}

	class PlayHandler1 implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			if (resultPlayStatus > 1) {
				resultPlayStatus = 1;
				playDBVideo();
			}
		}
	}

	class PauseHandler1 implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			if (resultPlayStatus == 1) {
				resultPlayStatus = 2;
				try {
					pauseDBVideo();
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					errorLabel.setText(e1.getMessage());
					e1.printStackTrace();
				}
			}
		}
	}

	class StopHandler1 implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			if (resultPlayStatus < 3) {
				resultPlayStatus = 3;
				stopDBVideo();
			}
		}
	}
}
