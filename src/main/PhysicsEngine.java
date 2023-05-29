package main;

import java.util.ArrayList;
import java.util.Arrays;
import java.io.FileNotFoundException;
import java.util.Iterator;

public class PhysicsEngine {
    public static final String pathRoot = "";
    public static Camera3D activeCamera;
    public static final ArrayList<Object3D> objects = new ArrayList<>();
    public static final ArrayList<Object3D> collisionQueue = new ArrayList<>();
    public static final ArrayList< ArrayList<Object3D> > objectSets = new ArrayList<>();
    public static PlayerCharacter3D player;

    public static final Vector3 spawnBoundsMax = Vector3.scale(new Vector3(75,50,75), 3);
    public static final Vector3 spawnBoundsMin = Vector3.scale(spawnBoundsMax, -1);
    public static final int MAX_ASTEROIDS = 30;

    public static final int MAX_ENEMIES = 6;

    public static final ArrayList<Object3D> activeAsteroids = new ArrayList<>();

    public static final ArrayList<Object3D> activeEnemies = new ArrayList<>();

    public static final ArrayList<Object3D> activeBullets = new ArrayList<>();

    public static final ArrayList<Object3D> freeQueue = new ArrayList<>();

    public static final boolean resolveCollisions = false;

    private static Camera3D cinematicCamera;

    public static State gameState = State.START;
    public static Object3D titleText, endText;

    public enum State { START, PLAY, LOSE }


    public static void ready() throws FileNotFoundException {
        gameState = State.START;

        objectSets.clear();
        objects.clear();
        activeBullets.clear();
        activeEnemies.clear();
        activeAsteroids.clear();

        generateAsteroids();
        generateEnemies();
        spawnMagnifyingGlass();

        player = new PlayerCharacter3D();
        activeCamera = player.cam;
        addObject(player);

        objectSets.add(objects);
        objectSets.add(activeBullets);
        objectSets.add(activeEnemies);
        objectSets.add(activeAsteroids);

        player.updateHealth(Double.POSITIVE_INFINITY);

        titleText = new Object3D( new Mesh(PhysicsEngine.pathRoot + "resources/obj/title.obj", true) );
        titleText.mesh.origin = new Vector3(0,4,0);
        titleText.mesh.rotation = new Vector3(Math.toRadians(-18),Math.toRadians(40),0);
        objects.add(titleText);

        endText = new Object3D( new Mesh(PhysicsEngine.pathRoot + "resources/obj/endtitle.obj", true) );
        endText.mesh.origin = new Vector3(-379.8, 1.5, -134.6);
        endText.mesh.rotation = new Vector3(Math.toRadians(-18),Math.toRadians(38),0);

        cinematicCamera = new Camera3D(new Vector3(-10.0, 1.5, -3.8), -1.233);
        activeCamera = cinematicCamera;
        player.acceptInput = false;
    }
    
    public static void process(double delta) throws FileNotFoundException {
        if(gameState.equals(State.START)) {
            if(InputManager.keyMap.get("enter")) {
                gameState = State.PLAY;
                activeCamera = player.cam;
                player.acceptInput = true;
                freeQueue.add(titleText);
            }
        } else if(gameState.equals(State.PLAY)) {
            if(player.getHealth() <= 0) {
                gameState = State.LOSE;

                activeCamera = cinematicCamera;
                player.acceptInput = false;
                objects.add(endText);

                cinematicCamera.position = new Vector3(-389.2, 1.5, -136.1);
                cinematicCamera.yaw = -1.233;
            }
        } else if (gameState.equals(State.LOSE)) {
            if(InputManager.keyMap.get("r")) {
                ready();
                return;
            }
        }

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


        Iterator<Object3D> iterator = freeQueue.iterator();
        while (iterator.hasNext()) {
            Object3D o = iterator.next();
            for (ArrayList<Object3D> oArr : objectSets) {
                if (oArr.remove(o)) {
                    iterator.remove();
                    break;
                }
            }
        }
    }

    // Physics Engine Functions

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
                        resolveCollisionAABBSimple(objA, objB);
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

    // Game Engine Functions

    public static void generateAsteroids() throws FileNotFoundException {
        while(activeAsteroids.size() != MAX_ASTEROIDS) {
            Asteroid3D asteroid = new Asteroid3D(Mth.getRandomLocationWithinBounds(spawnBoundsMin, spawnBoundsMax));

            while(isSpaceOccupiedForObject(asteroid)) {
                asteroid.mesh.origin = Mth.getRandomLocationWithinBounds(spawnBoundsMin, spawnBoundsMax);
            }

            activeAsteroids.add(asteroid);
        }
    }

    public static void generateEnemies() throws FileNotFoundException {
        while(activeEnemies.size() != MAX_ENEMIES) {
            Enemy3D enemy = new Enemy3D(Mth.getRandomLocationWithinBounds(spawnBoundsMin, spawnBoundsMax));

            while(isSpaceOccupiedForObject(enemy)) {
                enemy.mesh.origin = Mth.getRandomLocationWithinBounds(spawnBoundsMin, spawnBoundsMax);
            }

            activeEnemies.add(enemy);
        }
    }

    public static void spawnMagnifyingGlass() throws FileNotFoundException {
        MagnifyingGlassCollectible3D magnifyingGlass = new MagnifyingGlassCollectible3D(Mth.getRandomLocationWithinBounds(spawnBoundsMin, spawnBoundsMax));

        while(PhysicsEngine.isSpaceOccupiedForObject(magnifyingGlass)) {
            magnifyingGlass.mesh.origin = Mth.getRandomLocationWithinBounds(spawnBoundsMin, spawnBoundsMax);
        }

        objects.add(magnifyingGlass);
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