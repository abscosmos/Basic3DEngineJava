package main;

public class Vector3 implements Cloneable {
    public static final Vector3 ZERO = new Vector3(0.0,0.0,0.0);

    public double x, y, z;

    public Vector3(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vector3() { }

    public String toString() { return String.format("V: (%.1f, %.1f, %.1f)", x, y, z); }

    public double length() { return Math.sqrt(this.magnitudeSquared()); }

    public static Vector3 add(Vector3 lhs, Vector3 rhs) {
        return new Vector3(lhs.x + rhs.x, lhs.y + rhs.y, lhs.z + rhs.z);
    }

    public static Vector3 subtract(Vector3 lhs, Vector3 rhs) {
        return new Vector3(lhs.x - rhs.x, lhs.y - rhs.y, lhs.z - rhs.z);
    }

    public static Vector3 scale(Vector3 lhs, double scalar) {
        return new Vector3(lhs.x * scalar, lhs.y * scalar, lhs.z * scalar);
    }

    public static Vector3 mod(Vector3 lhs, double mod) {
        return new Vector3(lhs.x % mod, lhs.y % mod, lhs.z % mod);
    }

    public static double dot(Vector3 lhs, Vector3 rhs) {
        return lhs.x * rhs.x + lhs.y * rhs.y + lhs.z * rhs.z;
    }

    public static Vector3 cross(Vector3 lhs, Vector3 rhs) {
        return new Vector3(
                lhs.y * rhs.z - lhs.z * rhs.y,
                lhs.z * rhs.x - lhs.x * rhs.z,
                lhs.x * rhs.y - lhs.y * rhs.x
        );
    }

    public static Vector3 midpoint(Vector3 lhs, Vector3 rhs) {
        return Vector3.scale(Vector3.add(lhs, rhs), 0.5);
    }

    public double magnitudeSquared() {
        return Vector3.dot(this, this);
    }

    public Vector3 normalized() {
        double len = this.length();

        return new Vector3(this.x / len, this.y / len, this.z / len);
    }

    public static Vector3 componentMultiply(Vector3 lhs, Vector3 rhs) {
        return new Vector3(lhs.x * rhs.x, lhs.y * rhs.y, lhs.z * rhs.z);
    }

    public Vector3 signs() {
        return new Vector3(Math.signum(x), Math.signum(y), Math.signum(z));
    }

    public Vector3 moveTowards(Vector3 target, double maxDistance) {
        Vector3 difference = Vector3.subtract(target, this);
        double magnitude = difference.length();
        if(magnitude <= maxDistance || magnitude == 0) {
            return target;
        }
        return Vector3.add(this, Vector3.scale(Vector3.scale(difference, 1 / magnitude), maxDistance));
    }

    public Vector3 clampedMagnitude(double minLength, double maxLength) {
        double sqrMagnitude = this.magnitudeSquared();
        if(sqrMagnitude < minLength * minLength) {
            return Vector3.scale(this.normalized(), minLength);
        } else if(sqrMagnitude > maxLength * maxLength) {
            return Vector3.scale(this.normalized(), maxLength);
        } else {
            return this;
        }
    }

    public Vector3 applyMatrix(Mat4x4 m) {
        double[] result = new double[4];

        for (int i = 0; i < 4; i++) {
            result[i] = this.x * m.m[0][i] + this.y * m.m[1][i] + this.z * m.m[2][i] + m.m[3][i];
        }

        return new Vector3(result[0], result[1], result[2]);
    }

    public Vector3 applyNormalizeMatrix(Mat4x4 m) {
        Vector3 out = new Vector3();
        double[] result = new double[4];

        for (int i = 0; i < 4; i++) {
            result[i] = this.x * m.m[0][i] + this.y * m.m[1][i] + this.z * m.m[2][i] + m.m[3][i];
        }

        double w = result[3];
        if (w != 0.0) {
            out.x = result[0] / w; out.y = result[1] / w; out.z = result[2] / w;
        }

        return out;
    }

    public double minComponent() {
        return Math.min(x, Math.min(y, z));
    }

    public double maxComponent() {
        return Math.max(x, Math.max(y, z));
    }

    public boolean equals(Vector3 v) {
        return this == v || (v != null && v.x == x && v.y == y && v.z == z);
    }

    @Override
    public Vector3 clone() {
        try {
            return (Vector3) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
