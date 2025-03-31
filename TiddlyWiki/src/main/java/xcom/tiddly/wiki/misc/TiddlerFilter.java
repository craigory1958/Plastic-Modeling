

package xcom.tiddly.wiki.misc ;


import java.io.IOException ;
import java.util.ArrayList ;
import java.util.List ;
import java.util.Map ;
import java.util.Map.Entry ;
import java.util.regex.Matcher ;
import java.util.regex.Pattern ;

import javax.xml.parsers.DocumentBuilderFactory ;
import javax.xml.parsers.ParserConfigurationException ;

import org.apache.commons.lang3.StringUtils ;
import org.w3c.dom.Document ;
import org.w3c.dom.Element ;
import org.xml.sax.SAXException ;

import xcom.utils4j.data.structured.map.Maps ;


public class TiddlerFilter {

	public static final String QueryRegExPattern = "\\(\\s*(\\w+?)\\s*\\,\\s*(.+?)\\s*\\)" ;


	/**
	 *
	 */
	List<Entry<String, String>> query ;

	public List<Entry<String, String>> getQuery() {
		return query ;
	}

	public TiddlerFilter setQuery(final List<Entry<String, String>> query) {
		this.query = query ;
		return this ;
	}


	/**
	 * @param query
	 */
	public TiddlerFilter(final String query) {
		this.query = buildQuery(query) ;
	}


	/**
	 * @param query
	 * @return
	 */
	public static List<Entry<String, String>> buildQuery(final String query) {

		final List<Entry<String, String>> results = new ArrayList<Entry<String, String>>() ;

		final Matcher matcher = Pattern.compile(QueryRegExPattern, Pattern.DOTALL | Pattern.CASE_INSENSITIVE).matcher(query) ;

		while ( matcher.find() )
			results.add(Maps.getInstanceOfEntry(matcher.group(1), matcher.group(2))) ;

		return results ;
	}


	/**
	 * @param headers
	 * @param values
	 * @return
	 */
	public boolean matches(final Map<String, Short> headers, final Map<String, Object> values) {

		boolean results = (query.size() == 0 ? true : false) ;

		for ( final Entry<String, String> filter : query )
			if ( headers.containsKey(filter.getKey()) )
				results = results || Pattern.compile((String) values.get(filter.getKey()), Pattern.CASE_INSENSITIVE).matcher(filter.getValue()).find() ;

		return results ;
	}


	/**
	 * @param tiddler
	 * @return
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	public boolean matches(final StringBuilder tiddler) throws ParserConfigurationException, SAXException, IOException {

		boolean results = true ;

		if ( query.size() > 0 ) {
			final Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(tiddler.toString()) ;
			final Element element = doc.getDocumentElement() ;
			final String selector = StringUtils.trimToEmpty(element.getAttribute("twt-copy-selector")) ;

			if ( !selector.isEmpty() ) {
				results = false ;

				for ( final Entry<String, String> selectorCriterion : buildQuery(selector) ) {

					Pattern regEx = Pattern.compile(selectorCriterion.getValue(), Pattern.DOTALL | Pattern.CASE_INSENSITIVE) ;

					for ( final Entry<String, String> queryCriterion : query )
						if ( selectorCriterion.getKey().equalsIgnoreCase(queryCriterion.getKey()) )
							results = results || regEx.matcher(queryCriterion.getValue()).find() ;
				}
			}
		}

		return results ;
	}
}
