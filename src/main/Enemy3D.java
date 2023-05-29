package main;

import java.io.FileNotFoundException;

public class Enemy3D extends Object3D implements Collidable {
    public Enemy3D(Vector3 position) throws FileNotFoundException {
        super( new Mesh(PhysicsEngine.pathRoot + "resources/obj/enemy.obj", true) );
        mesh = mesh.scaled(6);
        mesh.origin = position;
        collision = Collision.ALL;
        type = Type.ENEMY;
    }

    public void onCollide(Object3D other) {
        PhysicsEngine.freeQueue.add(this);

        try {
            PhysicsEngine.generateEnemies();
        } catch (FileNotFoundException e) { throw new RuntimeException(e); }
    }
}
