import kotlin.random.Random

const val TEST = 5
fun `generate test set`(b: Int): Pair<MutableSet<Processor<Int>>, MutableSet<Processor<Int>>> {
    val first = mutableSetOf<Processor<Int>>()
    val second = mutableSetOf<Processor<Int>>()

    var counter = 0
    for (i in 1..10) {
        val id = counter.toString()
        val inputIds = mutableListOf<String>()
        first.add(ProccesorWithTime(id, inputIds))
        second.add(ProccesorWithTime(id, inputIds))
        counter++

    }

    for (i in 0..b) {
        val id = counter.toString()
        val numOfInputIds = Random.nextInt(1, counter - 1)
        val inputIds = (0 until counter).toList().shuffled().take(numOfInputIds).map { it.toString() }.toMutableList()
        first.add(ProccesorWithTime(id, inputIds))
        second.add(ProccesorWithTime(id, inputIds))
        counter++
    }
    return first to second
}

fun main() {

    for (numOfThreads in (1..16)) {
        var eff = 0.toFloat()
        for (test in 1..TEST) {
            //println("test$test")
            val runner = RunnerMultithreadingImpl<Int>()
            val simpleRunner = RunnerSimpleImpl<Int>()
            val testSet = `generate test set`(100)
            val x = System.nanoTime()
            runner.runProcessors(testSet.first, numOfThreads, 10)
            val y = System.nanoTime()
            simpleRunner.runProcessors(testSet.second, 1, 10)
            val z = System.nanoTime()
            eff += (z - y).toFloat() / (y - x).toFloat() / numOfThreads
            //println("Runner time: ${(y - x) / 1e9}")
            //println("Simple runner time: ${(z - y) / 1e9}")
            //println("Number of threads: $numOfThreads")
            //println("Ratio: ${(z - y).toFloat() / (y - x).toFloat()}")
        }
        println("Efficiency for $numOfThreads threads: ${eff / TEST}")
    }
}