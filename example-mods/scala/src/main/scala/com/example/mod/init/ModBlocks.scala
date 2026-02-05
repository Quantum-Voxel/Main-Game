package com.example.mod.init

import com.example.mod.ExampleMod
import com.example.mod.ExampleMod.id
import dev.ultreon.qvoxel.block.Block
import dev.ultreon.qvoxel.registry.{Registries, Registry}

object ModBlocks {
  private final val BLOCKS: Registry[Block] = Registries.BLOCK

  val EXAMPLE_BLOCK: Block = register("example_block", Block(Block.Settings().strength(1.0f)))

  private def register[T <: Block](name: String, value: T): T = 
    BLOCKS.register(id(name), value)
    value
  end register

  def init(): Unit = ()
}

