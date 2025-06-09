/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package io.github.jristretto.linesprocessor;

import java.io.PrintStream;
import java.nio.file.Path;
import java.util.List;

/**
 * Demo
 *
 * @author Pieter van den Hombergh {@code <pieter.van.den.hombergh@gmail.com>}
 */
public class Usage {

    /**
     * Demo features. Incomplete.
     *
     * @param args not used
     */
    public static void main(String[] args) {

        // note that the CS tags are a 'masked' version of cs,
        // preventing the codestripper from mistreating its own source files.
        String input
                = """
            World      //CS:hello
            SOME BIG STORY//CS:lower
            example text;//CS:replace://TODO
            example2 text;//CS:nop:
                  int a = 2;//CS:replaceFirst:/a/b/
            //CS:remove:start
            example3 the solution
            saved text;//CS:nop:
            //CS:remove:end
            More text
                  //CS:include:humpty.txt
            fin//CS:UPPER
            """.replaceAll("//CS", "//"+ "cs");

        List<String> lines = input.lines().toList();
        int i = 0;
        PrintStream out = System.out;
        var factory = new ProcessorFactory( Path.of( ".java" ) );

        var result = lines.stream()
                //                .peek(l-> out.println("in  "+ l))
                .map( factory::processorFor ) // wrap in recipe
                .peek( p -> out.println( "proc " + p ) )
                .map( p -> p.apply( p ) ) // apply the recipe
                .flatMap( x -> x ) // flatten the result
                //                .peek(l-> out.println("out "+ l))
                .toList();

        for ( String s : result ) {
            System.out.println( "result = " + s );
        }

    }
}
