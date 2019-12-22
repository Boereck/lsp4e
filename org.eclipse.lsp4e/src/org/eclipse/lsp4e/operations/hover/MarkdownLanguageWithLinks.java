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
		 * Regex based on characters mentioned in RFC-3986: https://www.ietf.org/rfc/rfc3986.txt
		 * Note that this Regex does not check for a completely valid HTTP link, it only checks
		 * for the {@code http(s)://} prefix and if the following characters are valid for URLs.
		 */
		// \b does not work, maybe this is because the regex is combined to a larger one.
		private static final String AUTOMATIC_LINK_REGEX = "(?<=^|\\s)(https?://[a-zA-Z0-9:/?#\\[\\]@!$&'\\(\\)\\*+,;=\\-\\._~%]+)(?=$|\\s)"; //$NON-NLS-1$

		@Override
		protected String getPattern(int groupOffset) {
			return AUTOMATIC_LINK_REGEX;
		}

		@Override
		protected int getPatternGroupCount() {
			return 1;
		}

		@Override
		protected PatternBasedElementProcessor newProcessor() {
			return new  PatternBasedElementProcessor() {
				@Override
				public void emit() {
					String href = group(1);
					builder.link(href, href);
				}
			};
		}

	}
}
