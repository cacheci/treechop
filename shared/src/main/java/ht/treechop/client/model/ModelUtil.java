package ht.treechop.client.model;

import ht.tuber.math.Vector3;
import net.minecraft.client.model.geom.builders.UVPair;
import net.minecraft.client.renderer.FaceInfo;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public class ModelUtil {
    public static BakedQuad makeQuad(TextureAtlasSprite sprite, Vector3 posFrom, Vector3 posTo, Direction orientation, Direction culling) {
        return makeQuad(sprite, posFrom, posTo, orientation, culling, getUVsForQuad(posFrom, posTo, orientation), 0);
    }

    private static float[] getUVsForQuad(Vector3 from, Vector3 to, Direction orientation) {
        return switch (orientation) {
            case UP, DOWN -> new float[]{(float) from.x, (float) from.z, (float) to.x, (float) to.z};
            case EAST, WEST -> new float[]{(float) from.z, (float) from.y, (float) to.z, (float) to.y};
            case NORTH, SOUTH -> new float[]{(float) from.x, (float) from.y, (float) to.x, (float) to.y};
        };
    }

    public static BakedQuad makeQuad(TextureAtlasSprite sprite, Vector3 posFrom, Vector3 posTo, Direction orientation, Direction culling, float[] uvs, int uvRotation) {
        Vector3fc from = new Vector3f((float) posFrom.x, (float) posFrom.y, (float) posFrom.z);
        Vector3fc to = new Vector3f((float) posTo.x, (float) posTo.y, (float) posTo.z);
        Vector3fc[] positions = new Vector3fc[4];
        long[] packedUvs = new long[4];
        for (int i = 0; i < 4; i++) {
            positions[i] = FaceInfo.fromFacing(orientation).getVertexInfo(i).select(from, to).div(16.0F);
            int rotated = (i + uvRotation / 90) % 4;
            float u = (rotated == 0 || rotated == 1) ? uvs[0] : uvs[2];
            float v = (rotated == 0 || rotated == 3) ? uvs[1] : uvs[3];
            packedUvs[i] = UVPair.pack(sprite.getU(u / 16.0F), sprite.getV(v / 16.0F));
        }
        return new BakedQuad(positions[0], positions[1], positions[2], positions[3], packedUvs[0], packedUvs[1], packedUvs[2], packedUvs[3], orientation,
                BakedQuad.MaterialInfo.of(new Material.Baked(sprite, false), sprite.contents().computeTransparency(0, 0, 1, 1), -1, null, 0));
    }

    public static BakedQuad trimQuad(BakedQuad quad, Vector3 corner1, Vector3 corner2) {
        Vector3 mins = new Vector3(Math.min(corner1.x(), corner2.x()), Math.min(corner1.y(), corner2.y()), Math.min(corner1.z(), corner2.z()));
        Vector3 maxes = new Vector3(Math.max(corner1.x(), corner2.x()), Math.max(corner1.y(), corner2.y()), Math.max(corner1.z(), corner2.z()));
        Vertex old1 = getVertex(quad, 0);
        Vertex old2 = getVertex(quad, 1);
        Vertex old3 = getVertex(quad, 2);
        Vertex[] vertices = new Vertex[4];
        for (int i = 0; i < 4; i++) {
            Vertex old = getVertex(quad, i);
            vertices[i] = lerpVertexUVsInTriangle(old.xyz().clamp(mins, maxes), old1, old2, old3);
        }
        return makeTransformedQuad(quad, vertices);
    }

    public static BakedQuad translateQuad(BakedQuad quad, Vector3 translation) {
        Vertex[] vertices = new Vertex[4];
        for (int i = 0; i < 4; i++) {
            Vertex old = getVertex(quad, i);
            vertices[i] = new Vertex(old.x + translation.x, old.y + translation.y, old.z + translation.z, old.u, old.v);
        }
        return makeTransformedQuad(quad, vertices);
    }

    private static BakedQuad makeTransformedQuad(BakedQuad original, Vertex[] vertices) {
        return new BakedQuad(position(vertices[0]), position(vertices[1]), position(vertices[2]), position(vertices[3]),
                uv(vertices[0]), uv(vertices[1]), uv(vertices[2]), uv(vertices[3]), original.direction(), original.materialInfo());
    }

    private static Vector3fc position(Vertex vertex) {
        return new Vector3f((float) vertex.x / 16, (float) vertex.y / 16, (float) vertex.z / 16);
    }

    private static long uv(Vertex vertex) {
        return UVPair.pack((float) vertex.u, (float) vertex.v);
    }

    private static Vertex getVertex(BakedQuad quad, int index) {
        Vector3fc pos = quad.position(index);
        long packedUV = quad.packedUV(index);
        return new Vertex(pos.x() * 16, pos.y() * 16, pos.z() * 16, UVPair.unpackU(packedUV), UVPair.unpackV(packedUV));
    }

    private static Vertex lerpVertexUVsInTriangle(Vector3 pos, Vertex v1, Vertex v2, Vertex v3) {
        Vector3 weights = getLerpWeightsInSimplex(pos, v1.xyz(), v2.xyz(), v3.xyz());
        return new Vertex(pos.x, pos.y, pos.z, weights.dot(v1.u, v2.u, v3.u), weights.dot(v1.v, v2.v, v3.v));
    }

    private static Vector3 getLerpWeightsInSimplex(Vector3 pos, Vector3 v1, Vector3 v2, Vector3 v3) {
        Vector3 side1 = v2.subtract(v1);
        Vector3 side2 = v3.subtract(v1);
        Vector3 normal = side2.cross(side1).normalize();
        Vector3 projected = pos.subtract(normal.scale(pos.subtract(v1).dot(normal)));
        Vector3 f1 = v1.subtract(projected);
        Vector3 f2 = v2.subtract(projected);
        Vector3 f3 = v3.subtract(projected);
        Vector3 area = side1.cross(side2);
        double magnitude = area.length();
        Vector3 a1 = f2.cross(f3);
        Vector3 a2 = f3.cross(f1);
        Vector3 a3 = f1.cross(f2);
        return new Vector3(a1.length() / magnitude * Math.signum(area.dot(a1)),
                a2.length() / magnitude * Math.signum(area.dot(a2)),
                a3.length() / magnitude * Math.signum(area.dot(a3)));
    }

    private record Vertex(double x, double y, double z, double u, double v) {
        Vector3 xyz() {
            return new Vector3(x, y, z);
        }
    }
}
