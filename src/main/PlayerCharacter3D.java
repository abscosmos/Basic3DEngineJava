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
    public final double TURNING_FRICTION = 22.5;
    public boolean acceptInput = false;


    public PlayerCharacter3D() throws FileNotFoundException {
        super( new Mesh("resources/obj/spaceship1.obj", true) );
        cam.userMovable = false;
        collision = Collision.NOT_PLAYER;
        type = Type.PLAYER;
        mesh.rotation = Vector3.add(mesh.rotation, new Vector3(0,Math.PI, 0));
    }
    public void process(double delta) {
        if(acceptInput) input(delta);

        Vector3 offsetVector = new Vector3(0,3,12).applyMatrix(Mat4x4.createRotationXYZMat(mesh.rotation));
        cam.position = Vector3.add(mesh.origin, offsetVector);
        cam.yaw = -(Math.PI - mesh.rotation.y);
    }

    public void onCollide(Object3D other) { }

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
