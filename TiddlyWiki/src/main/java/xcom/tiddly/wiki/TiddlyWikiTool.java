

package xcom.tiddly.wiki ;


import static xcom.tiddly.wiki.args.TiddlyWikiTool_CLArgs.CommandLineOption_f ;
import static xcom.tiddly.wiki.args.TiddlyWikiTool_CLArgs.CommandLineOption_m ;
import static xcom.tiddly.wiki.args.TiddlyWikiTool_CLArgs.CommandLineOption_tw ;
import static xcom.tiddly.wiki.args.TiddlyWikiTool_CLArgs.CommandLineOption_wb ;
import static xcom.tiddly.wiki.args.TiddlyWikiTool_CLArgs.CommandLineOptions ;
import static xcom.tiddly.wiki.args.TiddlyWikiTool_Props.PropertyTWT_Cmd_TagCloseDelimiter ;
import static xcom.tiddly.wiki.args.TiddlyWikiTool_Props.PropertyTWT_Cmd_TagOpenDelimiter ;
import static xcom.tiddly.wiki.args.TiddlyWikiTool_Props.PropertyTWT_Cmd_TiddlyWikiBaseDefault ;
import static xcom.tiddly.wiki.args.TiddlyWikiTool_Props.PropertyTWT_CreatedAtributeName ;
import static xcom.tiddly.wiki.args.TiddlyWikiTool_Props.PropertyTWT_InjectTiddleWikiValuePrefix ;
import static xcom.tiddly.wiki.args.TiddlyWikiTool_Props.PropertyTWT_MergedContentTagCloseDelimiter ;
import static xcom.tiddly.wiki.args.TiddlyWikiTool_Props.PropertyTWT_MergedContentTagOpenDelimiter ;
import static xcom.tiddly.wiki.args.TiddlyWikiTool_Props.PropertyTWT_ModifiedAtributeName ;
import static xcom.tiddly.wiki.args.TiddlyWikiTool_Props.PropertyTWT_StatusColumnName ;
import static xcom.tiddly.wiki.args.TiddlyWikiTool_Props.PropertyTWT_StatusColumn_Skip ;
import static xcom.tiddly.wiki.args.TiddlyWikiTool_Props.PropertyTWT_TagCloseDelimiter ;
import static xcom.tiddly.wiki.args.TiddlyWikiTool_Props.PropertyTWT_TagOpenDelimiter ;
import static xcom.tiddly.wiki.args.TiddlyWikiTool_Props.PropertyTWT_TemplateBasename ;
import static xcom.tiddly.wiki.args.TiddlyWikiTool_Props.PropertyTWT_TiddlerFSpec ;
import static xcom.tiddly.wiki.args.TiddlyWikiTool_Props.PropertyTWT_TiddlerIndexTag ;
import static xcom.tiddly.wiki.args.TiddlyWikiTool_Props.PropertyTWT_TiddlerIndexTemplateBasename ;
import static xcom.tiddly.wiki.args.TiddlyWikiTool_Props.PropertyTWT_TiddlersTag ;
import static xcom.tiddly.wiki.args.TiddlyWikiTool_Props.PropertyTWT_TitleAtributeName ;
import static xcom.utils4j.CLArgs.CLArg_Help ;
import static xcom.utils4j.logging.Loggers.ConsoleLoggerName ;

import java.io.File ;
import java.io.IOException ;
import java.net.URI ;
import java.net.URISyntaxException ;
import java.nio.charset.StandardCharsets ;
import java.util.ArrayList ;
import java.util.Arrays ;
import java.util.HashMap ;
import java.util.List ;
import java.util.Map ;
import java.util.Map.Entry ;
import java.util.Properties ;
import java.util.regex.Matcher ;
import java.util.regex.Pattern ;

import javax.xml.parsers.ParserConfigurationException ;

