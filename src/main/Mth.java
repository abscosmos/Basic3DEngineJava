package main;

public final class Mth {

    // General Math

    public static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    public static double lerp(double a, double b, double t) {
        return (1-t) * a + t * b;
    }

    public static double moveTowards(double current, double target, double maxDistance) {
        double difference = target - current;
        if(Math.abs(difference) <= maxDistance || difference == 0) return target;
        return current + (Math.signum(difference)) * maxDistance;
    }

    public static int randomIntInRange(int startInclusive, int endInclusive) {
        return (int) Math.floor(Math.random() * (endInclusive+1 - startInclusive) + startInclusive);
    }

    public static double randomDoubleInRange(double startInclusive, double endInclusive) {
        return Math.random() * (endInclusive - startInclusive) + startInclusive;
    }

    public static final double EPSILON = 1e-6;


    // Convert between pixel points & normalized points
    public static double denormalizePointW(double width, double x) { return (x * width/2.0) + width/2.0; }

    public static double denormalizePointH(double height, double y) { return (y * height/2.0) + height/2.0; }

    public static double normalizePointW(double width, double x) { return (x - width/2.0) / (width/2.0); }

    public static double normalizePointH(double height, double y) { return (y - height/2.0) / (height/2.0); }

    // Plane Operations
    public static ClipOutput calcNumTrianglesClippedAgainstPlane(Vector3 planePoint, Vector3 planeNormal, Triangle inTriangle) {
        Triangle inTri = inTriangle.clone(),
                outTri1 = new Triangle(),
                outTri2 = new Triangle();

        planeNormal = planeNormal.normalized();

        Vector3[] insidePoints = new Vector3[3];
        Vector3[] outsidePoints = new Vector3[3];

        int numInsidePoint = 0;
        int numOutsidePoint = 0;

        double distV1 = calcDistBetweenPointAndPlane(inTri.v1, planePoint, planeNormal);
        double distV2 = calcDistBetweenPointAndPlane(inTri.v2, planePoint, planeNormal);
        double distV3 = calcDistBetweenPointAndPlane(inTri.v3, planePoint, planeNormal);

        if(distV1 >= 0) {
            insidePoints[numInsidePoint++] = inTri.v1;
        } else {
            outsidePoints[numOutsidePoint++] = inTri.v1;
        }

        if(distV2 >= 0) {
            insidePoints[numInsidePoint++] = inTri.v2;
        } else {
            outsidePoints[numOutsidePoint++] = inTri.v2;
        }

        if(distV3 >= 0) {
            insidePoints[numInsidePoint++] = inTri.v3;
        } else {
            outsidePoints[numOutsidePoint++] = inTri.v3;
        }

        if(numInsidePoint == 0) {
            return new ClipOutput(outTri1, outTri2, 0);

        } else if(numInsidePoint == 3) {
            outTri1 = inTri;
            return new ClipOutput(outTri1, outTri2, 1);

        } else if(numInsidePoint == 1) {
            outTri1.lum = inTri.lum;
            outTri1.v1 = insidePoints[0];

            outTri1.v2 = planeIntersectVector3(planePoint, planeNormal, insidePoints[0], outsidePoints[0]);
            outTri1.v3 = planeIntersectVector3(planePoint, planeNormal, insidePoints[0], outsidePoints[1]);

            return new ClipOutput(outTri1, outTri2, 1);

        } else {
            outTri1.lum = inTri.lum;
            outTri2.lum = inTri.lum;

            outTri1.v1 = insidePoints[0];
            outTri1.v2 = insidePoints[1];
            outTri1.v3 = planeIntersectVector3(planePoint, planeNormal, insidePoints[0], outsidePoints[0]);

            outTri2.v1 = insidePoints[1];
            outTri2.v2 = outTri1.v3;
            outTri2.v3 = planeIntersectVector3(planePoint, planeNormal, insidePoints[1], outsidePoints[0]);

            return new ClipOutput(outTri1, outTri2, 2);
        }
    }

    private static double calcDistBetweenPointAndPlane(Vector3 p, Vector3 planePoint, Vector3 planeNormal) {
        // TODO might need to normalize p here, further testing required
        return (planeNormal.x * p.x + planeNormal.y * p.y + planeNormal.z * p.z - Vector3.dot(planeNormal, planePoint));
    }

    // Vector 3 Operations

    public static Vector3 planeIntersectVector3(Vector3 planePoint, Vector3 planeNormal, Vector3 lineStart, Vector3 lineEnd) {
        planeNormal = planeNormal.normalized();
        double distanceFromOrigin = -Vector3.dot(planeNormal, planePoint);
        double startProjOntoNormal = Vector3.dot(lineStart, planeNormal);
        double endProjOntoNormal = Vector3.dot(lineEnd, planeNormal);
        double lineIntersectionScalar = (-distanceFromOrigin - startProjOntoNormal) / (endProjOntoNormal - startProjOntoNormal);

        Vector3 lineStartToEnd = Vector3.subtract(lineEnd, lineStart);
        Vector3 lineToIntersect = Vector3.scale(lineStartToEnd, lineIntersectionScalar);

        return Vector3.add(lineStart, lineToIntersect);
    }


    // Matrix Operations

    public static Mat4x4 matrixPointAt(Vector3 position, Vector3 target, Vector3 up) {
        Vector3 newForwardVector = Vector3.subtract(target, position).normalized();

        Vector3 upSimilarity = Vector3.scale(newForwardVector, Vector3.dot(up, newForwardVector));
        Vector3 newUpVector = Vector3.subtract(up, upSimilarity).normalized();

        Vector3 rightVector = Vector3.cross(newUpVector, newForwardVector);

        return new Mat4x4(new double[][] {
                {rightVector.x, rightVector.y, rightVector.z, 0.0},
                {newUpVector.x, newUpVector.y, newUpVector.z, 0.0},
                {newForwardVector.x, newForwardVector.y, newForwardVector.z, 0.0},
                {position.x, position.y, position.z, 0.0}
        });
    }

    // Game Engine Operations

    public static Vector3 getRandomLocationWithinBounds(Vector3 min, Vector3 max) {
        return new Vector3(
                randomDoubleInRange(min.x, max.x),
                randomDoubleInRange(min.y, max.y),
                randomDoubleInRange(min.z, max.z)
        );
    }
}

