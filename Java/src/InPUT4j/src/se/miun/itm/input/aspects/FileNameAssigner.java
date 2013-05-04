/*-- $Copyright (C) 2012-13 Felix Dobslaw$

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is furnished
to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */package se.miun.itm.input.aspects;

import java.io.File;

/**
 * An abstract help class that allows the setting of a file name for import/export jobs.
 * 
 * @author Felix Dobslaw
 *
 */
public abstract class FileNameAssigner {

	protected String fileName;
	
	/**
	 * Create the object, setting the path to the context file.
	 * @param inputFilePath
	 */
	public FileNameAssigner(String inputFilePath) {
		this.fileName = new File(inputFilePath).getAbsolutePath();
	}

	/**
	 * resets the context of this assigner.
	 * @param inputPath
	 */
	public void resetFileName(String inputPath) {
			this.fileName = new File(inputPath).getAbsolutePath();
	}
	
	/**
	 * returns the context of the assigner.
	 * @return
	 */
	public String getInfo() {
		return fileName;
	}

	protected void initPotentialParentFolders(String fileName) {
		if (fileName.contains(File.separator)) {
			String parentFolders = fileName.substring(0,fileName.lastIndexOf(File.separatorChar));
			new File(parentFolders).mkdirs();
		}
		
	}
}