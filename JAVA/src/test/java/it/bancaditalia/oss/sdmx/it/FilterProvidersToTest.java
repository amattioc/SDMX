package it.bancaditalia.oss.sdmx.it;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FilterProvidersToTest
{
	private static final Set<String> keepProviders = new HashSet<>(Arrays.asList(new String[] {
//			"ABS",
			"ECB",
			"EUROSTAT",
			"ILO",
			"INSEE",
			"IMF2",
			"ISTAT",
			"ISTAT_RI",
			"OECD",
			"UNDATA",
			"WITS",
		}));

	public static List<Object[]> filter(Object[][] parameters, final int index)
	{
		List<Object[]> result = new ArrayList<>();
		
		for (Object[] row: parameters)
			if (keepProviders.contains(row[index]))
				result.add(row);
		
		Collections.sort(result, new Comparator<Object[]>() {
			@SuppressWarnings("unchecked")
			@Override
			public int compare(Object[] o1, Object[] o2)
			{
				return ((Comparable<Object>)o1[index]).compareTo(o2[index]);
			}
		});
		
		return result;
	}
}
