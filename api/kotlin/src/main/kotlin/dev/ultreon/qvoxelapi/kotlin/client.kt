package dev.ultreon.qvoxelapi.kotlin

import dev.ultreon.qvoxel.client.QuantumClient
import dev.ultreon.qvoxel.client.render.GraphicsMode
import dev.ultreon.qvoxel.client.render.GuiRenderer
import dev.ultreon.qvoxel.client.render.pipeline.ColorTextureTarget
import dev.ultreon.qvoxel.client.render.pipeline.DepthTextureFormat
import dev.ultreon.qvoxel.client.render.pipeline.DepthTextureTarget
import dev.ultreon.qvoxel.client.render.pipeline.RenderNode
import dev.ultreon.qvoxel.client.render.pipeline.RenderPipeline
import dev.ultreon.qvoxel.client.render.pipeline.TextureSource
import dev.ultreon.qvoxel.client.render.pipeline.TextureTarget
import dev.ultreon.qvoxel.client.texture.TextureFormat
import dev.ultreon.qvoxel.client.world.WorldRenderer

val client get() = QuantumClient.get()

@DslMarker
annotation class GraphicsModeDSL

@DslMarker
annotation class RenderPipelineDSL

@DslMarker
annotation class RenderNodeDSL

data class RenderNodeDepependency(val dependency: TextureSource) {
    constructor(node: RenderNode, name: String) : this(node.getShaderOutput(name))

    fun connect(pipeline: RenderPipeline, name: String, targetNode: RenderNode) {
        targetNode.setShaderInput(name, dependency)
    }
}

@RenderNodeDSL
class RenderNodeBuilder @InternalApi constructor(
    private val name: String,
    private val worldRenderer: WorldRenderer,
    private val renderer: GuiRenderer,
    private val width: Int,
    private val height: Int,
    depth: Boolean = false,
) {
    private val node: RenderNode
    private var dependencies: List<RenderNodeDepependency> = listOf()
    private var onRender: ((GuiRenderer) -> Unit)? = null
    private var onDelete: (() -> Unit)? = null
    private var onConstruct: ((WorldRenderer, GuiRenderer, Int, Int) -> Unit)? = null

    init {
        this.node = object : RenderNode(width, height, depth) {
            init {
                if (onConstruct != null)
                    onConstruct!!.invoke(
                        worldRenderer,
                        renderer,
                        width,
                        height
                    )
            }

            override fun doRender(renderer: GuiRenderer?) {
                if (onRender == null)
                    return super.doRender(renderer)
                onRender!!.invoke(renderer!!)
            }

            override fun delete() {
                super.delete()
                onDelete!!.invoke()
            }
        }
    }

    fun onRender(block: (GuiRenderer) -> Unit) {
        this.onRender = block
    }

    fun onConstruct(block: (WorldRenderer, GuiRenderer, Int, Int) -> Unit) {
        this.onConstruct = block
    }

    fun onDelete(block: () -> Unit) {
        this.onDelete = block
    }

    fun dependency(dependency: TextureSource) {
        this.dependencies += RenderNodeDepependency(dependency)
    }

    fun dependency(node: RenderNode, name: String) {
        this.dependencies += RenderNodeDepependency(node, name)
    }

    fun framebufferTexture(name: String, format: TextureFormat = TextureFormat.RGBA8): TextureTarget {
        return ColorTextureTarget(format).also {
            node.setShaderOutput(name, it)
        }
    }

    fun framebufferDepthTexture(name: String, format: DepthTextureFormat = DepthTextureFormat.DEPTH24): TextureTarget {
        return DepthTextureTarget(format).also {
            node.setShaderOutput(name, it)
        }
    }

    @InternalApi
    fun build(parent: RenderPipelineBuilder): RenderNode {
        return node.also {
            parent.pipeline.addNode(name, it)
            for (dependency in dependencies) {
                dependency.connect(parent.pipeline, name, it)
            }
        }
    }
}

@RenderPipelineDSL
class RenderPipelineBuilder @InternalApi constructor(
    private val worldRenderer: WorldRenderer,
    private val renderer: GuiRenderer,
    private val width: Int,
    private val height: Int
) {
    internal var pipeline: RenderPipeline = RenderPipeline(renderer, width, height)
    private var worldNode: RenderNode? = null

    @InternalApi
    fun build() =
        pipeline

    fun world(): RenderNode {
        val worldNode = worldNode
        if (worldNode != null)
            return worldNode

        return worldRenderer.createNode().also {
            this.worldNode = it
        }
    }

    @OptIn(InternalApi::class)
    fun node(name: String, depth: Boolean = false, block: RenderNodeBuilder.() -> Unit): RenderNode {
        return RenderNodeBuilder(name, worldRenderer, renderer, width, height, depth).apply(block).build(this).also {
            this.pipeline.addNode(name, it)
        }
    }
}

@GraphicsModeDSL
class GraphicsModeBuilder {
    private var name: String? = null
    private var pipelineBuilder: GraphicsMode.PipelineBuilder? = null

    fun name(name: String) {
        this.name = name
    }

    @OptIn(InternalApi::class)
    fun pipeline(block: RenderPipelineBuilder.() -> Unit) {
        this.pipelineBuilder = GraphicsMode.PipelineBuilder { worldRenderer, guiRenderer, width, height ->
            RenderPipelineBuilder(worldRenderer, guiRenderer, width, height).apply(block).build()
        }
    }

    @InternalApi
    fun build(): GraphicsMode =
        GraphicsMode(name, pipelineBuilder)
}

@OptIn(InternalApi::class)
@GraphicsModeDSL
fun graphicsMode(block: GraphicsModeBuilder.() -> Unit): GraphicsMode =
    GraphicsModeBuilder().apply(block).build()
