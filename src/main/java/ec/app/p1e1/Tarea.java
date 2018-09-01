package Classes;

import com.google.gson.Gson;

public class Tarea {

    private final static String TAG = "Tarea";

    private int id;             // entre 1 y T
    private int esfuerzo;       // esfuerzo requerido en horas

    public Tarea(int id, int esfuerzo) {
        this.id = id;
        this.esfuerzo = esfuerzo;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getEsfuerzo() {
        return esfuerzo;
    }

    public void setEsfuerzo(int esfuerzo) {
        this.esfuerzo = esfuerzo;
    }

    @Override
    public String toString() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

}
