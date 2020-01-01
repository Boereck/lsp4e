/*******************************************************************************
 * Copyright (c) 2019 Fraunhofer FOKUS and others.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *  Max Bureck (Fraunhofer FOKUS) - initial implementation
 *******************************************************************************/
package org.eclipse.lsp4e.operations.hover;

import org.eclipse.mylyn.wikitext.markdown.MarkdownLanguage;
import org.eclipse.mylyn.wikitext.parser.markup.PatternBasedElement;
import org.eclipse.mylyn.wikitext.parser.markup.PatternBasedElementProcessor;

import com.google.common.net.UrlEscapers;

/**
 * Markdown implementation that also converts bare http(s) text hyperlinks to
 * HTML links in the output.
 */
public class MarkdownLanguageWithLinks extends MarkdownLanguage {

	@Override
	protected void addStandardPhraseModifiers(PatternBasedSyntax phraseModifierSyntax) {
		super.addStandardPhraseModifiers(phraseModifierSyntax);
		phraseModifierSyntax.add(new AutomaticLinkReplacementToken());
	}

	/**
	 * Replacement for bare hyperlinks (e.g. http://www.eclipse.org) to links in the
	 * HTTP output.
	 */
	private static class AutomaticLinkReplacementToken extends PatternBasedElement {

		/**
		 * Regex based on characters mentioned in RFC-3986:
		 * https://www.ietf.org/rfc/rfc3986.txt Note that this Regex does not check for
		 * a completely valid HTTP link, it only checks for the {@code http(s)://}
		 * prefix and if the following characters are valid for URLs.
		 */
		// \b does not work, maybe this is because the regex is combined to a larger
		// one.
		private static final String AUTOMATIC_LINK_REGEX = "(?<=^|\\s|\\p{Punct})((https?://(?!/)|www\\.)[^\\s<]*)"; //$NON-NLS-1$

		@Override
		protected String getPattern(int groupOffset) {
			return AUTOMATIC_LINK_REGEX;
		}

		@Override
		protected int getPatternGroupCount() {
			return 2;
		}

		@Override
		protected PatternBasedElementProcessor newProcessor() {
			return new PatternBasedElementProcessor() {
				@Override
				public void emit() {
					String href = group(1);
					int parensBalance = href.codePoints().map(c -> {
						switch (c) {
						case '(':
							return 1;
						case ')':
							return -1;
						default:
							return 0;
						}
					}).sum();
					// omit punctuation
					int endIndex = -1;
					charLoop: for (int i = href.length() - 1; i > 3; i--) {
						switch (href.charAt(i)) {
						case '?':
						case '!':
						case '\'':
						case '"':
						case '*':
						case '.':
						case ':':
						case '_':
						case '~':
							endIndex = i;
							break;
						case ')':
							if (parensBalance > 0) {
								parensBalance--;
							} else {
								break charLoop;
							}
						case ';':
							i = skipHtmlEntity(href, i);
							endIndex = i;
							break;
						default:
							break charLoop;
						}
					}

					String linkText;
					String linkHref;
					if (endIndex > -1) {
						linkText = href.substring(0, endIndex);
					} else {
						linkText = href;
					}
					if (linkText.startsWith("www.")) { //$NON-NLS-1$
						linkHref = "http://" + linkText; //$NON-NLS-1$
					} else {
						linkHref = linkText;
					}

					if(linkText.equals("www.") || linkText.equals("http://")) { //$NON-NLS-1$ //$NON-NLS-2$
						// do not convert "empty" links
						builder.characters(href);
					} else {
						linkHref = UrlEscapers.urlFragmentEscaper().escape(linkHref).replace("%23", "#").replace("%25", "%"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
						builder.link(linkHref, linkText);
						// if characters were stripped, add them as regular text after link.
						if (endIndex > -1) {
							builder.characters(href.substring(endIndex));
						}
					}
				}

				private int skipHtmlEntity(String href, int endIndex) {
					for (int i = endIndex - 1; i > 3; i--) {
						char c = href.charAt(i);
						boolean isAlphaNum = inRange(c, 'a', 'z') || inRange(c, 'A', 'Z') || inRange(c, '0', '9');
						// if the character is not in [a-zA-Z0-9], don't skip anything
						if (c == '&') {
							return i;
						}
						if ( !isAlphaNum ) {
							return endIndex;
						}
					}
					// no & found, don't skip anything
					return endIndex;
				}

				private boolean inRange(char toCheck, char lowerBound, char upperBound) {
					return (toCheck >= lowerBound && toCheck <= upperBound);
				}
			};
		}

	}
}
