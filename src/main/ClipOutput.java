package main;

public class ClipOutput {
    public final Triangle triangle1;
    public final Triangle triangle2;
    public final int numClippedTriangles;

    public ClipOutput(Triangle tri1, Triangle tri2, int numClipped) {
        this.triangle1 = tri1;
        this.triangle2 = tri2;
        this.numClippedTriangles = numClipped;
    }
}
