/* (c) 2020 Dipl Ing Péter Varkoly <pvarkoly@cephalix.eu> All rights reserved*/
package de.cranix;

import static de.cranix.dao.tools.StaticHelpers.*;

public class TestUtil {
	public static void main (String[] args) {
		System.out.println("Random Password 8 digits: " + createRandomPassword());
		System.out.println("Normalize string 'asdÁŰköäűúőpöóüü': " + normalize("asdÁŰköäűúőpöóüü"));
	
	}
}
