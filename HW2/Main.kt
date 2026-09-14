var testsRun = 0
var testsPassed = 0

fun check(description: String, condition: Boolean) {
    testsRun++
    if (condition) {
        testsPassed++
        println("yes $description")
    } else {
        println("no $description")
    }
}

fun main() {
    // Test: new list is empty
    run {
        val list = DoublyLinkedList<Int>()
        check("new list is empty", list.isEmpty())
        check("new list peekFront is null", list.peekFront() == null)
        check("new list peekBack is null", list.peekBack() == null)
    }

    // Test: pushFront then peek/pop
    run {
        val list = DoublyLinkedList<Int>()
        list.pushFront(1)
        list.pushFront(2) // list: 2, 1
        check("peekFront is 2 after pushFront(1), pushFront(2)", list.peekFront() == 2)
        check("peekBack is 1 after pushFront(1), pushFront(2)", list.peekBack() == 1)
        check("popFront returns 2", list.popFront() == 2)
        check("peekFront is 1 after popping 2", list.peekFront() == 1)
    }

    // Test: popping the only element via popFront empties the list
    run {
        val list = DoublyLinkedList<Int>()
        list.pushFront(42)
        check("popFront returns 42", list.popFront() == 42)
        check("list is empty after popFront on only element", list.isEmpty())
        check("peekFront is null after popFront on only element", list.peekFront() == null)
        check("peekBack is null after popFront on only element", list.peekBack() == null)
    }

    // Test: pushBack then peek/pop
    run {
        val list = DoublyLinkedList<Int>()
        list.pushBack(1)
        list.pushBack(2) // list: 1, 2
        check("peekBack is 2 after pushBack(1), pushBack(2)", list.peekBack() == 2)
        check("peekFront is 1 after pushBack(1), pushBack(2)", list.peekFront() == 1)
        check("popBack returns 2", list.popBack() == 2)
        check("peekBack is 1 after popping 2", list.peekBack() == 1)
    }

    // Test: popping the only element via popBack empties the list
    run {
        val list = DoublyLinkedList<Int>()
        list.pushBack(42)
        check("popBack returns 42", list.popBack() == 42)
        check("list is empty after popBack on only element", list.isEmpty())
        check("peekFront is null after popBack on only element", list.peekFront() == null)
        check("peekBack is null after popBack on only element", list.peekBack() == null)
    }

    // Test: pushFront and pushBack together, popping from both ends
    run {
        val list = DoublyLinkedList<Int>()
        list.pushBack(2)
        list.pushFront(1)
        list.pushBack(3) // list: 1, 2, 3
        check("peekFront is 1 after mixed pushes", list.peekFront() == 1)
        check("peekBack is 3 after mixed pushes", list.peekBack() == 3)
        check("popFront returns 1", list.popFront() == 1)
        check("popBack returns 3", list.popBack() == 3)
        check("only middle element 2 remains", list.peekFront() == 2 && list.peekBack() == 2)
        check("popBack returns remaining 2", list.popBack() == 2)
        check("list is empty after popping all mixed elements", list.isEmpty())
    }

    // Test: pop on empty list returns null
    run {
        val list = DoublyLinkedList<Int>()
        check("popFront on empty list is null", list.popFront() == null)
        check("popBack on empty list is null", list.popBack() == null)
    }

    println("\n$testsPassed / $testsRun tests passed")

    run {
        val stack = LinkedListStack<Int>()
        check("new stack is empty", stack.isEmpty())
        check("new stack peek is null", stack.peek() == null)

        stack.push(1)
        stack.push(2)
        stack.push(3) // stack top-to-bottom: 3, 2, 1

        // check that peek() shows the last thing pushed (top of stack)
        check("peek shows last thing pushed (3)", stack.peek() == 3)

        // check that isEmpty() is now false
        check("stack is not empty after pushing", !stack.isEmpty())

        // check that pop() removes and returns the last thing pushed
        check("check pop returns the last pushed value (3)", stack.pop() == 3)

        // check that after that pop, peek() shows the next item down
        check("after popping 3, peek shows 2", stack.peek() == 2)

        // pop everything, then check isEmpty() is true again
        check("pop returns 2", stack.pop() == 2)
        check("pop returns 1", stack.pop() == 1)
        check("stack is empty after popping everything", stack.isEmpty())

        // check that pop() on an empty stack returns null
        check("pop on empty stack returns null", stack.pop() == null)
    }

    println("\n$testsPassed / $testsRun tests passed")

    run {
        val queue = LinkedListQueue<Int>()
        check("new queue is empty", queue.isEmpty())
        check("new queue peek is null", queue.peek() == null)

        queue.enqueue(1)
        queue.enqueue(2)
        queue.enqueue(3) // queue front-to-back: 1, 2, 3

        check("peek shows first thing enqueued (1)", queue.peek() == 1)
        check("queue is not empty after enqueueing", !queue.isEmpty())

        check("dequeue returns 1 (first enqueued)", queue.dequeue() == 1)
        check("after dequeuing 1, peek shows 2", queue.peek() == 2)

        check("dequeue returns 2", queue.dequeue() == 2)
        check("dequeue returns 3", queue.dequeue() == 3)
        check("queue is empty after dequeuing everything", queue.isEmpty())

        check("dequeue on empty queue returns null", queue.dequeue() == null)
    }

    println("\n$testsPassed / $testsRun tests passed")

    // Valid parentheses tests
    run {
        check("empty string is valid", isValidParentheses(""))
        check("simple pair is valid", isValidParentheses("()"))
        check("nested pairs are valid", isValidParentheses("([{}])"))
        check("sequential pairs are valid", isValidParentheses("()[]{}"))
        check("mismatched types are invalid", !isValidParentheses("(]"))
        check("wrong order is invalid", !isValidParentheses("([)]"))
        check("unclosed opener is invalid", !isValidParentheses("((("))
        check("unmatched closer is invalid", !isValidParentheses("))"))
        check("closer with nothing open is invalid", !isValidParentheses(")("))
    }
}