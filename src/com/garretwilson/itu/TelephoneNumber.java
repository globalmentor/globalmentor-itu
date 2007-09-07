package com.garretwilson.itu;

import java.net.URI;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.garretwilson.itu.CountryCode.*;
import static com.garretwilson.lang.ObjectUtilities.*;
import static com.garretwilson.lang.StringBuilderUtilities.*;

import com.garretwilson.lang.ObjectUtilities;
import com.garretwilson.model.*;
import com.garretwilson.net.Resource;
import com.garretwilson.text.ArgumentSyntaxException;
import com.garretwilson.text.CharacterConstants;
import static com.garretwilson.text.CharacterConstants.*;
import static com.garretwilson.text.FormatUtilities.*;
import com.garretwilson.util.*;

/**International public telecommunication number for geographic areas as defined in ITU-T E.164,
	"The international public telecommunication numbering plan".
The telephone number is formatted according to ITU-T E.123,
	"Notation for national and international telephone numbers, e-mail addresses and Web addresses".
This implementation does not support extensions.
The NDC and SN digits will be canonicized for the following country codes:
<ul>
	<li>1 (US, CA)</li>
</ul>
TODO update comments to match the TEL URI RFC 3966
@author Garret Wilson
*/
public class TelephoneNumber implements Resource, Comparable<TelephoneNumber>
{

	/**The international prefix symbol of a telephone number ('+').*/	
	public final static char INTERNATIONAL_PREFIX_SYMBOL='+';
	
	/**The separator for international telephone number components (' ').*/
	public final static char COMPONENT_SEPARATOR=' ';

	/**Symbols allowed for spacing between components.*/
	public final static String SPACING_SYMBOLS="-."+COMPONENT_SEPARATOR;

	/**The regular expression pattern for splitting out components based upon spacing symbols.*/
	protected final static Pattern SPACING_PATTERN=Pattern.compile("["+SPACING_SYMBOLS+"]+");

	/**The regular expression pattern for a Country Code (CC): '+' followed by one to three digits.
	The first matching group will be the actual country code digits.
	*/
	public final static Pattern CC_PATTERN=Pattern.compile("\\+(\\d{1,3})");	//E.164 6.2.1

	/**The regular expression pattern for a National Destination Code (NDC): up to 14 digits, optionally surrounded by parenthesis.
	The first matching group, if non-<code>null</code>, will be the actual national destination code digits without parenthesis,
	and the second matching group, if non-<code>null</code>, will be the actual national destination code digits with parenthesis.
	*/
	public final static Pattern NDC_PATTERN=Pattern.compile("(\\d{1,14})|(?:\\((\\d{1,14})\\))");	//E.164 6.2.1

	/**The regular expression pattern for a Subscriber Number (SN) component: up to 14 digits.*/
	public final static Pattern SN_COMPONENT_PATTERN=Pattern.compile("\\d{1,14}");	//E.164 6.2.1

	/**Returns whether this telephone number is a global telephone number.
	This method returns <code>true</code> if there is a known country code.
	@return <code>true</code> if this is a global telephone number, else <code>false</code> if it is only a local telephone number.
	*/
	public boolean isGlobal() {return cc>=0;}
	
	/**The Country Code (CC) for geographic areas, or -1 if this is a local number.*/
	private final int cc;

		/**@return The Country Code (CC) for geographic areas, or -1 if this is a local number.*/
		public int getCC() {return cc;}

		/**@return The Country Code (CC) string for geographic areas, or <code>null</code> if this is a local number.*/
		public String getCCString() {return cc>=0 ? Integer.toString(cc) : null;}
		
	/**The National Destination Code (NDC), or -1 if there is no NDC.*/
	private long ndc=-1;

		/**@return The National Destination Code (NDC), or -1 if there is no NDC.*/
		public long getNDC() {return ndc;}

		/**The National Destination Code (NDC) string, or <code>null</code> if there is no NDC.*/
		private String ndcString=null;
		
