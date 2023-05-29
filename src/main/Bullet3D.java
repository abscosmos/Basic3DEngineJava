package main;

import java.io.FileNotFoundException;

public class Bullet3D extends Object3D implements Collidable, PhysicsProcessable {
    public double lifeRemainingSeconds;
    public Vector3 targetPath;
    public final double SPEED = 5;

    public Bullet3D(Vector3 position, Vector3 targetPath, Vector3 rotation, double lifeSeconds) throws FileNotFoundException {
        super( new Mesh(PhysicsEngine.pathRoot + "resources/obj/bullet.obj", true) );
        mesh.origin = position;
        this.targetPath = targetPath;
        lifeRemainingSeconds = lifeSeconds;
        mesh.rotation = rotation;

        collision = Collision.NOT_PLAYER;
        type = Type.PROJECTILE;
    }
    public void process(double delta) {
        lifeRemainingSeconds -= delta;
        if(lifeRemainingSeconds <= 0) {
            PhysicsEngine.freeQueue.add(this);
        }

        mesh.origin = Vector3.add(mesh.origin, Vector3.scale(targetPath, SPEED));
    }

    public void onCollide(Object3D other) {
        if(other.type.equals(Type.PROJECTILE)) return;
        if(other.type.equals(Type.ENEMY)) PhysicsEngine.player.enemyElims++;
        PhysicsEngine.freeQueue.add(this);
    }

}
