package com.khuong;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.JComponent;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class myMethods extends JComponent {

	private Image image;
	private Graphics2D g2;
	private int currentX, currentY, oldX, oldY;
	private String txt = "";
	
	public myMethods() {
		setDoubleBuffered(false);
		addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				oldX = e.getX();
				oldY = e.getY();
			}
		});
		addMouseMotionListener(new MouseMotionAdapter() {
			public void mouseDragged(MouseEvent e) {
				currentX = e.getX();
				currentY = e.getY();
				if (g2 != null) {
					g2.drawLine(oldX, oldY, currentX, currentY);
					repaint();
					oldX = currentX;
					oldY = currentY;
				}
			}
		});
	}

	protected void paintComponent(Graphics g) {
		if (image == null) {
			image = createImage(getSize().width, getSize().height);
			g2 = (Graphics2D) image.getGraphics();
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			clear();
		}

		g.drawImage(image, 0, 0, null);
	}
	
	public void textArea() {
		
	}

	public void clear() {
		g2.setPaint(Color.white);
		g2.fillRect(0, 0, getSize().width, getSize().height);
		g2.setPaint(Color.black);
		repaint();
	}

	public void black() {
		g2.setPaint(Color.black);
	}

	public void saveImage() throws IOException {
		RenderedImage img = (RenderedImage) image;
		File outputfile = new File("image.jpg");
		ImageIO.write(img, "jpg", outputfile);
	}

	public String readJSON() {
		txt = "";
		JSONParser parser = new JSONParser();
		try {
			FileReader reader = new FileReader("txt.json");
			Object obj = parser.parse(reader);
			JSONObject jsonObject = (JSONObject) obj;
			JSONArray recognitionResults = (JSONArray) jsonObject.get("recognitionResults");
			for (Object recognitionResult : recognitionResults) {
				JSONObject jsonObject2 = (JSONObject) recognitionResult;
				JSONArray lines = (JSONArray) jsonObject2.get("lines");
				for (Object line : lines) {
					JSONObject jsonObject3 = (JSONObject) line;
					String text = (String) jsonObject3.get("text");
					txt = txt + text + " ";
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return txt;
	}
}
