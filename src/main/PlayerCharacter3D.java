package main;

import java.io.FileNotFoundException;

public class PlayerCharacter3D extends Object3D implements Collidable, PhysicsProcessable {
    public Camera3D cam = new Camera3D(Vector3.add(mesh.origin, new Vector3(0,3,12)));
    public double turnVelocity;
    public final double BASE_MAX_SPEED = 3;
    public final double ACCELERATION = 20;
    public final double FRICTION = 6;
    public final double MAX_TURN_SPEED = 6;
    public final double TURNING_ACCEL = 40;
    public final double TURNING_FRICTION = 25;
    private double health = 100;
    public int enemyElims = 0;
    private final double BLASTER_COOLDOWN = 0.35;
    private double activeBlasterCooldown = 0;
    public final double MAGNIFYING_GLASS_DURATION = 10;
    public double remainingMagnifyingGlassTime = 0;
    public boolean acceptInput = true;


    public PlayerCharacter3D() throws FileNotFoundException {
        super( new Mesh(PhysicsEngine.pathRoot + "resources/obj/spaceship1.obj", true) );
        cam.userMovable = false;
        collision = Collision.NOT_PLAYER;
        type = Type.PLAYER;
        mesh.rotation = Vector3.add(mesh.rotation, new Vector3(0,Math.PI, 0));
    }
    public void process(double delta) {
        if(acceptInput) input(delta);

        Vector3 shootVector = new Vector3(0, 0, -1).applyMatrix(Mat4x4.createRotationXYZMat(mesh.rotation)).normalized();

        Vector3 offsetVector = new Vector3(0,3,12).applyMatrix(Mat4x4.createRotationXYZMat(mesh.rotation));
        cam.position = Vector3.add(mesh.origin, offsetVector);
        cam.yaw = -(Math.PI - mesh.rotation.y);

        activeBlasterCooldown -= delta;
        activeBlasterCooldown = Mth.clamp(activeBlasterCooldown, 0, BLASTER_COOLDOWN);

        if(acceptInput && InputManager.keyMap.get("space") && activeBlasterCooldown == 0) {
            activeBlasterCooldown = BLASTER_COOLDOWN;
            try {
                Bullet3D bullet = new Bullet3D(
                        Vector3.add(mesh.origin, Vector3.scale(shootVector, 12)),
                        shootVector,
                        mesh.rotation,
                        4
                );

                PhysicsEngine.activeBullets.add(bullet);
            } catch (FileNotFoundException e) { throw new RuntimeException(e); }
        }

        remainingMagnifyingGlassTime = Mth.moveTowards(remainingMagnifyingGlassTime, 0, delta);
    }

    public void onCollide(Object3D other) {
        if(other.type.equals(Type.OBSTACLE)) updateHealth(-15);
        if(other.type.equals(Type.ENEMY)) { updateHealth(-50); enemyElims++; }
        if(other.type.equals(Type.COLLECTIBLE)) remainingMagnifyingGlassTime = MAGNIFYING_GLASS_DURATION;
    }

    public void updateHealth(double change) {
        health = Mth.clamp(health + change, 0, 100);
    }

    public double getHealth() { return health; }

    public void input(double delta) {
        Vector3 inputVector = Vector3.scale( new Vector3(0,0,-1) , InputManager.getAxis("w", "s"));
        inputVector = Vector3.add(inputVector, Vector3.scale( new Vector3(0,1,0) , InputManager.getAxis("e", "q")));

        if(inputVector.length() != 0) inputVector = inputVector.normalized();

        if(!inputVector.equals(Vector3.ZERO)) {
            velocity = velocity.moveTowards(Vector3.scale(inputVector, BASE_MAX_SPEED), ACCELERATION * delta);
        } else {
            velocity = velocity.moveTowards(Vector3.ZERO, FRICTION * delta);
        }

        double turningAxis =  InputManager.getAxis("a", "d");

        if(turningAxis != 0) {
            turnVelocity = Mth.moveTowards(turnVelocity, turningAxis * -MAX_TURN_SPEED, TURNING_ACCEL * delta);
        } else {
            turnVelocity = Mth.moveTowards(turnVelocity, 0, TURNING_FRICTION * delta);
        }

        mesh.rotation = Vector3.mod(Vector3.add(mesh.rotation, new Vector3(0, Math.toRadians(turnVelocity), 0)), 2*Math.PI);

        Vector3 forward = new Vector3(0,0,1).applyMatrix(Mat4x4.createRotationXYZMat(mesh.rotation));
        Vector3 right = Vector3.cross(upVector, forward);

        mesh.origin = Vector3.add(mesh.origin, Vector3.scale(forward, velocity.z));
        mesh.origin = Vector3.add(mesh.origin, Vector3.scale(right, velocity.x));
        mesh.origin.y = mesh.origin.y + velocity.y;

        mesh.rotation.x = 0.015625 * turnVelocity;
    }
}
