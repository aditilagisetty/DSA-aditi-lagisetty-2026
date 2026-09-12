# Translating grade_analyzer.py to Kotlin

## Purpose of the code

This is a small grade-analysis tool. It takes a list of students, each with a
name and a list of numeric grades, and computes:

- each student's average and median grade
- each student's letter grade (A–F)
- the overall class average
- which student has the highest average

I picked this example because it uses a few core ideas — a data class,
some basic collection operations (sum, sort, average), and a bit of
conditional logic — without depending on any external libraries, which
kept the port simple.

## The good

Translating this to Kotlin was smoother than I expected. Kotlin's data
classes map almost one-to-one onto Python's `@dataclass`, so `Student`
translated over cleanly, and I got `equals()`, `toString()`, etc. for
free without writing anything extra. Kotlin's collection functions
(`.sum()`, `.sorted()`, `.maxBy()`, `.sumOf()`) are just as expressive as
Python's built-ins and list comprehensions — in some cases even more
concise, like using `maxBy` instead of writing out a manual `max(..., key=...)`.
The `when` expression made the letter-grade logic read very naturally,
arguably more cleanly than Python's `if/elif` chain.

## The bad

The biggest adjustment was being explicit about types everywhere —
Python's `List[float]` becomes Kotlin's `List<Double>`, and I had to
make sure every grade literal was written as `95.0` instead of `95` to
avoid type mismatches between `Int` and `Double`. This is exactly the
kind of static-analysis safety net the language is designed to enforce,
but it does mean a little more upfront care compared to Python, where
types are inferred dynamically at runtime.

## The ugly

String formatting was the one part that felt clunkier than Python.
Python's f-strings handle inline formatting (like `:.2f`) very smoothly.
In Kotlin, I had to fall back to the `.format()` style (`"%.2f".format(...)`)
inside a string template, which works but reads a bit more awkwardly than
Python's native syntax.

## Overall

Overall the port took less time than I expected — most of the logic
translated almost directly, and the main friction points were type
strictness and string formatting syntax, both of which are minor once
you know to expect them.
