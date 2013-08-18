package org.vhorvath.valogato.common.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ThrottlingUtilsTest {


	@Test
	public void testCommaSeparated() {
		String[] strings = {"alma","fa","level"};
		String result = ThrottlingUtils.commaSeparated(strings);
		
		assertEquals("alma,fa,level", result);
	}

	@Test
	public void testCommaSeparated_null() {
		String[] strings = null;
		String result = ThrottlingUtils.commaSeparated(strings);
		
		assertNull(result);
	}

	@Test
	public void testCommaSeparated_empty() {
		String[] strings = {};
		String result = ThrottlingUtils.commaSeparated(strings);
		
		assertNotNull(result);
		assertTrue(result.length() == 0);
	}

}
