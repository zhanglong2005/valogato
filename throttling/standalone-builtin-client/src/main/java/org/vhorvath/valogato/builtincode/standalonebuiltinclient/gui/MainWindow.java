package org.vhorvath.valogato.builtincode.standalonebuiltinclient.gui;


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.vhorvath.valogato.builtincode.standalonebuiltinclient.service.client.WebserviceClient;


public class MainWindow extends JFrame {

	
	public MainWindow() {
		initUI();
	}
	
	
	private void initUI() {
		setTitle("Standalon sample for the Valogato system");
		setSize(300, 200);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		
		JPanel panel = new JPanel();
	    getContentPane().add(panel);
	    panel.setLayout(null);
	    
		JButton button = new JButton("Call the backend service");
		button.setBounds(50, 60, 180, 30);
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new WebserviceClient().call();
			}
		});
		
		panel.add(button);
	}


	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				new MainWindow().setVisible(true);
			}
		});
	}
}