		/**@return The National Destination Code (NDC) string, or <code>null</code> if there is no NDC.*/
		public String getNDCString() {return ndcString;}

		/**Sets the NDC string and updates the NDC value to match.
		@param ndcString The string representing the NDC, or <code>null</code> if there should be no NDC
		*/
		private void setNDCString(final String ndcString)
		{
			this.ndcString=ndcString;	//save the NCD string
			ndc=ndcString!=null ? Long.parseLong(ndcString) : -1;	//get the NDC value, if there is one
		}
		
	/**The Subscriber Number (SN).*/
//TODO bring back, maybe	private final long sn;

		/**@return The Subscriber Number (SN).*/
//TODO bring back, maybe		public long getSN() {return sn;}

	/**The components of the Subscriber Number (SN).*/
	private long[] snComponents;

		/**@return The components of the Subscriber Number (SN).*/
		public long[] getSNComponents() {return snComponents.clone();}

		/**The component strings of the Subscriber Number (SN).*/
		private String[] snComponentStrings;

		/**@return The component strings of the Subscriber Number (SN).*/
		public String[] getSNComponentStrings() {return snComponentStrings.clone();}

		/**Updates the SN component strings and updates the SN component values to match.
		@param snComponentStrings The strings representing the SN components.
		*/
		private void setSNComponentStrings(final String... snComponentStrings)
		{
			this.snComponentStrings=snComponentStrings.clone();	//save a copy of the SN component strings
			final int snComponentCount=snComponentStrings.length;	//see how many SN components there are
			snComponents=new long[snComponentCount];	//create an array of SN components
			for(int i=0; i<snComponentCount; ++i)	//for each SN component
			{
				snComponents[i]=Long.parseLong(snComponentStrings[i]);	//parse this SN component value from the SN component string
			}
		}
		
		/**@return The component strings of the Subscriber Number (SN).*/
/*TODO del
		public String[] getSNComponentStrings()
		{
			final int snComponentCount=snComponents.length;	//see how many SN components there are
			final String[] snComponentStrings=new String[snComponentCount];	//create an array of SN component strings
			for(int i=0; i<snComponentCount; ++i)	//for each SN component
			{
				snComponentStrings[i]=Long.toString(snComponents[i]);	//create a string for this component
			}
			return snComponentStrings;	//return the strings we constructed
		}
*/

	/**Constructs a string with the Subscriber Number (SN), separating the components with the given delimiter.
	@param delimiter The delimiter to use to separate the subscriber number components, or {@link CharacterConstants#NULL_CHAR} (Unicode code point 0) if no delimiter should be used.
	@return A string representing the subscriber number.
	@see CharacterConstants#NULL_CHAR
	*/
	public String getSNString(final char delimiter)
	{
		return formatList(new StringBuilder(), delimiter, getSNComponentStrings()).toString();	//format the SN components into a list
	}
	
	/**@return A string representing the Subscriber Number (SN) with component separated by spaces as specified in ITU-T E.123.*/ 
	public String getSNString()
	{
		return getSNString(COMPONENT_SEPARATOR);	//create the subscriber number, using a space as a delimiter
	}

	/**Constructs a telephone number from its separate components.
	@param cc The country code for geographic areas.
	@param ndc The national destination code
	@param sn The subscriber number.
	@exception ArgumentSyntaxException Thrown if the value violates ITU-T E.164 or ITU-T E.123.
	*/
	public TelephoneNumber(final String cc, final String ndc, final String sn) throws ArgumentSyntaxException
	{
		throw new UnsupportedOperationException("TODO: implement components constructor");
		/*TODO fix later if needed
			//TODO check each portion of the telephone number for validity
		if(cc==null || cc.length()==0)	//if there is no country code
			throw new SyntaxException(cc, "Telephone number missing country code.");	//indicate a missing international prefix
		countryCode=cc;	//save the country code
		if(ndc==null || ndc.length()==0)	//if there is no national destination code
			throw new SyntaxException(ndc, "Telephone number missing national destination code.");	//indicate a missing international prefix
		nationalDestinationCode=ndc;	//save the national destination code
		if(sn==null || sn.length()==0)	//if there is no subscriber number
			throw new SyntaxException(sn, "Telephone number missing subscriber number.");	//indicate a missing international prefix
		final StringTokenizer tokenizer=new StringTokenizer(sn, "-"+COMPONENT_SEPARATOR);	//divide the subscriber number string into tokens
		subscriberNumberComponents=StringTokenizerUtilities.getTokens(tokenizer);	//get the subscriber number components
		setReferenceURI(URI.create("tel:"+toString(CharacterConstants.NULL_CHAR)));	//construct and set the reference URI 
*/
	}

