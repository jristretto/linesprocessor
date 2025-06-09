/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package io.github.jristretto.linesprocessor;

import java.util.Map;
import static java.util.Map.entry;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Provides hello tag, which prefixes lines with "Hello "
 * @author Pieter van den Hombergh {@code <pieter.van.den.hombergh@gmail.com>}
 */
public class Hello implements TagProvider {

    // puts hello in front of the lines.
    private final Function<Processor, Stream<String>> hello
            = p -> Stream.of( "Hello " + p.line().split( "//cs" + ":" )[ 0 ] );

    // implementation using a plain method.
    private Stream<String> helloToo(Processor p) {
        return Stream.of( "Hello " + p.line().split( "//cs" + ":" )[ 0 ] );
    }

    private final Map<String, Function<Processor, Stream<String>>> transforms = Map
            .ofEntries(
                    entry( "hello", hello ),
                    entry( "hellotoo", this::helloToo )
            );

    @Override
    public final Map<String, Function<Processor, Stream<String>>> newTags() {
        System.out.println( "Hello called" );
        return transforms;
    }

}
