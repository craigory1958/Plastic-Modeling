

package xcom.tiddly.wiki.args ;


import static xcom.tiddly.wiki.args.TiddlyWikiTool_Props.PropertyTWT_Cmd_TiddlyWikiBaseDefault ;

import java.util.Map ;
import java.util.Properties ;

import org.apache.commons.cli.CommandLine ;
import org.apache.commons.cli.MissingArgumentException ;
import org.apache.commons.cli.Options ;
import org.apache.commons.cli.ParseException ;
import org.apache.commons.io.FilenameUtils ;
import org.apache.commons.lang3.StringUtils ;

import xcom.utils4j.CLArgs ;
import xcom.utils4j.logging.aspects.api.annotations.Log ;


public class TiddlyWikiTool_CLArgs extends CLArgs {


	public static final String CommandLineOption_base = "base" ;
	public static final String CommandLineOption_dir = "dir" ;
	public static final String CommandLineOption_e = "e" ;
	public static final String CommandLineOption_f = "f" ;
	public static final String CommandLineOption_m = "m" ;
	public static final String CommandLineOption_tw = "tw" ;
	public static final String CommandLineOption_wb = "wb" ;

	public static final Options CommandLineOptions = new Options() ;
	static {
		CommandLineOptions.addOption(CLArgOption_Help) ;
		CommandLineOptions.addOption(CommandLineOption_base, true, "TiddlyWiki template base") ;
		CommandLineOptions.addOption(CommandLineOption_dir, true, "Working directory") ;
		CommandLineOptions.addOption(CommandLineOption_e, false, "Extract") ;
		CommandLineOptions.addOption(CommandLineOption_f, true, "Filter") ;
		CommandLineOptions.addOption(CommandLineOption_m, true, "TiddlyWiki merge source") ;
		CommandLineOptions.addOption(CommandLineOption_wb, true, "Excel Workbook source") ;
		CommandLineOptions.addOption(CommandLineOption_tw, true, "TiddlyWiki destination file") ;
	}


	public TiddlyWikiTool_CLArgs(CommandLine cmd, Properties props) {
		super(cmd, props) ;
	}


	@Override
	public void validateCommandLine(CommandLine cmd, final Options options) throws ParseException {

		if ( StringUtils.trimToNull(cmd.getOptionValue(CommandLineOption_dir)) == null )
			throw new MissingArgumentException(CommandLineOption_dir) ;

		if ( StringUtils.trimToNull(cmd.getOptionValue(CommandLineOption_wb)) == null )
			throw new MissingArgumentException(CommandLineOption_wb) ;
	}


	@Log
	public String decodeCommandLineArg_base(final CommandLine cmd, final Properties props, final Map<String, String> cmdArgs) {

		if ( !StringUtils.trimToEmpty(cmd.getOptionValue(CommandLineOption_base)).isEmpty() )
			return cmd.getOptionValue(CommandLineOption_base) ;

		return props.getProperty(PropertyTWT_Cmd_TiddlyWikiBaseDefault) ;
	}


	@Log
	public String decodeCommandLineArg_dir(final CommandLine cmd, final Properties props, final Map<String, String> cmdArgs) {
		return StringUtils.trimToEmpty(cmd.getOptionValue(CommandLineOption_dir)) ;

	}


	@Log
	public String decodeCommandLineArg_e(final CommandLine cmd, final Properties props, final Map<String, String> cmdArgs) {
		return StringUtils.trimToEmpty(cmd.getOptionValue(CommandLineOption_e)) ;
	}


	@Log
	public String decodeCommandLineArg_f(final CommandLine cmd, final Properties props, final Map<String, String> cmdArgs) {
		return StringUtils.trimToEmpty(cmd.getOptionValue(CommandLineOption_f)) ;
	}


	@Log
	public String decodeCommandLineArg_m(final CommandLine cmd, final Properties props, final Map<String, String> cmdArgs) {

		String arg = StringUtils.trimToEmpty(cmd.getOptionValue(CommandLineOption_m)) ;

		if ( arg.isEmpty() )
			arg = "${tw-basename}" ;

		if ( FilenameUtils.getFullPath(arg).isEmpty() )
			arg = "${tw-dir}\\" + arg ;

		if ( FilenameUtils.getExtension(arg).isEmpty() )
			arg = arg + ".html" ;

		return arg ;
	}


	@Log
	public String decodeCommandLineArg_tw(final CommandLine cmd, final Properties props, final Map<String, String> cmdArgs) {

		String arg = StringUtils.trimToEmpty(cmd.getOptionValue(CommandLineOption_tw)) ;

		if ( arg.isEmpty() )
			arg = "${wb-basename}" ;

		if ( FilenameUtils.getFullPath(arg).isEmpty() )
			arg = "${wb-dir}\\" + arg ;

		if ( FilenameUtils.getExtension(arg).isEmpty() )
			arg = arg + ".html" ;

		cmdArgs.put(CommandLineOption_tw + "-dir", FilenameUtils.getFullPath(arg)) ;
		cmdArgs.put(CommandLineOption_tw + "-basename", FilenameUtils.getBaseName(arg)) ;

		return arg ;
	}


	@Log
	public String decodeCommandLineArg_wb(final CommandLine cmd, final Properties props, final Map<String, String> cmdArgs) {

		String arg = StringUtils.trimToEmpty(cmd.getOptionValue(CommandLineOption_wb)) ;

		if ( FilenameUtils.getFullPath(arg).isEmpty() )
			arg = "${dir}\\" + arg ;

		if ( FilenameUtils.getExtension(arg).isEmpty() )
			arg = arg + ".xlsx" ;

		cmdArgs.put(CommandLineOption_wb + "-dir", FilenameUtils.getFullPath(arg)) ;
		cmdArgs.put(CommandLineOption_wb + "-basename", FilenameUtils.getBaseName(arg)) ;

		return arg ;
	}
}
