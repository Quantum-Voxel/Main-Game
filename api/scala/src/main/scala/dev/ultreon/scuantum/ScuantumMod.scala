package dev.ultreon.scuantum

import dev.ultreon.qvoxel.server.QuantumServer
import net.fabricmc.api.ModInitializer

object ScuantumMod extends ModInitializer {
  val MOD_ID: String = "scuantum"

  override def onInitialize(): Unit = {

  }
}

def server = QuantumServer.get()
