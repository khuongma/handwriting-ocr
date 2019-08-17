package com.khuong;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import java.net.URI;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import org.json.JSONObject;

import com.khuong.myMethods;

public class myMain {
	private static final String subscriptionKey = "f9efc32cec464e1c83ec7c2526df8c85";
	private static final String uriBase = "https://westus.api.cognitive.microsoft.com/vision/v2.0/read/core/asyncBatchAnalyze";
	
	String txt = "";
	JButton clearBtn, blackBtn, analyzeBtn;
	myMethods myMethod;
	JFrame frame = new JFrame("Paint");
	Container content = frame.getContentPane();
	JPanel controls = new JPanel();
	JTextArea textArea = new JTextArea("");
	
	ActionListener actionListener = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == clearBtn) {
				myMethod.clear();
			} else if (e.getSource() == blackBtn) {
				myMethod.black();
			}
			else if (e.getSource() == analyzeBtn) {
				try {
					myMethod.saveImage();
					OCR();
					txt = myMethod.readJSON();
					System.out.println("Analyzed text: "+txt);
					txt = "Analyzed Text: "+ txt;
					textArea.setText(txt);
					
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		}
	};

	public void show() {
		content.setLayout(new BorderLayout());
		myMethod = new myMethods();
		content.add(myMethod, BorderLayout.CENTER);

		clearBtn = new JButton("Clear");
		clearBtn.addActionListener(actionListener);
		blackBtn = new JButton("Black");
		blackBtn.addActionListener(actionListener);
		analyzeBtn = new JButton("Analyze");
		analyzeBtn.addActionListener(actionListener);
		
		content.add(textArea, BorderLayout.WEST);
		controls.add(blackBtn);
		controls.add(clearBtn);
		controls.add(analyzeBtn);
		content.add(controls, BorderLayout.NORTH);

		frame.setSize(800, 500);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
		

		
		
		
		 
				// Execute when button is pressed
				//System.out.println("You clicked the button");
//
//				int messageType = JOptionPane.PLAIN_MESSAGE;
// 
//				JOptionPane.showMessageDialog(null, txt, "Text:", messageType);
				
				
			
		

	}

	public static void OCR() {
		CloseableHttpClient httpTextClient = HttpClientBuilder.create().build();
		CloseableHttpClient httpResultClient = HttpClientBuilder.create().build();

		StringEntity requestEntity = null;
		try {
			// This operation requires two REST API calls. One to submit the image
			// for processing, the other to retrieve the text found in the image.

			URIBuilder builder = new URIBuilder(uriBase);

			// Prepare the URI for the REST API method.
			URI uri = builder.build();
			HttpPost request = new HttpPost(uri);

			// Request headers.
			request.setHeader("Content-Type", "application/octet-stream");
			request.setHeader("Ocp-Apim-Subscription-Key", subscriptionKey);

			// Request body.
//			myMethods readjson = new myMethods();
			File file1 = new File("image.jpg");
			FileEntity reqEntity = new FileEntity(file1);
			request.setEntity(reqEntity);

			// Two REST API methods are required to extract handwritten text.
			// One method to submit the image for processing, the other method
			// to retrieve the text found in the image.

			// Call the first REST API method to detect the text.
			HttpResponse response = httpTextClient.execute(request);

			// Check for success.
			if (response.getStatusLine().getStatusCode() != 202) {
				// Format and display the JSON error message.
				HttpEntity entity = response.getEntity();
				String jsonString = EntityUtils.toString(entity);
				JSONObject json = new JSONObject(jsonString);
				System.out.println("Error:\n");
				System.out.println(json.toString(2));
				return;
			}

			// Store the URI of the second REST API method.
			// This URI is where you can get the results of the first REST API method.
			String operationLocation = null;

			// The 'Operation-Location' response header value contains the URI for
			// the second REST API method.
			Header[] responseHeaders = response.getAllHeaders();
			for (Header header : responseHeaders) {
				if (header.getName().equals("Operation-Location")) {
					operationLocation = header.getValue();
					break;
				}
			}

			if (operationLocation == null) {
				System.out.println("\nError retrieving Operation-Location.\nExiting.");
				System.exit(1);
			}

			// If the first REST API method completes successfully, the second
			// REST API method retrieves the text written in the image.
			//
			// Note: The response may not be immediately available. Handwriting
			// recognition is an asynchronous operation that can take a variable
			// amount of time depending on the length of the handwritten text.
			// You may need to wait or retry this operation.

			System.out.println("Wait 10 seconds for analysis.");
			Thread.sleep(10000);

			// Call the second REST API method and get the response.
			HttpGet resultRequest = new HttpGet(operationLocation);
			resultRequest.setHeader("Ocp-Apim-Subscription-Key", subscriptionKey);

			HttpResponse resultResponse = httpResultClient.execute(resultRequest);
			HttpEntity responseEntity = resultResponse.getEntity();

			if (responseEntity != null) {
				// Format and display the JSON response.
				String jsonString = EntityUtils.toString(responseEntity);
				JSONObject json = new JSONObject(jsonString);
//				System.out.println("Successfully Analyzed");
				try (FileWriter file2 = new FileWriter("txt.json")) {
					file2.write(json.toString());
				}
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	public static void main(String[] args) {
		new myMain().show();
	}

}
