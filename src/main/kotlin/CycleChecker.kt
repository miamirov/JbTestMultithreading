open class CycleChecker<T> {
    fun checkCyclesOrUnexpectedProcessors(processors: MutableSet<Processor<T>>) {
        val visited: MutableSet<String> = HashSet()
        val idsToProcessors: MutableMap<String, Processor<T>> = mutableMapOf()
        fun dfs(processor: Processor<T>) {
            visited.add(processor.id)
            processor.inputIds.forEach {
                if (visited.contains(it)) {
                    throw ProcessorException("Cycle in processor graph")
                } else {
                    if (idsToProcessors.contains(it)) {
                        throw ProcessorException("Unexpected processor")
                    } else {
                        dfs(processor)
                    }
                }
            }
        }
        processors.forEach { idsToProcessors[it.id] = it }
        processors.forEach {
            if (!visited.contains(it.id)) {
                dfs(it)
            }
        }
    }

}