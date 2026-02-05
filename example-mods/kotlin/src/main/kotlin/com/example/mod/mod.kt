package com.example.mod

import dev.ultreon.libs.commons.v0.Identifier
import dev.ultreon.qvoxel.block.Block
import dev.ultreon.qvoxel.client.QuantumClient
import dev.ultreon.qvoxel.registry.Registries
import org.slf4j.LoggerFactory

const val MOD_ID = "examplemod"
const val MOD_NAME = "Example Mod"

val LOGGER = LoggerFactory.getLogger(MOD_NAME)!!

fun id(path: String) = Identifier(MOD_ID, path)

fun main() {
    Registries.BLOCK!!.register(id("example_block"), Block(Block.Settings().hardness(1.0f)))
}