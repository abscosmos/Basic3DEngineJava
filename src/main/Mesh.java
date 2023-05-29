package main;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Mesh {
    private final ArrayList<Triangle> tris;
    public Vector3 origin = Vector3.ZERO;
    public Vector3 rotation = Vector3.ZERO;
    public Vector3 offset = Vector3.ZERO;
    private final boolean movable;


    // Constructors
    public Mesh(ArrayList<Triangle> tris, boolean movable) { this.tris = tris; this.movable = movable; }

    public Mesh() {
        this(new ArrayList<>(), false);
    }

    public Mesh(String filePath, boolean movable) throws FileNotFoundException {
        File file = new File(filePath);
        tris = parseOBJ(file);
        this.movable = movable;
    }
    
    // Functions
    public Triangle[] getTris() {
        Triangle[] triangles = new Triangle[tris.size()];

        for(int i = 0; i < triangles.length; i++) {
            triangles[i] = tris.get(i);

            if(!rotation.equals(Vector3.ZERO)) triangles[i] = triangles[i].applyMatrix(Mat4x4.createRotationXYZMat(rotation));
            if(!offset.equals(Vector3.ZERO)) triangles[i] = triangles[i].translate(offset);
            if(movable && !(origin.equals(Vector3.ZERO))) triangles[i] = triangles[i].translate(origin);
        }

        return triangles;
    }

    public int getTriangleCount() { return tris.size(); }

    public boolean isMovable() {
        return movable;
    }

    public static Integer[] triangulateFaces(String str) {
        ArrayList<Integer> out = new ArrayList<>();
        String[] split = str.split(" ");
        for(String face : List.of(split[1], split[2], split[3], split[4])) {
            String[] splitFace = face.split("/");
            out.add(Integer.parseInt(splitFace[0]));
        }


        return out.toArray(Integer[]::new);
    }

    public static ArrayList<Triangle> parseOBJ(File file) throws FileNotFoundException {
        ArrayList<Triangle> triangles = new ArrayList<>();
        ArrayList<Vector3> vertices = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if(line.startsWith("v ")) {
                    Vector3 vector = new Vector3();
                    String[] values = line.split(" ");
                    vector.x = Double.parseDouble(values[1]);
                    vector.y = Double.parseDouble(values[2]);
                    vector.z = Double.parseDouble(values[3]);
                    vertices.add(vector);
                } else if(line.startsWith("f ")) {
                    if(line.contains("/")) {
                        Integer[] values = triangulateFaces(line);
                        Triangle tri1 = new Triangle(vertices.get(values[0] - 1), vertices.get(values[1] - 1), vertices.get(values[2] - 1));
                        Triangle tri2 = new Triangle(vertices.get(values[0] - 1), vertices.get(values[2] - 1), vertices.get(values[3] - 1));
                        triangles.add(tri1);
                        triangles.add(tri2);
                    } else {
                        String[] values = line.split(" ");
                        Triangle tri = new Triangle();
                        tri.v1 = vertices.get(Integer.parseInt(values[1]) - 1);
                        tri.v2 = vertices.get(Integer.parseInt(values[2]) - 1);
                        tri.v3 = vertices.get(Integer.parseInt(values[3]) - 1);
                        triangles.add(tri);
                    }

                }
            }
        } catch(FileNotFoundException e) {
            throw new FileNotFoundException("The program could not find the file specified.");
        } catch (IOException e) {
            System.out.println("An error occurred while reading the file: " + e.getMessage());
        }
        return triangles;
    }

    public Mesh scaled(double scalar) {
        ArrayList<Triangle> scaled = new ArrayList<>();
        for(Triangle t : this.getTris()) {
            scaled.add(t.scale(scalar));
        }
        return new Mesh(scaled, this.isMovable());
    }

    // Misc
    @Override
    public String toString() {
        StringBuilder out = new StringBuilder();

        for(Triangle tri : tris) {
            out.append(tri).append("\n");
        }

        return out.toString();
    }
}