import org.apache.commons.cli.CommandLine ;
import org.apache.commons.cli.DefaultParser ;
import org.apache.commons.cli.ParseException ;
import org.apache.commons.io.FileUtils ;
import org.apache.commons.io.FilenameUtils ;
import org.apache.commons.lang3.StringEscapeUtils ;
import org.apache.commons.lang3.StringUtils ;
import org.apache.poi.EncryptedDocumentException ;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException ;
import org.apache.poi.ss.usermodel.Row ;
import org.apache.poi.ss.usermodel.Sheet ;
import org.apache.poi.ss.usermodel.Workbook ;
import org.apache.poi.ss.usermodel.WorkbookFactory ;
import org.joda.time.DateTime ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;
import org.xml.sax.SAXException ;

import xcom.tiddly.wiki.args.TiddlyWikiTool_CLArgs ;
import xcom.tiddly.wiki.misc.TiddlerFilter ;
import xcom.utils4j.CLArgs ;
import xcom.utils4j.data.Excels ;
import xcom.utils4j.format.Templator ;
import xcom.utils4j.format.Templator.Templator$Delimited ;
import xcom.utils4j.resources.Props ;


/**
 * Usage:
 *
 * <pre>
 *      -dir {working directory}
 *      -wb {Excel source}
 *      [ -base {TiddlyWiki template base} ]
 *      [ -m {TiddlyWiki merge source} ]
 *      [ -twt {TiddlyWiki destination file} ]
 * </pre>
 *
 * @author Craig Gregory
 *
 */
public class TiddlyWikiTool {

	private static final Logger Logger = LoggerFactory.getLogger(TiddlyWikiTool.class) ;
	private static final Logger Console = LoggerFactory.getLogger(ConsoleLoggerName) ;


	static final String AppName = TiddlyWikiTool.class.getSimpleName() ;
	static final String AppClassname = TiddlyWikiTool.class.getName() ;
	static final String AppDesc = "???." ;
	static final String AppSee = "See https://github.com/???" + AppName + ".md" ;


	CLArgs clArgs ;
	Properties props ;

	//

	Templator$Delimited cmdTaggedTemplate ;
	boolean cmdExtract ;

	TiddlerFilter filter ;

	List<String> selectedBuilds ;

	String statusColumn_Skip ;

	String statusColumnName ;

	StringBuilder twtDocument ;

	StringBuilder twtMerge ;

	Templator$Delimited twtTaggedTemplate ;

	String twtCreated ;

	String twtModified ;

	Map<String, StringBuilder> twtTiddlers ;

	List<StringBuilder> twtTiddlerIndexes ;

	Map<String, Object> twtComponents ;

	String twtTitleAtributeName ;

	String twtCreatedAtributeName ;

	String twtModifiedAtributeName ;

	StringBuilder twtTiddlerIndexTemplate ;


	/**
	 * o * @param cmd
	 *
	 * @throws IOException
	 * @throws EncryptedDocumentException
	 * @throws InvalidFormatException
	 * @throws URISyntaxException
	 */
	public TiddlyWikiTool(final CLArgs clArgs, final Properties props) throws IOException, URISyntaxException {

		this.clArgs = clArgs ;
		this.props = props ;

		initialize(this) ;
	}


