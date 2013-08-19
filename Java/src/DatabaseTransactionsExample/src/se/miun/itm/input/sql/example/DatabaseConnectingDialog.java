package se.miun.itm.input.sql.example;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import se.miun.itm.input.util.sql.DatabaseAdapter;

/**
 * A {@code DatabaseConnectingDialog} is a modal {@link JDialog}
 * that prompts the user for a SQL database URL and credentials,
 * and then connects to the designated database.
 * 
 * @NotThreadSafe
 * @author Stefan Karlsson
 */
public class DatabaseConnectingDialog extends JDialog {

	private static final long serialVersionUID = -3707532989426480258L;
	
	/**
	 * The default number of columns in the {@link JTextField}:s
	 * of {@code DatabaseConnectingDialog}:s.
	 */
	private static final int FIELD_LENGTH = 10;
	
	/**
	 * A database connection.
	 */
	private Connection conn;
	
	/**
	 * The text field for entering the password
	 * of the database user account.
	 */
	private JTextField	passwordField = new JPasswordField(FIELD_LENGTH);
	
	/**
	 * The text field for entering the database URL.
	 */
	private JTextField	urlField = new JTextField(FIELD_LENGTH);
	
	/**
	 * The text field for entering the username
	 * of the database user account.
	 */
	private JTextField	usernameField = new JTextField(FIELD_LENGTH);
	
	/**
	 * Creates and shows a {@code DatabaseConnectingDialog}.
	 * 
	 * @return	The established database connection.
	 */
	public static Connection connect() {
		DatabaseConnectingDialog dialog = new DatabaseConnectingDialog();
		dialog.setVisible(true);
		return dialog.getConnection();
	}
	
	/**
	 * Constructs a {@code DatabaseConnectingDialog}.
	 */
	public DatabaseConnectingDialog() {
		super((Dialog) null, "Connect to database", true);
		
		JButton	connectButton = new JButton("Connect");
		JLabel	passwordLabel = new JLabel("Password:   "),
				urlLabel = new JLabel("Database URL:   "),
				usernameLabel = new JLabel("Username:   ");
		JPanel panel = new JPanel(new GridLayout(3, 2));

		connectButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		connectButton.addActionListener(new ConnectButtonListener());

		urlField.setToolTipText(
			"jdbc:<subprotocol>:<subname>");
		usernameField.setToolTipText(
			"Leave the field blank if an username isn't required");
		passwordField.setToolTipText(
			"Leave the field blank if a password isn't required");
	
		panel.add(urlLabel);
		panel.add(urlField);
		panel.add(usernameLabel);
		panel.add(usernameField);
		panel.add(passwordLabel);
		panel.add(passwordField);

		setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
		add(panel);
		add(Box.createRigidArea(new Dimension(0, 10)));
		add(connectButton);

		pack();
		setResizable(false);
		setLocationRelativeTo(null);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
	}
	
	/**
	 * Returns the established database connection, or
	 * {@code null} if no connection has been established.
	 */
	public Connection getConnection() {
		return conn;
	}
	
	/**
	 * An {@link ActionListener} that listens to the "Connect" {@link JButton}
	 * of this {@code DatabaseConnectingDialog}. When the button is pressed,
	 * the listener attempts to connect to the designated database using the
	 * given credentials. If the attempt succeeds, the dialog is disposed.
	 * Otherwise, an error message is added to the dialog.
	 */
	private class ConnectButtonListener implements ActionListener {
		/**
		 * A text area that displays error messages.
		 */
		JTextArea errorArea;
		
		
		public void actionPerformed(ActionEvent e) {
			String	databaseURL,
			errorMessage = null,
			password,
			username;

			databaseURL = urlField.getText();
				
			if (databaseURL.isEmpty()) {
				errorMessage = "You must enter a database URL";
			} else {
				username = usernameField.getText();
				password = passwordField.getText();
					
				try {
					conn = username.isEmpty() || password.isEmpty() ?
							DriverManager.getConnection(databaseURL) :
							DriverManager.getConnection(databaseURL,
								username, password);
					dispose();
					
					return;
				} catch (SQLException ex) {
					errorMessage = DatabaseAdapter.concatSQLMessages(ex);
				}
			}
			
			if (errorArea == null) {
				Font font;
				
				errorArea = new JTextArea();
				font = errorArea.getFont();
				errorArea.setFont(new Font(font.getFontName(),
					Font.BOLD | Font.ITALIC, font.getSize()));
				errorArea.setForeground(Color.RED);
				errorArea.setLineWrap(true);
				errorArea.setWrapStyleWord(true);
				errorArea.setOpaque(false);
				errorArea.setEditable(false);
	
				add(Box.createRigidArea(new Dimension(0, 10)));
				add(errorArea);
			}
		
			errorArea.setText(errorMessage);
			
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					pack();
				}
			});
		}
	}
}
