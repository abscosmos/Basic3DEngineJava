package main;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.Arrays;

public class Renderer {
    // 3D -> 2D Projection Variables
    private static Mat4x4 projectionMatrix;

    // Menu Variables
    public static int numTriangles, numVisibleTris;
    public static boolean doShowWireframe = false;
    public static boolean doFillTriangles = true;
    public static boolean doDisplayColliders = false;

    // Rendering Variables
    private static final ArrayList<Triangle> triangleRenderQueue = new ArrayList<>();
    private static final ArrayList<Triangle> AABBMeshRenderQueue = new ArrayList<>();
    public static Color drawColor = new Color(209, 45, 46);

    // Main Functions
    public static void ready() { }

    public static void process(double delta) { }

    public static void frameProcess(JPanel panel, Graphics g) {
        int width = panel.getWidth(), height = panel.getHeight();
        updateMatrices(width, height);
        PhysicsEngine.activeCamera.updateInternals();

        if(doDisplayColliders) drawAABBs(width, height, g);

        for(ArrayList<Object3D> oArr : PhysicsEngine.objectSets) {
            processMeshRendering(PhysicsEngine.activeCamera.posCorrected(), oArr);
        }

        sortDrawTriangles(width, height, g, doShowWireframe, doFillTriangles);
    }
    
    // Mesh Processing Functions
    public static void processMeshRendering(Vector3 cam, ArrayList<Object3D> o) {
        for (Object3D object : o ) {
            processMeshRendering(object, cam);
        }
    }

    public static void processMeshRendering(Object3D object, Vector3 cam) {
        if(!object.isVisible) return;
        processMeshRendering(object.mesh, cam, false);
    }

    public static void processMeshRendering(Mesh m, Vector3 cam, boolean isMeshFromAABB) {
        numTriangles = m.getTriangleCount();

        for (Triangle tri : m.getTris()) {
            Triangle triProj, triTrans, triView;

            triTrans = tri.translate(new Vector3(0, 0, 6));

            Vector3 normal = triTrans.normal();

            if (Vector3.dot(normal, Vector3.subtract(triTrans.v1, cam)) < 0 || isMeshFromAABB) {
                Vector3 lightDirection = new Vector3(0.0, 0.5, -1.0);
                lightDirection = lightDirection.normalized(); //TODO attach point light to camera

                triView = triTrans.applyMatrix(PhysicsEngine.activeCamera.viewMatrix);

                ClipOutput clipOutput = Mth.calcNumTrianglesClippedAgainstPlane(
                        new Vector3(0.0, 0.0, 0.1), new Vector3(0.0, 0.0, 1.0), triView
                );

                Triangle[] clipped = {clipOutput.triangle1, clipOutput.triangle2};

                for (int i = 0; i < clipOutput.numClippedTriangles; i++) {
                    triProj = clipped[i].applyProjectionMatrix(projectionMatrix);

                    triProj.lum = (int) Mth.clamp((Vector3.dot(normal, lightDirection) * 255), 1, 255);

                    triProj.v1.x *= -1; triProj.v2.x *= -1; triProj.v3.x *= -1;
                    triProj.v1.y *= -1; triProj.v2.y *= -1; triProj.v3.y *= -1;

                    if(isMeshFromAABB) {
                        AABBMeshRenderQueue.add(triProj);
                    } else {
                        triangleRenderQueue.add(triProj);
                    }
                }
            }
        }
    }

    // Projection & Drawing Functions
    public static void updateMatrices(double width, double height) {
        double zNear = Mth.EPSILON;
        double zFar = 1000;
        double fov = 90;
        double aspectRatio = height / width;
        double fovRad = 1.0 / Math.tan(Math.toRadians(fov * 0.5));

        projectionMatrix = new Mat4x4(new double[][]{
                {aspectRatio * fovRad, 0, 0, 0},
                {0, fovRad, 0, 0},
                {0, 0, zFar / (zFar - zNear), 1},
                {0, 0, (-zFar * zNear) / (zFar - zNear), 0}
        });
    }

