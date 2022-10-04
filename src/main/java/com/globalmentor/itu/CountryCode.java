/*
 * Copyright © 1996-2008 GlobalMentor, Inc. <https://www.globalmentor.com/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.globalmentor.itu;

import java.util.Locale;

/**
 * Country code information for international public telecommunication numbers for geographic areas as defined in ITU-T E.164, "The international public
 * telecommunication numbering plan". Information from Annex to ITU Operational Bulletin No. 763 — 1.V.2002, "List of ITU-T Recommendation E.164 Assigned
 * Country Codes (Position on 1 May 2002)". Country codes are associated with country codes of ISO 3166 at
 * <a href="https://datahub.io/core/country-codes">Comprehensive country codes: ISO 3166, ITU, ISO 4217 currency codes and many more</a>.
 * @see <a href="https://en.wikipedia.org/wiki/ISO_3166">ISO 3166</a>
 * @author Garret Wilson
 */
public enum CountryCode { //TODO complete list

	/** United States. */
	US(1),

	/** Canada. */
	CA(1);

	/** The value of this telephone country code. */
	private final int value;

	/** @return The value of this telephone country code. */
	public int getValue() {
		return value;
	}

	/**
	 * Value constructor.
	 * @param value The value of the telephone country code.
	 */
	CountryCode(final int value) {
		this.value = value; //save the value
	}

	/**
	 * Retrieves a country code based upon the given locale's country, if present. The country code is determined by the locale's country, if present.
	 * @param locale The locale from which to determine a country code.
	 * @return A country code for the given locale, or <code>null</code> if this locale has no country designation or if no country code could be determined.
	 * @see Locale#getCountry()
	 */
	public static CountryCode getCountryCode(final Locale locale) {
		try {
			return valueOf(locale.getCountry()); //see if we have a country code to match the locale's country, which will be two uppercase letters or the empty string, and never null
		} catch(final IllegalArgumentException illegalArgumentException) { //if we have no matching country code
			return null; //indicate that we couldn't find a country cod
		}
	}

}
