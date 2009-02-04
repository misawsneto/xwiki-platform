/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package com.xpn.xwiki.wysiwyg.client.widget.rta;

import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.dom.client.Node;
import com.google.gwt.user.client.Timer;
import com.xpn.xwiki.wysiwyg.client.dom.Range;
import com.xpn.xwiki.wysiwyg.client.dom.Selection;
import com.xpn.xwiki.wysiwyg.client.widget.rta.cmd.Command;

/**
 * Unit tests for {@link SelectionPreserver}.
 * 
 * @version $Id$
 */
public class SelectionPreserverTest extends AbstractRichTextAreaTest
{
    /**
     * The selection preserver instance being tested.
     */
    private SelectionPreserver preserver;

    /**
     * {@inheritDoc}
     * 
     * @see AbstractRichTextAreaTest#gwtSetUp()
     */
    protected void gwtSetUp() throws Exception
    {
        super.gwtSetUp();

        if (preserver == null) {
            preserver = new SelectionPreserver(rta);
        }
    }

    /**
     * Tests the preserver when the selection is inside a text node and the selected text doesn't change.
     */
    public void testPlainTextSelectionWithoutModification()
    {
        delayTestFinish(FINISH_DELAY);
        (new Timer()
        {
            public void run()
            {
                doTestPlainTextSelectionWithoutModification();
                finishTest();
            }
        }).schedule(START_DELAY);
    }

    /**
     * Tests the preserver when the selection is inside a text node and the selected text doesn't change.
     */
    private void doTestPlainTextSelectionWithoutModification()
    {
        rta.setHTML("xwiki");

        Range range = rta.getDocument().createRange();
        range.setStart(getBody().getFirstChild(), 1);
        range.setEnd(getBody().getFirstChild(), 3);
        String selectedText = "wi";

        Selection selection = rta.getDocument().getSelection();
        selection.removeAllRanges();
        selection.addRange(range);
        assertEquals(selectedText, selection.toString());

        preserver.saveSelection();
        range.collapse(true);
        selection.removeAllRanges();
        selection.addRange(range);
        assertEquals("", selection.toString());

        preserver.restoreSelection();
        assertEquals(selectedText, selection.toString());
    }

    /**
     * Tests the preserver when the selection is inside a text node and the selected text is replaced by some HTML.
     */
    public void testPlainTextSelectionWithHTMLInsertion()
    {
        delayTestFinish(FINISH_DELAY);
        (new Timer()
        {
            public void run()
            {
                doTestPlainTextSelectionWithHTMLInsertion();
                finishTest();
            }
        }).schedule(START_DELAY);
    }

    /**
     * Tests the preserver when the selection is inside a text node and the selected text is replaced by some HTML.
     */
    private void doTestPlainTextSelectionWithHTMLInsertion()
    {
        rta.setHTML("toucan");

        Range range = rta.getDocument().createRange();
        range.setStart(getBody().getFirstChild(), 0);
        range.setEnd(getBody().getFirstChild(), 2);
        String selectedText = "to";

        Selection selection = rta.getDocument().getSelection();
        selection.removeAllRanges();
        selection.addRange(range);
        assertEquals(selectedText, selection.toString());

        preserver.saveSelection();
        assertEquals(selectedText, selection.getRangeAt(0).toHTML());
        assertTrue(rta.getCommandManager().execute(Command.INSERT_HTML, "<ins>the</ins> <em>To</em>"));

        preserver.restoreSelection();
        assertEquals("the To", selection.toString());
    }

    /**
     * Tests the preserver when we have a text range and the range contents are replaced by some HTML.
     * 
     * @see com.xpn.xwiki.wysiwyg.client.dom.DOMUtils#getTextRange(Range)
     */
    public void testTextRangeSelectionWithHTMLInsertion()
    {
        delayTestFinish(FINISH_DELAY);
        (new Timer()
        {
            public void run()
            {
                doTestTextRangeSelectionWithHTMLInsertion();
                finishTest();
            }
        }).schedule(START_DELAY);
    }

    /**
     * Tests the preserver when we have a text range and the range contents are replaced by some HTML.
     * 
     * @see com.xpn.xwiki.wysiwyg.client.dom.DOMUtils#getTextRange(Range)
     */
    private void doTestTextRangeSelectionWithHTMLInsertion()
    {
        rta.setHTML("ab<em>cd</em>ef<ins>gh</ins>ij");

        Range range = rta.getDocument().createRange();
        range.setStart(getBody().getChildNodes().getItem(1).getFirstChild(), 1);
        range.setEnd(getBody().getChildNodes().getItem(3).getFirstChild(), 2);

        Selection selection = rta.getDocument().getSelection();
        selection.removeAllRanges();
        selection.addRange(range);
        assertEquals("defgh", selection.toString());

        preserver.saveSelection();
        assertTrue(rta.getCommandManager().execute(Command.INSERT_HTML, "<!--x--><span>y</span><!--z-->"));

        preserver.restoreSelection();
        assertEquals("y", selection.toString());
    }

