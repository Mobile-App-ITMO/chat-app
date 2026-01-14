import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.yield
import kotlin.collections.ArrayDeque

class EmotionAnalyzer(
    private val scope: CoroutineScope,
    private val sentiment: io.ktor.chat.emoml.SentimentService
) {
    private val mutex = Mutex()
    private val queue = ArrayDeque<io.ktor.chat.Message>()
    private var job: Job? = null

    fun onOpen(messages: List<io.ktor.chat.Message>, emotions: MutableMap<Long, io.ktor.chat.emoml.EmotionOutput?>) {
        enqueueNewestFirst(messages, emotions)
        start(emotions)
    }

    fun onNewMessage(message: io.ktor.chat.Message, emotions: MutableMap<Long, io.ktor.chat.emoml.EmotionOutput?>) {
        scope.launch {
            mutex.withLock {
                if (emotions[message.id] == null) queue.addFirst(message)
            }
            start(emotions)
        }
    }

    private fun enqueueNewestFirst(messages: List<io.ktor.chat.Message>, emotions: MutableMap<Long, io.ktor.chat.emoml.EmotionOutput?>) {
        val toQueue = messages.asReversed().filter { emotions[it.id] == null }
        scope.launch {
            mutex.withLock {
                toQueue.forEach { queue.addLast(it) }
            }
        }
    }

    private fun start(emotions: MutableMap<Long, io.ktor.chat.emoml.EmotionOutput?>) {
        if (job?.isActive == true) return
        job = scope.launch {
            while (true) {
                val msg = mutex.withLock { queue.removeFirstOrNull() } ?: break
                val out = runCatching { sentiment.analyze(msg.text) }
                    .getOrElse { io.ktor.chat.emoml.EmotionOutput("EMO: error", 0f, io.ktor.chat.emoml.EmotionOutput.Tone.NEUTRAL) }
                emotions[msg.id] = out
                yield()
            }
        }
    }
}

private fun <T> ArrayDeque<T>.removeFirstOrNull(): T? = if (isEmpty()) null else removeFirst()
