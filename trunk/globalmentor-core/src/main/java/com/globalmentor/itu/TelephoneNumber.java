/*
 * Copyright © 1996-2012 GlobalMentor, Inc. <http://www.globalmentor.com/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.globalmentor.itu;

import java.net.URI;
import java.util.*;
import java.util.regex.*;

import com.globalmentor.collections.*;
import com.globalmentor.java.CharSequences;
import com.globalmentor.java.Characters;
import com.globalmentor.java.Longs;

import static com.globalmentor.java.Characters.*;
import com.globalmentor.java.Objects;
import com.globalmentor.net.Resource;

import static com.globalmentor.java.Objects.*;
import static com.globalmentor.java.StringBuilders.*;
import static com.globalmentor.java.Strings.*;

import com.globalmentor.text.ArgumentSyntaxException;

import static com.globalmentor.text.RegularExpressions.*;
import static com.globalmentor.text.TextFormatter.*;

/**
 * International public telecommunication number for geographic areas as defined in ITU-T E.164, "The international public telecommunication numbering plan".
 * The default formatting is a string in ITU-T E.164 format with no delimiters except for the introductory international prefix symbol, if any. The label form
 * of the telephone number is formatted according to ITU-T E.123,
 * "Notation for national and international telephone numbers, e-mail addresses and Web addresses".
 * 
 * <p>
 * Following ITU-T E.164, a telephone number is considered to have the following parts: Country Code (CC) and National (Significant) Number (NSN). The NSN is
 * further divided into National Destination Code (NDC) and Subscriber Number (SN).
 * </p>
 * 
 * <p>
 * This implementation does not support extensions.
 * </p>
 * <p>
 * The NDC and SN digits will be canonicized for the following country codes:
 * </p>
 * <ul>
 * <li>1 (US, CA)</li>
 * </ul>
 * TODO update comments to match the TEL URI RFC 3966
 * @author Garret Wilson
 */
public class TelephoneNumber implements Resource, Comparable<TelephoneNumber>
{

	/** The international prefix symbol of a telephone number ('+'). */
	public final static char INTERNATIONAL_PREFIX_SYMBOL = '+';

	/** The separator for international telephone number components (' '). */
	public final static char COMPONENT_SEPARATOR = ' ';

	/** Symbols allowed for spacing between components. */
	public final static Characters SPACING_SYMBOLS = new Characters('-', '.', COMPONENT_SEPARATOR);

	/** The regular expression pattern for splitting out components based upon spacing symbols. */
	protected final static Pattern SPACING_PATTERN = Pattern.compile(createCharacterClass(SPACING_SYMBOLS) + "+");

	/**
	 * The regular expression pattern for a Country Code (CC): '+' followed by one to three digits. The first matching group will be the actual country code
	 * digits.
	 */
	public final static Pattern CC_PATTERN = Pattern.compile(escapePatternString(String.valueOf(INTERNATIONAL_PREFIX_SYMBOL)) + "(\\d{1,3})"); //E.164 6.2.1

	/**
	 * The regular expression pattern for a National Destination Code (NDC): up to 14 digits, optionally surrounded by parenthesis. The first matching group, if
	 * non-<code>null</code>, will be the actual national destination code digits without parenthesis, and the second matching group, if non-<code>null</code>,
	 * will be the actual national destination code digits with parenthesis.
	 */
	public final static Pattern NDC_PATTERN = Pattern.compile("(\\d{1,14})|(?:\\((\\d{1,14})\\))"); //E.164 6.2.1

	/** The regular expression pattern for a Subscriber Number (SN) component: up to 14 digits. */
	public final static Pattern SN_COMPONENT_PATTERN = Pattern.compile("\\d{1,14}"); //E.164 6.2.1

	/**
	 * Strings of country codes that are shorter than three digits long; taken from <cite>List of ITU-T Recommendation E-164 Assigned Country Codes (Position on
	 * 15 April 2009)</cite>, Annex to ITU Operational Bulletin No. 930 – 15.IV.2009.
	 */
	private final static Set<String> SHORT_CC_STRINGS = Sets.immutableSetOf("1", "20", "27", "30", "31", "32", "33", "34", "36", "39", "40", "41", "43", "44",
			"45", "46", "47", "48", "49", "51", "52", "53", "54", "55", "56", "56", "58", "60", "61", "62", "63", "64", "65", "66", "7", "81", "82", "84", "86",
			"90", "91", "92", "93", "94", "95", "98");