	/**
	 * @param $
	 * @return
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	TiddlyWikiTool initialize(final TiddlyWikiTool $) throws IOException, URISyntaxException {

		// Process filters
		{
			$.filter = new TiddlerFilter(StringUtils.trimToEmpty($.clArgs.cmd().getOptionValue(CommandLineOption_f))) ;
			$.clArgs.cmdArgs().put(CommandLineOption_f, StringUtils.trimToEmpty($.clArgs.cmd().getOptionValue(CommandLineOption_f))) ;
		}


		$.cmdTaggedTemplate = Templator.delimiters(Templator.UnixDelimiters) ;
		$.cmdTaggedTemplate.openTagDelimiter(props.getProperty(PropertyTWT_Cmd_TagOpenDelimiter)) ;
		$.cmdTaggedTemplate.closeTagDelimiter(props.getProperty(PropertyTWT_Cmd_TagCloseDelimiter)) ;

		$.twtTaggedTemplate = Templator.delimiters(Templator.DefaultDelimiters) ;
		$.twtTaggedTemplate.openTagDelimiter(props.getProperty(PropertyTWT_TagOpenDelimiter)) ;
		$.twtTaggedTemplate.closeTagDelimiter(props.getProperty(PropertyTWT_TagCloseDelimiter)) ;

		$.twtCreated = new DateTime().toString("yyyyMMddhhmmssSSS") ;
		$.twtModified = twtCreated ;

		$.twtComponents = new HashMap<>() ;
		$.twtTiddlers = new HashMap<>() ;
		$.twtTiddlerIndexes = new ArrayList<>() ;

		$.statusColumnName = props.getProperty(PropertyTWT_StatusColumnName) ;
		$.statusColumn_Skip = props.getProperty(PropertyTWT_StatusColumn_Skip) ;
		$.twtTitleAtributeName = props.getProperty(PropertyTWT_TitleAtributeName) ;
		$.twtCreatedAtributeName = props.getProperty(PropertyTWT_CreatedAtributeName) ;
		$.twtModifiedAtributeName = props.getProperty(PropertyTWT_ModifiedAtributeName) ;


		$.twtMerge = new StringBuilder() ;

		if ( $.clArgs.cmdArgs().containsKey(CommandLineOption_m) ) {

			String fSpec = (String) $.clArgs.cmdArgs().get(CommandLineOption_m) ;
			fSpec = replaceCmdTags(fSpec, $.clArgs.cmdArgs(), $.cmdTaggedTemplate) ;
			Logger.debug("fSpec: {}", fSpec) ;

			final File file = new File(fSpec) ;

			if ( file.exists() )
				$.twtMerge = new StringBuilder(FileUtils.readFileToString(file, StandardCharsets.UTF_8)) ;

			Logger.debug("merge size: |{}|", $.twtMerge.length()) ;
		}


		{
			String fSpec = "${base}/" + props.getProperty(PropertyTWT_TiddlerIndexTemplateBasename) ;
			Logger.debug("tiddler.index.template: {}", fSpec) ;

			fSpec = replaceCmdTags(fSpec, $.clArgs.cmdArgs()) ;
			Logger.debug("fSpec: |{}|", fSpec) ;

			final URI uri = getClass().getResource(fSpec).toURI() ;
			$.twtTiddlerIndexTemplate = new StringBuilder(FileUtils.readFileToString(new File(uri), StandardCharsets.UTF_8)) ;

			Logger.debug("tiddlerIndexTemplate: |{}|", $.twtTiddlerIndexTemplate) ;
		}

		return this ;
	}


	/**
	 * @return
	 * @throws URISyntaxException
	 * @throws IOException
	 */
	TiddlyWikiTool assemble() throws URISyntaxException, IOException {

		// Assemble TW ...

		Console.info("\nAssembling ...") ;

		final Map<String, Object> values = new HashMap<>(twtComponents) ;


		{
			Logger.debug("twtTiddlerIndexes {}", twtTiddlerIndexes) ;

			final StringBuilder sb = new StringBuilder() ;
			for ( final StringBuilder tiddlerIndex : twtTiddlerIndexes )
				sb.append(tiddlerIndex) ;
			values.put(props.getProperty(PropertyTWT_TiddlerIndexTag), sb.toString()) ;
		}


		{
			final StringBuilder sb = new StringBuilder() ;
			for ( final StringBuilder tiddler : twtTiddlers.values() )
				sb.append(tiddler) ;
			values.put(props.getProperty(PropertyTWT_TiddlersTag), sb.toString()) ;
		}


		{
			String fSpec = "${base}/" + props.getProperty(PropertyTWT_TemplateBasename) ;
			Logger.debug("Template: |{}|", fSpec) ;

			fSpec = replaceCmdTags(fSpec, clArgs.cmdArgs()) ;
			Console.info("    template @{}", fSpec) ;

			final URI uri = getClass().getResource(fSpec).toURI() ;
			final StringBuilder template = new StringBuilder(FileUtils.readFileToString(new File(uri), StandardCharsets.UTF_8)) ;

			twtDocument = twtTaggedTemplate.template(template).inject(values) ;
		}

		return this ;
	}


