package se.miun.itm.input.model.mapping;

import java.util.AbstractSet;
import java.util.Arrays;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.NavigableSet;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeSet;

import org.jdom2.Element;

import se.miun.itm.input.aspects.Exportable;
import se.miun.itm.input.aspects.Identifiable;
import se.miun.itm.input.export.InPUTExporter;
import se.miun.itm.input.model.Document;
import se.miun.itm.input.model.InPUTDocument;
import se.miun.itm.input.model.InPUTException;
import se.miun.itm.input.util.Q;

/**
 * A {@code Framework} is a {@link Set} of code-mapping {@link Document}:s.
 * 
 * @ThreadSafe
 * @author Stefan Karlsson
 */
public final class Framework extends AbstractSet<Document> implements Exportable, Identifiable {
	
	/**
	 * Maps every framework ID to the framework to which it belongs.
	 */
	private static Map<String, Framework> frameworks = new HashMap<String, Framework>();
	
	/**
	 * A {@link Set} of all code-mapping documents that are part of a framework.
	 * The documents are wrapped in {@code DocumentWrapper}:s.
	 */
	/* The documents are stored in one single data structure in order to simplify
	 * lookups that involves more than one framework. */
	private static NavigableSet<DocumentWrapper> documents =
		new TreeSet<DocumentWrapper>();
	
	/**
	 * {@code true} if this framework has been released, otherwise {@code false}.
	 */
	private volatile boolean isReleased = false;
	
	/**
	 * The number of times this framework has been structurally modified.
	 */
	private volatile int modCount;
	
	/**
	 * The number of code-mapping documents in this framework.
	 */
	private volatile int size;
	
	/**
	 * A dummy {@link DocumentWrapper} that is always first in this
	 * framework's sequence of linked {@code DocumentWrapper}:s.
	 */
	private DocumentWrapper head;
	
	
	/**
	 * A dummy {@link DocumentWrapper} that is always last in this
	 * framework's sequence of linked {@code DocumentWrapper}:s.
	 */
	private DocumentWrapper tail;
	
	/**
	 * The ID of this framework.
	 */
	private String id;
	
	/**
	 * Constructs a {@code Framework} with the specified ID. Each given document
	 * is added to this framework provided that it's a well-formed code-mapping
	 * document that isn't already part of another framework.
	 * 
	 * @param id The ID of this framework.
	 * @param documents	 Code-mapping documents to be added to this framework.
	 * @throws IllegalArgumentException		If the ID is already taken, or if
	 * 										any of the documents isn't a
	 * 										well-formed code-mapping document.
	 * @throws NullPointerException If any of the documents is {@code null}.
	 */
	public Framework(String id, Document ... documents) {
		this(id, Arrays.asList(documents));
	}
	
	/**
	 * Constructs a {@code Framework} with the specified ID. Each given document
	 * is added to this framework provided that it's a well-formed code-mapping
	 * document that isn't already part of another framework.
	 * 
	 * @param id The ID of this framework.
	 * @param c		A collection containing code-mapping documents that shall be
	 * 				added to this framework.
	 * @throws IllegalArgumentException		If the ID is already taken, or if
	 * 										any of the documents isn't a
	 * 										well-formed code-mapping document.
	 * @throws NullPointerException If any of the documents is {@code null}.
	 */
	public Framework(String id, Collection<? extends Document> c) {
		head = new DocumentWrapper(null, this, null, null);
		tail = new DocumentWrapper(null, this, head, null);
		head.next = tail;
		
		this.id = id;
		
		synchronized (Framework.class) {
			
			if (frameworks.containsKey(id))
				throw new IllegalArgumentException(
					"The framework ID is already taken.");
			
			frameworks.put(id, this);
			addAll(c);
		}
	}
	
	/**
	 * Returns the framework of the given code-mapping document, or {@code null}
	 * if it isn't part of any framework.
	 * 
	 * @param doc	The code-mapping document whose framework shall be returned.
	 */
	public static Framework frameworkOf(Document doc) {
		DocumentWrapper resultWrapper, searchWrapper = new DocumentWrapper(doc);
		
		synchronized (Framework.class) {
			resultWrapper = documents.ceiling(searchWrapper);
			
			return resultWrapper != null && resultWrapper.equals(searchWrapper) ?
					resultWrapper.framework : null;
		}
	}
	