	/** The canonical string form of the telephone number, with no delimiters except the beginning international prefix symbol if this is a global number. */
	private final String string;

	/**
	 * Returns whether this telephone number is a global telephone number. This method returns <code>true</code> if there is a known country code.
	 * @return <code>true</code> if this is a global telephone number, else <code>false</code> if it is only a local telephone number.
	 */
	public boolean isGlobal()
	{
		return cc >= 0;
	}

	/** The Country Code (CC) for geographic areas, or -1 if this is a local number. */
	private final int cc;

	/** @return The Country Code (CC) for geographic areas, or -1 if this is a local number. */
	public int getCountryCode()
	{
		return cc;
	}

	/** @return The Country Code (CC) string for geographic areas, or <code>null</code> if this is a local number. */
	public String getCountryCodeString()
	{
		return cc >= 0 ? Integer.toString(cc) : null;
	}

	/** The National Significant Number. */
	private final long nsn;

	/**
	 * Returns the National Significant Number (NSN). Every valid telephone number has an NSN. The NSN may or may not be further divided into its components,
	 * based upon whether these component divisions are known. Similarly the telephone number may or may not have a separate country code provided.
	 * @return The National Significant Number (NSN).
	 */
	public long getNationalNumber()
	{
		return nsn;
	}

	/** @return The National Significant Number (NSN) in canonical string format without delimiters. */
	public String getNationalNumberString()
	{
		return Long.toString(nsn);
	}

	/** The National Destination Code (NDC), or -1 if there is no NDC. */
	private final long ndc;

	/** @return The National Destination Code (NDC), or -1 if there is no NDC. */
	public long getNationalDestinationCode()
	{
		return ndc;
	}

	/** @return The National Destination Code (NDC) string, or <code>null</code> if there is no NDC. */
	public String getNationalDestinationCodeString()
	{
		return ndc >= 0 ? Long.toString(ndc) : null;
	}

	/** The Subscriber Number (SN). */
	//TODO bring back, maybe	private final long sn;

	/** @return The Subscriber Number (SN). */
	//TODO bring back, maybe		public long getSN() {return sn;}

	/** The components of the Subscriber Number (SN). */
	private long[] snComponents;

	/** @return The components of the Subscriber Number (SN). */
	public long[] getSubscriberNumberComponents()
	{
		return snComponents.clone();
	}

	/** The component strings of the Subscriber Number (SN). */
	private String[] snComponentStrings;

	/** @return The component strings of the Subscriber Number (SN). */
	public String[] getSubscriberNumberComponentStrings()
	{
		return snComponentStrings.clone();
	}

	/**
	 * Constructs a string with the Subscriber Number (SN), separating the components with the given delimiter.
	 * @param delimiter The delimiter to use to separate the subscriber number components, or {@link Characters#UNDEFINED_CHAR} if no delimiter should be used.
	 * @return A string representing the subscriber number.
	 * @see Characters#UNDEFINED_CHAR
	 */
	public String getSubscriberNumberString(final char delimiter)
	{
		return formatList(delimiter, getSubscriberNumberComponentStrings()); //format the SN components into a list
	}

	/** @return A string representing the Subscriber Number (SN) with component separated by spaces as specified in ITU-T E.123. */
	public String getSubscriberNumberString()
	{
		return getSubscriberNumberString(COMPONENT_SEPARATOR); //create the subscriber number, using a space as a delimiter
	}

