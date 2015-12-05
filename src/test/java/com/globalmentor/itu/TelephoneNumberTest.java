/*
 * Copyright © 2011 GlobalMentor, Inc. <http://www.globalmentor.com/>
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

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import org.junit.Test;

import com.globalmentor.text.ArgumentSyntaxException;

/**
 * Tests for telephone numbers.
 * 
 * @author Garret Wilson
 * 
 */
public class TelephoneNumberTest {

	/** Tests that the components of a telephone number are equal to +1 415 555 1212. */
	protected void testTelephoneNumber14155551212(final TelephoneNumber telephoneNumber) {
		assertThat(telephoneNumber.toString(), is("+14155551212"));
		assertThat(telephoneNumber.getCountryCode(), is(1));
		assertThat(telephoneNumber.getCountryCodeString(), is("1"));
		assertThat(telephoneNumber.getNationalNumber(), is(4155551212L));
		assertThat(telephoneNumber.getNationalNumberString(), is("4155551212"));
		//TODO add tests for NDC and SN components once those are implemented for all variations
	}

	@Test
	public void testConstructorInternationalNumbers() {
		testTelephoneNumber14155551212(new TelephoneNumber("+14155551212"));
		try {
			new TelephoneNumber("+1 415 555 1212");
			fail();
		} catch(final ArgumentSyntaxException argumentSyntaxException) {
		}
		try {
			new TelephoneNumber("+1 4155551212");
			fail();
		} catch(final ArgumentSyntaxException argumentSyntaxException) {
		}
		try {
			new TelephoneNumber("+1 (415) 555-1212");
			fail();
		} catch(final ArgumentSyntaxException argumentSyntaxException) {
		}
	}

	@Test
	public void testParseInternationalNumbers() {
		testTelephoneNumber14155551212(TelephoneNumber.parse("+14155551212"));
		testTelephoneNumber14155551212(TelephoneNumber.parse("+1 415 555 1212"));
		testTelephoneNumber14155551212(TelephoneNumber.parse("+1 (415) 555-1212"));
		testTelephoneNumber14155551212(TelephoneNumber.parse("+1.415.555.1212"));
		testTelephoneNumber14155551212(TelephoneNumber.parse("4155551212", 1));
		testTelephoneNumber14155551212(TelephoneNumber.parse("415 555 1212", 1));
		testTelephoneNumber14155551212(TelephoneNumber.parse("(415) 555-1212", 1));
		testTelephoneNumber14155551212(TelephoneNumber.parse("415.555.1212", 1));
	}

}
