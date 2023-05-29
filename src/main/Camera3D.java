package main;

public class Camera3D implements PhysicsProcessable {
    public static final Vector3 upVector = new Vector3(0, 1, 0);

    public static final double SPEED = 12.0;

    public Vector3 lookDirection = new Vector3(0,0,1);
    public Vector3 position;
    public Mat4x4 viewMatrix;
    public double yaw, pitch;
    public boolean userMovable = false;

    public Camera3D(Vector3 pos, double yaw) { position = pos; this.yaw = yaw; }

    public Camera3D(Vector3 pos) { this(pos, 0); }

    public void process(double delta) {
        if(userMovable) updateMovement(delta);
    }

    public void updateInternals() {
        Vector3 targetVector = new Vector3(0, 0, 1);
        pitch = Mth.clamp(pitch, -Math.PI / 2 + Mth.EPSILON, Math.PI / 2 - Mth.EPSILON);
        lookDirection = targetVector.applyMatrix(Mat4x4.createRotationXYZMat(0, yaw, 0));
        viewMatrix = Mth.matrixPointAt(posCorrected(), Vector3.add(posCorrected(), lookDirection), Camera3D.upVector).rotTransQuickInverse();
    }

    private void updateMovement(double delta) {
        position.y += 12 * delta * InputManager.getAxis("e", "q");
        yaw += 1.5 * delta * InputManager.getAxis("right_arrow", "left_arrow");

        Vector3 forwardVector = Vector3.scale(lookDirection, SPEED * 2 * delta);
        Vector3 rightVector = Vector3.scale(Vector3.cross(Camera3D.upVector, lookDirection), SPEED * delta);

        Vector3 movement = Vector3.add(
                Vector3.scale(forwardVector, InputManager.getAxis("w", "s")),
                Vector3.scale(rightVector, InputManager.getAxis("a", "d"))
        );

        position = Vector3.add(position, movement);
    }

    public Vector3 posCorrected() {
        return Vector3.add(position, new Vector3(0,0,6));
    }
}
