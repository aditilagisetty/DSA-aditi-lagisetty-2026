# Assignment 2 — Strategies for Exercises 3 and 5

## Exercise 3: Reverse a Stack

We want to flip the stack so the bottom item ends up on top, and the
top item ends up on the bottom.

1. Make one empty temporary stack.
2. Pop everything off the original stack, one item at a time, and push
   each item onto the temporary stack as we go.
3. That's it — the temporary stack is now the reversed version.

Why this works: popping takes items off starting from the top. Pushing
puts each new item on top of the pile. So the very first item we pop
(the old top) ends up at the bottom of the temp stack, and the very last
item we pop (the old bottom) ends up on top of the temp stack. That's
a reversal.

## Exercise 4: Valid parentheses
1. Scan the string left to right.
2. When you see an opening bracket ((, {, [), push it onto the stack.
3. When you see a closing bracket, check the top of the stack: if it's null (stack empty) or doesn't match the corresponding opening bracket, the string is invalid.
4. Otherwise, pop the matching opener off the stack and continue.
5. At the end, the string is valid only if the stack is empty (every opener found its closer).


## Exercise 5: Copy a Stack 

Moving items from a stack into a queue keeps their
order the same (queues don't flip order). But moving items from a queue
onto a stack does flip the order. So if we only move things around
once, we'll accidentally end up with things backwards.

As a solution we should flip it twice, so the two flips cancel out.

1. Pop every item off the original stack and put it into the queue.
   (Original stack is now empty. Order is still correct in the queue.)
2. Take every item out of the queue and push it onto a temporary stack.
   (This flips the order once.)
3. Pop every item off that temporary stack and put it back into the
   queue. (Order is preserved again — still flipped from step 2.)
4. Take every item out of the queue one more time. For each item, push it
   onto two stacks at the same time: the original stack (to restore
   it) and a new stack (the copy).

The original stack is back to normal, and we have a new
stack that's an exact copy of it.