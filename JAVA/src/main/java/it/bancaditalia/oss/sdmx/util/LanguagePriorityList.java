package it.bancaditalia.oss.sdmx.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents a language priority list. This class is an immutable convenient
 * wrapper around list of Locale.LanguageRange. It is designed to be used
 * directly in the "Accept-Language" header of an HTTP request.
 *
 * @author Philippe Charles
 * @see <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Accept-Language">Accept-Language</a>
 * @see <a href="https://github.com/sdmx-twg/sdmx-rest/wiki/HTTP-content-negotiation">HTTP content negotiation</a>
 */
public final class LanguagePriorityList
{

	/**
	 * Any language.
	 */
	public static final LanguagePriorityList ANY = LanguagePriorityList.parse("*");

	/**
	 * Parses the given ranges to generate a priority list.
	 *
	 * @param ranges
	 *            a non-null list of comma-separated language ranges or a list of
	 *            language ranges in the form of the "Accept-Language" header
	 *            defined in <a href="http://tools.ietf.org/html/rfc2616">RFC
	 *            2616</a>
	 * @return a non-null priority list
	 * @throws NullPointerException
	 *             if {@code ranges} is null
	 * @throws IllegalArgumentException
	 *             if a language range or a weight found in the given {@code ranges}
	 *             is ill-formed
	 */
	public static LanguagePriorityList parse(String ranges) throws IllegalArgumentException
	{
		return new LanguagePriorityList(Locale8.LanguageRange.parse(ranges));
	}

	private final List<Locale8.LanguageRange> list;
	private final String str;

	private LanguagePriorityList(List<Locale8.LanguageRange> list)
	{
		this.list = list;
		this.str = asString(list);
	}

	/**
	 * Returns the best-matching language tag using the lookup mechanism defined in
	 * RFC 4647.
	 *
	 * @param tags
	 *            a non-null list of language tags used for matching
	 * @return the best matching language tag chosen based on priority or weight, or
	 *         {@code null} if nothing matches.
	 * @throws NullPointerException
	 *             if {@code tags} is {@code null}
	 */
	public String lookupTag(Collection<String> tags)
	{
		return Locale8.lookupTag(list, tags);
	}

	@Override
	public String toString()
	{
		return str;
	}

	@Override
	public boolean equals(Object obj)
	{
		return obj instanceof LanguagePriorityList && ((LanguagePriorityList) obj).str.equals(str);
	}

	@Override
	public int hashCode()
	{
		return str.hashCode();
	}

	private static String asString(List<Locale8.LanguageRange> list)
	{
		if (list.isEmpty())
		{
			return "";
		}
		StringBuilder result = new StringBuilder();
		result.append(asString(list.get(0)));
		for (int i = 1; i < list.size(); i++)
		{
			result.append(',').append(asString(list.get(i)));
		}
		return result.toString();
	}

	private static String asString(Locale8.LanguageRange o)
	{
		return o.getRange() + (o.getWeight() != 1.0 ? (";q=" + o.getWeight()) : "");
	}

	// TODO: this code exist in Java8 so can be removed when migrating
	private static final class Locale8
	{

		public static String lookupTag(List<LanguageRange> priorityList, Collection<String> tags)
		{
			for (LanguageRange o : priorityList)
			{
				String lang = o.getLanguage();
				if (tags.contains(lang))
				{
					return lang;
				}
			}
			return null;
		}

		private static final class LanguageRange
		{

			public static List<LanguageRange> parse(String ranges)
			{
				List<LanguageRange> result = new ArrayList<>();
				for (String o : ranges.split("\\s*,\\s*", -1))
				{
					result.add(parseItem(o));
				}
				Collections.sort(result, ByWeightDesc.INSTANCE);
				return result;
			}

			private final String range;
			private final double weight;

			private LanguageRange(String range, double weight)
			{
				this.range = range;
				this.weight = weight;
			}

			public String getRange()
			{
				return range;
			}

			public double getWeight()
			{
				return weight;
			}

			private String getLanguage()
			{
				int index = range.indexOf("-");
				return index != -1 ? range.substring(0, index) : range;
			}

			private static LanguageRange parseItem(String o)
			{
				Matcher m = INPUT_PATTERN.matcher(o);
				if (!m.matches())
				{
					throw new IllegalArgumentException(o);
				}
				return new LanguageRange(m.group(1).toLowerCase(), parseWeight(m.group(2)));
			}

			private static double parseWeight(String weight)
			{
				if (weight != null)
				{
					Matcher m = WEIGHT_PATTERN.matcher(weight);
					m.matches();
					weight = m.group(1);
				}
				return weight != null ? Double.parseDouble(weight) : 1;
			}

			private static final String RANGE = "(?:[a-z]{2,3}|\\*)(?:-[a-zA-Z]{2,3}";
			private static final String WEIGHT = ";q\\s*=\\s*(1|0\\.[0-9]+)";
			private static final Pattern INPUT_PATTERN = Pattern.compile("(" + RANGE + ")?)\\s*(" + WEIGHT + ")?");
			private static final Pattern WEIGHT_PATTERN = Pattern.compile(WEIGHT);

			private enum ByWeightDesc implements Comparator<LanguageRange> {

				INSTANCE;

				@Override
				public int compare(LanguageRange l, LanguageRange r)
				{
					return r.getWeight() < l.getWeight() ? -1 : r.getWeight() > l.getWeight() ? 1 : 0;
				}
			}
		}
	}
}
