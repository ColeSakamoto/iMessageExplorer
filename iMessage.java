package iMessageReader;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.Statement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class iMessage {

	JTextArea tbox = new JTextArea();
	String input;
	String pstate = null;
	Connection conn = null;
	Statement stmt = null;
	String sql;

	public iMessage() {
		GUI();
	}

	public void GUI() {
		JFrame frame = new JFrame("iMessage Reader");
		// frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent we) {
				System.exit(0);
				try {
					conn.close();

				} catch (SQLException e1) {
					e1.printStackTrace();
				}
			}
		});
		frame.setSize(700, 700);

		JMenuBar mb = new JMenuBar();
		JMenu m1 = new JMenu("File");
		JMenu m2 = new JMenu("Path");
		JMenu m3 = new JMenu("Help");
		mb.add(m1);
		mb.add(m2);
		mb.add(m3);

		JMenuItem m11 = new JMenuItem("Open");
		JMenuItem m22 = new JMenuItem("Save as");
		JMenuItem m33 = new JMenuItem("New path");
		m1.add(m11);
		m1.add(m22);
		m2.add(m33);

		JPanel panel = new JPanel();
		JLabel label = new JLabel("Enter Text");
		JTextField tf = new JTextField(10);

		JButton send = new JButton("Send");
		JButton key = new JButton("Keyword");
		JButton reset = new JButton("Reset");
		panel.add(label); // Components Added using Flow Layout
		panel.add(tf);
		panel.add(send);
		panel.add(key);
		panel.add(reset);
		JScrollPane scroll = new JScrollPane(tbox, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		tbox.setEditable(false);
		frame.getContentPane().add(BorderLayout.SOUTH, panel);
		frame.getContentPane().add(BorderLayout.NORTH, mb);
		frame.getContentPane().add(BorderLayout.CENTER, scroll);

		frame.setVisible(true);

		m33.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				addItem("Enter SQLite DB path");
				pstate = "path";
			}
		});
		send.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				if (pstate.equals("path")) { // Open DB List conversations
					pstate = "path1";

					lsConvos(tf.getText());

				} else if (pstate.equals("capture")) { // Enter conversation
					opConvo(tf.getText());
				} else if (pstate.equals("key")) {
					keyConvos(tf.getText());
				}
				else {
					try {
						if (conn != null) {
							conn.close();
							clear();
							addItem("Database closed");
							pstate = "path";
						}
					} catch (SQLException e1) {

						e1.printStackTrace();
					}
				}

			}
		});
		key.addActionListener(new ActionListener() {  //Find keyword
			public void actionPerformed(ActionEvent e) {

				if (conn != null) { // Open DB List conversations
					addItem("Enter ID and keyword: ID,keyword");
					pstate = "key";
				} else {
					addItem("Open DB first");
				}
			}
		});
	}
	public void keyConvos(String input) {
	int id;
	String key;
	
	String[] values = input.split(",");
	 id = Integer.valueOf(values[0]);
	 key = values[1];
	 
	 sql = "SELECT text, datetime(message.date/1000000000 + strftime(\"%s\", \"2001-01-01\") ,\"unixepoch\",\"localtime\") as date FROM MESSAGE WHERE HANDLE_ID ="+ id +" AND TEXT LIKE '%"+key+"%'";
	 try {
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			addItem(String.format("%-22s%-22s\n", "Message", "Date"));
			while (rs.next()) {
				addItem(String.format("%-22s%-22s\n", rs.getString("text"), rs.getString("date")));
			}
			
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
	}
	public void lsConvos(String name) {
		/// Users/ColeSakamoto/Desktop/chats.db
		String url = "jdbc:sqlite:/Users/ColeSakamoto/Desktop/chats.db";
		addItem(url);
		try {
			conn = DriverManager.getConnection(url);
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		if (conn != null) {
			clear();
			pstate = "list";

			sql = "SELECT * FROM HANDLE";

			try {
				stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(sql);
				addItem(String.format("%-22s%-22s%-22s\n", "RowID", "Number", "Service"));
				while (rs.next()) {
					addItem(String.format("%-22s%-22s%-22s\n", rs.getString("rowid"), rs.getString("id"),
							rs.getString("service")));
				}
				addItem("");
				addItem("Enter a conversation number");
				pstate = "capture";
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		}
	}

	public void opConvo(String number) {
		sql = "SELECT ROWID, TEXT, datetime(message.date/1000000000 + strftime(\"%s\", \"2001-01-01\") ,\"unixepoch\",\"localtime\")  as date FROM MESSAGE WHERE HANDLE_ID = "
				+ number + " LIMIT 60";
		try {
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(sql);

			addItem(String.format("%-22s%-22s%-22s\n", "RowID", "Text", "Date"));
			while (rs.next()) {

				addItem(String.format("%-22s%-22s%-22s\n", rs.getString("rowid"), rs.getString("text"),
						rs.getString("date")));
			}
		} catch (SQLException e1) {
			e1.printStackTrace();
		}

	}

	public void addItem(String item) {
		tbox.append(item + "\n");

	}

	public void clear() {
		tbox.setText(null);

	}

}
