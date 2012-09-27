/*-- $Copyright (C) 2012 Felix Dobslaw$


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
 */

package se.miun.itm.input.standalone;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Scanner;

import se.miun.itm.input.export.InPUTExporter;
import se.miun.itm.input.export.LaTeXFileExporter;
import se.miun.itm.input.impOrt.InPUTImporter;
import se.miun.itm.input.impOrt.XMLFileImporter;
import se.miun.itm.input.model.Document;
import se.miun.itm.input.model.InPUTException;
import se.miun.itm.input.model.design.DesignSpace;
import se.miun.itm.input.model.design.IDesign;
import se.miun.itm.input.model.design.IDesignSpace;

/**
 * The class that delegates all commands to the respective InPUT components,
 * initiated by the command line tool.
 * 
 * @author Felix Dobslaw
 * 
 */
public class Main {

	private static final String HELP = "Type as argument 'help' to get an overview of supported commands.";

	private Main(String[] args) {
		if (args == null || args.length == 0) {
			System.out.println("InPUT requires user input. " + HELP);
			return;
		}

		Class<?>[] types = getStringTypes(args);

		Method m;
		try {
			m = Main.class.getMethod(args[0], types);
			Object[] mArgs = getArgs(args);
			m.invoke(this, mArgs);
		} catch (IllegalArgumentException e) {
			System.out.println("The command " + args[0]
					+ " cannot be used that way. " + HELP);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			System.out.println("A command '" + args[0] + "' with "
					+ (args.length - 1) + " arguments does not exist. " + HELP);
		}
	}

	public static void main(String[] args) {
		new Main(args);
	}

	public void nextDesign(String designSpaceFile, String latexOutput,
			String expId) throws InPUTException, FileNotFoundException {
		IDesignSpace input = new DesignSpace(designSpaceFile);
		input.nextDesign(expId);
	}

	public void toLatex(String inputInput, String latexOutput)
			throws InPUTException, FileNotFoundException {
		IDesignSpace space = new DesignSpace(inputInput);
		InPUTExporter<Void> exporter = new LaTeXFileExporter(latexOutput);
		space.export(exporter);
	}

	public void toLatex(String inputInput, String designInput,
			String latexOutput) throws InPUTException, FileNotFoundException {
		IDesignSpace space = new DesignSpace(inputInput);
		InPUTImporter<Document> importer = new XMLFileImporter(designInput);
		IDesign design = space.impOrt(importer);
		InPUTExporter<Void> exporter = new LaTeXFileExporter(latexOutput);
		design.export(exporter);
	}

	public void help() throws InPUTException {
		try {
			System.out.println(convertStreamToString(getClass()
					.getResourceAsStream("help.txt")));
		} catch (NoSuchElementException e) {
			throw new InPUTException(
					"An internal error occured. The help file is missing. Please make sure that you have an unaltered version of the jar file.",
					e);
		}
	}

	String convertStreamToString(InputStream is) {
		return new Scanner(is).useDelimiter("\\A").next();
	}

	private String[] getArgs(String[] args) {
		String[] mArgs;
		if (args.length < 2)
			mArgs = new String[0];
		else
			mArgs = Arrays.copyOfRange(args, 1, args.length);
		return mArgs;
	}

	private Class<?>[] getStringTypes(String[] args) {
		Class<?>[] types = new Class<?>[args.length - 1];
		for (int i = 1; i < args.length; i++)
			types[i - 1] = String.class;
		return types;
	}
}