	/**Constructs a telephone number from a string with no default country code.
	The string must either be a global number beginning with '+' or a local number.
	The National Destination Code (NDC) may optionally be surrounded by parentheses.
	The components of the number may be separated by '-', '.', or ' '. 
	@param input The character sequence to be parsed into a telephone number.
	@exception NullPointerException if the given character sequence is <code>null</code>.
	@exception ArgumentSyntaxException if the value violates ITU-T E.164 or ITU-T E.123.
	*/
	public TelephoneNumber(final CharSequence input) throws ArgumentSyntaxException
	{
		this(input, null);	//construct the telephone number with no default country code
	}

	/**Constructs a telephone number by parsing the given string, with a default country code.
	The string must either be a global number beginning with '+' or a local number.
	The National Destination Code (NDC) may optionally be surrounded by parentheses.
	The components of the number may be separated by '-', '.', or ' '. 
	@param telephoneNumber The character sequence to be parsed into a telephone number.
	@param defaultCC The Country Code (CC) to use by default, or <code>null</code> if no default country code is provided.
	@exception NullPointerException if the given character sequence is <code>null</code>.
	@exception ArgumentSyntaxException if the value violates ITU-T E.164 or ITU-T E.123.
	*/
	public TelephoneNumber(final CharSequence telephoneNumber, final CountryCode defaultCC) throws ArgumentSyntaxException
	{
		this(telephoneNumber, defaultCC!=null ? defaultCC.getValue() : -1);	//construct the telephone number with the default country code value, if given
	}