	/**
	 * @return
	 * @throws EncryptedDocumentException
	 * @throws InvalidFormatException
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	TiddlyWikiTool generate() throws EncryptedDocumentException, InvalidFormatException, IOException, URISyntaxException {

		Console.info("\nGenerating Tiddlers from Workbook ...") ;

		selectedBuilds = new ArrayList<>() ;


		Workbook workbook = null ;
		{
			String fSpec = (String) clArgs.cmdArgs().get(CommandLineOption_wb) ;
			Logger.debug("workbook: {}", fSpec) ;

			fSpec = replaceCmdTags(fSpec, clArgs.cmdArgs()) ;
			Console.info("    workbook @{}", fSpec) ;

			workbook = WorkbookFactory.create(new File(fSpec), null, true) ;
		}

		generate(workbook, twtMerge, twtTiddlers, twtTiddlerIndexTemplate, twtTiddlerIndexes, twtComponents, clArgs.cmdArgs(), props) ;

		workbook.close() ;

		Logger.debug("components: {}", twtComponents) ;
		Logger.debug("tiddlers: {}", twtTiddlers) ;
		Logger.debug("tiddlerIndexes: {}", twtTiddlerIndexes) ;

		return this ;
	}


	/**
	 * @param workbook
	 * @param cmdArgs
	 * @param props
	 * @return
	 * @throws EncryptedDocumentException
	 * @throws InvalidFormatException
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	TiddlyWikiTool generate(final Workbook workbook, final StringBuilder merge, final Map<String, StringBuilder> tiddlers,
			final StringBuilder tiddlerIndexTemplate, final List<StringBuilder> tiddlerIndexes, final Map<String, Object> twtComponents,
			final Map<String, Object> cmdArgs, final Properties props)
			throws EncryptedDocumentException, InvalidFormatException, IOException, URISyntaxException {

		//
		// Add TiddleWiki customizing settings to components map ...
		//
		{
			final String prefix = props.getProperty(PropertyTWT_InjectTiddleWikiValuePrefix) ;

			for ( final Entry<Object, Object> prop : props.entrySet() ) {
				final String key = (String) prop.getKey() ;

				if ( key.startsWith(prefix) && !key.equals(PropertyTWT_InjectTiddleWikiValuePrefix) ) {
					Logger.debug("prop: {}=|{}|", prop.getKey(), prop.getValue()) ;

					final String[] tokens = ((String) prop.getValue()).split(",") ;
					twtComponents.put(StringUtils.trimToEmpty(tokens[0]), StringUtils.trimToEmpty(tokens[1])) ;
				}
			}
		}


		//
		// Process each sheet in workbook ...
		//
		{
			for ( int sIdx = 0; (sIdx < workbook.getNumberOfSheets()); sIdx++ ) {
				final Sheet sheet = workbook.getSheetAt(sIdx) ;
				String sheetName = sheet.getSheetName() ;


				// Decode sheet type from sheet name ...

				String sheetType = "" ;
				if ( "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ".indexOf(sheetName.substring(0, 1).toUpperCase()) == -1 ) {
					sheetType = sheetName.substring(0, 1) ;
					sheetName = sheetName.substring(1) ;
				}


				// Process sheet based on sheet type ...

				switch ( sheetType ) {
					case "!":    // Skip processing of sheet.
						break ;

					case "@":    // Match all rows in sheet.
					case "": {    // Match rows based of filter.

						final HashMap<String, Object> parms = new HashMap<>(cmdArgs) ;
						parms.put("tiddler", sheetName) ;
						final String fSpec = replaceCmdTags(props.getProperty(PropertyTWT_TiddlerFSpec), parms) ;
						Logger.debug("tiddler.template: {}", fSpec) ;

						final StringBuilder tiddlerTemplate = new StringBuilder(FileUtils.readFileToString(new File(fSpec), StandardCharsets.UTF_8)) ;
						Logger.debug("tiddlerTemplate: |{}|", tiddlerTemplate) ;

						final boolean matchAll = sheetType.equals("@") ;
						generateTiddlersFromSheet(sheet, sheetName, tiddlerTemplate, tiddlers, tiddlerIndexTemplate, tiddlerIndexes, props, matchAll) ;

						break ;
					}
				}
			}

			Logger.debug("components: {}", twtComponents) ;
		}

		return this ;
	}


	/**
	 * @return
	 * @throws EncryptedDocumentException
	 * @throws InvalidFormatException
	 * @throws IOException
	 * @throws URISyntaxException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 */
	TiddlyWikiTool merge()
			throws EncryptedDocumentException, InvalidFormatException, IOException, URISyntaxException, ParserConfigurationException, SAXException {

		if ( clArgs.cmdArgs().containsKey(CommandLineOption_m) ) {
			Console.info("\nMerging ...") ;

			String fSpec = (String) clArgs.cmdArgs().get(CommandLineOption_m) ;
			fSpec = replaceCmdTags(fSpec, clArgs.cmdArgs()) ;
			Console.info("    merge @{}", fSpec) ;

			merge(twtMerge, twtTiddlers, twtTiddlerIndexTemplate, twtTiddlerIndexes, clArgs.cmdArgs(), props) ;
		}

		return this ;
	}


