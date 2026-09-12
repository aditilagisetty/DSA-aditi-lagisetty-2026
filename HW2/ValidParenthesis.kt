/**
 * Exercise 4: determines whether a string of brackets is validly matched.
 *
 * A string is valid if every closing bracket matches the most recently
 * seen unmatched opening bracket of the same type, and every opener is
 * eventually closed.
 *
 * @param s the string to check, containing only '(', ')', '[', ']', '{', '}'
 * @return true if the brackets in [s] are properly matched and nested
 */
fun isValidParentheses(s: String): Boolean {
    val stack = LinkedListStack<Char>()
    val closingToOpening = mapOf(')' to '(', ']' to '[', '}' to '{')

    for (c in s) {
        when (c) {
            // Openers are pushed so we can match them against a later closer.
            '(', '[', '{' -> stack.push(c)
            ')', ']', '}' -> {
                val top = stack.pop()
                // Invalid if there's nothing open to match, or the most
                // recent opener is the wrong type for this closer.
                if (top == null || top != closingToOpening[c]) {
                    return false
                }
            }
        }
    }

    // Valid only if every opener was eventually matched (stack is empty).
    return stack.isEmpty()
}