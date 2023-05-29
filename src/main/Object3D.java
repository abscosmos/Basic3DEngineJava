package main;

public class Object3D {
    public static final Vector3 upVector = new Vector3(0, 1, 0);
    public Mesh mesh;
    private AABB collider;
    private boolean autoGenerateCollider = true;
    public boolean isVisible = true;
    public Vector3 velocity = Vector3.ZERO;
    public Collision collision = Collision.NOTHING;
    public Type type = Type.WORLD;


    public enum Collision {
        ALL,
        NOTHING,
        ONLY_PLAYER,
        NOT_PLAYER
    }

    public enum Type {
        PLAYER,
        COLLECTIBLE,
        WORLD,
        ENEMY,
        TRIGGER,
        OBSTACLE,
        PROJECTILE
    }

    // Constructors
    public Object3D(Mesh mesh, AABB aabb) {
        this.mesh = mesh;
        collider = aabb;
        autoGenerateCollider = false;
    }

    public Object3D(Mesh mesh) {
        this.mesh = mesh;
        generateCollider();
    }

    public Object3D() {}

    // Collision
    public void generateCollider() {
        autoGenerateCollider = true;
        collider = AABB.calculateAABB(mesh.getTris());
    }

    public AABB getCollider() {
        if(mesh.isMovable() && autoGenerateCollider) collider = AABB.calculateAABB(mesh.getTris()); // TODO optimize calculation for movable objects, might not work as intended for custom defined AABBs
        return collider;
    }
}
