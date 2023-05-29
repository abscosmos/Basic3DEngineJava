package main;

public class Mat4x4 {
    public static final Mat4x4 IDENTITY = new Mat4x4(new double[][] {
            {1,0,0,0},
            {0,1,0,0},
            {0,0,1,0},
            {0,0,0,1}
    });

    public double[][] m;

    public Mat4x4(double[][] matrix) {
        if(matrix.length != 4) {
            throw new IllegalArgumentException("Matrix must be 4x4.");
        } else {
            for (double[] row : matrix) {
                if (row.length != 4) {
                    throw new IllegalArgumentException("Matrix must be 4x4.");
                }
            }
        }

        m = matrix;
    }

    public Mat4x4() {
        this(IDENTITY.m);
    }

    public static Mat4x4 multiply(Mat4x4 m1, Mat4x4 m2) {
        double[][] result = new double[4][4];

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                result[i][j] = m1.m[i][0] * m2.m[0][j] + m1.m[i][1] * m2.m[1][j] + m1.m[i][2] * m2.m[2][j] + m1.m[i][3] * m2.m[3][j];
            }
        }

        return new Mat4x4(result);
    }

    public Mat4x4 rotTransQuickInverse() {

        Mat4x4 matrix = new Mat4x4();

        matrix.m[0][0] = this.m[0][0]; matrix.m[0][1] = this.m[1][0]; matrix.m[0][2] = this.m[2][0]; matrix.m[0][3] = 0.0;
        matrix.m[1][0] = this.m[0][1]; matrix.m[1][1] = this.m[1][1]; matrix.m[1][2] = this.m[2][1]; matrix.m[1][3] = 0.0;
        matrix.m[2][0] = this.m[0][2]; matrix.m[2][1] = this.m[1][2]; matrix.m[2][2] = this.m[2][2]; matrix.m[2][3] = 0.0;
        matrix.m[3][0] = -(this.m[3][0] * matrix.m[0][0] + this.m[3][1] * matrix.m[1][0] + this.m[3][2] * matrix.m[2][0]);
        matrix.m[3][1] = -(this.m[3][0] * matrix.m[0][1] + this.m[3][1] * matrix.m[1][1] + this.m[3][2] * matrix.m[2][1]);
        matrix.m[3][2] = -(this.m[3][0] * matrix.m[0][2] + this.m[3][1] * matrix.m[1][2] + this.m[3][2] * matrix.m[2][2]);
        matrix.m[3][3] = 1.0;
        return matrix;
    }

    @Override
    public String toString() {
        return String.format("{%.1f, %.1f, %.1f, %.1f}\n{%.1f, %.1f, %.1f, %.1f}\n{%.1f, %.1f, %.1f, %.1f}\n{%.1f, %.1f, %.1f, %.1f}",
                m[0][0], m[0][1], m[0][2], m[0][3],
                m[1][0], m[1][1], m[1][2], m[1][3],
                m[2][0], m[2][1], m[2][2], m[2][3],
                m[3][0], m[3][1], m[3][2], m[3][3]
                );
    }


    // Create special matrices

    public static Mat4x4 createRotationXMat(double thetaRad) {
        return new Mat4x4(new double[][] {
                {1,0,0,0},
                {0,Math.cos(thetaRad),Math.sin(thetaRad),0},
                {0,-Math.sin(thetaRad),Math.cos(thetaRad),0},
                {0,0,0,1}
        });
    }

    public static Mat4x4 createRotationYMat(double thetaRad) {
        return new Mat4x4(new double[][] {
                {Math.cos(thetaRad),0,Math.sin(thetaRad),0},
                {0,1,0,0},
                {-Math.sin(thetaRad),0,Math.cos(thetaRad),0},
                {0,0,0,1}
        });
    }

    public static Mat4x4 createRotationZMat(double thetaRad) {
        return new Mat4x4(new double[][] {
                {Math.cos(thetaRad),Math.sin(thetaRad),0,0},
                {-Math.sin(thetaRad),Math.cos(thetaRad),0,0},
                {0,0,1,0},
                {0,0,0,1}
        });
    }

    public static Mat4x4 createRotationXYZMat(double alpha, double beta, double gamma) {
        return new Mat4x4(new double[][] {
                {Math.cos(alpha) * Math.cos(beta), Math.cos(alpha) * Math.sin(beta) * Math.sin(gamma) - Math.sin(alpha) * Math.cos(gamma), Math.cos(alpha) * Math.sin(beta) * Math.cos(gamma) + Math.sin(alpha) * Math.sin(gamma), 0},
                {Math.sin(alpha) * Math.cos(beta), Math.sin(alpha) * Math.sin(beta) * Math.sin(gamma) + Math.cos(alpha) * Math.cos(gamma), Math.sin(alpha) * Math.sin(beta) * Math.cos(gamma) - Math.cos(alpha) * Math.sin(gamma), 0},
                {-Math.sin(beta), Math.cos(beta) * Math.sin(gamma), Math.cos(beta) * Math.cos(gamma), 0},
                {0, 0, 0, 1}
        });
    }

    public static Mat4x4 createRotationXYZMat(Vector3 rotation) {
        return createRotationXYZMat(rotation.x, rotation.y, rotation.z);
    }
}