	/**Constructs a telephone number by parsing the given string, with a default country code.
	The string must either be a global number beginning with '+' or a local number.
	The National Destination Code (NDC) may optionally be surrounded by parentheses.
	The components of the number may be separated by '-', '.', or ' '. 
	@param telephoneNumber The character sequence to be parsed into a telephone number.
	@param defaultCC The Country Code (CC) to use by default, or -1 if no default country code is provided.
	@exception NullPointerException if the given character sequence is <code>null</code>.
	@exception ArgumentSyntaxException if the value violates ITU-T E.164 or ITU-T E.123.
	*/
	public TelephoneNumber(final CharSequence telephoneNumber, final int defaultCC) throws ArgumentSyntaxException
	{
		final StringBuilder telephoneNumberStringBuilder=new StringBuilder(checkInstance(telephoneNumber, "Telephone number character sequence must not be null."));	//we may need to manipulate the telephone number, first
			//canonicize the country code, if it is the default country code without a country code symbol
		if(defaultCC>=0)	//if a default country code was given
		{
			final String defaultCCString=Integer.toString(defaultCC);	//convert the default CC to a string
			int nonZeroIndex=telephoneNumberStringBuilder.length()>0 && telephoneNumberStringBuilder.charAt(0)==INTERNATIONAL_PREFIX_SYMBOL ? 1 : 0;	//skip any preceeding '+'
			nonZeroIndex=indexNotOf(telephoneNumberStringBuilder, '0', nonZeroIndex);	//find the first character that izn't a zero digit, skipping the leading '+' if we need to
			final int testCCEnd=nonZeroIndex+defaultCCString.length();	//we'll test this character all the way to what we would think should be the last character, based upon our default CC
			if(nonZeroIndex>=0 && testCCEnd<=telephoneNumberStringBuilder.length() && defaultCCString.equals(telephoneNumberStringBuilder.substring(nonZeroIndex, testCCEnd)))	//if there is a non-zero digit that begins a description of the default country code
			{
				telephoneNumberStringBuilder.replace(0, testCCEnd, String.valueOf(INTERNATIONAL_PREFIX_SYMBOL)+defaultCCString+COMPONENT_SEPARATOR);	//canonicize the country code to "+X[X[X]]"
			}
		}
			//make sure there is a delimiter before a left parenthesis
		final int leftParenthesisIndex=indexOf(telephoneNumberStringBuilder, '(');	//see if there is a left parenthesis in the string
			//if there is a left parenthesis that isn't at the beginning of the string and isn't preceded by a spacing symbol
		if(leftParenthesisIndex>0 && SPACING_SYMBOLS.indexOf(telephoneNumberStringBuilder.charAt(leftParenthesisIndex-1))<0)
		{
			telephoneNumberStringBuilder.insert(leftParenthesisIndex-1, COMPONENT_SEPARATOR);	//insert a delimiter before the left parenthesis
		}
			//make sure there is a delimiter after a right parenthesis
		final int rightParenthesisIndex=indexOf(telephoneNumberStringBuilder, ')');	//see if there is a right parenthesis in the string
			//if there is a right parenthesis that isn't at the end of the string and isn't followed by a spacing symbol
		if(rightParenthesisIndex>=0 && rightParenthesisIndex<telephoneNumberStringBuilder.length()-1 && SPACING_SYMBOLS.indexOf(telephoneNumberStringBuilder.charAt(rightParenthesisIndex+1))<0)
		{
			telephoneNumberStringBuilder.insert(rightParenthesisIndex+1, COMPONENT_SEPARATOR);	//insert a delimiter after the right parenthesis
		}
		int cc=defaultCC;
		String ndcString=null;
		final String[] snComponentStrings;	//we'll calculate the SN component strings and place them here
		final int snComponentCount;	//we'll find out how many subscriber number components there are
		//TODO make sure the sequence doesn't start with a space
		final String[] components=SPACING_PATTERN.split(telephoneNumberStringBuilder);	//get the components of the telephone number, using any modifications we made up front
//TODO del Debug.trace("Found components", Arrays.toString(components));
		final int componentCount=components.length;	//find out the number of components
		int componentIndex=0;	//start at the first component
			//country code
		if(componentCount>componentIndex)	//if there is at least one component
		{
//TODO fix			final String component=components[0];	//get the first component
//TODO fix			final int ndcIndex;	//we'll determine the index of the national destination code
			final Matcher ccMatcher=CC_PATTERN.matcher(components[componentIndex]);	//get a matcher to check for country code
			if(ccMatcher.matches())	//if the component is the country code
			{
				final String ccString=ccMatcher.group(1);	//get the country code string
				cc=Integer.parseInt(ccString);	//parse the country code, overriding the given default country code
				++componentIndex;	//go to the next component
			}
				//NDC or SN
			if(componentCount>componentIndex)	//if there is an NDC or SN
			{
				if(componentCount>componentIndex+1)	//if this is not the last component, calculate the NDC
				{
					final Matcher ndcMatcher=NDC_PATTERN.matcher(components[componentIndex]);	//get a matcher to check for NDC
					if(!ndcMatcher.matches())	//if this is not an NDC
					{
						throw new ArgumentSyntaxException("Invalid NDC: "+components[componentIndex]);
					}
					ndcString=ndcMatcher.group(1);	//get the NDC string
					if(ndcString==null)	//if the parentheses group matched instead
					{
						ndcString=ndcMatcher.group(2);	//get the NDC string inside the parentheses
					}
					++componentIndex;	//go to the next component
				}
				snComponentCount=componentCount-componentIndex;	//find out how many subscriber number components there are
				snComponentStrings=new String[snComponentCount];	//create an array of SN components
				for(int i=0; i<snComponentCount; ++i)	//for each remaining component (each of which is a SN component)
				{
					final String snComponent=components[componentIndex+i];	//get this SN component
					final Matcher snComponentMatcher=SN_COMPONENT_PATTERN.matcher(snComponent);	//get a matcher to check for SN component
					if(!snComponentMatcher.matches())	//if this is not an SN component
					{
						throw new ArgumentSyntaxException("Invalid SN component: "+snComponent);
					}
					snComponentStrings[i]=snComponent;	//save this SN component
				}
			}
			else	//if there is no NDC or SN
			{
				throw new ArgumentSyntaxException("Missing Subscriber Number (SN): "+telephoneNumber);
			}
		}
		else
		{
			throw new ArgumentSyntaxException("No telephone number content: "+telephoneNumber);
		}
		this.cc=cc;	//save the CC
		setNDCString(ndcString);	//save the NDC and NDC string
		setSNComponentStrings(snComponentStrings);	//save the SN components and SN component strings
		canonicize();	//canonicize the telephone number if we can
//TODO del		setReferenceURI(URI.create("tel:"+getSimpleString('-')));	//construct and set the reference URI TODO fix to work with TEL URI RFC TODO don't use a plain string TODO del plain string routines 
	}

