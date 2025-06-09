
package io.github.jristretto.linesprocessor;

import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Box that processes its content with a given function into a Stream of
 * Strings.
 *
 * @author Pieter van den Hombergh {@code <pieter.van.den.hombergh@gmail.com>}
 */
public record Processor(String line, String payLoad,
        Function<Processor, Stream<String>> transformation,
        String instruction, int lineNumber, String text, String indent,
        String startEnd) implements
        Function<Processor, Stream<String>> {

    @Override
    public Stream<String> apply(Processor proc) {
        return this.transformation.apply( this );
    }

    @Override
    public String toString() {
        return lineNumber + "  line=(" + line + ") - payload {" + payLoad + "}- instruction<" + instruction +
                ">\" text:'"+text+"'"+ " indent:["+indent+"]="+indent.length();
    }

}
