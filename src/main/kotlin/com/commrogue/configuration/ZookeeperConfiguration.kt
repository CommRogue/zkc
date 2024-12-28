package com.commrogue.configuration

import jakarta.enterprise.context.ApplicationScoped
import jakarta.ws.rs.Produces
import org.apache.zookeeper.ZooKeeper
import org.eclipse.microprofile.config.inject.ConfigProperty

class ZookeeperConfiguration(@ConfigProperty(name = "zk.address") private val zkAddress: String) {
    @Produces
    @ApplicationScoped
    fun zookeeper(): ZooKeeper {
        return ZooKeeper(zkAddress, 3000, null)
    }
}