/*
 * Copyright 2025 homberghp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.jristretto.linesprocessor;

import io.github.jristretto.linesprocessor.ProcessorFactory;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static io.github.jristretto.linesprocessor.OpTest.revealTags;

/**
 * Tests that plugin 'Hello' is found and executed.
 *
 * The 'Hello' plugin prefixes the text before the tag (before //cs) with 'Hello
 * '.
 *
 *
 * @author homberghp
 */
public class HelloTest {

    /**
     * Verify that hello tag is understood and implemented as expected.
     */
    @Test
    public void testHello() {
        List<String> input = revealTags(
                """
                World //cs:hello:
                """
                        .lines()).toList();
        List<String> expected
                = """
                Hello World \
                """
                        .lines().toList();
        var factory = new ProcessorFactory();
        assertThat(input.stream()
                .map(factory::apply) // wrap in recipe
                .flatMap(x -> x) // flatten the result
                //                .peek(l-> out.println("out "+ l))
                .toList()).isEqualTo(expected);
    }
    @Test
    public void testHelloRange() {
        List<String> input = revealTags(
                """
                //cs:hello:start
                World
                and all good people
                //cs:hello:end
                """
                        .lines()).toList();
        List<String> expected
                = """
                Hello World 
                Hello and all good people
                """
                        .lines().toList();
        var factory = new ProcessorFactory();
        assertThat(input.stream()
                .map(factory::apply) // wrap in recipe
                .flatMap(x -> x) // flatten the result
                //                .peek(l-> out.println("out "+ l))
                .toList()).isEqualTo(expected);

    }
}
