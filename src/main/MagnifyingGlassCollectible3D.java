package main;

import java.io.FileNotFoundException;

public class MagnifyingGlassCollectible3D extends Object3D implements Collidable {
    public MagnifyingGlassCollectible3D(Vector3 position) throws FileNotFoundException {
        super( new Mesh(PhysicsEngine.pathRoot + "resources/obj/magnifying_glass.obj", true) );
        mesh = mesh.scaled(0.75);
        mesh.origin = position;
        collision = Object3D.Collision.ALL;
        type = Type.COLLECTIBLE;
    }

    public void onCollide(Object3D other) {
        PhysicsEngine.freeQueue.add(this);

        try {
            PhysicsEngine.spawnMagnifyingGlass();
        } catch (FileNotFoundException e) { throw new RuntimeException(e); }
    }
}
