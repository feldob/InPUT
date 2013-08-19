package se.miun.itm.input.sql.example.query;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import se.miun.itm.input.model.SQLInPUTException;
import se.miun.itm.input.util.sql.DatabaseAdapter;

/**
 * Used for querying SQL databases.
 * 
 * @NotThreadSafe
 * @author Stefan Karlsson
 */
public class DatabaseQuerier {
	
	/**
	 * A {@link JFrame} used to display the queries and their results.
	 */
	private QueryFrame queryFrame;
	
	/**
	 * Queries the designated database.
	 * 
	 * @param conn A connection to the database that shall be queried.
	 * @param queries The queries to send to the database.
	 * @throws SQLInPUTException If a database error occurs.
	 */
	public void query(Connection conn, String ... queries) throws SQLInPUTException {
		Statement stmt = null;
		
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				queryFrame = new QueryFrame();
				queryFrame.setVisible(true);
			}
		});
		
		try {
			stmt = conn.createStatement();
			
			for (final String QUERY : queries) {
				final String RESULT;
				
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						queryFrame.addHTMLText("<h3>Executing the follwing SQL statement:</h3>");
						queryFrame.addPlainText(QUERY);
					}
				});

				if (stmt.execute(QUERY)) {
					int columnCount;
					HTMLTable table = new HTMLTable(true);
					ResultSet rs = stmt.getResultSet();
					ResultSetMetaData metaData = rs.getMetaData();
					
					columnCount = metaData.getColumnCount();
			
					for (int i = 1; i <= columnCount; i++)
						table.addHeader(metaData.getColumnLabel(i));
			
					while (rs.next()) {
						table.newRow();
				
						for (int i = 1; i <= columnCount; i++)
							table.addCell(rs.getString(i));
					}
			
					
					RESULT = "<h3>Result:</h3><p>" + table.toString() + "</p>";
				} else {
					int updateCount = stmt.getUpdateCount();
					
					RESULT = "<h3>" + (updateCount == -1 ? "Success!" :
						"Number of rows affected: " + updateCount) + "</h3>";
				}
				
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						queryFrame.addHTMLText(RESULT + "</p><p><hr width=\"2000\"></p>");
					}
				});
			}
		} catch (SQLException e) {
			throw new SQLInPUTException(DatabaseAdapter.concatSQLMessages(e), e);
		} finally {
			DatabaseAdapter.close(stmt);
		}
			
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				queryFrame.done();
			}
		});
	}
	
	/**
	 * A {@code QueryFrame} is a {@link JFrame} that
	 * displays the queries and their results.
	 */
	private static class QueryFrame extends JFrame {
		
		static final long serialVersionUID = -7774589477098965167L;
		
		/**
		 * A {@code JLabel} used as heading for this {@code QueryFrame}.
		 */
		JLabel headingLabel = new JLabel("Querying the database...");
		
		/**
		 * The {@link Component} displaying the queries and their results.
		 */
		JPanel queryPanel = new JPanel();
		
		/**
		 * A progress bar.
		 */
		JProgressBar progressBar = new JProgressBar();
		
		/**
		 * Constructs a {@code QueryFrame}.
		 */
		QueryFrame() {
			super("DatabaseQuerier");
			
			JScrollPane scrollPane = new JScrollPane(queryPanel);
			
			headingLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
			
			progressBar.setIndeterminate(true);
			progressBar.setAlignmentX(Component.CENTER_ALIGNMENT);
			progressBar.setMaximumSize(new Dimension(250, 15));
			
			queryPanel.setLayout(new BoxLayout(queryPanel, BoxLayout.Y_AXIS));
			
			setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
			add(headingLabel);
			add(Box.createRigidArea(new Dimension(0, 10)));
			add(progressBar);
			add(Box.createRigidArea(new Dimension(0, 10)));
			add(scrollPane);
			
			setMinimumSize(new Dimension(300, 200));
			setSize(new Dimension(950, 400));
			setLocationRelativeTo(null);
			setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
			addWindowListener(new WindowAdapter() {
				@Override
				public void windowDeiconified(WindowEvent e) {
					queryPanel.revalidate();
				}
			});
		}
		
		/**
		 * Encloses given text in a HTML body and adds it to the
		 * end of text containing the queries and their results.
		 * 
		 * @param text The HTML text to be added.
		 */
		void addHTMLText(String text) {
			JLabel queryLabel = new JLabel("<html><body>" + text + "</body></html>");
			
			queryLabel.setBackground(Color.WHITE);
			queryLabel.setOpaque(true);
			queryLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
			
			queryPanel.add(queryLabel);
			queryPanel.revalidate();
		}

		/**
		 * Adds the given plain text to the end of the
		 * text containing the queries and their results.
		 * 
		 * @param text The plain text to be added.
		 */
		void addPlainText(String text) {
			Font font;
			JTextArea queryArea = new JTextArea(text);
			
			font = queryArea.getFont();

			queryArea.setFont(new Font("Monospaced", Font.BOLD, font.getSize()));
			queryArea.setEditable(false);
			queryArea.setAlignmentX(Component.LEFT_ALIGNMENT);
			
			queryPanel.add(queryArea);
			queryPanel.revalidate();
		}
		
		/**
		 * Alters this {@code QueryFrame} to reflect
		 * that all the queries has been executed.
		 */
		void done() {
			headingLabel.setText(headingLabel.getText() + " Done!");
			progressBar.setIndeterminate(false);
			progressBar.setValue(progressBar.getMaximum());
			progressBar.setStringPainted(true);
		}
	}
	
	/**
	 * Used for building HTML table {@code String}:s.
	 */
	private static class HTMLTable {
		
		/**
		 * A list of the data rows of this table.
		 */
		List<List<Object>> dataRows = new ArrayList<List<Object>>();
		
		/**
		 * A list of the headers of this table.
		 */
		List<String> headers = new ArrayList<String>();
		
		/**
		 * The value of the border attribute of this table.
		 */
		String border;
		
		/**
		 * Constructs a {@code HTMLTable}.
		 * 
		 * @param hasBorders	{@code true} if the table cells shall
		 * 						have borders, otherwise {@code false}.
		 */
		HTMLTable(boolean hasBorders) {
			border = hasBorders ? "1" : "";
		}
		
		/**
		 * Adds a cell containing a string representation of the given
		 * data to the last row of this table. The string representation
		 * of the data is created using the {@code toString()} method.
		 * 
		 * @param data	The data whose string representation shall be
		 * 				contained by the cell.
		 * @throws IllegalStateException	If the {@code newRow} method
		 * 									hasn't been called.
		 */
		void addCell(Object data) {
			
			if (dataRows.size() == 0)
				throw new IllegalStateException(
					"The 'newRow' method hasn't been called.");
			
			dataRows.get(dataRows.size() - 1).add(data);
		}
		
		/**
		 * Adds the given header to this table.
		 * 
		 * @param header The header to be added.
		 */
		void addHeader(String header) {
			headers.add(header);
		}
		
		/**
		 * Adds an empty row to this table.
		 */
		void newRow() {
			dataRows.add(new ArrayList<Object>());
		}
		
		/**
		 * Returns a string representation of this table.
		 */
		@Override
		public String toString() {
			StringBuilder tableBuilder = new StringBuilder(100);
			
			tableBuilder.append("<table border=\"");
			tableBuilder.append(border);
			tableBuilder.append("\"><tr>");
			
			for (String h : headers) {
				tableBuilder.append("<th>");
				tableBuilder.append(h);
				tableBuilder.append("</th>");
			}
			
			tableBuilder.append("</tr>");
			
			for (List<Object> row : dataRows) {
				tableBuilder.append("<tr>");
				
				for (Object data : row) {
					tableBuilder.append("<td>");
					tableBuilder.append(data);
					tableBuilder.append("</td>");
				}
				
				tableBuilder.append("</tr>");
			}
			
			tableBuilder.append("</table>");
			
			return tableBuilder.toString();
		}
	}
}
