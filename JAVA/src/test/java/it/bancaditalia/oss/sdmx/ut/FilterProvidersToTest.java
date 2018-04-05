package it.bancaditalia.oss.sdmx.ut;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FilterProvidersToTest
{
	private static final Set<String> keepProviders = new HashSet<>(Arrays.asList(new String[] {
			"ABS",
			"ECB",
			"EUROSTAT",
			"ILO",
			"INEGI",
			"INSEE",
			"IMF2",
			"IMF_SDMX_CENTRAL",
			"ISTAT",
			"NBB",
			"OECD",
			"UIS",
			"UNDATA",
			"WB",
			"WITS",
		}));

	public static List<Object[]> filter(Object[][] parameters, int index)
	{
		List<Object[]> result = new ArrayList<>();
		
		for (Object[] row: parameters)
			if (keepProviders.contains(row[index]))
				result.add(row);
		
		return result;
	}
}
