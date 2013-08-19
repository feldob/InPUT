package se.miun.itm.input.sql.example;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileFilter;
import java.sql.Connection;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import se.miun.itm.input.model.IOInPUTException;
import se.miun.itm.input.model.InPUTException;
import se.miun.itm.input.model.SQLInPUTException;
import se.miun.itm.input.util.sql.DatabaseAdapter;

/**
 * An abstract base class for classes that transfers objects from files to
 * SQL databases and displays a frame with information about the progress.
 * 
 * @NotThreadSafe
 * @author Stefan Karlsson
 */
public abstract class FilesToDatabaseTransferrer<T> {
	
	/**
	 * The filter used for determining which files to import objects from.
	 */
	private FileFilter filter;
	
	/**
	 * A frame that displays the progress of the current transfer.
	 */
	private ProgressFrame progressFrame;
	
	/**
	 * The text displayed by the progress frame.
	 */
	private String frameText;
	
	/**
	 * Constructs a {@code FilesToDatabaseTransferrer} that uses
	 * the given filter to determine which files to import objects
	 * from and displays the given text in the progress frame.
	 * 
	 * @param filter	The filter to use for determining which
	 * 					files to import objects from.
	 * @param frameText	 The text to display in the progress frame.
	 */
	protected FilesToDatabaseTransferrer(FileFilter filter, String frameText) {
		this.filter = filter;
		this.frameText = frameText;
	}
	
	/**
	 * Transfers objects from the files in the given directory that passes the
	 * {@link FileFilter} of this {@code FilesToDatabaseTransferrer} to the
	 * designated database.
	 * 
	 * @param dir	The directory containing the files that objects shall be
	 * 				imported from.
	 * @param conn A connection to the database that objects shall be exported to.
	 * @throws InPUTException If a non-I/O non-database error occurs.
	 * @throws IOInPUTException If a I/O error occurs.
	 * @throws SQLInPUTException If a database error occurs.
	 */
	public void transferFiles(File dir, Connection conn) throws InPUTException, IOInPUTException, SQLInPUTException {
		final File[] files = dir.listFiles(filter);
		DatabaseAdapter adapter = new DatabaseAdapter(conn);
		Runnable frameCreator = new Runnable() {
			public void run() {
				progressFrame = new ProgressFrame(files.length);
				progressFrame.setVisible(true);
			}
		};
		
		if (SwingUtilities.isEventDispatchThread())
			frameCreator.run();
		else
			try {
				SwingUtilities.invokeAndWait(frameCreator);
			} catch (Exception e) {
				throw new InPUTException(e.getMessage(), e);
			}
		
		for (int i = 0; i < files.length; i++) {
			final int progress = Math.round(100f  * i / files.length);
			
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					progressFrame.setValue(progress);
				}
			});
			
			export(impOrt(files[i]), adapter);
		}
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {}
		
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				progressFrame.dispose();
			}
		});
	}
	
	/**
	 * Exports the given object to the designated database.
	 * 
	 * @param t The object to be exported.
	 * @param adapter	A {@code DatabaseAdapter} with a connection to the
	 * 					database that the object shall be exported to.
	 * @throws InPUTException If a non-database export error occurs.
	 * @throws SQLInPUTException If a database error occurs.
	 */
	protected abstract void export(T t, DatabaseAdapter adapter) throws InPUTException, SQLInPUTException;
	
	/**
	 * Imports the object contained by the given file.
	 * 
	 * @param f The file that contains the object.
	 * @return The imported object.
	 * @throws InPUTException If a non-I/O non-database error occurs.
	 * @throws IOInPUTException If a I/O error occurs.
	 * @throws SQLInPUTException If a database error occurs.
	 */
	protected abstract T impOrt(File f) throws InPUTException, IOInPUTException, SQLInPUTException;
	
	/**
	 * A {@code ProgressFrame} displays information about the progress of a transfer.
	 */
	private class ProgressFrame extends JFrame {

		private static final long serialVersionUID = -1551301776334235915L;

		/**
		 * A progress bar.
		 */
		private JProgressBar progressBar;
		
		/**
		 * Constructs a {@code ProgressFrame} with a progress
		 * bar that has the specified maximum value.
		 * 
		 * @param max The maximum value of the progress bar.
		 */
		public ProgressFrame(int max) {
			super("Transferring (0 %)");

			JLabel progressLabel = new JLabel(frameText);
			
			progressLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
			
			progressBar = new JProgressBar(0, max);
			progressBar.setStringPainted(true);
			progressBar.setAlignmentX(Component.CENTER_ALIGNMENT);
			progressBar.setMaximumSize(new Dimension(250, Integer.MAX_VALUE));
			
			setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
			add(progressLabel);
			add(Box.createRigidArea(new Dimension(0, 10)));
			add(progressBar);
			
			pack();
			setResizable(false);
			setLocationRelativeTo(null);
			setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
			addWindowListener(new WindowAdapter() {
				@Override
				public void windowDeiconified(WindowEvent e) {
					progressBar.repaint();
				}
			});
		}
		
		/**
		 * Sets the value of the progress bar of this
		 * {@code ProgressFrame} to the given value.
		 * 
		 * @param val The new value of the progress bar.
		 */
		public void setValue(int val) {
			setTitle("Transferring (" + val + " %)");
			progressBar.setValue(val);
		}
	}
}
