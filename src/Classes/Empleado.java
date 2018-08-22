package Classes;

import com.google.gson.Gson;

public class Empleado {

    private final static String TAG = "Empleado";

    private int id;             // entre 1 y E
    private float habilidad;    // entre 0 y 1
    private int sueldo;         // sueldo diario
    private int dedicacion;     // dedicacion horaria diaria

    public Empleado(int id, float habilidad, int sueldo, int dedicacion) {
        this.id = id;
        this.habilidad = habilidad;
        this.sueldo = sueldo;
        this.dedicacion = dedicacion;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public float getHabilidad() {
        return habilidad;
    }

    public void setHabilidad(float habilidad) {
        this.habilidad = habilidad;
    }

    public int getSueldo() {
        return sueldo;
    }

    public void setSueldo(int sueldo) {
        this.sueldo = sueldo;
    }

    public int getDedicacion() {
        return dedicacion;
    }

    public void setDedicacion(int dedicacion) {
        this.dedicacion = dedicacion;
    }

    @Override
    public String toString() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

}
