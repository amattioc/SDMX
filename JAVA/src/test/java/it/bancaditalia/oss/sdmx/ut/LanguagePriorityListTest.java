package it.bancaditalia.oss.sdmx.ut;

import java.util.Arrays;
import org.junit.Assert;
import org.junit.Test;

import it.bancaditalia.oss.sdmx.util.LanguagePriorityList;

/**
 *
 * @author Philippe Charles
 */
public class LanguagePriorityListTest
{

	@Test
	public void testParse()
	{
		Assert.assertEquals("*", LanguagePriorityList.parse("*").toString());
		Assert.assertEquals("fr", LanguagePriorityList.parse("fr").toString());
		Assert.assertEquals("fr-be", LanguagePriorityList.parse("fr-BE").toString());
		Assert.assertEquals("fr-be,fr;q=0.5", LanguagePriorityList.parse("fr-BE,fr;q=0.5").toString());
		Assert.assertEquals("fr-ch,fr;q=0.9,en;q=0.8,de;q=0.7,*;q=0.5", LanguagePriorityList.parse("fr-CH, fr;q=0.9, en;q=0.8, de;q=0.7, *;q=0.5").toString());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testParseInvalid()
	{
		LanguagePriorityList.parse("fr-BE;");
	}

	@Test(expected = NullPointerException.class)
	public void testParseNull()
	{
		LanguagePriorityList.parse(null);
	}

	@Test
	public void testEquals()
	{
		Assert.assertEquals(LanguagePriorityList.parse("*"), LanguagePriorityList.parse("*"));
		Assert.assertEquals(LanguagePriorityList.parse("*"), LanguagePriorityList.ANY);
		Assert.assertEquals(LanguagePriorityList.parse("fr-BE"), LanguagePriorityList.parse("fr-BE"));
		Assert.assertEquals(LanguagePriorityList.parse("fr-BE"), LanguagePriorityList.parse("fr-BE;q=1"));
	}

	@Test
	public void testLookupTag()
	{
		Assert.assertEquals("fr", LanguagePriorityList.parse("fr").lookupTag(Arrays.asList("fr", "nl")));
		Assert.assertEquals("fr", LanguagePriorityList.parse("fr-BE").lookupTag(Arrays.asList("fr", "nl")));
		Assert.assertEquals("nl", LanguagePriorityList.parse("fr,nl;q=0.7,en;q=0.3").lookupTag(Arrays.asList("de", "nl", "en")));
		Assert.assertNull(LanguagePriorityList.parse("fr").lookupTag(Arrays.asList("nl")));
	}

	@Test(expected = NullPointerException.class)
	public void testLookupTagNull()
	{
		Assert.assertNull(LanguagePriorityList.parse("fr").lookupTag(null));
	}
}