	/**
	 * Constructs a telephone number from its separate components.
	 * @param cc The country code for geographic areas, or <code>null</code> if the country code is not known
	 * @param ndc The national destination code; if no subscriber number components are given, this will be considered the national significant number.
	 * @param snComponents The strings representing the SN components.
	 * @throws NullPointerException if there are no SN components and the NDC is <code>null</code>.
	 * @throws ArgumentSyntaxException Thrown if the value violates ITU-T E.164 or ITU-T E.123.
	 */
	public TelephoneNumber(final String cc, final String ndc, final String... snComponents) throws ArgumentSyntaxException
	{
		final StringBuilder stringBuilder = new StringBuilder();
		if(cc != null) //if a country code was given
		{
			if(!SHORT_CC_STRINGS.contains(cc) && cc.length() != 3) //we must have a record of the country code, or it must be a three-digit country code
			{
				throw new IllegalArgumentException("Invalid Country Code: " + cc);
			}
			this.cc = Integer.parseInt(cc); //store the country code
			stringBuilder.append(INTERNATIONAL_PREFIX_SYMBOL).append(cc);
		}
		else
		//if no country code was given
		{
			this.cc = -1;
		}
		final StringBuilder nsnStringBuilder = new StringBuilder();
		if(snComponents.length > 0) //if there are SN components
		{
			if(ndc != null) //store the NDC if we have one
			{
				this.ndc = Long.parseLong(ndc);
				nsnStringBuilder.append(ndc);
			}
			else
			{
				this.ndc = -1;
			}
			this.snComponentStrings = snComponents.clone(); //save a copy of the SN component strings
			final int snComponentCount = snComponents.length; //see how many SN components there are
			this.snComponents = new long[snComponentCount]; //create an array of SN components
			for(int i = 0; i < snComponentCount; ++i) //for each SN component
			{
				final String snComponent = snComponents[i];
				this.snComponents[i] = Long.parseLong(snComponent); //parse this SN component value from the SN component string
				nsnStringBuilder.append(snComponent);
			}
		}
		else
		//if there are no SN components
		{
			nsnStringBuilder.append(checkInstance(ndc, "Missing National Significant Number.")); //we only know the NSN
			this.ndc = -1; //we don't know the NDC
			this.snComponents = Longs.NO_LONGS; //we don't know any SN components
			this.snComponentStrings = NO_STRINGS;
		}
		this.nsn = Long.parseLong(nsnStringBuilder.toString());
		this.string = stringBuilder.append(nsnStringBuilder).toString();
	}

	/**
	 * Constructs a telephone number from a string with no default country code. The string must either be a global number beginning with '+' or a local number.
	 * The National Destination Code (NDC) may optionally be surrounded by parentheses. The components of the number may be separated by '-', '.', or ' '.
	 * @param input The character sequence to be parsed into a telephone number.
	 * @throws NullPointerException if the given character sequence is <code>null</code>.
	 * @throws ArgumentSyntaxException if the value violates ITU-T E.164 or ITU-T E.123.
	 */
	public TelephoneNumber(final CharSequence input) throws ArgumentSyntaxException
	{
		this(input, null); //construct the telephone number with no default country code
	}

	/**
	 * Constructs a telephone number by parsing the given string, with a default country code. The string must either be a global number beginning with '+' or a
	 * local number. The National Destination Code (NDC) may optionally be surrounded by parentheses. The components of the number may be separated by '-', '.',
	 * or ' '.
	 * @param telephoneNumber The character sequence to be parsed into a telephone number.
	 * @param defaultCC The Country Code (CC) to use by default, or <code>null</code> if no default country code is provided.
	 * @throws NullPointerException if the given character sequence is <code>null</code>.
	 * @throws ArgumentSyntaxException if the value violates ITU-T E.164 or ITU-T E.123.
	 */
	public TelephoneNumber(final CharSequence telephoneNumber, final CountryCode defaultCC) throws ArgumentSyntaxException
	{
		this(telephoneNumber, defaultCC != null ? defaultCC.getValue() : -1); //construct the telephone number with the default country code value, if given
	}

	/**
	 * Constructs a telephone number from a character sequence in canonical format, with a default country code. The string must either be a global number
	 * beginning with '+' or a local number. The National Destination Code (NDC) may optionally be surrounded by parentheses. The components of the number may be
	 * separated by '-', '.', or ' '.
	 * @param telephoneNumber The character sequence to be parsed into a telephone number.
	 * @param defaultCC The Country Code (CC) to use by default, or -1 if no default country code is provided.
	 * @throws NullPointerException if the given character sequence is <code>null</code>.
	 * @throws ArgumentSyntaxException if the value violates ITU-T E.164 or ITU-T E.123.
	 */
	public TelephoneNumber(final CharSequence telephoneNumber, final int defaultCC) throws ArgumentSyntaxException
	{
		if(CharSequences.contains(telephoneNumber, SPACING_SYMBOLS))
		{
			throw new ArgumentSyntaxException("Spacing symbols not allowed in canonical telephone number: " + telephoneNumber);
		}
		string = telephoneNumber.toString(); //get the first and only component
		if(CharSequences.startsWith(string, INTERNATIONAL_PREFIX_SYMBOL)) //if this is an international number, parse out the country code
		{
			String ccString = CharSequences.getStartsWith(string.substring(1), SHORT_CC_STRINGS); //see if the country code is a short string
			if(ccString == null) //if the country code is a long string
			{
				if(string.length() < 4) //there must be enough room for a three-digit country code
				{
					throw new IllegalArgumentException("Invalid Country Code: " + string.substring(1));
				}
				ccString = string.substring(1, 4); //extract the three-digit country code
			}
			cc = Integer.parseInt(ccString); //parse the country code
			nsn = Long.parseLong(string.substring(ccString.length() + 1)); //the NSN is the rest of the number after the '+' and the country code; we don't know its subcomponents
		}
		else
		//if this is a national number
		{
			cc = defaultCC; //we don't know the country code; use the default, if any
			nsn = Long.parseLong(string); //the NSN is all we have, really
		}
		ndc = -1; //we don't know the NDC
		snComponents = Longs.NO_LONGS; //we don't know any SN components
		snComponentStrings = NO_STRINGS;
	}

