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
 */package se.miun.itm.input.sql.example.inputoutput;

import se.miun.itm.input.junit.DatabaseConnectingDialog;
import se.miun.itm.input.model.InPUTException;
import se.miun.itm.input.model.design.IDesign;
import se.miun.itm.input.model.design.IDesignSpace;
import se.miun.itm.input.util.sql.DatabaseAdapter;

/**
 * The example imports a design space from a database, creates a random 
 * instance of that space, and exports it to the database.
 * @author Felix Dobslaw
 *
 */
public class SimpleImportExport {

	public static void main(String[] args) throws InPUTException {
		DatabaseAdapter adapter =
			new DatabaseAdapter(DatabaseConnectingDialog.connect());
		IDesignSpace ds = adapter.importDesignSpace("externalizedParameterExample");
		IDesign design = ds.nextDesign(
			"SimpleImportExport-design-" + System.currentTimeMillis());
		adapter.export(design);
	}
}