	/**
	 * Returns the specified framework, or {@code null} if there
	 * is no such framework.
	 * 
	 * @param id The ID of the framework that shall be returned.
	 */
	public static Framework getInstance(String id) {

		synchronized (Framework.class) {
			return frameworks.get(id);
		}
	}
	
	/**
	 * Returns all of the given design space document's code-mapping
	 * documents that are part of a framework.
	 * 
	 * @param doc	The design space document whose non-frameworkless
	 * 				code-mapping documents shall be returned.
	 */
	public static Set<Document> mappingsOf(Document doc) {
		Element root = doc.getRootElement();
		Set<Document> mappingDocs = new HashSet<Document>();
		
		if (!root.getName().equalsIgnoreCase(Q.DESIGN_SPACE_ROOT))
			throw new IllegalArgumentException(
				"The given document isn't a design space document");
		
		synchronized (Framework.class) {
			for (String id : new String[] {root.getAttributeValue(Q.ID_ATTR),
				root.getAttributeValue(Q.MAPPING_ATTR, " ").split("\\.")[0]})
			{
				for (DocumentWrapper w : documents.tailSet(new DocumentWrapper(new Document(
					new Element("Dummy").setAttribute(Q.ID_ATTR, id.substring(0, id.length() - 1)))),
					false))
				{
					int comparisonValue = w.doc.getRootElement().
						getAttributeValue(Q.ID_ATTR).compareTo(id);

					if (comparisonValue == 0)
						mappingDocs.add(w.doc);
					else if (comparisonValue > 0)
						break;
				}
			}
		}
		
		
		return	mappingDocs;
	}
	
	/**
	 * Releases the specified framework, making it inaccessible via the {@code getInstance}
	 * method. In addition, all elements are removed from it and any subsequent attempt to
	 * add new elements will result in an {@link IllegalStateException}.
	 * 
	 * @param id The ID of the framework that shall be released.
	 * @return	{@code true} if the framework was released, or {@code false} if there was
	 * 			no framework with the given ID.
	 */
	public static boolean release(String id) {
		
		synchronized (Framework.class) {
			Framework fw = frameworks.remove(id);
			
			if (fw == null)
				return false;
			
			fw.clear();
			fw.isReleased = true;
		}
		
		return true;
	}
	
	/**
	 * Releases all frameworks, making them inaccessible via the {@code getInstance}
	 * method. In addition, all elements are removed from them and any subsequent attempt
	 * to add new elements to any of them will result in an {@link IllegalStateException}.
	 */
	public static void releaseAll() {

		synchronized (Framework.class) {
			
			for (Framework fw : frameworks.values()) {
				fw.clear();
				fw.isReleased = true;
			}
			
			frameworks.clear();
		}
	}
	
	/**
	 * Adds the given document to this framework provided that it's a well-formed
	 * code-mapping document that isn't already part of a framework.
	 * 
	 * @param doc The document to be added to this framework.
	 * @return {@code true} if the document was added, otherwise {@code false}.
	 * @throws IllegalArgumentException		If the document isn't a well-formed
	 * 										code-mapping document.
	 * @throws IllegalStateException If this framework has been released.
	 * @throws NullPointerException If the document is {@code null}.
	 */
	@Override
	public boolean add(Document doc) {
		RuntimeException ex = check(doc);
		
		if (ex != null)
			throw ex;
		
		synchronized (Framework.class) {
			
			if (isReleased)
				throw new IllegalStateException("The framework has been released.");
			
			return uncheckedAdd(doc);
		}
	}
	
	/**
	 * Adds each document in the given collection to this framework provided that it's
	 * a well-formed code-mapping document that isn't already part of a framework.
	 * 
	 * @param c		The collection containing the code-mapping documents that shall be
	 * 				added to this framework.
	 * @return {@code true} if any of the documents was added, otherwise {@code false}.
	 * @throws IllegalArgumentException		If any of the documents isn't a well-formed
	 * 										code-mapping document.
	 * @throws NullPointerException If any of the documents is {@code null}.
	 */
	@Override
	public boolean addAll(Collection<? extends Document> c) {
		boolean modified = false;
		
		for (Document doc : c) {
			RuntimeException ex = check(doc);
			
			if (ex != null)
				throw ex;
		}
		
		synchronized (Framework.class) {
			
			if (isReleased)
				throw new IllegalStateException("The framework has been released.");
			
			for (Document doc : c)
				modified |= uncheckedAdd(doc);
		}
		
		return modified;
	}
	
