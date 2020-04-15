import java.util.concurrent.Phaser
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.thread
import java.util.concurrent.atomic.*
import java.util.*
import kotlin.math.min

class RunnerMultithreadingImpl<T> : Runner<T>, CycleChecker<T>() {
    override fun runProcessors(
        processors: MutableSet<Processor<T>>?,
        maxThreads: Int,
        maxIterations: Int
    ): MutableMap<String, MutableList<T>> {
        if (processors.isNullOrEmpty()) {
            return mutableMapOf()
        } else {
            checkCyclesOrUnexpectedProcessors(processors)
            val results = processors.associate {
                it.id to mutableListOf<T>()
            }
            val locks = processors.associate { it.id to ReentrantLock() }
            val iterationToStop = AtomicInteger(maxIterations - 1)
            val processedForIteration = (1..maxIterations).map { AtomicInteger(0) }
            val exception: AtomicReference<ProcessorException?> = AtomicReference(null)

            fun needToStop(): Boolean {
                val stop = iterationToStop.get()
                return (stop == -1 || processedForIteration[stop].get() == processors.size || exception.get() != null)
            }

            fun workWithLock(processor: Processor<T>) {
                val id = processor.id
                val curList: MutableList<T> = results[id]!!
                val iteration = curList.size
                if (iterationToStop.get() >= iteration && (processor.inputIds.isEmpty() || processor.inputIds.all { results[it]!!.size > iteration })) {
                    try {
                        val result = processor.process(processor.inputIds.map {
                            results[it]!![iteration]
                        })
                        if (result != null) {
                            curList.add(result)
                            processedForIteration[iteration].incrementAndGet()
                        } else {
                            iterationToStop.accumulateAndGet(iteration - 1) { a, b -> min(a, b) }
                        }
                    } catch (e: ProcessorException) {
                        exception.compareAndSet(exception.get(), e)
                    }
                }
            }

            val onFinish = Phaser(maxThreads + 1)

            fun onThread() {
                mainLoop@ while (true) {
                    for (processor in processors) {
                        if (needToStop()) {
                            break@mainLoop
                        }
                        val lock = locks[processor.id]!!
                        if (lock.tryLock()) {
                            workWithLock(processor)
                            lock.unlock()
                        }
                    }
                }
                onFinish.arrive()
            }

            repeat(maxThreads - 1) {
                thread {
                    onThread()
                }
            }
            onThread()
            onFinish.arriveAndAwaitAdvance()
            val exc = exception.get()
            if (exc != null) {
                throw exc
            }
            return results
                .mapValues { (_, value) -> value.take(iterationToStop.get() + 1).toMutableList() }
                .toMutableMap()
        }
    }
}

