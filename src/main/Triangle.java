package main;

public class Triangle implements Cloneable {
    public Vector3 v1, v2 ,v3;
    public int lum;

    public Triangle(Vector3 v1, Vector3 v2, Vector3 v3) {
        this.v1 = v1;
        this.v2 = v2;
        this.v3 = v3;
    }

    public Triangle(Vector3 v1, Vector3 v2, Vector3 v3, int lum) {
        this.v1 = v1;
        this.v2 = v2;
        this.v3 = v3;
        this.lum = lum;
    }

    public Triangle() { }

    public Triangle scale(double scalar) {
        return new Triangle(
                new Vector3(v1.x * scalar, v1.y * scalar, v1.z * scalar),
                new Vector3(v2.x * scalar, v2.y * scalar, v2.z * scalar),
                new Vector3(v3.x * scalar, v3.y * scalar, v3.z * scalar)
        );
    }

    public Vector3 normal() {
        Vector3 line1 = Vector3.subtract(v2, v1),
                line2 = Vector3.subtract(v3, v1);

        return Vector3.cross(line1, line2).normalized();
    }

    public Triangle translate(Vector3 offset) {
        return new Triangle(
                Vector3.add(v1, offset),
                Vector3.add(v2, offset),
                Vector3.add(v3, offset)
        );
    }

    public Triangle applyProjectionMatrix(Mat4x4 matrix) {
        return new Triangle(
                v1.applyNormalizeMatrix(matrix),
                v2.applyNormalizeMatrix(matrix),
                v3.applyNormalizeMatrix(matrix)
        );
    }

    public Triangle applyMatrix(Mat4x4 matrix) {
        return new Triangle(
                v1.applyMatrix(matrix),
                v2.applyMatrix(matrix),
                v3.applyMatrix(matrix)
        );
    }

    public Vector3[] getVertices() { return new Vector3[]{v1, v2, v3}; }

    public String toString() { return String.format("T:(%s, %s, %s)", v1.toString(), v2.toString(), v3.toString()); }

    @Override
    public Triangle clone() {
        try {
            Triangle cloned = (Triangle) super.clone();
            cloned.v1 = v1.clone();
            cloned.v2 = v2.clone();
            cloned.v3 = v3.clone();
            return cloned;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}