    /**
     * Tests the preserver when the selection wraps an element and we replace it with some HTML.
     */
    public void testReplaceElement()
    {
        delayTestFinish(FINISH_DELAY);
        (new Timer()
        {
            public void run()
            {
                doTestReplaceElement();
                finishTest();
            }
        }).schedule(START_DELAY);
    }

    /**
     * Tests the preserver when the selection wraps an element and we replace it with some HTML.
     */
    private void doTestReplaceElement()
    {
        rta.setHTML("ab<em>cd</em>ef");

        Range range = rta.getDocument().createRange();
        range.setStartBefore(getBody().getChildNodes().getItem(1));
        range.setEndAfter(getBody().getChildNodes().getItem(1));

        Selection selection = rta.getDocument().getSelection();
        selection.removeAllRanges();
        selection.addRange(range);
        assertEquals("cd", selection.toString());

        preserver.saveSelection();
        assertTrue(rta.getCommandManager().execute(Command.INSERT_HTML, "<ins>#</ins>"));

        preserver.restoreSelection();
        assertEquals("#", selection.toString());
    }

    /**
     * Tests the preserver when the edited document is empty and the user takes no action between save and restore.
     */
    public void testEmptyDocumentWithoutAction()
    {
        delayTestFinish(FINISH_DELAY);
        (new Timer()
        {
            public void run()
            {
                doTestEmptyDocumentWithoutAction();
                finishTest();
            }
        }).schedule(START_DELAY);
    }

    /**
     * Tests the preserver when the edited document is empty and the user takes no action between save and restore.
     */
    private void doTestEmptyDocumentWithoutAction()
    {
        rta.setHTML("");
        assertEquals("", rta.getDocument().getSelection().toString());
        preserver.saveSelection();
        preserver.restoreSelection();
        // We need to trim the selected text because the IE range implementation adds and selects a single-space text
        // when we try to place the caret inside an empty DOM element.
        assertEquals("", rta.getDocument().getSelection().toString().trim());
    }

    /**
     * Tests the preserver when the edited document is empty and the user takes an editing action which affects the
     * selected content, between save and restore.
     */
    public void testEmptyDocumentWithAction()
    {
        delayTestFinish(FINISH_DELAY);
        (new Timer()
        {
            public void run()
            {
                doTestEmptyDocumentWithAction();
                finishTest();
            }
        }).schedule(START_DELAY);
    }

    /**
     * Tests the preserver when the edited document is empty and the user takes an editing action which affects the
     * selected content, between save and restore.
     */
    private void doTestEmptyDocumentWithAction()
    {
        rta.setHTML("");
        preserver.saveSelection();
        String symbol = "*";
        assertTrue(rta.getCommandManager().execute(Command.INSERT_HTML, symbol));
        preserver.restoreSelection();
        assertEquals(symbol, rta.getDocument().getSelection().toString());
    }

    /**
     * Tests the preserver when an image is selected.
     */
    public void testPreserveImageSelection()
    {
        delayTestFinish(FINISH_DELAY);
        (new Timer()
        {
            public void run()
            {
                doTestPreserveImageSelection();
                finishTest();
            }
        }).schedule(START_DELAY);
    }

    /**
     * Tests the preserver when an image is selected.
     */
    private void doTestPreserveImageSelection()
    {
        String imageHTML = "<img src=\"clear.cache.gif\" height=\"10\" width=\"10\"/>";
        rta.setHTML("pq" + imageHTML + "r");

        Range range = rta.getDocument().createRange();
        range.selectNode(getBody().getChildNodes().getItem(1));
        select(range);

        preserver.saveSelection();
        assertTrue(rta.getCommandManager().execute(Command.INSERT_HTML, imageHTML.replace("10", "15")));
        preserver.restoreSelection();
        assertEquals(15, ((ImageElement) getBody().getChildNodes().getItem(1)).getWidth());

        range = rta.getDocument().getSelection().getRangeAt(0);
        assertEquals(getBody(), range.getStartContainer());
        assertEquals(1, range.getStartOffset());
        assertEquals(getBody(), range.getEndContainer());
        assertEquals(2, range.getEndOffset());
    }

    /**
     * Test the preserver when we select a text node and we replace it with the empty string.
     */
    public void testSelectTextNodeAndReplaceItWithEmptyString()
    {
        delayTestFinish(FINISH_DELAY);
        (new Timer()
        {
            public void run()
            {
                doTestSelectTextNodeAndReplaceItWithEmptyString();
                finishTest();
            }
        }).schedule(START_DELAY);
    }