	/**
	 * Canonicizes the components of the telephone number, if possible, based upon the country code. This implementation canonicizes numbers in country code 1.
	 * @throws ArgumentSyntaxException if the telephone number is not correct for the country code.
	 */
	/*TODO migrate to getLabelString()
		protected void canonicize() throws ArgumentSyntaxException
		{
			switch(getCC())
			//see which country code we have
			{
				case 1: //US and Canada
					if(ndc < 100 && ndc > 999 || snComponents.length != 2 || snComponents[0] < 100 || snComponents[0] > 999 || snComponents[1] < 1000
							|| snComponents[1] > 9999) //if the number isn't in the form (XXX) XXX-XXXX
					{
						final StringBuilder stringBuilder = new StringBuilder(); //create a string builder
						final String ndcString = getNDCString(); //get the NDC, if any
						if(ndcString != null) //if there is an NDC
						{
							stringBuilder.append(ndcString); //append the NDC
						}
						append(stringBuilder, getSNComponentStrings()); //append the SN components
						if(stringBuilder.length() != 10) //if there aren't 10 digits altogether
						{
							throw new ArgumentSyntaxException("Incorrect number of digits for country code 1 telephone number: " + stringBuilder);
						}
						setNDCString(stringBuilder.substring(0, 3)); //save the NDC (area code): digits 1-3
						setSNComponentStrings(stringBuilder.substring(3, 6), stringBuilder.substring(6, 10)); //save the SN components and SN component strings
					}
					break;
			}
		}
	*/

	/**
	 * Constructs a string representation of the telephone number in international format according to ITU-T E.123 using the given delimiter. This method must
	 * only be called on a global telephone number.
	 * @param delimiter The delimiter to use to separate the telephone number components, or {@link Characters#UNDEFINED_CHAR} if no delimiter should be used.
	 * @return An international string representation of the telephone number using the specified delimiter.
	 * @throws IllegalStateException if this is not a global telephone number.
	 * @see Characters#UNDEFINED_CHAR
	 * @see #isGlobal()
	 */
	protected String getInternationalString(final char delimiter)
	{
		if(!isGlobal()) //if this isn't a global telephone number
		{
			throw new IllegalStateException("International string cannot be constructed for a local telephone number.");
		}
		final StringBuilder stringBuilder = new StringBuilder(); //create a string buffer to hold the telephone number
		stringBuilder.append(INTERNATIONAL_PREFIX_SYMBOL).append(getCountryCodeString()); //append the country code
		if(delimiter != UNDEFINED_CHAR) //if the delimiter is not the null character
		{
			stringBuilder.append(delimiter); //add the delimiter
		}
		final long ndc = getNationalDestinationCode(); //get the national destination code, if there is one; don't use the string, as it may be prefixed by zeros
		if(ndc >= 0) //if there is an NDC
		{
			stringBuilder.append(ndc); //append the national destination code
			if(delimiter != UNDEFINED_CHAR) //if the delimiter is not the null character
			{
				stringBuilder.append(delimiter); //add the delimiter
			}
		}
		stringBuilder.append(getSubscriberNumberString(delimiter)); //append the subscriber number, using the given delimiter
		return stringBuilder.toString(); //return the telephone number we constructed 
	}

