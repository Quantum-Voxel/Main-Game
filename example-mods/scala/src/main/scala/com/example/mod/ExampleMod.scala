package com.example.mod

import com.example.mod.ExampleMod.onInitialize
import com.example.mod.init.ModBlocks
import dev.ultreon.libs.commons.v0.Identifier
import net.fabricmc.api.ModInitializer

object ExampleMod extends ModInitializer {
  val MOD_ID: String = "example_mod"

  def id(path: String): Identifier = Identifier(MOD_ID, path)

  override def onInitialize(): Unit =
    ModBlocks.init()
  end onInitialize
}
