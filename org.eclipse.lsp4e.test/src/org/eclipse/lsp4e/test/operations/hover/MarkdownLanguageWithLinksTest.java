package org.eclipse.lsp4e.test.operations.hover;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;

import org.eclipse.lsp4e.operations.hover.MarkdownLanguageWithLinks;
import org.eclipse.mylyn.wikitext.parser.MarkupParser;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class MarkdownLanguageWithLinksTest {

	@Parameters
	public static Collection<Object[]> data() {
		return Arrays.asList(new Object[][] { { "http://" }, { "https://" }, { "" } });
	}

	private final String urlPrefix;
	private final String hrefPrefix;

	public MarkdownLanguageWithLinksTest(String prefix) {
		urlPrefix = prefix;
		if(prefix.equals(""))
			hrefPrefix = "http://";
		else
			hrefPrefix = prefix;
	}

	private MarkupParser createParser() {
		return new MarkupParser(new MarkdownLanguageWithLinks());
	}

	@Test
	public void testOnlyLink() {
		MarkupParser sut = createParser();
		String markdown = urlPrefix
				+ "www.eclipse.org:80/p2%20update/!+*,';$[foo]/(bar)/~/_emf_/-?bar=baz&oomph#foo";
		String htmlOut = sut.parseToHtml(markdown);
		// note that in xhmtl attribute values are escaped, therefore
		// ' is escaped as &apos; and & is escaped as &amp;
		boolean containsLink = htmlOut.contains("<body><p><a href=\"" + hrefPrefix
				+ "www.eclipse.org:80/p2%20update/!+*,&apos;;$%5Bfoo%5D/(bar)/~/_emf_/-?bar=baz&amp;oomph#foo\">"
				+ urlPrefix
				+ "www.eclipse.org:80/p2%20update/!+*,';$[foo]/(bar)/~/_emf_/-?bar=baz&amp;oomph#foo</a></p></body>");
		assertTrue(containsLink);
	}

	@Test
	public void testLinkAtBeginning() {
		MarkupParser sut = createParser();
		String markdown = urlPrefix + "www.eclipse.org foo bar";
		String htmlOut = sut.parseToHtml(markdown);
		String expectedOutput = "<body><p><a href=\"" + hrefPrefix + "www.eclipse.org\">" + urlPrefix
				+ "www.eclipse.org</a> foo bar</p></body>";
		boolean containsExpectedOutput = htmlOut.contains(expectedOutput);
		assertTrue(containsExpectedOutput);
	}

	@Test
	public void testInvalidHtmlEntity() {
		MarkupParser sut = createParser();
		String markdown = urlPrefix + "www.eclipse.org&@mp;";
		String htmlOut = sut.parseToHtml(markdown);
		String expectedOutput = "<body><p><a href=\"" + hrefPrefix + "www.eclipse.org&amp;@mp\">" + urlPrefix + "www.eclipse.org&amp;@mp</a>;</p></body>";
		boolean containsExpectedOutput = htmlOut.contains(expectedOutput);
		assertTrue(containsExpectedOutput);
	}

	@Test
	public void testHtmlEntityDetection() {
		MarkupParser sut = createParser();
		String markdown = urlPrefix + "www.eclipse.org&amp;";
		String htmlOut = sut.parseToHtml(markdown);
		String expectedOutput = "<body><p><a href=\"" + hrefPrefix + "www.eclipse.org\">" + urlPrefix + "www.eclipse.org</a>&amp;amp;</p></body>";
		boolean containsExpectedOutput = htmlOut.contains(expectedOutput);
		assertTrue(containsExpectedOutput);
	}

	@Test
	public void testHtmlEntityDetectionAtStart() {
		MarkupParser sut = createParser();
		String markdown = urlPrefix + "www.eclipse.org!?.&amp;";
		String htmlOut = sut.parseToHtml(markdown);
		String expectedOutput = "<body><p><a href=\"" + hrefPrefix + "www.eclipse.org\">" + urlPrefix + "www.eclipse.org</a>!?.&amp;amp;</p></body>";
		boolean containsExpectedOutput = htmlOut.contains(expectedOutput);
		assertTrue(containsExpectedOutput);
	}

	@Test
	public void testHtmlEntityDetectionAtEnd() {
		MarkupParser sut = createParser();
		String markdown = urlPrefix + "www.eclipse.org&amp;!?.";
		String htmlOut = sut.parseToHtml(markdown);
		String expectedOutput = "<body><p><a href=\"" + hrefPrefix + "www.eclipse.org\">" + urlPrefix + "www.eclipse.org</a>&amp;amp;!?.</p></body>";
		boolean containsExpectedOutput = htmlOut.contains(expectedOutput);
		assertTrue(containsExpectedOutput);
	}

	@Test
	public void testLinkEndOfQuote() {
		MarkupParser sut = createParser();
		String markdown = "foo " + urlPrefix + "www.eclipse.org/downloads\" bar";
		String htmlOut = sut.parseToHtml(markdown);
		String expectedOutput = "<body><p>foo <a href=\"" + hrefPrefix + "www.eclipse.org/downloads\">" + urlPrefix + "www.eclipse.org/downloads</a>\" bar</p></body>";
		boolean containsExpectedOutput = htmlOut.contains(expectedOutput);
		assertTrue(containsExpectedOutput);
	}

	@Test
	public void testLinkEndWithMultiplePunctuationChars() {
		MarkupParser sut = createParser();
		String markdown = "foo " + urlPrefix + "www.eclipse.org/downloads\"~. bar";
		String htmlOut = sut.parseToHtml(markdown);
		String expectedOutput = "<body><p>foo <a href=\"" + hrefPrefix + "www.eclipse.org/downloads\">" + urlPrefix + "www.eclipse.org/downloads</a>\"~. bar</p></body>";
		boolean containsExpectedOutput = htmlOut.contains(expectedOutput);
		assertTrue(containsExpectedOutput);
	}

	@Test
	public void testLinkInText() {
		MarkupParser sut = createParser();
		String markdown = "foo " + urlPrefix + "www.eclipse.org bar";
		String htmlOut = sut.parseToHtml(markdown);
		String expectedOutput = "<body><p>foo <a href=\"" + hrefPrefix + "www.eclipse.org\">" + urlPrefix + "www.eclipse.org</a> bar</p></body>";
		boolean containsExpectedOutput = htmlOut.contains(expectedOutput);
		assertTrue(containsExpectedOutput);
	}

	@Test
	public void testLinkInList() {
		MarkupParser sut = createParser();
		String markdown = "\n  - " + urlPrefix + "www.eclipse.org\n";
		String htmlOut = sut.parseToHtml(markdown);
		String expectedOutput = "<body><ul><li><a href=\"" + hrefPrefix + "www.eclipse.org\">" + urlPrefix + "www.eclipse.org</a></li></ul></body>";
		boolean containsExpectedOutput = htmlOut.contains(expectedOutput);
		assertTrue(containsExpectedOutput);
	}

	@Test
	public void testLinkAtEnd() {
		MarkupParser sut = createParser();
		String markdown = "foo bar " + urlPrefix + "www.eclipse.org";
		String htmlOut = sut.parseToHtml(markdown);
		String expectedOutput = "<body><p>foo bar <a href=\"" + hrefPrefix + "www.eclipse.org\">" + urlPrefix + "www.eclipse.org</a></p></body>";
		boolean containsExpectedOutput = htmlOut.contains(expectedOutput);
		assertTrue(containsExpectedOutput);
	}

}
