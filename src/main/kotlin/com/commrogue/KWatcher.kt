//package com.commrogue
//
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.GlobalScope
//import kotlinx.coroutines.channels.Channel
//import kotlinx.coroutines.launch
//import java.io.File
//import java.nio.file.WatchEvent
//
//abstract class KWatcher(
//    private val scope: CoroutineScope = GlobalScope,
//    val mode: Mode,
//    val tag: Any? = null,
//    private val channel: Channel<KWatchEvent> = Channel()
//) : Channel<KWatchEvent> by channel {
//    init {
//        scope.launch {
//            send(
//                KWatchEvent(
//                    file = path.toFile(),
//                    tag = tag,
//                    kind = KWatchEvent.Kind.Initialized
//                )
//            )
//        }
//    }
//}
//
///**
// * Wrapper around [WatchEvent] that comes with properly resolved absolute path
// */
//data class KWatchEvent(
//    /**
//     * Abolute path of modified folder/file
//     */
//    val file: File,
//
//    /**
//     * Kind of file system event
//     */
//    val kind: Kind,
//
//    /**
//     * Optional extra data that should be associated with this event
//     */
//    val tag: Any?
//) {
//    /**
//     * File system event, wrapper around [WatchEvent.Kind]
//     */
//    enum class Kind(val kind: String) {
//        /**
//         * Triggered upon initialization of the channel
//         */
//        Initialized("initialized"),
//
//        /**
//         * Triggered when file or directory is created
//         */
//        Created("created"),
//
//        /**
//         * Triggered when file or directory is modified
//         */
//        Modified("modified"),
//
//        /**
//         * Triggered when file or directory is deleted
//         */
//        Deleted("deleted")
//    }
//}
//
///**
// * Describes the mode this channels is running in
// */
//enum class Mode {
//    /**
//     * Watches only the given file
//     */
//    SingleFile,
//
//    /**
//     * Watches changes in subdirectories
//     */
//    Recursive
//}