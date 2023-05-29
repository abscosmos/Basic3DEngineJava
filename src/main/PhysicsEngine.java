package main;

import java.util.ArrayList;
import java.util.Arrays;
import java.io.FileNotFoundException;

public class PhysicsEngine {
    public static final String pathRoot = "";

    public static final ArrayList<Object3D> objects = new ArrayList<>();
    public static final ArrayList< ArrayList<Object3D> > objectSets = new ArrayList<>();
    public static final ArrayList<Object3D> collisionQueue = new ArrayList<>();
    public static final ArrayList<Object3D> freeQueue = new ArrayList<>();

    public static Camera3D activeCamera;
    public static PlayerCharacter3D player;
    public static Camera3D cam1;
    public static Object3D exampleWorld;
    public static final boolean resolveCollisions = true;
    
    public static void ready() throws FileNotFoundException {
        objectSets.clear();
        objects.clear();

        player = new PlayerCharacter3D();
        player.acceptInput = true;
        activeCamera = player.cam;
        addObject(player);

        exampleWorld = new Object3D( new Mesh(pathRoot+ "resources/obj/CartoonLand.obj", true));
        addObject(exampleWorld);
        exampleWorld.mesh.origin = Vector3.add(exampleWorld.mesh.origin, new Vector3(0,-20,0));
        exampleWorld.collision = Object3D.Collision.ONLY_PLAYER;
        exampleWorld.type = Object3D.Type.OBSTACLE;

        objectSets.add(objects);
    }
    
    public static void process(double delta) {
        activeCamera.process(delta);

        for (ArrayList<Object3D> oArr : objectSets) {
            for(Object3D object : oArr) {
                if(object instanceof PhysicsProcessable) ((PhysicsProcessable) object).process(delta);
            }
        }

        for(ArrayList<Object3D> oArr : objectSets) {
            addToCollisionQueue(oArr.toArray(Object3D[]::new));
        }

        detectCollisions();
    }
    
    public static void addObject(Object3D... object) {
        objects.addAll(Arrays.asList(object));
    }

    public static void addToCollisionQueue(Object3D... objects) {
        collisionQueue.addAll(Arrays.asList(objects));
    }
    
    public static void detectCollisions() {
        for(int i = 0; i < collisionQueue.size() -1; i++) {
            if(collisionQueue.get(i).collision.equals(Object3D.Collision.NOTHING)) continue;

            for(int j = i + 1; j <= collisionQueue.size() -1; j++) {
                Object3D objA = collisionQueue.get(i);
                Object3D objB = collisionQueue.get(j);

                if(objB.collision.equals(Object3D.Collision.NOTHING)) continue;
                if(objA.collision.equals(Object3D.Collision.ONLY_PLAYER) && !objB.type.equals(Object3D.Type.PLAYER)) continue;
                if(objB.collision.equals(Object3D.Collision.ONLY_PLAYER) && !objA.type.equals(Object3D.Type.PLAYER)) continue;
                if(objA.collision.equals(Object3D.Collision.NOT_PLAYER) && objB.type.equals(Object3D.Type.PLAYER)) continue;
                if(objB.collision.equals(Object3D.Collision.NOT_PLAYER) && objA.type.equals(Object3D.Type.PLAYER)) continue;

                if( AABB.isCollidingAABB(objA.getCollider(), objB.getCollider())) {
                    if(!(objA.type.equals(Object3D.Type.TRIGGER) || objB.type.equals(Object3D.Type.TRIGGER)) && resolveCollisions) {
                        resolveCollisionAABB(objA, objB);
                    }

                    if(objA instanceof Collidable) ((Collidable) objA).onCollide(objB);
                    if(objB instanceof Collidable) ((Collidable) objB).onCollide(objA);
                }
            }
        }

        collisionQueue.clear();
    }
    
    public static void resolveCollisionAABBSimple(Object3D a, Object3D b) {
        a.mesh.origin = Vector3.subtract(a.mesh.origin, a.velocity);
        b.mesh.origin = Vector3.subtract(b.mesh.origin, b.velocity);
    }
    
    public static void resolveCollisionAABB(Object3D a, Object3D b) {
        Vector3 intersectionDistance = new Vector3(
                a.getCollider().halfSize().x + b.getCollider().halfSize().x - Math.abs(a.getCollider().center().x - b.getCollider().center().x),
                a.getCollider().halfSize().y + b.getCollider().halfSize().y - Math.abs(a.getCollider().center().y - b.getCollider().center().y),
                a.getCollider().halfSize().z + b.getCollider().halfSize().z - Math.abs(a.getCollider().center().z - b.getCollider().center().z)
        );

        Vector3 signA = Vector3.add(a.velocity.signs(), Vector3.scale(a.velocity.signs(), Mth.EPSILON));
        Vector3 signB = Vector3.add(b.velocity.signs(), Vector3.scale(b.velocity.signs(), Mth.EPSILON));
        
        Vector3 moveDistanceA = Vector3.componentMultiply(intersectionDistance, Vector3.scale(signA, -1));
        Vector3 moveDistanceB = Vector3.componentMultiply(intersectionDistance, Vector3.scale(signB, -1));

        a.mesh.origin = Vector3.add(a.mesh.origin, moveDistanceA);
        b.mesh.origin = Vector3.add(b.mesh.origin, moveDistanceB);
    }

    public static boolean isSpaceOccupiedForObject(Object3D o) {
        for(ArrayList<Object3D> oArr : objectSets) {
            for(Object3D ob : oArr) {
                if(AABB.isCollidingAABB(o.getCollider(), ob.getCollider())) return true;
            }
        }

        return false;
    }
}