	/**
	 * Checks whether the given document is a well-formed code-mapping document.
	 * 
	 * @param doc The document to be checked.
	 * @return	{@code null} if the document is a well-formed code-mapping document,
	 * 			otherwise a {@code RuntimeException} whose detail message describes
	 * 			what's wrong with the document.
	 */
	private RuntimeException check(Document doc) {
		Element root;
		String id;
		
		if (doc == null)
			return new NullPointerException(
				"Frameworks doesn't permit null elements.");
		
		root = doc.getRootElement();
		
		if (!root.getName().equalsIgnoreCase(Q.MAPPINGS))
			return new IllegalArgumentException(
				"The document isn't a code-mapping document.");
		
		id = root.getAttributeValue(Q.ID_ATTR);
		
		if (id == null)
			return new IllegalArgumentException(
				"The root element of the document must have " +
				"an attribute named \"" + Q.ID_ATTR + "\".");
		
		if (id.isEmpty())
			return new IllegalArgumentException(
				"The value of the attribute \"" + Q.ID_ATTR + "\" of " +
				"the document's root element cannot be an empty string.");
		
		return null;
	}
	
	@Override
	public boolean contains(Object o) {
		return o instanceof Document && frameworkOf((Document) o) == this;
	}
	
	@Override
	public void clear() {

		synchronized (Framework.class) {
			
			for (Document doc : this)
				documents.remove(new DocumentWrapper(doc));
			
			head.next = tail;
			tail.prev = head;
			
			size = 0;
			modCount++;
		}
	}
	
	@Override
	public boolean equals(Object o) {

		synchronized (Framework.class) {
			return	super.equals(o);
		}
	}
	
	public <O> O export(InPUTExporter<O> exporter) throws InPUTException {
		
		synchronized (Framework.class) {
			return exporter.export(this);
		}
	}
	
	public String getId() {
		return id;
	}

	@Override
	public int hashCode() {
		
		synchronized (Framework.class) {
			return super.hashCode();
		}
	}

	/**
	 * Returns {@code true} if this framework has been released,
	 * otherwise {@code false}.
	 */
	public boolean isReleased() {
		return isReleased;
	}
	
	/**
	 * Returns an iterator over the code-mapping documents that are part of this framework.
	 * This method and the methods of the iterator are only thread-safe if they are called
	 * in a synchronized block in which the framework {@link Class} object
	 * ({@code Framework.class}) is locked:
	 * 
	 * <pre>
	 * synchronized (Framework.class) {
     *     for (Iterator it = myFramework.iterator(); it.hasNext();) {
     *         Document doc = it.next();
     *         ...
     *     }
     * }
     * </pre>
	 */
	@Override
	public Iterator<Document> iterator() {
		return new Iterator<Document>() {
			int expectedModCount = modCount;
			DocumentWrapper current = head;
			
			public boolean hasNext() {

				if (expectedModCount != modCount)
					throw new ConcurrentModificationException(
						"The framework has been concurrently modified.");
					
				return current.next != tail;
			}

			public Document next() {
				
				if (!hasNext())
					throw new NoSuchElementException(
						"The iteration has no more elements.");
				
				current = current.next;

				return current.doc;
			}

			public void remove() {
				
				if (expectedModCount != modCount)
					throw new ConcurrentModificationException(
						"The framework has been concurrently modified.");
				
				if (current.doc == null)
					throw new IllegalStateException(
						"The 'next' method has not yet been called, " +
						"or the 'remove' method has already been called " +
						"after the last call to the 'next' method.");
				
				Framework.this.remove(current);
				current.doc = null;
				expectedModCount++;
			}
		};
	}

	/**
	 * Releases this framework, making it inaccessible via the {@code getInstance}
	 * method. In addition, all elements are removed from it and any subsequent
	 * attempt to add new elements will result in an {@link IllegalStateException}.
	 * 
	 * @return	{@code true} if the framework wasn't already released, otherwise
	 * 			{@code false}.
	 */
	public boolean release() {
		
		synchronized (Framework.class) {
			return isReleased ? false : release(id);
		}
	}
	
