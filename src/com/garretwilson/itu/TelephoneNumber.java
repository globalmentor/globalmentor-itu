package com.garretwilson.itu;

import java.net.URI;
import java.util.*;
import com.garretwilson.text.CharacterConstants;
import com.garretwilson.util.*;

/**International public telecommunication number for geographic areas
	as defined in ITU-T E.164,
	"The international public telecommunication numbering plan".
	The telephone number is formatted according to ITU-T E.123,
	"Notation for national and international telephone numbers, e-mail addresses
	and Web addresses".
TODO update comments to match the new TEL URI RFC when it comes out
@author Garret Wilson
*/
public class TelephoneNumber extends DefaultResource implements TelephoneNumberConstants
{

	/**The country code for geographic areas.*/
	private final String countryCode;

		/**@return The country code for geographic areas.*/
		public String getCountryCode() {return countryCode;}
	
	/**The national destination code.*/
	private final String nationalDestinationCode;

		/**@return The national destination code.*/
		public String getNationalDestinationCode() {return nationalDestinationCode;}
	
	/**The components of the subscriber number.*/
	private final String[] subscriberNumberComponents;

		/**@return The components of the subscriber number.*/
		protected String[] getSubscriberNumberComponents() {return subscriberNumberComponents;}
		
		/**@return The number of subscriber number components.*/
		public int getSubscriberNumberComponentCount() {return subscriberNumberComponents.length;}
		
		/**Determines the subscriber number component at the given index.
		@param index The index of the subscriber number component to return.
		@return The subscriber number component at the given index.
		@exception ArrayIndexOutOfBoundsException Thrown if an invalid index is given.
		*/
		public String getSubscriberNumberComponent(final int index) {return subscriberNumberComponents[index];}

		/**Constructs a string with the subscriber number, separating the components
			with the given delimiter.
		@param delimiter The delimiter to use to separate the subscriber number
			components, or <code>CharacterConstants.NULL_CHAR</code> (Unicode code
			point 0) if no delimiter should be used.
		@return A string representing the subscriber number.
		@see CharacterConstants#NULL_CHAR
		*/ 
		public String getSubscriberNumber(final char delimiter)
		{
			final StringBuffer stringBuffer=new StringBuffer();	//create a string buffer to hold the subscriber number components
			for(int i=0; i<subscriberNumberComponents.length-1; ++i)	//look at each the subscriber number components except the last one
			{
				stringBuffer.append(subscriberNumberComponents[i]);	//append this component
				if(delimiter!=CharacterConstants.NULL_CHAR)	//if the delimiter is not the null character
					stringBuffer.append(delimiter);	//add the delimiter
			}
			if(subscriberNumberComponents.length>0)	//if there are subscriber number components
			{
				stringBuffer.append(subscriberNumberComponents[subscriberNumberComponents.length-1]);	//append the last component
			}
			return stringBuffer.toString();	//return the subscriber number we constructed 
		}
		
		/**@return A string representing the subscriber number as specified in ITU-T E.123.*/ 
		public String getSubscriberNumber()
		{
			return getSubscriberNumber(COMPONENT_SEPARATOR);	//create the subscriber number, using a space as a delimiter
		}

	/**Constructs a telephone number from its separate components.
	@param cc The country code for geographic areas.
	@param ndc The national destination code
	@param sn The subscriber number.
	@exception TelephoneNumberSyntaxException Thrown if the values violate ITU-T
		E.164.
	*/
	public TelephoneNumber(final String cc, final String ndc, final String sn) throws TelephoneNumberSyntaxException
	{
		countryCode=cc;	//save the country code
		nationalDestinationCode=ndc;	//save the national destination code
		final StringTokenizer tokenizer=new StringTokenizer(sn, "-"+COMPONENT_SEPARATOR);	//divide the subscriber number string into tokens
		subscriberNumberComponents=new String[tokenizer.countTokens()];	//create an array of strings for our subscriber number components
		for(int i=0; i<subscriberNumberComponents.length; ++i)	//fill the subscriber number components
		{
			//G***check this subscriber number componet
			subscriberNumberComponents[i]=tokenizer.nextToken();		//get this subscriber number component 
		}
		setReferenceURI(URI.create("tel:"+toString(CharacterConstants.NULL_CHAR)));	//construct and set the reference URI 
	}