    /**
     * Test the preserver when we select a text node and we replace it with the empty string.
     */
    private void doTestSelectTextNodeAndReplaceItWithEmptyString()
    {
        rta.setHTML("<p>123</p>");

        Range range = rta.getDocument().createRange();
        range.selectNodeContents(getBody().getFirstChild().getFirstChild());
        select(range);

        preserver.saveSelection();
        assertTrue(rta.getCommandManager().execute(Command.INSERT_HTML, ""));
        preserver.restoreSelection();
        assertTrue(rta.getCommandManager().execute(Command.INSERT_HTML, "%"));
        assertEquals("<p>%</p>", rta.getHTML().toLowerCase());
    }

    /**
     * Test the preserver when we select a text node and then we delete it.
     */
    public void testSelectTextNodeAndDeleteIt()
    {
        delayTestFinish(FINISH_DELAY);
        (new Timer()
        {
            public void run()
            {
                doTestSelectTextNodeAndDeleteIt();
                finishTest();
            }
        }).schedule(START_DELAY);
    }

    /**
     * Test the preserver when we select a text node and then we delete it.
     */
    private void doTestSelectTextNodeAndDeleteIt()
    {
        rta.setHTML("<p>321</p>");
        Node text = getBody().getFirstChild().getFirstChild();

        Range range = rta.getDocument().createRange();
        range.selectNodeContents(text);
        select(range);

        preserver.saveSelection();
        text.getParentNode().removeChild(text);
        preserver.restoreSelection();
        assertTrue(rta.getCommandManager().execute(Command.INSERT_HTML, "+"));
        assertEquals("<p>+</p>", rta.getHTML().toLowerCase());
    }

    /**
     * Test the preserver when we select an image and then delete that image.
     */
    public void testSelectImageAndDeleteIt()
    {
        delayTestFinish(FINISH_DELAY);
        (new Timer()
        {
            public void run()
            {
                doTestSelectImageAndDeleteIt();
                finishTest();
            }
        }).schedule(START_DELAY);
    }

    /**
     * Test the preserver when we select an image and then delete that image.
     */
    private void doTestSelectImageAndDeleteIt()
    {
        rta.setHTML("<div><img src=\"clear.cache.gif\" height=\"10\" width=\"10\"/></div>");
        Node image = getBody().getFirstChild().getFirstChild();

        Range range = rta.getDocument().createRange();
        range.selectNode(image);
        select(range);

        preserver.saveSelection();
        image.getParentNode().removeChild(image);
        preserver.restoreSelection();
        assertTrue(rta.getCommandManager().execute(Command.INSERT_HTML, "@"));
        assertEquals("<div>@</div>", rta.getHTML().toLowerCase());
    }

    /**
     * Test if the selection wraps an image after it was inserted. This is needed in order to have control selection
     * over the image after it was inserted.
     */
    public void testSelectionAfterImageInsertion()
    {
        delayTestFinish(FINISH_DELAY);
        (new Timer()
        {
            public void run()
            {
                doTestSelectionAfterImageInsertion();
                finishTest();
            }
        }).schedule(START_DELAY);
    }

    /**
     * Test if the selection wraps an image after it was inserted. This is needed in order to have control selection
     * over the image after it was inserted.
     */
    private void doTestSelectionAfterImageInsertion()
    {
        rta.setHTML("albatross");

        Range range = rta.getDocument().createRange();
        range.setStart(getBody().getFirstChild(), 2);
        range.setEnd(getBody().getFirstChild(), 4);
        select(range);

        preserver.saveSelection();
        assertTrue(rta.getCommandManager().execute(Command.INSERT_HTML, "<img src=\"clear.cache.gif\"/>"));

        preserver.restoreSelection();
        range = rta.getDocument().getSelection().getRangeAt(0);
        assertEquals(getBody(), range.getStartContainer());
        assertEquals(1, range.getStartOffset());
        assertEquals(getBody(), range.getEndContainer());
        assertEquals(2, range.getEndOffset());
    }

    /**
     * Test if the range boundary markers inserted by the selection preserver in the edited document appear in the HTML
     * output.
     */
    public void testRangeBoundaryMarkersAreHidden()
    {
        delayTestFinish(FINISH_DELAY);
        (new Timer()
        {
            public void run()
            {
                doTestRangeBoundaryMarkersAreHidden();
                finishTest();
            }
        }).schedule(START_DELAY);
    }

    /**
     * Test if the range boundary markers inserted by the selection preserver in the edited document appear in the HTML
     * output.
     */
    private void doTestRangeBoundaryMarkersAreHidden()
    {
        String content = "bluebird";
        rta.setHTML(content);

        Range range = rta.getDocument().createRange();
        range.setStart(getBody().getFirstChild(), 0);
        range.setEnd(getBody().getFirstChild(), 4);
        select(range);

        preserver.saveSelection();
        assertTrue(rta.getCommandManager().execute(Command.BOLD));

        preserver.restoreSelection();
        assertEquals("blue", rta.getDocument().getSelection().toString());
        assertEquals("<strong>blue</strong>bird", rta.getHTML().toLowerCase());

        rta.getHistory().undo();
        assertEquals(content, rta.getHTML());
    }
}