	/**
	 * @param workbook
	 * @param merge
	 * @param cmdArgs
	 * @param props
	 * @return
	 * @throws IOException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 */
	TiddlyWikiTool merge(final StringBuilder merge, final Map<String, StringBuilder> tiddlers, final StringBuilder tiddlerIndexTemplate,
			final List<StringBuilder> tiddlerIndexes, final Map<String, Object> cmdArgs, final Properties props)
			throws IOException, ParserConfigurationException, SAXException {

		final Map<String, Object> values = new HashMap<>() ;

		final String titleRegEx = "title=\"(.*?)\"" ;
		Logger.debug("titleRegEx: |{}|", titleRegEx) ;


		//
		// Copy tiddlers from merge with 'twt-copy' tag and matches 'twt-copy-selector' into twtTiddlers ...
		//
		{
			// Look for div's with 'twt-copy' tag ...
			final String copyRegEx = "twt-copy.*?</div>" ;
			Logger.debug("copyRegEx: |{}|", copyRegEx) ;

			final Matcher copyMatcher = Pattern.compile(copyRegEx, Pattern.DOTALL | Pattern.CASE_INSENSITIVE).matcher(merge) ;

			while ( copyMatcher.find() ) {
				Logger.debug("copyMatcher.group(): |{}|", copyMatcher.group()) ;

				int start = copyMatcher.start() ;
				final int stop = copyMatcher.end() ;

				// Backup to start of div ...
				while ( !merge.substring(start, start + 4).equalsIgnoreCase("<div") )
					start-- ;

				final StringBuilder copy = new StringBuilder(merge.substring(start, stop)) ;
				Logger.debug("copy: |{}|", copy) ;


				if ( filter.matches(copy) ) {

					final Pattern titlePattern = Pattern.compile(titleRegEx, Pattern.DOTALL | Pattern.CASE_INSENSITIVE) ;
					final Matcher titleMatcher = titlePattern.matcher(new StringBuilder(merge.substring(start, stop))) ;

					titleMatcher.find() ;
					final String _title = titleMatcher.group(1) ;
					Logger.debug("_title: |{}|", _title) ;

					tiddlers.put(_title, copy) ;

					values.put(twtTitleAtributeName, _title) ;
					tiddlerIndexes.add(twtTaggedTemplate.template(tiddlerIndexTemplate).inject(values)) ;
				}
			}
		}


		//
		// Extract any 'merged-content' from merge and inject into corresponding twtTiddlers ...
		//
		{
			final String openMergeTag = StringEscapeUtils.escapeXml10(props.getProperty(PropertyTWT_MergedContentTagOpenDelimiter)) ;
			final String closeMergeTag = StringEscapeUtils.escapeXml10(props.getProperty(PropertyTWT_MergedContentTagCloseDelimiter)) ;

			final String sourceRegEx = openMergeTag + "(.*?)" + closeMergeTag + ".*?</div>" ;
			Logger.debug("sourceRegEx: |{}|", sourceRegEx) ;

			final Pattern sourcePattern = Pattern.compile(sourceRegEx, Pattern.DOTALL | Pattern.CASE_INSENSITIVE) ;
			final Matcher sourceMatcher = sourcePattern.matcher(merge) ;


			final String targetRegEx = openMergeTag + "(.*?)" + closeMergeTag ;
			Logger.debug("targetRegEx: |{}|", targetRegEx) ;

			final Pattern targetPattern = Pattern.compile(targetRegEx, Pattern.DOTALL | Pattern.CASE_INSENSITIVE) ;


			while ( sourceMatcher.find() ) {
				int start = sourceMatcher.start() ;
				final int stop = sourceMatcher.end() ;

				// Backup to start of div ...
				while ( !merge.substring(start, start + 4).equalsIgnoreCase("<div") )
					start-- ;

				final Pattern titlePattern = Pattern.compile(titleRegEx, Pattern.DOTALL | Pattern.CASE_INSENSITIVE) ;
				final Matcher titleMatcher = titlePattern.matcher(new StringBuilder(merge.substring(start, stop))) ;

				titleMatcher.find() ;
				final String _title = titleMatcher.group(1) ;
				Logger.debug("_title: |{}|", _title) ;


				if ( tiddlers.containsKey(_title) ) {
					final StringBuilder tiddler = tiddlers.get(_title) ;
					final Matcher targetMatcher = targetPattern.matcher(tiddler) ;

					targetMatcher.find() ;
					Logger.debug("targetMatcher.group(1): |{}|", targetMatcher.group(1)) ;

					tiddler.replace(targetMatcher.start(1), targetMatcher.end(1), sourceMatcher.group(1)) ;
					tiddlers.put(_title, tiddler) ;
				}
			}
		}

		return this ;
	}


