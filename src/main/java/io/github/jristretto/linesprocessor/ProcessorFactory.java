package io.github.jristretto.linesprocessor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import static java.util.Map.entry;
import java.util.ServiceLoader;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import static java.util.stream.Stream.of;

/**
 * Creator of Processor boxes based on the content os strings and previous
 * Strings.
 *
 * @author Pieter van den Hombergh {@code <pieter.van.den.hombergh@gmail.com>}
 */
public class ProcessorFactory implements Function<String, Stream<String>> {

    public static final Path JAVA_PATH = Path.of( ".java" );
    /**
     * The secret sauce.
     */
    private final String myPreciousRegex;
    private final Pattern pattern;

    public ProcessorFactory() {
        this( JAVA_PATH );
    }

    public ProcessorFactory(Path filePath) {
        this( filePath, "cs" );
    }

    public ProcessorFactory(Path filePath, String tag) {
        commentToken = commentTokenFor( filePath );
        myPreciousRegex
                = "(?<indent>\\s*)" //optional indentation
                + "(?<text>\\S.*)?" // anything other starting with non space
                + "(?<commentToken>" + commentToken + ")" // mandatory comment token
                + tag // required tag, split over two lines to self-protect against stripping
                + ":"
                + "(?<instruction>\\w+)" // required instruction group
                + "(:(?<startEnd>(start|end)))?" // optional start end group
                + ":?(?<payLoad>(.*$))?" // optional  payLoad
                ;
        pattern = Pattern.compile( myPreciousRegex );
        this.transforms = new HashMap<>( defaultTransforms );
        for ( TagProvider tagProvider : pluginLoader ) {
            System.err.println("tagProvider = " + tagProvider);
            Map<String, Function<Processor, Stream<String>>> newTags = tagProvider
                    .newTags();
            this.transforms.putAll( newTags );
        }
    }

    public Pattern getPattern() {
        return pattern;
    }

    final Map<String, Function<Processor, Stream<String>>> transforms;
    private static ServiceLoader<TagProvider> pluginLoader
            = ServiceLoader.load( TagProvider.class );

    public Matcher matcherFor(String line) {
        Matcher m = pattern.matcher( line );
        return m;
    }

    public String[] getInstructions() {
        return transforms.keySet().stream().sorted().toArray( String[]::new );
    }

    public Processor processorFor(String line) {
        Matcher m = pattern.matcher( line );
        if ( m.matches() ) {
            String instruction = m.group( "instruction" ).trim();
            var startEndText = m.group( "startEnd" );
            // avoid NPE on lines without startEnd
            var startEnd = null == startEndText ? "" : startEndText;
            var payLoad = m.group( "payLoad" );
            var text = m.group( "text" );
            var indent = m.group( "indent" );
            var transformation = transformFor( instruction );

            if ( "start".equals( startEnd ) ) {
                // pickup the transformation
                activeTransformation = transformation;
                // but remove current line
                transformation = remove;
            }
            if ( "end".equals( startEnd ) ) {
                activeTransformation = nop;
                // but remove current line
                transformation = remove;
            }
            var result = new Processor( line, payLoad, transformation, instruction,
                    ++lineNumber, text, indent, startEnd );
            return result;
        }
        // lines without instructions are subject to activeTansformation
        return newProcessor( line );
    }

    // shorthand for non tagged lines factories
    private Processor newProcessor(String line) {
        return new Processor( line, "", activeTransformation, "", ++lineNumber,
                line, "",
                "" );
    }

    /**
     * Apply the creation of a processor and applying its effect on the input
     * string. Collapses two mappings into 1.
     *
     * @param line to process, remove or replace.
     * @return The result of the processor box applied
     */
    @Override
    public Stream<String> apply(String line) {
        var x = processorFor( line );
        var applied = x.apply( x );
        return applied;
    }

    Function<Processor, Stream<String>> activeTransformation = nop;
    int lineNumber = 0;

    static Stream<String> include(Processor proc) {
        try {
            return Files.lines( Path.of( proc.payLoad().trim() ) ).map(
                    l -> proc.indent() + l );
        } catch ( IOException ex ) {

            Logger.getLogger( ProcessorFactory.class.getName() )
                    .severe( ex.getMessage() );
            return Stream.empty();
        }
    }
    static final Function<Processor, Stream<String>> replace = p -> of( p
            .payLoad() );
    static final Function<Processor, Stream<String>> add = p -> of( p
            .payLoad() );
    static final Function<Processor, Stream<String>> uncomment
            = p -> of( p.text().replaceFirst( "//", "" ) );
    static final Function<Processor, Stream<String>> comment
            = p -> of( "//" + p.text() );
    static final Function<Processor, Stream<String>> nop
            = p -> of( p.text() );
    static final Function<Processor, Stream<String>> remove
            = p -> {
        if ( !p.payLoad().isBlank() ) {
                    return Stream.of( p.payLoad() );
                }
                return Stream.empty();
            };
    static final Function<Processor, Stream<String>> include
            = ProcessorFactory::include;
    static final Function<Processor, Stream<String>> UPPER
            = p -> Stream.of( p.text().toUpperCase() );
    static final Function<Processor, Stream<String>> lower
            = p -> Stream.of( p.text().toLowerCase() );
    static final Function<Processor, Stream<String>> replaceFirst
            = ( Processor p ) -> {
                String separator = p.payLoad().substring( 0, 1 );
                String[] split = p.payLoad().substring( 1 ).split( separator );
                String result = p.text().replaceFirst( split[ 0 ], split[ 1 ] );
                return of( result );
            };
    static final Function<Processor, Stream<String>> replaceAll
            = ( Processor p ) -> {
                String separator = p.payLoad().substring( 0, 1 );
                String[] split = p.payLoad().split( separator, 2 );
                String result = p.text().replaceAll( split[ 0 ], split[ 1 ] );
                return of( result );
            };

    // lookup
    static Map<String, Function<Processor, Stream<String>>> defaultTransforms = Map
            .ofEntries(
                    entry( "add", add ),
                    entry( "comment", comment ),
                    entry( "replace", replace ),
                    entry( "nop", nop ),
                    entry( "uncomment", uncomment ),
                    entry( "remove", remove ),
                    entry( "UPPER", UPPER ),
                    entry( "lower", lower ),
                    entry( "include", include ),
                    entry( "replaceFirst", replaceFirst ),
                    entry( "replaceAll", replaceAll )
            );

    public final Function<Processor, Stream<String>> transformFor(
            String line) {
        return transforms.getOrDefault( line, nop );
    }

    public static Stream<String> revealTags(Stream<String> in) {
        return in.map( l -> l.replaceFirst( "CS:", "cs" + ":" ) );
    }

    /**
     * Keep use of Processor Factory simple for default case
     */
//    public static class Factory {
    final String commentToken;

    public ProcessorFactory newProcessorFactory() {
        return new ProcessorFactory( Path.of( ".java" ), "cs" );
    }

    public static String commentTokenFor(Path p) {
        var filename = p.getFileName().toString();
        int lastIndex = filename.lastIndexOf( "." );
        if ( lastIndex < 0 ) {
            return "#";
        }
        String extension = filename.substring( lastIndex + 1 );
        switch ( extension ) {
            case "java": return "//";
            case "bat":
            case "cmd": return "@REM";
            case "py":
            case "sh":
            default:
                return "#";
        }
    }
}
