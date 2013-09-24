package se.miun.itm.input.export;

import se.miun.itm.input.aspects.Exportable;
import se.miun.itm.input.model.Document;

/**
 * Used for exporting {@link Document}:s to {@code Document}:s
 * in order to access {@code Document}:s of {@link Exportable}:s.
 * 
 * @ThreadSafe
 * @author Stefan Karlsson
 */
public class DocumentExporter implements InPUTExporter<Document> {
	
	/**
	 * Returns the given document.
	 * 
	 * @param doc The document that shall be returned.
	 * @return The given document.
	 */
	public Document export(Document doc) {
		return doc;
	}

	/**
	 * Throws an {@link UnsupportedOperationException}.
	 */
	public Document export(Exportable exportable) {
		throw new UnsupportedOperationException(
			"Export of 'Exportable' objects is unsupported by 'DocumentExporter'.");
	}

	
	/**
	 * Returns export details about this {@code DocumentExporter}.
	 */
	public String getInfo() {
		return	"Exports 'Document' objects to 'Document' objects.";
	}
}
