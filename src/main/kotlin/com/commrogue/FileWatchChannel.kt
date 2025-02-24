//package com.commrogue
//
//import kotlinx.coroutines.*
//import kotlinx.coroutines.channels.*
//import java.io.File
//import java.nio.file.*
//import java.nio.file.WatchKey
//import java.nio.file.FileVisitResult
//import java.nio.file.attribute.BasicFileAttributes
//import java.nio.file.SimpleFileVisitor
//import java.nio.file.Files
//import java.nio.file.StandardWatchEventKinds.*
//
///**
// * Watches directory. If file is supplied it will use parent directory. If it's an intent to watch just file,
// * developers must filter for the file related events themselves.
// *
// * @param [mode] - mode in which we should observe changes, can be SingleFile, SingleDirectory, Recursive
// * @param [tag] - any kind of data that should be associated with this channel
// * @param [scope] - coroutine context for the channel, optional
// */
//fun File.asWatchChannel(
//    mode: Mode? = null,
//    tag: Any? = null,
//    scope: CoroutineScope = GlobalScope
//) = FileWatchChannel(
//    file = this,
//    mode = mode ?: if (isFile) Mode.SingleFile else Mode.Recursive,
//    scope = scope,
//    tag = tag
//)
//
///**
// * Channel based wrapper for Java's WatchService
// *
// * @param [file] - file or directory that is supposed to be monitored by WatchService
// * @param [scope] - CoroutineScope in within which Channel's sending loop will be running
// * @param [mode] - channel can work in one of the three modes: watching a single file,
// * watching a single directory or watching directory tree recursively
// * @param [tag] - any kind of data that should be associated with this channel, optional
// */
//class FileWatchChannel(
//    val file: File,
//    val scope: CoroutineScope = GlobalScope,
//    val mode: Mode,
//    val tag: Any? = null,
//    private val channel: Channel<KWatchEvent> = Channel()
//) : Channel<KWatchEvent> by channel {
//
//    private val watchService: WatchService = FileSystems.getDefault().newWatchService()
//    private val registeredKeys = ArrayList<WatchKey>()
//    private val path: Path = if (file.isFile) {
//        file.parentFile
//    } else {
//        file
//    }.toPath()
//
//    /**
//     * Registers this channel to watch any changes in path directory and its subdirectories
//     * if applicable. Removes any previous subscriptions.
//     */
//    private fun registerPaths() {
//        registeredKeys.apply {
//            forEach { it.cancel() }
//            clear()
//        }
//        if (mode == Mode.Recursive) {
//            Files.walkFileTree(path, object : SimpleFileVisitor<Path>() {
//                override fun preVisitDirectory(subPath: Path, attrs: BasicFileAttributes): FileVisitResult {
//                    registeredKeys += subPath.register(watchService, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE)
//                    return FileVisitResult.CONTINUE
//                }
//            })
//        } else {
//            registeredKeys += path.register(watchService, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE)
//        }
//    }
//
//    init {
//        // commence emitting events from channel
//        scope.launch(Dispatchers.IO) {
//
//            // sending channel initalization event
//            send(
//                KWatchEvent(
//                    file = path.toFile(),
//                    tag = tag,
//                    kind = KWatchEvent.Kind.Initialized
//                )
//            )
//
//            var shouldRegisterPath = true
//
//            while (!isClosedForSend) {
//
//                if (shouldRegisterPath) {
//                    registerPaths()
//                    shouldRegisterPath = false
//                }
//
//                val monitorKey = watchService.take()
//                val dirPath = monitorKey.watchable() as? Path ?: break
//                monitorKey.pollEvents().forEach {
//                    val eventPath = dirPath.resolve(it.context() as Path)
//
//                    if (mode == Mode.SingleFile && eventPath.toFile().absolutePath != file.absolutePath) {
//                        return@forEach
//                    }
//
//                    val eventType = when(it.kind()) {
//                        ENTRY_CREATE -> KWatchEvent.Kind.Created
//                        ENTRY_DELETE -> KWatchEvent.Kind.Deleted
//                        else -> KWatchEvent.Kind.Modified
//                    }
//
//                    val event = KWatchEvent(
//                        file = eventPath.toFile(),
//                        tag = tag,
//                        kind = eventType
//                    )
//
//                    // if any folder is created or deleted... and we are supposed
//                    // to watch subtree we re-register the whole tree
//                    if (mode == Mode.Recursive &&
//                        event.kind in listOf(KWatchEvent.Kind.Created, KWatchEvent.Kind.Deleted) &&
//                        event.file.isDirectory) {
//                        shouldRegisterPath = true
//                    }
//
//                    channel.send(event)
//                }
//
//                if (!monitorKey.reset()) {
//                    monitorKey.cancel()
//                    close()
//                    break
//                }
//                else if (isClosedForSend) {
//                    break
//                }
//            }
//        }
//    }
//
//    override fun close(cause: Throwable?): Boolean {
//        registeredKeys.apply {
//            forEach { it.cancel() }
//            clear()
//        }
//
//        return channel.close(cause)
//    }
//}