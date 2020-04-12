class RunnerSimpleImpl<T> : Runner<T>, CycleChecker<T>() {
    override fun runProcessors(
        processors: MutableSet<Processor<T>>?,
        maxThreads: Int,
        maxIterations: Int
    ): MutableMap<String, MutableList<T>> {
        return if (processors != null) {
            checkCyclesOrUnexpectedProcessors(processors)
            val results: MutableMap<String, MutableList<T>> = mutableMapOf()
            mainLoop@ for (iterations in 1..maxIterations) {
                val iterationResults: MutableMap<String, T> = mutableMapOf()
                var processorsCnt = 0
                while (processorsCnt < processors.size) {
                    for (processor in processors) {
                        if (processor.inputIds.isNullOrEmpty()) {
                            processor.process(listOf())
                        } else if (processor.inputIds.all { iterationResults.contains(it) }) {
                            val input: List<T> = processor.inputIds.map { iterationResults[it]!! }
                            val result = processor.process(input)
                            if (result == null) {
                                break@mainLoop
                            } else {
                                iterationResults[processor.id] = result
                            }
                        }
                    }
                }
                iterationResults.forEach { (key, value) ->
                    results.getOrDefault(key, mutableListOf()).add(value)
                }
            }
            results.toMutableMap()
        } else {
            mutableMapOf()
        }
    }
}