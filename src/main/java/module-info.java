/**
 *
 */
module io.github.jristretto.linesprocessor {
    requires java.logging;
    provides io.github.jristretto.linesprocessor.TagProvider with io.github.jristretto.linesprocessor.Hello;
    uses io.github.jristretto.linesprocessor.TagProvider;
}