	/**Constructs a telephone number by parsing the given string.
	Expects the country code to begin with '+' and accepts code field delimiters
		of '-' and ' '. 
	@param string The string to be parsed into a telephone number.
	@exception TelephoneNumberSyntaxException Thrown if the value violates ITU-T
		E.164.
	*/
	public TelephoneNumber(final String string) throws TelephoneNumberSyntaxException
	{
		if(string.length()==0 || string.charAt(0)!=INTERNATIONAL_PREFIX)	//if the string doesn't start with the international prefix
			throw new TelephoneNumberSyntaxException(string, "Telephone number nissing international prefix (+)", 0);	//indicate a missing international prefix
		final StringTokenizer tokenizer=new StringTokenizer(string.substring(1), "-"+COMPONENT_SEPARATOR);	//divide the string into tokens
		if(tokenizer.hasMoreTokens())	//if there is a country code
		{
			countryCode=tokenizer.nextToken();	//save the country code G***check the value
			if(tokenizer.hasMoreTokens())	//if there is a national destination code
			{
				nationalDestinationCode=tokenizer.nextToken();	//save the national destination code G***check the value
				if(tokenizer.hasMoreTokens())	//if there is a subscriber number
				{
					subscriberNumberComponents=new String[tokenizer.countTokens()];	//create an array of strings for our subscriber number components
					for(int i=0; i<subscriberNumberComponents.length; ++i)	//fill the subscriber number components
					{
						//G***check this subscriber number componet
						subscriberNumberComponents[i]=tokenizer.nextToken();		//get this subscriber number component 
					}
				}
				else	//if there is no subscriber number
				{
					throw new TelephoneNumberSyntaxException(string, "Telephone number missing subscriber number");	//indicate a missing international prefix
				}
			}
			else	//if there is no country code
			{
				throw new TelephoneNumberSyntaxException(string, "Telephone number missing national destination code");	//indicate a missing international prefix
			}
		}
		else	//if there is no country code
		{
			throw new TelephoneNumberSyntaxException(string, "Telephone number missing country code");	//indicate a missing international prefix
		}
		setReferenceURI(URI.create("tel:"+toString(CharacterConstants.NULL_CHAR)));	//construct and set the reference URI 
	}

	/**Constructs a string representation of the telephone number.
	@param delimiter The delimiter to use to separate the telephone number
		components, or <code>CharacterConstants.NULL_CHAR</code> (Unicode code
		point 0) if no delimiter should be used.
	@return A string representation of the telephone number using the specified
		delimiter.
	@see CharacterConstants#NULL_CHAR
	*/
	public String toString(final char delimiter)
	{
		final StringBuffer stringBuffer=new StringBuffer();	//create a string buffer to hold the telephone number
		stringBuffer.append(INTERNATIONAL_PREFIX).append(getCountryCode());	//append the country code
		if(delimiter!=CharacterConstants.NULL_CHAR)	//if the delimiter is not the null character
			stringBuffer.append(delimiter);	//add the delimiter
		stringBuffer.append(getNationalDestinationCode());	//append the national destination code
		if(delimiter!=CharacterConstants.NULL_CHAR)	//if the delimiter is not the null character
			stringBuffer.append(delimiter);	//add the delimiter
		stringBuffer.append(getSubscriberNumber(delimiter));	//append the subscriber number, using the given delimiter
		return stringBuffer.toString();	//return the telephone number we constructed 
	}

	/**@return A string representation of the telephone number as specified in ITU-T E.123.*/ 
	public String toCanonicalString()
	{
		return toString(COMPONENT_SEPARATOR);	//return the string, using a space as a delimiter
	}

	/**@return A string representation of the telephone number as specified in ITU-T E.123.*/ 
	public String toString()
	{
		return toCanonicalString();	//return the canonical telephone number string
	}
}
