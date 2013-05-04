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
 */package org.rosuda.JRI;

// JRclient library - client interface to Rserve, see http://www.rosuda.org/Rserve/
// Copyright (C) 2004 Simon Urbanek
// --- for licensing information see LICENSE file in the original JRclient distribution ---

/** Implementation of tri-state logical data type in R.
    The three states are TRUE, FALSE and NA. To obtain truly boolean
    value, you'll need to use {@link #isTRUE} or {@link #isFALSE} since there is
    no canonical representation of RBool in boolean

    @version $Id: RBool.java 2720 2007-03-15 17:35:42Z urbanek $
*/
public class RBool extends Object {
    int val;

    public RBool(boolean b) {
	val=(b)?1:0;
    };
    public RBool(RBool r) {
	val=r.val;
    };
    public RBool(int i) { /* 0=FALSE, 2=NA, anything else = TRUE */
	val=(i==0||i==2)?i:1;
    };

    public boolean isNA() { return (val==2); };
    public boolean isTRUE() { return (val==1); };
    public boolean isFALSE() { return (val==0); };

    public String toString() { return (val==0)?"FALSE":((val==2)?"NA":"TRUE"); };
}
