package com.commrogue.zk

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.apache.zookeeper.ZooKeeper

data class Collection(val name: String, val config: String, val nrtReplicaCount: Int, val shardCount: Int)

fun getCollections(zk: ZooKeeper): List<Collection> =
    zk.getChildren("/collections", false).map {
        val parsed = Json.parseToJsonElement(
            String(
                zk.getData(
                    "/collections/$it/state.json",
                    false,
                    null
                )
            )
        ).jsonObject[it]

        Collection(
            it,
            parsed?.jsonObject?.get("configName")?.jsonPrimitive?.content ?: "Unknown",
            parsed?.jsonObject?.get("nrtReplicas")?.jsonPrimitive?.content?.toInt() ?: -1,
            parsed?.jsonObject?.get("shards")?.jsonObject?.size ?: -1
        )
    }