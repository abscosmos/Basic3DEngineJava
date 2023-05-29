package main;

import java.io.FileNotFoundException;

public class Asteroid3D extends Object3D implements Collidable {
    public Asteroid3D(Vector3 position) throws FileNotFoundException {
        super();
        this.mesh = getRandomAsteroidMesh(Mth.randomDoubleInRange(3.75, 7.5));
        generateCollider();
        mesh.origin = position;

        collision = Collision.ALL;
        type = Type.OBSTACLE;
    }

    public void onCollide(Object3D other) {
         if(other.type.equals(Type.PROJECTILE)) return;

        PhysicsEngine.freeQueue.add(this);
        try {
            PhysicsEngine.generateAsteroids();
        } catch (FileNotFoundException e) { throw new RuntimeException(e); }
    }

    private Mesh getRandomAsteroidMesh(double scalar) throws FileNotFoundException {
        String path = "resources/obj/rock" + Mth.randomIntInRange(1,8) + ".obj";
        return new Mesh(PhysicsEngine.pathRoot + path, true).scaled(scalar);
    }
}