import kotlin.random.Random

abstract class IntOrNullProcessor(private val id: String, private val inputIds: MutableList<String>) : Processor<Int> {

    override fun getInputIds(): MutableList<String> {
        return inputIds
    }

    override fun getId(): String {
        return id
    }

}

abstract class IntOrNullGenerator(id: String, inputIds: MutableList<String>) : IntOrNullProcessor(id, inputIds) {
    private val value = getValue()

    fun getValue(): Iterator<Int?> {
        return getSequence().iterator()
    }

    abstract fun getSequence(): Sequence<Int?>


    override fun process(input: MutableList<Int>?): Int? {
        Thread.sleep(Random.nextInt(10, 100).toLong())
        return value.next()
    }


}

class Stable(id: String, inputIds: MutableList<String>, private val stableValue: Int) :
    IntOrNullGenerator(id, inputIds) {
    override fun getSequence(): Sequence<Int?> {
        return sequence<Int?> {
            while (true) {
                yield(stableValue)
            }
        }
    }
}

class StableEndsWithNulls(
    id: String,
    inputIds: MutableList<String>,
    private val stableValue: Int,
    private val numOfStable: Int
) :
    IntOrNullGenerator(id, inputIds) {

    override fun getSequence(): Sequence<Int?> {
        return sequence<Int?> {
            for (i in 1..numOfStable) {
                yield(stableValue)
            }
            while (true) {
                yield(null)
            }
        }
    }
}

class SequenceEndsWithNulls(id: String, inputIds: MutableList<String>, private val seq: List<Int>) :
    IntOrNullGenerator(id, inputIds) {

    override fun getSequence(): Sequence<Int?> {
        return sequence<Int?> {
            yieldAll(seq)
            while (true) {
                yield(null)
            }
        }
    }

}

class CyclicSequence(id: String, inputIds: MutableList<String>, private val seq: List<Int>) :
    IntOrNullGenerator(id, inputIds) {

    override fun getSequence(): Sequence<Int?> {
        return sequence<Int?> {
            while (true) {
                yieldAll(seq)
            }
        }
    }
}

class ModSummator(id: String, inputIds: MutableList<String>, private val mod: Int) : IntOrNullProcessor(id, inputIds) {
    override fun process(input: MutableList<Int>?): Int? {
        return if (input == null || input.isEmpty())
            throw ProcessorException()
        else
            input.reduce { acc, i -> (acc + i) % mod } % mod
    }
}

class Max(id: String, inputIds: MutableList<String>) : IntOrNullProcessor(id, inputIds) {
    override fun process(input: MutableList<Int>?): Int? {
        if (null == input || input.isEmpty()) {
            throw ProcessorException()
        } else {
            return input.max()
        }

    }
}


fun main() {
    print(ModSummator("11", mutableListOf(), 11).process(mutableListOf()))
}