	@Override
	public boolean remove(Object o) {
		DocumentWrapper resultWrapper, searchWrapper;
		
		if (!(o instanceof Document))
			return false;
		
		searchWrapper = new DocumentWrapper((Document) o);
		
		synchronized (Framework.class) {
			Iterator<DocumentWrapper> it = documents.tailSet(searchWrapper).iterator();
			
			if (it.hasNext()) {
				resultWrapper = it.next();
				
				if (resultWrapper.equals(searchWrapper)) {
					it.remove();
					
					resultWrapper.prev.next = resultWrapper.next;
					resultWrapper.next.prev = resultWrapper.prev;
					
					size--;
					modCount++;
					
					return true;
				}
			} 
			
			return false;
		}
	}
	
	@Override
	public int size() {
		return size;
	}

	@Override
	public Object[] toArray() {

		synchronized (Framework.class) {
			return super.toArray();
		}
	}

	@Override
	public <T> T[] toArray(T[] a) {
		
		synchronized (Framework.class) {
			return super.toArray(a);
		}
	}

	@Override
	public String toString() {
		
		synchronized (Framework.class) {
			return super.toString();
		}
	}
	
	/**
	 * Adds the given document to this framework if it's not already part of a framework.
	 * No check is made to ensure that the document is a well-formed code-mapping document.
	 * 
	 * @param doc The document to be added to this framework.
	 * @return {@code true} if the document was added, otherwise {@code false}.
	 */
	private boolean uncheckedAdd(Document doc) {
		DocumentWrapper wrapper = new DocumentWrapper(doc, this, tail.prev, tail); 
		
		synchronized (Framework.class) {
			
			if (!documents.add(wrapper))
				return false;
			
			tail.prev = tail.prev.next = wrapper;
			
			size++;
			modCount++;
		}
		
		return true;
	}
	
	/**
	 * A {@code DocumentWrapper} wraps a code-mapping document in order to provide
	 * references to the framework to which it belongs and to the next and previous
	 * element in the framework's linked sequence of {@code DocumentWrapper}:s.
	 * In addition, {@code DocumentWrapper}:s are {@link Comparable}.
	 */
	private static class DocumentWrapper implements Comparable<DocumentWrapper> {
		
		/**
		 * The wrapped document.
		 */
		Document doc;
		
		/**
		 * The next element in the framework's linked
		 * sequence of {@code DocumentWrapper}:s.
		 */
		DocumentWrapper next;
		
		/**
		 * The previous element in the framework's linked
		 * sequence of {@code DocumentWrapper}:s.
		 */
		DocumentWrapper prev;
		
		/**
		 * The framework that the the wrapped document belongs to.
		 */
		Framework framework;
		
		/**
		 * Constructs a {@code DocumentWrapper} that wraps the
		 * given code-mapping document.
		 * 
		 * @param doc The code-mapping document to be wrapped.
		 */
		DocumentWrapper(Document doc) {
			this.doc = doc;
		}
		
		/**
		 * Constructs a {@code DocumentWrapper} that wraps the given
		 * code-mapping document.
		 * 
		 * @param doc The code-mapping document to be wrapped.
		 * @param framework The framework that the document belongs to.
		 * @param prev	The previous element in the given framework's
		 * 				linked sequence of {@code DocumentWrapper}:s. 
		 * @param next	The next element in the given framework's linked
		 * 				sequence of {@code DocumentWrapper}:s.
		 */
		DocumentWrapper(Document doc, Framework framework, DocumentWrapper prev, DocumentWrapper next) {
			this.doc = doc;
			this.framework = framework;
			this.prev = prev;
			this.next = next;
		}

		/**
		 * Compares the document of this {@code DocumentWrapper} to that of the
		 * given {@code DocumentWrapper} using an {@link InPUTDocument.Comparator}.
		 * 
		 * @param other		The {@code DocumentWrapper} whose document shall be
		 * 					compared to the document of this {@code DocumentWrapper}.
		 * @return	A negative integer, zero, or a positive integer as the document
		 * 			of this {@code DocumentWrapper} is less than, equal to, or greater
		 * 			than that of the given {@code DocumentWrapper}.
		 */
		public int compareTo(DocumentWrapper other) {
			return new InPUTDocument.Comparator().compare(doc, other.doc);
		}
		
		/**
		 * Returns {@code true} if the given object is a {@code DocumentWrapper}
		 * that is equal to this {@code DocumentWrapper} according to the
		 * {@code compareTo} method, otherwise {@code false}.
		 */
		@Override
		public boolean equals(Object o) {
			return o instanceof DocumentWrapper &&
					compareTo((DocumentWrapper) o) == 0;
		}
	}
}