	/**Canonicizes the components of the telephone number, if possible, based upon the country code.
	This implementation canonicizes numbers in country code 1.
	@exception ArgumentSyntaxException if the telephone number is not correct for the country code.
	*/
	protected void canonicize() throws ArgumentSyntaxException
	{
		switch(getCC())	//see which country code we have
		{
			case 1:	//US and Canada
				if(ndc<100 && ndc>999 || snComponents.length!=2 || snComponents[0]<100 || snComponents[0]>999 || snComponents[1]<1000 || snComponents[1]>9999)	//if the number isn't in the form (XXX) XXX-XXXX
				{
					final StringBuilder stringBuilder=new StringBuilder();	//create a string builder
					final String ndcString=getNDCString();	//get the NDC, if any
					if(ndcString!=null)	//if there is an NDC
					{
						stringBuilder.append(ndcString);	//append the NDC
					}
					append(stringBuilder, getSNComponentStrings());	//append the SN components
					if(stringBuilder.length()!=10)	//if there aren't 10 digits altogether
					{
						throw new ArgumentSyntaxException("Incorrect number of digits for country code 1 telephone number: "+stringBuilder);
					}
					setNDCString(stringBuilder.substring(0, 3));	//save the NDC (area code): digits 1-3
					setSNComponentStrings(stringBuilder.substring(3, 6), stringBuilder.substring(6, 10));	//save the SN components and SN component strings
				}
				break;
		}
	}

	/**Constructs a string representation of the telephone number in international format according to ITU-T E.123 using the given delimiter.
	This method must only be called on a global telephone number.
	@param delimiter The delimiter to use to separate the telephone number components, or {@link CharacterConstants#NULL_CHAR} (Unicode code point 0) if no delimiter should be used.
	@return An international string representation of the telephone number using the specified delimiter.
	@exception IllegalStateException if this is not a global telephone number.
	@see CharacterConstants#NULL_CHAR
	@see #isGlobal() 
	*/
	protected String getInternationalString(final char delimiter)
	{
		if(!isGlobal())	//if this isn't a global telephone number
		{
			throw new IllegalStateException("International string cannot be constructed for a local telephone number.");
		}
		final StringBuilder stringBuilder=new StringBuilder();	//create a string buffer to hold the telephone number
		stringBuilder.append(INTERNATIONAL_PREFIX_SYMBOL).append(getCCString());	//append the country code
		if(delimiter!=NULL_CHAR)	//if the delimiter is not the null character
			stringBuilder.append(delimiter);	//add the delimiter
		final long ndc=getNDC();	//get the national destination code, if there is one; don't use the string, as it may be prefixed by zeros
		if(ndc>=0)	//if there is an NDC
		{
			stringBuilder.append(ndc);	//append the national destination code
			if(delimiter!=NULL_CHAR)	//if the delimiter is not the null character
				stringBuilder.append(delimiter);	//add the delimiter
		}
		stringBuilder.append(getSNString(delimiter));	//append the subscriber number, using the given delimiter
		return stringBuilder.toString();	//return the telephone number we constructed 
	}

