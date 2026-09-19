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
        val list = MyMutableIntList()
        check("new list has size 0", list.size() == 0)
    }

    // Test: add increases size and stores values
    run {
        val list = MyMutableIntList()
        list.add(10)
        list.add(20)
        list.add(30)
        check("size is 3 after 3 adds", list.size() == 3)
        check("get(0) is 10", list.get(0) == 10)
        check("get(1) is 20", list.get(1) == 20)
        check("get(2) is 30", list.get(2) == 30)
    }

    // Test: set overwrites an existing value
    run {
        val list = MyMutableIntList()
        list.add(1)
        list.add(2)
        list.set(1, 99)
        check("set(1, 99) changes get(1) to 99", list.get(1) == 99)
        check("set does not change size", list.size() == 2)
    }

    // Test: clear empties the list
    run {
        val list = MyMutableIntList()
        list.add(1)
        list.add(2)
        list.clear()
        check("size is 0 after clear", list.size() == 0)
    }

    // Test: get/set out of bounds throws
    run {
        val list = MyMutableIntList()
        list.add(1)
        var threw = false
        try {
            list.get(5)
        } catch (e: IndexOutOfBoundsException) {
            threw = true
        }
        check("get(5) throws when size is 1", threw)

        threw = false
        try {
            list.get(-1)
        } catch (e: IndexOutOfBoundsException) {
            threw = true
        }
        check("get(-1) throws", threw)
    }

    // Test: resizing beyond initial capacity of 10 works correctly
    run {
        val list = MyMutableIntList()
        for (i in 0 until 25) {
            list.add(i)
        }
        check("size is 25 after adding 25 elements", list.size() == 25)
        var allCorrect = true
        for (i in 0 until 25) {
            if (list.get(i) != i) {
                allCorrect = false
            }
        }
        check("all 25 elements retained correct values after resizing", allCorrect)
    }

    println("$testsPassed / $testsRun tests passed")

    // Timing: measure add() performance across increasing list sizes
    println()
    println("size,total_ms,ns_per_add")
    val sizes = listOf(100, 1_000, 10_000, 100_000, 1_000_000, 10_000_000, 100_000_000)
    for (n in sizes) {
        val list = MyMutableIntList()
        val start = System.nanoTime()
        for (i in 0 until n) {
            list.add(i)
        }
        val elapsedNs = System.nanoTime() - start
        val elapsedMs = elapsedNs / 1_000_000.0
        val nsPerAdd = elapsedNs.toDouble() / n
        println("$n,$elapsedMs,$nsPerAdd")
    }
}