	/**
	 * @return
	 * @throws IOException
	 */
	TiddlyWikiTool write() throws IOException {

		Console.info("\nWriting ...") ;

		write(twtDocument, clArgs.cmdArgs(), props) ;

		return this ;
	}


	/**
	 * @param ebb
	 * @param cmdArgs
	 * @param props
	 * @return
	 * @throws IOException
	 */
	TiddlyWikiTool write(final StringBuilder ebb, final Map<String, Object> cmdArgs, final Properties props) throws IOException {

		String fSpec = (String) cmdArgs.get(CommandLineOption_tw) ;
		Logger.debug("TiddlyWiki: |{}|", fSpec) ;

		fSpec = replaceCmdTags(fSpec, cmdArgs) ;
		Console.info("    TiddlyWiki @{}", fSpec) ;

		FileUtils.writeStringToFile(new File(fSpec), ebb.toString(), StandardCharsets.UTF_8) ;

		if ( !((String) cmdArgs.get(CommandLineOption_f)).isEmpty() ) {
			final String src = FilenameUtils.getFullPath((String) cmdArgs.get(CommandLineOption_m)) ;
			final String dest = FilenameUtils.getFullPath((String) cmdArgs.get(CommandLineOption_tw)) ;

			FileUtils.copyDirectory(new File(src + "_resources"), new File(dest + "_resources")) ;

			for ( final String build : selectedBuilds )
				FileUtils.copyDirectory(new File(src + "_builds\\" + build), new File(dest + "_builds\\" + build)) ;
		}

		return this ;
	}


