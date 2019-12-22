package org.eclipse.lsp4e.test.operations.hover;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.lsp4e.operations.hover.MarkdownLanguageWithLinks;
import org.eclipse.mylyn.wikitext.parser.MarkupParser;
import org.junit.Test;

public class MarkdownLanguageWithLinksTest {

	private MarkupParser createParser() {
		return new MarkupParser(new MarkdownLanguageWithLinks());
	}

	@Test
	public void testLinkWithIllegalCharacters() {
		MarkupParser sut = createParser();
		String markdown = "http://eclipsö.org";
		String htmlOut = sut.parseToHtml(markdown);
		boolean doesContainLink = htmlOut.contains("<a href=");
		assertFalse(doesContainLink);
	}

	
	@Test
	public void testOnlyLink() {
		MarkupParser sut = createParser();
		String markdown = "http://mbureck@eclipse.org:80/p2%20update/!+*,';$[foo]/(bar)/~/_emf_/-?bar=baz&oomph#foo";
		String htmlOut = sut.parseToHtml(markdown);
		// note that in xhmtl attribute values are escaped, therefore 
		// ' is escaped as &apos; and & is escaped as &amp;
		boolean containsLink = htmlOut.contains("<body><p><a href=\"http://mbureck@eclipse.org:80/p2%20update/!+*,&apos;;$[foo]/(bar)/~/_emf_/-?bar=baz&amp;oomph#foo\">http://mbureck@eclipse.org:80/p2%20update/!+*,';$[foo]/(bar)/~/_emf_/-?bar=baz&amp;oomph#foo</a></p></body>");
		assertTrue(containsLink);
	}

	@Test
	public void testLinkAtBeginning() {
		MarkupParser sut = createParser();
		String markdown = "https://eclipse.org foo bar";
		String htmlOut = sut.parseToHtml(markdown);
		String expectedOutput = "<body><p><a href=\"https://eclipse.org\">https://eclipse.org</a> foo bar</p></body>";
		boolean containsExpectedOutput = htmlOut.contains(expectedOutput);
		assertTrue(containsExpectedOutput);
	}

	@Test
	public void testLinkInText() {
		MarkupParser sut = createParser();
		String markdown = "foo https://eclipse.org bar";
		String htmlOut = sut.parseToHtml(markdown);
		String expectedOutput = "<body><p>foo <a href=\"https://eclipse.org\">https://eclipse.org</a> bar</p></body>";
		boolean containsExpectedOutput = htmlOut.contains(expectedOutput);
		assertTrue(containsExpectedOutput);
	}

	@Test
	public void testLinkInList() {
		MarkupParser sut = createParser();
		String markdown = "\n  - https://eclipse.org\n";
		String htmlOut = sut.parseToHtml(markdown);
		String expectedOutput = "<body><ul><li><a href=\"https://eclipse.org\">https://eclipse.org</a></li></ul></body>";
		boolean containsExpectedOutput = htmlOut.contains(expectedOutput);
		assertTrue(containsExpectedOutput);
	}

	@Test
	public void testLinkAtEnd() {
		MarkupParser sut = createParser();
		String markdown = "foo bar https://eclipse.org";
		String htmlOut = sut.parseToHtml(markdown);
		String expectedOutput = "<body><p>foo bar <a href=\"https://eclipse.org\">https://eclipse.org</a></p></body>";
		boolean containsExpectedOutput = htmlOut.contains(expectedOutput);
		assertTrue(containsExpectedOutput);
	}

}