	/**
	 * Constructs a string representation of the telephone number in national format according to ITU-T E.123 using the given delimiter. If a simple national
	 * string is requested, no parentheses are used for the NDC and the given delimiter is used following the NDC. Otherwise, a space is used after the NDC,
	 * regardless of the delimiter requested.
	 * @param delimiter The delimiter to use to separate the telephone number components, or {@link Characters#UNDEFINED_CHAR} if no delimiter should be used.
	 * @param simple <code>false</code> if the NDC should be surrounded by parentheses.
	 * @return A national string representation of the telephone number using the specified delimiter.
	 * @see Characters#UNDEFINED_CHAR
	 */
	protected String getNationalString(final char delimiter, final boolean simple)
	{
		final StringBuilder stringBuilder = new StringBuilder(); //create a string buffer to hold the telephone number
		final String ndcString = getNationalDestinationCodeString(); //get the national destination code, if there is one
		if(ndcString != null) //if there is an NDC string
		{
			if(!simple) //if this should not be simple
			{
				stringBuilder.append('('); //(
			}
			stringBuilder.append(ndcString); //append the national destination code
			if(simple) //if this should be a simple national string
			{
				if(delimiter != UNDEFINED_CHAR) //if the delimiter is not the null character
				{
					stringBuilder.append(delimiter); //add the delimiter
				}
			}
			else
			//if this should not be simple
			{
				stringBuilder.append(')'); //)
				stringBuilder.append(' '); //separate the NDC with a space, regardless of the requested delimiter
			}
		}
		stringBuilder.append(getSubscriberNumberString(delimiter)); //append the subscriber number, using the given delimiter
		return stringBuilder.toString(); //return the telephone number we constructed 
	}

	/**
	 * Constructs a string representation of the telephone number in national format according to ITU-T E.123 using the given delimiter. A space is used after the
	 * NDC, regardless of the delimiter requested.
	 * @param delimiter The delimiter to use to separate the telephone number components, or {@link Characters#UNDEFINED_CHAR} if no delimiter should be used.
	 * @return A national string representation of the telephone number using the specified delimiter.
	 * @see Characters#UNDEFINED_CHAR
	 */
	public String getNationalString(final char delimiter) //TODO rename to format
	{
		return getNationalString(delimiter, false); //return a normal national string using the delimiter
	}

	/**
	 * Constructs a string representation of the telephone number in national format according to ITU-T E.123 using the recommended component separator, a space.
	 * @return A national string representation of the telephone number.
	 * @see #COMPONENT_SEPARATOR
	 */
	public String getNationalString() //TODO rename to format
	{
		return getNationalString(COMPONENT_SEPARATOR); //return the national string, using a space as a delimiter
	}

	/**
	 * Constructs a simple string representation of the telephone number in national format using the given delimiter.
	 * @param delimiter The delimiter to use to separate the telephone number components, or {@link Characters#UNDEFINED_CHAR} if no delimiter should be used.
	 * @return A national string representation of the telephone number using the specified delimiter.
	 * @see Characters#UNDEFINED_CHAR
	 */
	public String getSimpleNationalString(final char delimiter) //TODO rename to format
	{
		return getNationalString(delimiter, true); //return a normal national string using the delimiter
	}

	/**
	 * Constructs a simple string representation of the telephone number in national format with no delimiter.
	 * @return A national string representation of the telephone number.
	 * @see CharacterConstants#UNDEFINED_CHAR
	 */
	/*TODO del; loses information
		public String getPlainNationalString()
		{
			return getSimpleNationalString(UNDEFINED_CHAR);	//return a simple national string using no delimiter
		}
	*/

	/**
	 * Constructs a human-readable string representation of the telephone number in international format if possible.
	 * @param delimiter The delimiter to use to separate the telephone number components, or {@link Characters#UNDEFINED_CHAR} if no delimiter should be used.
	 * @return A string representation of the telephone number using the specified delimiter.
	 */
	public String getLabel(final char delimiter)
	{
		if(isGlobal()) //if this is a global telephone number
		{
			return getInternationalString(delimiter); //return an international string
		}
		else
		//if this is a local telephone number
		{
			return getNationalString(delimiter); //return a national string
		}
	}

	/**
	 * Constructs a human-readable string representation of the telephone number as specified in ITU-T E.123, using international format if possible.
	 * @return A string representation of the telephone number as specified in ITU-T E.123.
	 * @see #COMPONENT_SEPARATOR
	 */
	public String getLabel()
	{
		return getLabel(COMPONENT_SEPARATOR); //return the string, using a space as a delimiter
	}

