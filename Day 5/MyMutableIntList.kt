class MyMutableIntList {
    private var array = Array(10) { 0 }  // initial capacity
    private var count = 0

    fun size(): Int {
    return count
    }

    fun get(index: Int): Int {
        // check index is in range, then return array[index]
        if (index < 0 || index >= count){
            throw IndexOutOfBoundsException()
        }
        else {
            return array[index]
        }

    }

    fun set(index: Int, value: Int) {
        // check index is in range, then array[index] = value
        if (index < 0 || index >= count){
            throw IndexOutOfBoundsException()
        }
        else
            array[index] = value
    }

    fun clear() {
        count = 0
    }

    fun add(element: Int) {
        if (count == array.size) {
            val newArray = Array(array.size * 2) { 0 }
            for (i in 0 until count) {
                newArray[i] = array[i]
            }
            array = newArray
        }
        array[count] = element
        count++
    }
}

