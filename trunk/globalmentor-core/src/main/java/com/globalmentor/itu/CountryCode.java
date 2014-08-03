/*
 * Copyright © 1996-2008 GlobalMentor, Inc. <http://www.globalmentor.com/>
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

import java.util.Locale;

/**
 * Country code information for international public telecommunication numbers for geographic areas as defined in ITU-T E.164,
 * "The international public telecommunication numbering plan". Information from Annex to ITU Operational Bulletin No. 763 — 1.V.2002,
 * "List of ITU-T Recommendatoin E.164 Assigned Country Codes (Position on 1 May 2002)". Country codes are associated with country codes of ISO 3166 at <a
 * href="http://userpage.chemie.fu-berlin.de/diverse/doc/ISO_3166.html">ISO 3166 Codes (Countries)</a>.
 * @see <a href="http://userpage.chemie.fu-berlin.de/diverse/doc/ISO_3166.html">ISO 3166 Codes (Countries)</a>
 * @author Garret Wilson
 */
public enum CountryCode {

	US(1),

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

	/*TODO fix
	"AF",	93	Afghanistan (Islamic State of)	
	"AL",	355	Albania (Republic of)	
	"",	213	Algeria (People's Democratic Republic of)	
	"",	684	American Samoa	
	"",	376	Andorra (Principality of)	
	"",	244	Angola (Republic of)	
	"",	1	Anguilla	b
	"",	1	Antigua and Barbuda	b
	"",	54	Argentine Republic	
	"",	374	Armenia (Republic of)	
	"",	297	Aruba	
	"",	247	Ascension	
	"",	61	Australia	i
	"",	672	Australian External Territories	g
	"",	43	Austria	
	"",	994	Azerbaijani Republic	
	"",	1	Bahamas (Commonwealth of the)	b
	"",	973	Bahrain (Kingdom of)	
	"",	880	Bangladesh (People's Republic of)	
	"",	1	Barbados	b
	"",	375	Belarus (Republic of)	
	"",	32	Belgium	
	"",	501	Belize	
	"",	229	Benin (Republic of)	
	"",	1	Bermuda	b
	"",	975	Bhutan (Kingdom of)	
	"",	591	Bolivia (Republic of)	
	"",	387	Bosnia and Herzegovina	
	"",	267	Botswana (Republic of)	
	"",	55	Brazil (Federative Republic of)	
	"",	1	British Virgin Islands	b
	"",	673	Brunei Darussalam	
	"",	359	Bulgaria (Republic of)	
	"",	226	Burkina Faso	
	"",	257	Burundi (Republic of)	
	"",	855	Cambodia (Kingdom of)	
	"",	237	Cameroon (Republic of)	
	"",	1	Canada	b
	"",	238	Cape Verde (Republic of)	
	"",	1	Cayman Islands	b
	"",	236	Central African Republic	
	"",	235	Chad (Republic of)	
	"",	56	Chile	
	"",	86	China (People's Republic of)	
	"",	57	Colombia (Republic of)	
	"",	269	Comoros (Islamic Federal Republic of the)	c
	"",	242	Congo (Republic of the)	
	"",	682	Cook Islands	
	"",	506	Costa Rica	
	"",	225	C�te d'Ivoire (Republic of)	
	"",	385	Croatia (Republic of)	
	"",	53	Cuba	
	"",	357	Cyprus (Republic of)	
	"",	420	Czech Republic	
	"",	850	Democratic People's Republic of Korea	
	"",	243	Democratic Republic of the Congo	
	"",	45	Denmark	
	"",	246	Diego Garcia	
	"",	253	Djibouti (Republic of)	
	"",	1	Dominica (Commonwealth of)	b
	"",	1	Dominican Republic	b
	"",	670	East Timor	
	"",	593	Ecuador	
	"",	20	Egypt (Arab Republic of)	
	"",	503	El Salvador (Republic of)	
		240	Equatorial Guinea (Republic of)	
		291	Eritrea	
		372	Estonia (Republic of)	
		251	Ethiopia (Federal Democratic Republic of)	
		500	Falkland Islands (Malvinas)	
		298	Faroe Islands	
		679	Fiji (Republic of)	
		358	Finland	
		33	France	
		594	French Guiana (French Department of)	
		689	French Polynesia (Territoire fran�ais d'outre-mer)	
		241	Gabonese Republic	
		220	Gambia (Republic of the)	
		995	Georgia	
		49	Germany (Federal Republic of)	
		233	Ghana	
		350	Gibraltar	
		881	Global Mobile Satellite System (GMSS), shared code	k
		30	Greece	
		299	Greenland (Denmark)	
		1	Grenada	b
		388	Group of countries, shared code	n
		590	Guadeloupe (French Department of)	
		1	Guam	b
		502	Guatemala (Republic of)	
		224	Guinea (Republic of)	
		245	Guinea-Bissau (Republic of)	
		592	Guyana	
		509	Haiti (Republic of)	
		504	Honduras (Republic of)	
		852	Hongkong, China	
		36	Hungary (Republic of)	
		354	Iceland	
		91	India (Republic of)	
		62	Indonesia (Republic of)	
		871	Inmarsat (Atlantic Ocean-East)	
		874	Inmarsat (Atlantic Ocean-West)	
		873	Inmarsat (Indian Ocean)	
		872	Inmarsat (Pacific Ocean)	
		870	Inmarsat SNAC	
		800	International Freephone Service	
		882	International Networks, shared code	j
		98	Iran (Islamic Republic of)	
		964	Iraq (Republic of)	
		353	Ireland	
		972	Israel (State of)	
		39	Italy	
		1	Jamaica	b
		81	Japan	
		962	Jordan (Hashemite Kingdom of)	
		7	Kazakstan (Republic of)	b
		254	Kenya (Republic of)	
		686	Kiribati (Republic of)	
		82	Korea (Republic of)	
		965	Kuwait (State of)	
		996	Kyrgyz Republic	
		856	Lao People's Democratic Republic	
		371	Latvia (Republic of)	
		961	Lebanon	
		266	Lesotho (Kingdom of)	
		231	Liberia (Republic of)	
		218	Libya (Socialist People's Libyan Arab Jamahiriya)	
		423	Liechtenstein (Principality of)	
		370	Lithuania (Republic of)	
		352	Luxembourg	
		853	Macao, China	
		261	Madagascar (Republic of)	
		265	Malawi	
		60	Malaysia	
		960	Maldives (Republic of)	
		223	Mali (Republic of)	
		356	Malta	
		692	Marshall Islands (Republic of the)	
		596	Martinique (French Department of)	
		222	Mauritania (Islamic Republic of)	
		230	Mauritius (Republic of)	
		269	Mayotte (Collectivit� territoriale de la R�publique fran�aise)	c
		52	Mexico	
		691	Micronesia (Federated States of)	
		373	Moldova (Republic of)	
		377	Monaco (Principality of)	
		976	Mongolia	
		1	Montserrat	b
		212	Morocco (Kingdom of)	
		258	Mozambique (Republic of)	
		95	Myanmar (Union of)	
		264	Namibia (Republic of)	
		674	Nauru (Republic of)	
		977	Nepal	
		31	Netherlands (Kingdom of the)	
		599	Netherlands Antilles	
		687	New Caledonia (Territoire fran�ais d'outre-mer)	
		64	New Zealand	
		505	Nicaragua	
		227	Niger (Republic of the)	
		234	Nigeria (Federal Republic of)	
		683	Niue	
		1	Northern Mariana Islands (Commonwealth of the)	b
		47	Norway	
		968	Oman (Sultanate of)	
		92	Pakistan (Islamic Republic of)	
		680	Palau (Republic of)	
		507	Panama (Republic of)	
		675	Papua New Guinea	
		595	Paraguay (Republic of)	
		51	Peru	
		63	Philippines (Republic of the)	
		48	Poland (Republic of)	
		351	Portugal	
		1	Puerto Rico	b
		974	Qatar (State of)	
		262	Reunion (French Department of)	
		40	Romania	
		7	Russian Federation	b
		250	Rwandese Republic	
		290	Saint Helena	
		1	Saint Kitts and Nevis	b
		1	Saint Lucia	b
		508	Saint Pierre and Miquelon (Collectivit� territoriale de la R�publique fran�aise)	
		1	Saint Vincent and the Grenadines	b
		685	Samoa (Independent State of)	
		378	San Marino (Republic of)	
		239	Sao Tome and Principe (Democratic Republic of)	
		966	Saudi Arabia (Kingdom of)	
		221	Senegal (Republic of)	
		248	Seychelles (Republic of)	
		232	Sierra Leone	
		65	Singapore (Republic of)	
		421	Slovak Republic	
		386	Slovenia (Republic of)	
		677	Solomon Islands	
		252	Somali Democratic Republic	
		27	South Africa (Republic of)	
		34	Spain	
		94	Sri Lanka (Democratic Socialist Republic of)	
		249	Sudan (Republic of the)	
		597	Suriname (Republic of)	
		268	Swaziland (Kingdom of)	
		46	Sweden	
		41	Switzerland (Confederation of)	
		963	Syrian Arab Republic	
		992	Tajikistan (Republic of)	
		255	Tanzania (United Republic of)	
		66	Thailand	
		389	The Former Yugoslav Republic of Macedonia	
		228	Togolese Republic	
		690	Tokelau	
		676	Tonga (Kingdom of)	
		991	Trial of a proposed new international telecommunication public correspondence service, shared code	o
		1	Trinidad and Tobago	b
		216	Tunisia	
		90	Turkey	
		993	Turkmenistan	
		1	Turks and Caicos Islands	b
		688	Tuvalu	
		256	Uganda (Republic of)	
		380	Ukraine	
		971	United Arab Emirates	h
		44	United Kingdom of Great Britain and Northern Ireland	
		1	United States of America	b
		1	United States Virgin Islands	b
		598	Uruguay (Eastern Republic of)	
		998	Uzbekistan (Republic of)	
		678	Vanuatu (Republic of)	
		379	Vatican City State	f
		39	Vatican City State	
		58	Venezuela (Bolivarian Republic of)	
		84	Viet Nam (Socialist Republic of)	
		681	Wallis and Futuna (Territoire fran�ais d'outre-mer)	
		967	Yemen (Republic of)	
		381	Yugoslavia (Federal Republic of)	
		260	Zambia (Republic of)	
		263	Zimbabwe (Republic of)	
		979	International Premium Rate Service (IPRS)	
		808	International Shared Cost Service (ISCS)	
		0	Reserved	a
		886	Reserved	
		970	Reserved	l
		875	Reserved � Maritime Mobile Service Applications	
		876	Reserved � Maritime Mobile Service Applications	
		877	Reserved � Maritime Mobile Service Applications	
		969	Reserved � Reservation currently under investigation	
		878	Reserved � Universal Personal Telecommunication Service (UPT)	e
		888	Reserved for future global service	
		879	Reserved for national non-commercial purposes	
		210	Spare code	
		211	Spare code	
		214	Spare code	
		215	Spare code	
		217	Spare code	
		219	Spare code	
		259	Spare code	
		280	Spare code	m
		281	Spare code	m
		282	Spare code	m
		283	Spare code	m
		284	Spare code	m
		285	Spare code	m
		286	Spare code	m
		287	Spare code	m
		288	Spare code	m
		289	Spare code	m
		292	Spare code	
		293	Spare code	
		294	Spare code	
		295	Spare code	
		296	Spare code	
		382	Spare code	
		383	Spare code	
		384	Spare code	
		422	Spare code	
		424	Spare code	
		425	Spare code	
		426	Spare code	
		427	Spare code	
		428	Spare code	
		429	Spare code	
		671	Spare code	
		693	Spare code	
		694	Spare code	
		695	Spare code	
		696	Spare code	
		697	Spare code	
		698	Spare code	
		699	Spare code	
		801	Spare code	d
		802	Spare code	d
		803	Spare code	d
		804	Spare code	d
		805	Spare code	d
		806	Spare code	d
		807	Spare code	d
		809	Spare code	d
		830	Spare code	m
		831	Spare code	m
		832	Spare code	m
		833	Spare code	m
		834	Spare code	m
		835	Spare code	m
		836	Spare code	m
		837	Spare code	m
		838	Spare code	m
		839	Spare code	m
		851	Spare code	
		854	Spare code	
		857	Spare code	
		858	Spare code	
		859	Spare code	
		883	Spare code	
		884	Spare code	
		885	Spare code	
		887	Spare code	
		889	Spare code	
		890	Spare code	m
		891	Spare code	m
		892	Spare code	m
		893	Spare code	m
		894	Spare code	m
		895	Spare code	m
		896	Spare code	m
		897	Spare code	m
		898	Spare code	m
		899	Spare code	m
		978	Spare code	
		990	Spare code	
		997	Spare code	
		999	Spare code	
	*/
}
