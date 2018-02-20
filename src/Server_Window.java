import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class Server_Window extends JFrame {
	
	public static JFrame frame = new JFrame("QplayServer");
	public static JLabel lb_title = new JLabel("Server Status");
	public static JTextArea ta_content = new JTextArea();
	public static JButton btn_exit = new JButton("EXIT");
	public static JScrollPane ta_content_scroll = new JScrollPane(ta_content,  JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
	
	private static final int WIDTH = 500;
	private static final int HEIGHT = 800;
	
	public Server_Window() {
		init_set();
	}
	
	public void init_set() {
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

		int WINDOW_SIZEX = (int) screenSize.getWidth();
		int WINDOW_SIZEY = (int) screenSize.getHeight();
	
		frame.setSize(500,700);
		frame.setResizable(false);
		frame.setLayout(null);
		frame.setLocation((WINDOW_SIZEX - WIDTH) / 2, (WINDOW_SIZEY - HEIGHT) / 2);
		frame.add(lb_title);
		frame.add(btn_exit);
//		frame.add(ta_content);
		frame.add(ta_content_scroll);

		lb_title.setBounds(200, 10, 155, 60);
		ta_content_scroll.setBounds(75, 75, 350, 500);
//		ta_content.setBounds(75, 75, 350, 500);
		btn_exit.setBounds(100, 600, 300, 60);
		
		ta_content.setEditable(false);
		
		 btn_exit.addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent e) {
					
					System.exit(1);
				}
			 });
		 
		 frame.setBackground(Color.white);
		 frame.setVisible(true);		
	}
	
	public void say(String msg) {
		String text = msg + "\n";
		ta_content.append(text);
	}
}