	/**Constructs a string representation of the telephone number in national format according to ITU-T E.123 using the given delimiter.
	If a simple national string is requested, no parentheses are used for the NDC and the given delimiter is used following the NDC.
	Otherwise, a space is used after the NDC, regardless of the delimiter requested. 
	@param delimiter The delimiter to use to separate the telephone number components, or {@link CharacterConstants#NULL_CHAR} (Unicode code point 0) if no delimiter should be used.
	@return A national string representation of the telephone number using the specified delimiter.
	@see CharacterConstants#NULL_CHAR
	*/
	protected String getNationalString(final char delimiter, final boolean simple)
	{
		final StringBuilder stringBuilder=new StringBuilder();	//create a string buffer to hold the telephone number
		final String ndcString=getNDCString();	//get the national destination code, if there is one
		if(ndcString!=null)	//if there is an NDC string
		{
			if(!simple)	//if this should not be simple
			{
				stringBuilder.append('(');	//(
			}
			stringBuilder.append(ndcString);	//append the national destination code
			if(simple)	//if this should be a simple national string
			{
				if(delimiter!=NULL_CHAR)	//if the delimiter is not the null character
					stringBuilder.append(delimiter);	//add the delimiter				
			}
			else	//if this should not be simple
			{
				stringBuilder.append(')');	//)
				stringBuilder.append(' ');	//separate the NDC with a space, regardless of the requested delimiter
			}
		}
		stringBuilder.append(getSNString(delimiter));	//append the subscriber number, using the given delimiter
		return stringBuilder.toString();	//return the telephone number we constructed 
	}

	
	/**Constructs a string representation of the telephone number in national format according to ITU-T E.123 using the given delimiter.
	A space is used after the NDC, regardless of the delimiter requested. 
	@param delimiter The delimiter to use to separate the telephone number components, or {@link CharacterConstants#NULL_CHAR} (Unicode code point 0) if no delimiter should be used.
	@return A national string representation of the telephone number using the specified delimiter.
	@see CharacterConstants#NULL_CHAR
	*/
	public String getNationalString(final char delimiter)
	{
		return getNationalString(delimiter, false);	//return a normal national string using the delimiter
	}

	/**Constructs a string representation of the telephone number in national format according to ITU-T E.123 using the recommended component separator, a space.
	@return A national string representation of the telephone number.
	@see #COMPONENT_SEPARATOR
	*/
	public String getNationalString()
	{
		return getNationalString(COMPONENT_SEPARATOR);	//return the national string, using a space as a delimiter
	}

	/**Constructs a simple string representation of the telephone number in national format using the given delimiter.
	@param delimiter The delimiter to use to separate the telephone number components, or {@link CharacterConstants#NULL_CHAR} (Unicode code point 0) if no delimiter should be used.
	@return A national string representation of the telephone number using the specified delimiter.
	@see CharacterConstants#NULL_CHAR
	*/
	public String getSimpleNationalString(final char delimiter)
	{
		return getNationalString(delimiter, true);	//return a normal national string using the delimiter
	}

	/**Constructs a simple string representation of the telephone number in national format with no delimiter.
	@return A national string representation of the telephone number.
	@see CharacterConstants#NULL_CHAR
	*/
/*TODO del; loses information
	public String getPlainNationalString()
	{
		return getSimpleNationalString(NULL_CHAR);	//return a simple national string using no delimiter
	}
*/

	/**Constructs a string representation of the telephone number in international format if possible.	
	@param delimiter The delimiter to use to separate the telephone number components, or {@link CharacterConstants#NULL_CHAR} (Unicode code point 0) if no delimiter should be used.
	@return A string representation of the telephone number using the specified delimiter.
	*/
	public String getString(final char delimiter)
	{
		if(isGlobal())	//if this is a global telephone number
		{
			return getInternationalString(delimiter);	//return an international string
		}
		else	//if this is a local telephone number
		{
			return getNationalString(delimiter);	//return a national string
		}
	}

