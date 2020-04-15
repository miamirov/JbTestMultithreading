open class CycleChecker<T> {
    fun checkCyclesOrUnexpectedProcessors(processors: MutableSet<Processor<T>>) {
        val visited = processors.associate { it.id to 0 }.toMutableMap()
        val idsToProcessors = processors.associateBy { it.id }
        fun dfs(processor: Processor<T>) {
            visited[processor.id] = 1
            processor.inputIds.forEach {
                if (!idsToProcessors.contains(it)) {
                    throw ProcessorException("Unexpected processor")
                }
                if (visited[it]!! == 1) {
                    throw ProcessorException("Cycle in processor graph")
                }
                if (visited[it]!! == 0) {
                    dfs(idsToProcessors[it]!!)
                }
            }
            visited[processor.id] = 2
        }
        processors.forEach { if (visited[it.id] == 0) dfs(it) }


    }

}