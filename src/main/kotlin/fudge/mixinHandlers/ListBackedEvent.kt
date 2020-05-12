//package fudge.mixinHandlers
//
//import net.fabricmc.fabric.api.event.Event
//import java.util.*
//import java.util.function.Function
//
//internal class ListBackedEvent<T>(
//    private val type: Class<in T>,
//    dummyInvoker: T,
//    invokerFactory: Function<Array<T>, T>
//) :
//    Event<T?>() {
//    private val invokerFactory: Function<Array<T>, T>
//    private val dummyInvoker: T?
//    private var handlers: Array<T>? = null
//    fun update() {
//        invoker = if (handlers == null) {
//            dummyInvoker ?: invokerFactory.apply(java.lang.reflect.Array.newInstance(type, 0) as Array<T>)
//        } else if (handlers!!.size == 1) {
//            handlers!![0]
//        } else {
//            invokerFactory.apply(handlers!!)
//        }
//    }
//
//    override fun register(listener: T?) {
//        if (listener == null) {
//            throw NullPointerException("Tried to register a null listener!")
//        }
//        if (handlers == null) {
//            handlers = arrayOf(listener)
//            handlers!![0] = listener
//        } else {
//            handlers = Arrays.copyOf(handlers, handlers!!.size + 1)
//            handlers.get(handlers.size - 1) = listener
//        }
//        update()
//    }
//
//    init {
//        this.dummyInvoker = dummyInvoker
//        this.invokerFactory = invokerFactory
//        update()
//    }
//}
