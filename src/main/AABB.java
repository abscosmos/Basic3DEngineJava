package main;

import java.util.ArrayList;
import java.util.Arrays;

public class AABB {
    public final Vector3 positive;
    public final Vector3 negative;
    
    public AABB(Vector3 pos, Vector3 neg) {
        positive = pos;
        negative = neg;
    }
    
    public Vector3 center() {
        return Vector3.midpoint(positive, negative);
    }
    
    public Vector3 size() {
        return new Vector3(
                Math.abs(positive.x - negative.x),
                Math.abs(positive.y - negative.y),
                Math.abs(positive.z - negative.z)
        );
    }
    
    public Vector3 halfSize() {
        return Vector3.scale(size(), 0.5);
    }

    public Mesh getMesh() {
        return getMeshFromAABB(this);
    }
    
    public static AABB calculateAABB(Triangle[] tris) {
        Vector3 min = tris[0].v1.clone();
        Vector3 max = tris[0].v1.clone();
        
        for(Triangle tri : tris) {
            for(Vector3 v : tri.getVertices() ) {
                if(v.x < min.x) min.x = v.x;
                if(v.y < min.y) min.y = v.y;
                if(v.z < min.z) min.z = v.z;
                
                if(v.x > max.x) max.x = v.x;
                if(v.y > max.y) max.y = v.y;
                if(v.z > max.z) max.z = v.z;
            }
        }
        
        return new AABB(max, min);
    }
    
    public static boolean isCollidingAABB(AABB a, AABB b) {
        Vector3 centerA = a.center(),
                centerB = b.center();

        Vector3 halfSizeA = a.halfSize(),
                halfSizeB = b.halfSize();

        return Math.abs(centerA.x - centerB.x) <= halfSizeA.x + halfSizeB.x
                && Math.abs(centerA.y - centerB.y) <= halfSizeA.y + halfSizeB.y
                && Math.abs(centerA.z - centerB.z) <= halfSizeA.z + halfSizeB.z;
    }

    public static Mesh getMeshFromAABB(AABB aabb) {
        Vector3 pos = aabb.positive,
                neg = aabb.negative;
        
        Vector3 v1 = new Vector3(neg.x, neg.y, neg.z),
                v2 = new Vector3(pos.x, neg.y, neg.z),
                v3 = new Vector3(neg.x, pos.y, neg.z),
                v4 = new Vector3(pos.x, pos.y, neg.z),
                v5 = new Vector3(neg.x, neg.y, pos.z),
                v6 = new Vector3(pos.x, neg.y, pos.z),
                v7 = new Vector3(neg.x, pos.y, pos.z),
                v8 = new Vector3(pos.x, pos.y, pos.z);

        return new Mesh(new ArrayList<>(Arrays.asList(
                new Triangle(v1, v3, v4), new Triangle(v1, v4, v2), // south
                new Triangle(v2, v4, v8), new Triangle(v2, v8, v6), // east
                new Triangle(v6, v8, v7), new Triangle(v6, v7, v5), // north
                new Triangle(v5, v7, v3), new Triangle(v5, v3, v1), // west
                new Triangle(v3, v7, v8), new Triangle(v3, v8, v4), // top
                new Triangle(v6, v5, v1), new Triangle(v6, v1, v2)  // bottom
        )), false);
    }
}