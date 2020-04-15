import kotlin.random.Random

class RunnerSimpleImpl<T> : Runner<T>, CycleChecker<T>() {
    override fun runProcessors(
        processors: MutableSet<Processor<T>>?,
        maxThreads: Int,
        maxIterations: Int
    ): MutableMap<String, MutableList<T>> {
        if (processors != null) {
            checkCyclesOrUnexpectedProcessors(processors)
            val results = processors.associate { it.id to mutableListOf<T>() }
            mainLoop@ for (iteration in 1..maxIterations) {
                val iterationResults: MutableMap<String, T> = mutableMapOf()
                var processorsCnt = 0
                while (processorsCnt < processors.size) {
                    for (processor in processors) {
                        if (processor.inputIds.isNullOrEmpty()) {
                            val result = processor.process(listOf())
                            if (result == null) {
                                break@mainLoop
                            } else {
                                iterationResults[processor.id] = result
                            }
                            processorsCnt += 1
                        } else if (processor.inputIds.all { iterationResults.contains(it) }) {
                            val input: List<T> = processor.inputIds.map { iterationResults[it]!! }
                            val result = processor.process(input)
                            if (result == null) {
                                break@mainLoop
                            } else {
                                iterationResults[processor.id] = result
                            }
                            processorsCnt += 1
                        }

                    }
                }
                iterationResults.forEach { (key, value) ->
                    results[key]!!.add(value)
                }
            }
            return results.toMutableMap()
        } else {
            return mutableMapOf()
        }
    }


}