	/**
	 * @param sheet
	 * @param sheetName
	 * @param tiddlerTemplate
	 * @param tiddlerIndexTemplate
	 * @param props
	 * @param matchAll
	 * @return
	 * @throws IOException
	 */
	TiddlyWikiTool generateTiddlersFromSheet(final Sheet sheet, final String sheetName, final StringBuilder tiddlerTemplate,
			final Map<String, StringBuilder> tiddlers, final StringBuilder tiddlerIndexTemplate, final List<StringBuilder> tiddlerIndexes,
			final Properties props, final boolean matchAll) throws IOException {

		Console.info("    {} ...", sheet.getSheetName()) ;

		final Map<String, Short> headers = Excels.readColumnHeadersAndIndex(sheet, true) ;

		final Map<String, Object> values = new HashMap<>() ;
		values.put(twtCreatedAtributeName, twtCreated) ;
		values.put(twtModifiedAtributeName, twtModified) ;

		int selected = 0 ;
		int processed = 0 ;
		for ( int rIdx = 1; (rIdx <= sheet.getLastRowNum()); rIdx++ ) {    // Skip row 0 (column headers).
			final Row row = sheet.getRow(rIdx) ;

			for ( final Entry<String, Short> header : headers.entrySet() )
				if ( !header.getKey().startsWith("!") )
					values.put(header.getKey(),
							StringEscapeUtils.escapeXml10(StringUtils.trimToEmpty(Excels.getCellValueAsString(row.getCell(header.getValue()))))) ;

			Logger.debug("values: {}", values) ;

			if ( !((String) values.get(statusColumnName)).equalsIgnoreCase(statusColumn_Skip) ) {

				if ( matchAll || filter.matches(headers, values) ) {

					selected++ ;
					tiddlers.put((String) values.get(twtTitleAtributeName), twtTaggedTemplate.template(tiddlerTemplate).inject(values)) ;
					Logger.debug("tiddler: |{}|", values.get(twtTitleAtributeName)) ;
					tiddlerIndexes.add(twtTaggedTemplate.template(tiddlerIndexTemplate).inject(values)) ;

					if ( sheetName.equalsIgnoreCase("builds") )
						selectedBuilds.add((String) values.get("file-name")) ;
				}

				processed++ ;
			}
		}

		Console.info("        {} of {} tiddlers", selected, processed) ;

		return this ;
	}


	String replaceCmdTags(final String fSpec, final Map<String, Object> cmdArgs) {
		return replaceCmdTags(fSpec, cmdArgs, cmdTaggedTemplate) ;
	}


	String replaceCmdTags(final String fSpec, final Map<String, Object> cmdArgs, final Templator$Delimited tt) {

		String results = fSpec ;

		while ( results.indexOf(props.getProperty(PropertyTWT_Cmd_TagOpenDelimiter)) != -1 )
			results = tt.template(results).inject(cmdArgs) ;

		return results ;
	}


	/**
	 * @param args
	 * @throws ParseException
	 * @throws IOException
	 * @throws EncryptedDocumentException
	 * @throws InvalidFormatException
	 * @throws URISyntaxException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 */
	public static void main(final String[] args) throws ParseException, IOException, EncryptedDocumentException, InvalidFormatException, URISyntaxException,
			ParserConfigurationException, SAXException {

		// Parse and process command line args ...

		final CommandLine cmd = new DefaultParser().parse(CommandLineOptions, args) ;

		if ( cmd.hasOption(CLArg_Help) ) {
			CLArgs.printToolHelp(Console, AppClassname, CommandLineOptions, AppDesc, AppSee) ;
			return ;
		}


		// Load properties and decode args ...

		Properties props = Props.load(TiddlyWikiTool.class) ;
		props = Props.merge(TiddlyWikiTool.class, props, props.get(PropertyTWT_Cmd_TiddlyWikiBaseDefault) + "/twt.properties") ;
		Logger.debug("props: {}", props) ;

		TiddlyWikiTool_CLArgs clArgs = new TiddlyWikiTool_CLArgs(cmd, props) ;
		clArgs.decodeCommandLine(cmd, CommandLineOptions, props, clArgs, Logger) ;
		Logger.debug("cmdArgs: {}", Arrays.asList(clArgs.cmdArgs())) ;

		CLArgs.printToolUsage(Console, AppClassname, clArgs, AppDesc) ;


		// Instantiate and process ...

		final TiddlyWikiTool $ = new TiddlyWikiTool(clArgs, props) ;

		$.generate().merge().assemble().write() ;


		Console.info("\nDone.") ;
	}
}