    public static void sortDrawTriangles(int width, int height, Graphics g, boolean drawWireFrame, boolean fillTriangle) {
        triangleRenderQueue.sort((t1, t2) -> {
            double z1 = (t1.v1.z + t1.v2.z + t1.v3.z) / 3.0f;
            double z2 = (t2.v1.z + t2.v2.z + t2.v3.z) / 3.0f;
            return Double.compare(z2, z1);
        });

        numVisibleTris = 0;

        for (Triangle triToRender : triangleRenderQueue) {

            Triangle[] clipped = new Triangle[2];
            ArrayList<Triangle> listTriangles = new ArrayList<>();
            listTriangles.add(triToRender);
            int numNewTriangles = 1;

            for (int i = 0; i < 4; i++) {
                int numTrisToAdd = 0;

                while (numNewTriangles > 0) {
                    Triangle triFirst = listTriangles.get(0);
                    listTriangles.remove(0);
                    numNewTriangles--;

                    switch (i) {
                        case 0 -> {
                            ClipOutput clipOutput0 = Mth.calcNumTrianglesClippedAgainstPlane(
                                    new Vector3(0.0, -1.0, 0.0), new Vector3(0.0, 1.0, 0.0), triFirst);
                            clipped = new Triangle[]{clipOutput0.triangle1, clipOutput0.triangle2};
                            numTrisToAdd = clipOutput0.numClippedTriangles;
                        }
                        case 1 -> {
                            ClipOutput clipOutput1 = Mth.calcNumTrianglesClippedAgainstPlane(
                                    new Vector3(0.0, 1.0, 0.0), new Vector3(0.0, -1.0, 0.0), triFirst);
                            clipped = new Triangle[]{clipOutput1.triangle1, clipOutput1.triangle2};
                            numTrisToAdd = clipOutput1.numClippedTriangles;
                        }
                        case 2 -> {
                            ClipOutput clipOutput2 = Mth.calcNumTrianglesClippedAgainstPlane(
                                    new Vector3(-1, 0.0, 0.0), new Vector3(1.0, 0.0, 0.0), triFirst);
                            clipped = new Triangle[]{clipOutput2.triangle1, clipOutput2.triangle2};
                            numTrisToAdd = clipOutput2.numClippedTriangles;
                        }
                        case 3 -> {
                            ClipOutput clipOutput3 = Mth.calcNumTrianglesClippedAgainstPlane(
                                    new Vector3(1, 0.0, 0.0), new Vector3(-1.0, 0.0, 0.0), triFirst);
                            clipped = new Triangle[]{clipOutput3.triangle1, clipOutput3.triangle2};
                            numTrisToAdd = clipOutput3.numClippedTriangles;
                        }
                    }

                    listTriangles.addAll(Arrays.asList(clipped).subList(0, numTrisToAdd));
                }

                numNewTriangles = listTriangles.size();
            }

            for (Triangle tri : listTriangles) {
                numVisibleTris++;
                drawTriangle(width, height, g, tri, drawWireFrame, fillTriangle);
            }
        }

        triangleRenderQueue.clear();
    }

    public static void drawTriangle(double width, double height, Graphics g, Triangle t, boolean drawWireFrame, boolean fillTriangle) {
        double lum = t.lum / 255.0;
        g.setColor(new Color((int) (drawColor.getRed() * lum), (int) (drawColor.getGreen() * lum), (int) (drawColor.getBlue() * lum)));

        Path2D path = new Path2D.Double();
        Graphics2D g2d = (Graphics2D) g;

        path.moveTo(Mth.denormalizePointW(width, t.v1.x), Mth.denormalizePointH(height, t.v1.y));
        path.lineTo(Mth.denormalizePointW(width, t.v2.x), Mth.denormalizePointH(height, t.v2.y));
        path.lineTo(Mth.denormalizePointW(width, t.v3.x), Mth.denormalizePointH(height, t.v3.y));
        path.closePath();

        if (fillTriangle) g2d.fill(path);

        if (drawWireFrame) {
            g.setColor(Color.BLACK);
            g2d.draw(path);
        }
    }

    public static void drawAABBs(double width, double height, Graphics g) {
        AABBMeshRenderQueue.clear();
        for(Object3D o: PhysicsEngine.objects) {
            processMeshRendering(o.getCollider().getMesh(), PhysicsEngine.activeCamera.posCorrected(), true);
        }

        for(Triangle tri : AABBMeshRenderQueue) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setColor(new Color(0, 32, 255));

            Path2D path = new Path2D.Double();

            path.moveTo(Mth.denormalizePointW(width, tri.v1.x), Mth.denormalizePointH(height, tri.v1.y));
            path.lineTo(Mth.denormalizePointW(width, tri.v2.x), Mth.denormalizePointH(height, tri.v2.y));
            path.lineTo(Mth.denormalizePointW(width, tri.v3.x), Mth.denormalizePointH(height, tri.v3.y));
            path.closePath();

            g2d.draw(path);
        }
    }
}