	/**Constructs a string representation of the telephone number as specified in ITU-T E.123, using international format if possible.
	@return A string representation of the telephone number as specified in ITU-T E.123. 
	@see #COMPONENT_SEPARATOR
	*/
	public String getCanonicalString()
	{
		return getString(COMPONENT_SEPARATOR);	//return the string, using a space as a delimiter
	}

	/**Constructs a simple string representation of the telephone number in international format if possible.	
	@param delimiter The delimiter to use to separate the telephone number components, or {@link CharacterConstants#NULL_CHAR} (Unicode code point 0) if no delimiter should be used.
	@return A simple string representation of the telephone number using the specified delimiter.
	@see #getSimpleNationalString(char)
	*/
	public String getSimpleString(final char delimiter)
	{
		if(isGlobal())	//if this is a global telephone number
		{
			return getInternationalString(delimiter);	//return an international string
		}
		else	//if this is a local telephone number
		{
			return getSimpleNationalString(delimiter);	//return a national string
		}
	}

	/**Constructs a string representation of the telephone number with no delimiter, using international format if possible.
	@return A plain string representation of the telephone number. 
	*/
/*TODO del; loses information
	public String getPlainString()
	{
		if(isGlobal())	//if this is a global telephone number
		{
			return getInternationalString(NULL_CHAR);	//return an international string without any delimiter
		}
		else	//if this is a local telephone number
		{
			return getPlainNationalString();	//return a plain national string
		}
	}
*/

	/**@return A hash code representing this object.*/
	public int hashCode()
	{
		return ObjectUtilities.hashCode(getCC(), getNDCString(), Arrays.hashCode(getSNComponentStrings()));	//return a hash code for the country code, NDC string, and SN component strings
	}

	/**Determines if this object is equivalent to another object.
	This method considers another object equivalent if it is another telephone number with the same country code, NDC strings, and SN component strings.
	@return <code>true</code> if the given object is an equivalent telephone number.
	*/
	public boolean equals(final Object object)
	{
		if(object instanceof TelephoneNumber)	//if the other object is a telephone number
		{
			final TelephoneNumber telephoneNumber=(TelephoneNumber)object;	//get the other object as a telephone number
			return getCC()==telephoneNumber.getCC() && ObjectUtilities.equals(getNDCString(), telephoneNumber.getNDCString()) && Arrays.equals(getSNComponentStrings(), telephoneNumber.getSNComponentStrings());	//compare CC, NDC string, and SN component strings
		}
		else	//if the other object is not a telephone number
		{
			return false;	//the objects aren't equal
		}
	}

	/**Compares this object with the specified object for order.
	This implementation compares canonical string representations.
	@param telephoneNumber The object to be compared.
	@return A negative integer, zero, or a positive integer as this object is less than, equal to, or greater than the specified object.
	*/
	public int compareTo(final TelephoneNumber telephoneNumber)
	{
		return getCanonicalString().compareTo(telephoneNumber.getCanonicalString());	//compare canonical string representations
	}

	/**Constructs a string representation of the telephone number.
	This implementation returns the canonical version of the telephone number.
	@return A string representation of the telephone number.
	@see #getCanonicalString()
	*/
	public String toString()
	{
		return getCanonicalString();	//return the canonical telephone number string
	}

	//Resource

	/**@return The resource identifier URI, or <code>null</code> if the identifier is not known.*/
	public URI getURI()	//TODO decide if we want to implement resource
	{
		return URI.create("tel:"+getSimpleString('-'));	//construct the reference URI TODO fix to work with TEL URI RFC TODO
	}

	/**Sets the reference URI of the resource.
	@param uri The new reference URI, or <code>null</code> if the identifier is not known.
	*/
	public void setReferenceURI(final URI uri)	//TODO del this from the Resource interface when we can
	{
		throw new UnsupportedOperationException("TelephoneNumber.setReferenceURI() is not supported.");
	}

}
