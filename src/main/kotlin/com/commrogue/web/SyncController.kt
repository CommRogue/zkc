package com.commrogue.web

import com.commrogue.zk.getCollections
import jakarta.enterprise.context.ApplicationScoped
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import org.apache.zookeeper.ZooKeeper

@ApplicationScoped
@Path("/sync")
class SyncController(
    private val zookeeper: ZooKeeper
) {
    @GET
    @Path("/{collection}")
    @Produces(MediaType.APPLICATION_JSON)
    fun syncCollection(@PathParam("collection") collection: String): String {
        return getCollections(zookeeper).find { it.name.equals(collection, true) }?.config
            ?: throw WebApplicationException(
                Response.status(Response.Status.NOT_FOUND).entity("Collection $collection was not found!").build()
            )
    }
}