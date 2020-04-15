import org.junit.Test
import org.junit.jupiter.api.assertThrows
import kotlin.random.Random
import kotlin.test.assertEquals

internal class RunnerMultithreadingImplTest {
    private val runner = RunnerMultithreadingImpl<Int>()
    private val simpleRunner = RunnerSimpleImpl<Int>()

    @Test
    fun `empty set test`() {
        assertEquals(mutableMapOf(), runner.runProcessors(mutableSetOf(), 1, 1))
    }


    @Test
    fun `simple set with cycles`() {
        val exception = assertThrows<ProcessorException> {
            runner.runProcessors(
                mutableSetOf<Processor<Int>>(
                    Maximizator("1", mutableListOf("1"))
                ),
                1,
                1
            )
        }
        assertEquals("Cycle in processor graph", exception.message)
    }

    @Test
    fun `simple set with unexpected processors`() {
        val exception = assertThrows<ProcessorException> {
            runner.runProcessors(
                mutableSetOf(
                    Maximizator("1", mutableListOf("2"))
                ),
                1,
                1
            )
        }
        assertEquals("Unexpected processor", exception.message)
    }

    @Test
    fun `processor throws exception`() {
        val exception = assertThrows<ProcessorException> {
            runner.runProcessors(
                mutableSetOf(ExceptionGenerator("1", mutableListOf())),
                7,
                1
            )
        }
        assertEquals("id: 1", exception.message)
    }


    @Test
    fun `random test`() {
        fun `generate test set`(a: Int, b: Int): Pair<MutableSet<Processor<Int>>, MutableSet<Processor<Int>>> {
            val first = mutableSetOf<Processor<Int>>()
            val second = mutableSetOf<Processor<Int>>()
            var counter = 0
            for (i in 1..a) {
                val id = counter.toString()
                val inputIds = mutableListOf<String>()
                when (Random.nextInt(1, 4)) {
                    1 -> {
                        val value = Random.nextInt(1, 10)
                        first.add(Stable(id, inputIds, value))
                        second.add(Stable(id, inputIds, value))
                    }
                    2 -> {
                        val value = Random.nextInt(1, 10)
                        val numOfStable = Random.nextInt(0, 20)
                        first.add(StableEndsWithNulls(id, inputIds, value, numOfStable))
                        second.add(StableEndsWithNulls(id, inputIds, value, numOfStable))
                    }
                    3 -> {
                        val sequence = List(Random.nextInt(2, 10)) { Random.nextInt(1, 10) }
                        first.add(CyclicSequence(id, inputIds, sequence))
                        second.add(CyclicSequence(id, inputIds, sequence))
                    }
                    4 -> {
                        val sequence = List(Random.nextInt(2, 10)) { Random.nextInt(1, 10) }
                        first.add(SequenceEndsWithNulls(id, inputIds, sequence))
                        second.add(SequenceEndsWithNulls(id, inputIds, sequence))
                    }
                    else -> {
                    }
                }
                counter++
            }
            for (i in 1..b) {
                val id = counter.toString()
                val numOfInputIds = Random.nextInt(1, counter - 1)
                val inputIds =
                    (1 until counter).shuffled().take(numOfInputIds).map { it.toString() }.toMutableList()
                when (Random.nextInt(1, 4)) {
                    1 -> {
                        first.add(Maximizator(id, inputIds))
                        second.add(Maximizator(id, inputIds))
                    }
                    2 -> {
                        first.add(Median(id, inputIds))
                        second.add(Median(id, inputIds))
                    }
                    3 -> {
                        val mod = Random.nextInt(2, 10)
                        first.add(ModSummator(id, inputIds, mod))
                        second.add(ModSummator(id, inputIds, mod))
                    }
                }
                counter++
            }
            return first to second
        }

        for (test in 1..10000) {
            val testSet = `generate test set`(100, 200)
            val numOfThreads = Random.nextInt(1, 16)
            val iterations = Random.nextInt(1, 10)
            assertEquals(
                simpleRunner.runProcessors(testSet.first, 1, iterations),
                runner.runProcessors(testSet.second, numOfThreads, iterations)
            )
        }
    }
}