package se.miun.itm.input.model;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.WeakHashMap;

import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import se.miun.itm.input.aspects.Identifiable;
import se.miun.itm.input.export.DocumentExporter;
import se.miun.itm.input.impOrt.XMLFileImporter;
import se.miun.itm.input.model.design.DesignSpace;
import se.miun.itm.input.model.design.IDesignSpace;
import se.miun.itm.input.model.mapping.Framework;
import se.miun.itm.input.model.mapping.IMappings;
import se.miun.itm.input.model.mapping.Mappings;
import se.miun.itm.input.util.Q;

/**
 * An {@code InPUTDocument} wraps an InPUT XML {@link Document}
 * in order to provide convenience methods.
 * 
 * @ThreadSafe
 * @author Stefan Karlsson.
 */
public class InPUTDocument extends Document implements Comparable<Document>, Identifiable {
	
	private static final long serialVersionUID = -6300996286727400082L;

	/**
	 * A cache of XML strings created with the {@code outputXMLString} method.
	 */
	private static Map<Document, String> xmlStrings =
		new WeakHashMap<Document, String>();
	
	/**
	 * Constructs an {@code InPUTDocument} as a wrapper
	 * of the given InPUT XML document.
	 * 
	 * @param doc	An InPUT XML document that this
	 * 				{@code InPUTDocument} shall wrap.
	 * @throws InPUTException	If the given document
	 * 							isn't an InPUT XML
	 * 							document.
	 */
	public InPUTDocument(Document doc) throws InPUTException {
		super(doc.getRootElement().clone(), doc.getDocType(), doc.getBaseURI());

		if (getInPUTType() == null || !isDesign() && !isDesignSpace() && !isMapping())
			throw new InPUTException("The given document isn't an InPUT XML document.");
	}
	
	/**
	 * Creates a XML string representation of the given document,
	 * using a {@link XMLOutputter} with a {@link Format} returned
	 * by the {@code Format.getPrettyFormat} method.
	 * 
	 * @param doc	The document whose XML string representation
	 * 				shall be returned.
	 * @return A XML string representation of the document.
	 */
	public static String outputXMLString(Document doc) {
		String xml = xmlStrings.get(doc);
		
		if (xml == null) {
			xml = new XMLOutputter(Format.getPrettyFormat()).outputString(doc);
			xmlStrings.put(doc, xml);
		}
		
		return xml;
	}
	
	/**
	 * Compares this document to the given one using an
	 * {@link InPUTDocument.Comparator}.
	 * 
	 * @return	A negative integer, zero, or a positive
	 * 			integer as this document is less than,
	 * 			equal to, or greater than the given
	 * 			document.
	 */
	@Override
	public int compareTo(Document other) {
		return new Comparator().compare(this, other);
	}
	
	/**
	 * Return the ID of this document.
	 */
	public String getId() {
		return getRootElement().getAttributeValue(Q.ID_ATTR);
	}
	
	/**
	 * Returns the name of this document's InPUT XML document type.
	 */
	public String getInPUTType() {
		return getRootElement().getName();
	}
	
	/**
	 * If this document is a design space, this method
	 * checks whether it has structural parameters.
	 * Otherwise, it throws an {@link InPUTException}.
	 * 
	 * @return {@code true} if this document has
	 * 			structural parameters, otherwise
	 * 			{@code false}.
	 * @throws InPUTException If this document isn't
	 * 			a design space.
	 */
	public boolean hasStructuralParameters() throws InPUTException {
		
		if (!isDesignSpace())
			throw new InPUTException("The document isn't a design space.");
		
		for (Element e : getRootElement().getChildren())
			if (e.getName().equalsIgnoreCase("SParam")) {
				String type = e.getAttributeValue(Q.TYPE_ATTR);
				
				if (type == null || !type.equalsIgnoreCase("String"))
					return true;
			}
		
		return false;
	}
	
	/**
	 * Checks whether this document is a design.
	 * 
	 * @return	{@code true} if this document is a
	 * 			design, otherwise {@code false}.
	 */
	public boolean isDesign() {
		return getInPUTType().equalsIgnoreCase(Q.DESIGN_ROOT);
	}
	
	/**
	 * Checks whether this document is a design space.
	 * 
	 * @return	{@code true} if this document is a
	 * 			design space, otherwise {@code false}.
	 */
	public boolean isDesignSpace() {
		return getInPUTType().equalsIgnoreCase(Q.DESIGN_SPACE_ROOT);
	}
	