	/**
	 * Constructs a simple string representation of the telephone number in international format if possible.
	 * @param delimiter The delimiter to use to separate the telephone number components, or {@link Characters#UNDEFINED_CHAR} if no delimiter should be used.
	 * @return A simple string representation of the telephone number using the specified delimiter.
	 * @see #getSimpleNationalString(char)
	 */
	public String getSimpleString(final char delimiter)
	{
		if(isGlobal()) //if this is a global telephone number
		{
			return getInternationalString(delimiter); //return an international string
		}
		else
		//if this is a local telephone number
		{
			return getSimpleNationalString(delimiter); //return a national string
		}
	}

	/**
	 * Constructs a string representation of the telephone number with no delimiter, using international format if possible.
	 * @return A plain string representation of the telephone number.
	 */
	/*TODO del; loses information
		public String getPlainString()
		{
			if(isGlobal())	//if this is a global telephone number
			{
				return getInternationalString(UNDEFINED_CHAR);	//return an international string without any delimiter
			}
			else	//if this is a local telephone number
			{
				return getPlainNationalString();	//return a plain national string
			}
		}
	*/

	/** @return A hash code representing this object. */
	public int hashCode()
	{
		return Objects.getHashCode(getCountryCode(), getNationalDestinationCodeString(), getSubscriberNumberComponentStrings()); //return a hash code for the country code, NDC string, and SN component strings
	}

	/**
	 * Determines if this object is equivalent to another object. This method considers another object equivalent if it is another telephone number with the same
	 * Country Code and National Significant Number.
	 * @return <code>true</code> if the given object is an equivalent telephone number.
	 */
	public boolean equals(final Object object)
	{
		if(this == object)
		{
			return true;
		}
		if(!(object instanceof TelephoneNumber)) //if the other object is a telephone number
		{
			return false;
		}
		final TelephoneNumber telephoneNumber = (TelephoneNumber)object; //get the other object as a telephone number
		return getCountryCode() == telephoneNumber.getCountryCode() && getNationalNumber() == telephoneNumber.getNationalNumber();
	}

	/**
	 * Compares this object with the specified object for order. This implementation compares first country code and then National Significant Number.
	 * @param telephoneNumber The object to be compared.
	 * @return A negative integer, zero, or a positive integer as this object is less than, equal to, or greater than the specified object.
	 */
	public int compareTo(final TelephoneNumber telephoneNumber)
	{
		int result = getCountryCode() - telephoneNumber.getCountryCode();
		if(result == 0)
		{
			result = Longs.compare(getNationalNumber(), telephoneNumber.getNationalNumber());
		}
		return result;
	}

	/**
	 * Returns the E.164 canonical representation of the telephone number, with no delimiters except the beginning international prefix symbol if this is a global
	 * number.
	 * @return The canonical string representation of the telephone number.
	 */
	public String getCanonicalString()
	{
		return string;
	}

	/**
	 * Returns a string representation of the telephone number.
	 * <p>
	 * This implementation returns the E.164 canonical version of the telephone number, with no delimiters except the beginning international prefix symbol if
	 * this is a global number.
	 * </p>
	 * @return A string representation of the telephone number.
	 * @see #getCanonicalString()
	 */
	public String toString()
	{
		return getCanonicalString();
	}

	//Resource

	/** @return The resource identifier URI, or <code>null</code> if the identifier is not known. */
	public URI getURI() //TODO decide if we want to implement resource
	{
		return URI.create("tel:" + getSimpleString('-')); //construct the reference URI TODO fix to work with TEL URI RFC
	}

	/**
	 * Parses a telephone number into a canonical telephone number, with no default country code. The string must either be a global number beginning with '+' or
	 * a local number. The National Destination Code (NDC) may optionally be surrounded by parentheses. The components of the number may be separated by '-', '.',
	 * or ' '.
	 * @param telephoneNumber The character sequence to be parsed into a telephone number.
	 * @return A canonical telephone number.
	 * @throws NullPointerException if the given character sequence is <code>null</code>.
	 * @throws ArgumentSyntaxException if the value violates ITU-T E.164 or ITU-T E.123.
	 */
	public static TelephoneNumber parse(CharSequence telephoneNumber) throws ArgumentSyntaxException
	{
		return parse(telephoneNumber, -1);
	}

