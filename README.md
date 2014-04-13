## Overview

This project is a [PEG][peg] implementation in Java based on the excellent [LPeg][lpeg] library for Lua.

## Motivation

There's not that many [PEG][peg] parsers implemented in Java that I know of, and whenever
I had a need for one I always hoped that there would exist one that was similar to [LPeg][lpeg].
A parser that didn't need any code generation or byte code weaving etc. A parser that I could use
in plain Java or any other JVM based language. I've also always felt that [LPeg][lpeg] was an
interesting kind of implementation. Turning a parse tree into a fast virtual machine that's run
during the match phase. This led me to start this project. To learn more about the internals
of [LPeg][lpeg] and see if I could do an implementation for the platform I spend my days at work
developing for.

## Implementation details

The DSL for creating patterns in Java and Lua is of course different.

Initially many of the capturing constructs where ported, but I settled on only
having match time captures (called actions in my implementation), since I could
do most of the things needed with only one kind of construct. There are default
implementations provided for simple captures (i.e. capturing the substring of a match),
named captures that could be used like named group captures and back captures in Lua.
It's easy to roll you own since an action is just an interface to be implemented, and
most often you'll want to manage your parsing state in some other kind of form than a
simple string capture.

Grammars in this implementation requires that every rule has a name. Even if it's not
referenced in another rule or pattern.

A pattern in this implementation cannot be matched against. A pattern must be compiled
to a pattern matcher which converts the parse tree to a VM. Compiling the patterns can
be quite expensive when dealing with large character sets since this library provides
full [Unicode][unicode] support. Hence it's preferable to construct the pattern matchers
up front if possible.

## Status

This is a new project and I'm sure that many things could be done better or be more
polished. It's derived from the original C code and there might be some traces of that.
Please feel free to improve upon it if you think this is an interesting hack. The lack
of examples is one area that would benefit of improvments right away.

## Example

Here's an example of taken from the [LPeg][lpeg] manual that cannot be expressed without
support for grammars and recursive patterns - balanced parentheses:

```java
Pattern c = diff(n(1), set("()")); // All characters except '()'
Pattern p = seq(ch('('), choice(c, ref("1")).repeat(0), ch(')'));

Pattern g = grammar(rule("1", p));
g.print(System.out);

PatternMatcher matcher = g.compile();
matcher.print(System.out);

System.out.println(matcher.match("()").matched());
System.out.println(matcher.match("((abc))").matched());
System.out.println(matcher.match("(((abc)))").matched());
System.out.println(matcher.match("((((abc)))").matched());
```

Calling `print` on a pattern prints its parse tree:

```
[0 = 1]
 grammar 1
   rule index: 0, name = '1'
     seq
       char (
       seq
         rep
           choice
             set [(0-27)(2a-10ffff)]
             call index: 0, name: '1'
         char )
```

Calling `print` on a pattern matcher prints its instructions:

```
00: CALL -> 2
01: END
02: CHAR '('
03: TEST_SET [(0-28)(2a-10ffff)]-> 9
04: TEST_SET [(0-27)(2a-10ffff)]-> 7
05: ANY
06: JMP -> 3
07: CALL -> 2
08: JMP -> 3
09: CHAR ')'
10: RET
11: END
```

The last string in the example above will return a match failure.

Here's a simple example just to show one of the default capturers:

```java
Capturer capturer = new Capturer();

Pattern d = capturer.capture(range("09").repeat(1));
Pattern s = set("\t ").repeat(0);

seq(d, seq(s, ch('+'), s, d).repeat(0)).compile().match("1 + 2 + 3");

int n = 0;
while (capturer.size() != 0)
    n += parseInt(capturer.pop().value);
```

[peg]:http://pdos.csail.mit.edu/%7Ebaford/packrat/
[lpeg]:http://www.inf.puc-rio.br/~roberto/lpeg/
[unicode]:http://www.unicode.org/