	/**
	 * Checks whether this document is a code-mapping document.
	 * 
	 * @return	{@code true} if this document is a code-mapping document,
	 * 			otherwise {@code false}.
	 */
	public boolean isMapping() {
		return getInPUTType().equalsIgnoreCase(Q.MAPPINGS);
	}
	
	/**
	 * If this document is a design or code-mapping document, this method searches
	 * for its design space. Otherwise, an {@link InPUTException} is thrown.
	 * 
	 * @return	The design space document if it's found, otherwise {@code null}.
	 * @throws InPUTException	If this document isn't a design or code-mapping
	 * 							document.
	 * @throws IOInPUTException If an I/O error occurs.
	 */
	public InPUTDocument locateDesignSpace() throws InPUTException, IOInPUTException {
		IDesignSpace space;
		String	fullRef = getRootElement().getAttributeValue(Q.REF_ATTR),
				thisID = getId();
		
		if (!isDesign() && !isMapping())
			throw new InPUTException(
				"The document isn't a design or code-mapping document.");
		
		space = DesignSpace.lookup(thisID);
		
		if (space == null && fullRef != null) {
			String baseRef = fullRef.split("\\.")[0];
				
			space = DesignSpace.lookup(baseRef);
				
			if (space == null && new File(fullRef).exists()) {
				InPUTDocument spaceDoc = new InPUTDocument(
					new XMLFileImporter(fullRef).impOrt());
					
				if (spaceDoc.isDesignSpace())
					return spaceDoc;
			}
		}
		
		return space == null ? null: new InPUTDocument(
				space.export(new DocumentExporter()));
	}
	
	/**
	 * If this document is a design space, this method searches for its
	 * code mappings. Otherwise, an {@link InPUTException} is thrown.
	 * 
	 * @return	A set containing the found code-mapping documents.
	 * @throws InPUTException	If this document isn't a design space.
	 * @throws IOInPUTException If an I/O error occurs.
	 */
	public SortedSet<InPUTDocument> locateMappings() throws InPUTException {
		DocumentExporter exporter = new DocumentExporter();
		List<IMappings> mappings = new ArrayList<IMappings>();
		NavigableSet<InPUTDocument> mappingDocs = new TreeSet<InPUTDocument>();
		String completeFilename = getRootElement().getAttributeValue(Q.MAPPING_ATTR);
		String id = getId();
		
		if (!isDesignSpace())
			throw new InPUTException("The document isn't a design space.");
		
		mappings.add(Mappings.getInstance(id));

		if (completeFilename != null) {
			String baseFilename = completeFilename.split("\\.")[0];

			mappings.add(Mappings.getInstance(baseFilename));
			
			if (new File(completeFilename).exists()) {
				InPUTDocument doc = new InPUTDocument(new XMLFileImporter(
					completeFilename).impOrt());
				
				if (doc.isMapping())
					mappingDocs.add(doc);
			}
		}
		
		for (IMappings m : mappings)
			if (m != null)
				mappingDocs.add(new InPUTDocument(m.export(exporter)));
		
		for (Document doc : Framework.mappingsOf(this))
			mappingDocs.add(new InPUTDocument(doc));
		
		return mappingDocs;
	}
	
	/**
	 * Returns a XML string representation of this document,
	 * created with the {@code outputXMLString} method.
	 * 
	 * @return A XML string representation of this document.
	 */
	@Override
	public String toString() {
		return outputXMLString(this);
	}
	
	/**
	 * Used for comparing {@link Document}:s primary by ID and secondary
	 * by XML string representation. The comparisons are performed
	 * lexicographically with the {@code String.compareTo} method, and
	 * the string representations are created with the
	 * {@code outputXMLString} method.
	 * 
	 * @author Stefan Karlsson
	 */
	public static class Comparator implements java.util.Comparator<Document> {
		
		/**
		 * Compares the given documents primary by ID and secondary by
		 * XML string representation. The comparisons are performed
		 * lexicographically with the {@code String.compareTo} method,
		 * and the string representations are created with the
		 * {@code outputXMLString} method.
		 * 
		 * @param doc1 The first document to be compared.
		 * @param doc2 The second document to be compared.
		 * @return	A negative integer, zero, or a positive integer as
		 * 			the first document is less than, equal to, or
		 * 			greater than the second document.
		 */
		public int compare(Document doc1, Document doc2) {
			int comparisonValue =
				doc1.getRootElement().getAttributeValue(Q.ID_ATTR).compareTo(
				doc2.getRootElement().getAttributeValue(Q.ID_ATTR));
			
			return comparisonValue != 0 ? comparisonValue :
					outputXMLString(doc1).compareTo(outputXMLString(doc2));
		}
	}
}