	/**
	 * Parses a telephone number into a canonical telephone number, with a default country code. The string must either be a global number beginning with '+' or a
	 * local number. The National Destination Code (NDC) may optionally be surrounded by parentheses. The components of the number may be separated by '-', '.',
	 * or ' '.
	 * @param telephoneNumber The character sequence to be parsed into a telephone number.
	 * @param defaultCC The Country Code (CC) to use by default, or -1 if no default country code is provided.
	 * @return A canonical telephone number.
	 * @throws NullPointerException if the given character sequence is <code>null</code>.
	 * @throws ArgumentSyntaxException if the value violates ITU-T E.164 or ITU-T E.123.
	 */
	public static TelephoneNumber parse(CharSequence telephoneNumber, final int defaultCC) throws ArgumentSyntaxException
	{
		final StringBuilder telephoneNumberStringBuilder = new StringBuilder(
				checkInstance(telephoneNumber, "Telephone number character sequence must not be null.")); //we may need to manipulate the telephone number, first
		String ccString;
		if(CharSequences.startsWith(telephoneNumberStringBuilder, INTERNATIONAL_PREFIX_SYMBOL)) //if this is an international number, parse out the country code
		{
			ccString = CharSequences.getStartsWith(telephoneNumberStringBuilder, 1, SHORT_CC_STRINGS); //see if the country code is a short string
			if(ccString == null) //if the country code is a long string
			{
				if(telephoneNumberStringBuilder.length() < 4) //there must be enough room for a three-digit country code
				{
					throw new IllegalArgumentException("Invalid Country Code: " + telephoneNumberStringBuilder.substring(1));
				}
				ccString = telephoneNumberStringBuilder.substring(1, 4); //extract the three-digit country code
			}
			telephoneNumberStringBuilder.delete(0, ccString.length() + 1); //remove the country code and delimiter from the beginning
		}
		else
		//if there is no country code
		{
			ccString = defaultCC >= 0 ? Integer.toString(defaultCC) : null; //use the default
		}
		boolean ndcIndicated = false;
		final int leftParenthesisIndex = indexOf(telephoneNumberStringBuilder, '('); //see if there is a left parenthesis in the string
		if(leftParenthesisIndex >= 0) //if there is a left parenthesis
		{
			ndcIndicated = true;
			telephoneNumberStringBuilder.setCharAt(leftParenthesisIndex, COMPONENT_SEPARATOR); //replace the left parenthesis with a separator
		}
		final int rightParenthesisIndex = indexOf(telephoneNumberStringBuilder, ')'); //see if there is a right parenthesis in the string
		if(rightParenthesisIndex >= 0) //if there is a right parenthesis
		{
			telephoneNumberStringBuilder.setCharAt(rightParenthesisIndex, COMPONENT_SEPARATOR); //replace the right parenthesis with a separator
		}
		if(((leftParenthesisIndex >= 0) != (rightParenthesisIndex >= 0)) || leftParenthesisIndex > rightParenthesisIndex)
		{
			throw new ArgumentSyntaxException("Mismatched parentheses: " + telephoneNumber);
		}
		trim(telephoneNumberStringBuilder, SPACING_SYMBOLS); //remove all delimiters from the beginning and end of the string, or they will produce empty components
		final String[] components = SPACING_PATTERN.split(telephoneNumberStringBuilder); //get the components of the telephone number, using any modifications we made up front
		//TODO del Log.trace("Found components", Arrays.toString(components));
		final String ndcString;
		final String[] snComponentStrings;
		final int componentCount = components.length; //find out the number of components
		if(componentCount == 0) //if there are no components at all
		{
			throw new ArgumentSyntaxException("No National Significant Number: " + telephoneNumber);
		}
		else if(componentCount == 1) //if the components aren't separated
		{
			if(ndcIndicated == true)
			{
				throw new IllegalArgumentException("NDC indicated but no subscriber number components provided.");
			}
			ndcString = components[0]; //use the rest of the number as the "NDC" to indicate the NSN
			snComponentStrings = NO_STRINGS;
		}
		else
		//if there are components
		{
			if(ndcIndicated == true)
			{
				ndcString = components[0]; //the first component is the NDC
				snComponentStrings = com.globalmentor.java.Arrays.createCopy(components, 1); //use the other components for the SN components
			}
			else
			//if no NDC was indicated
			{
				ndcString = null; //no NDC was indicated
				snComponentStrings = components.clone();
			}
		}
		return new TelephoneNumber(ccString, ndcString, snComponentStrings); //create the telephone number
	}

}
