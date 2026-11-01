package io.github.goocompound.travelstaff.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.Mesh;
import net.fabricmc.fabric.api.client.renderer.v1.render.FabricOrderedSubmitNodeCollector;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class OutlineBufferTest {
    @Test
    @SuppressWarnings("unchecked")
    void keepsFabricBlockMeshesInDirectAndOrderedSubmissions() {
        Object[][] received = new Object[1][];
        SubmitNodeCollector delegate = recordingCollector(received);
        OutlineBuffer buffer = new OutlineBuffer(delegate);
        Mesh mesh = emptyMesh();
        PoseStack pose = new PoseStack();
        int[] tints = {0xFF123456};
        Function<ChunkSectionLayer, RenderType> filteredMaterial = layer -> null;

        for (boolean ordered : new boolean[]{false, true}) {
            for (boolean translucent : new boolean[]{false, true}) {
                FabricOrderedSubmitNodeCollector collector = ordered
                        ? (FabricOrderedSubmitNodeCollector) buffer.order(3) : buffer;
                collector.submitBlockModel(pose, filteredMaterial, translucent, List.of(), mesh, tints, 123, 456, 789);

                Object[] args = received[0];
                assertEquals(9, args.length, "Must forward the Fabric overload, not the vanilla fallback that drops meshes");
                assertSame(pose, args[0]);
                assertNull(((Function<ChunkSectionLayer, RenderType>) args[1]).apply(ChunkSectionLayer.SOLID));
                assertEquals(translucent, args[2]);
                assertSame(mesh, args[4]);
                assertSame(tints, args[5]);
                assertEquals(123, args[6]);
                assertEquals(456, args[7]);
                assertEquals(789, args[8]);
            }
        }
    }

    @Test
    void keepsMeshesInItemAndBreakingSubmissions() {
        Object[][] received = new Object[1][];
        OutlineBuffer buffer = new OutlineBuffer(recordingCollector(received));
        Mesh mesh = emptyMesh();
        PoseStack pose = new PoseStack();

        buffer.submitBreakingBlockModel(pose, List.of(), mesh, 2, true);
        assertEquals(5, received[0].length);
        assertSame(mesh, received[0][2]);

        buffer.submitItem(pose, null, 123, 456, 789, new int[0], null, mesh, null);
        assertEquals(9, received[0].length);
        assertSame(mesh, received[0][7]);
    }

    private static SubmitNodeCollector recordingCollector(Object[][] received) {
        return (SubmitNodeCollector) Proxy.newProxyInstance(OutlineBufferTest.class.getClassLoader(),
                new Class<?>[]{SubmitNodeCollector.class, FabricOrderedSubmitNodeCollector.class},
                (proxy, method, args) -> {
                    if (method.getName().equals("order")) return proxy;
                    received[0] = args;
                    return null;
                });
    }

    private static Mesh emptyMesh() {
        return (Mesh) Proxy.newProxyInstance(OutlineBufferTest.class.getClassLoader(), new Class<?>[]{Mesh.class},
                (proxy, method, args) -> {
                    throw new AssertionError("The wrapper must forward the mesh without inspecting or replacing it");
                });
    }
}
