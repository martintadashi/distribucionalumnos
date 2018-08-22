package Classes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Solucion {

    private final static String TAG = "Solucion";

    private List<Integer> listaIdTareas;
    private List<Integer> listaSeparadores;


    public Solucion(List<Integer> tareas, List<Integer> separadores) {
        this.listaIdTareas = tareas;
        this.listaSeparadores = separadores;
    }

    public List<Integer> getTareas() {
        return listaIdTareas;
    }

    public void setTareas(List<Integer> tareas) {
        this.listaIdTareas = tareas;
    }

    public List<Integer> getSeparadores() {
        return listaSeparadores;
    }

    public void setSeparadores(List<Integer> separadores) {
        this.listaSeparadores = separadores;
    }

    public boolean esFactible(Map<Integer, Tarea> tareas, Map<Integer, Empleado> empleados, int F) {
        int tiempoProyecto = 0;     // en días
        int iterEmpleados = 1;      // id Empleado
        int cantTareasBase = 0;     // cant total de tareas de los empleados anteriores al actual
        boolean pasadoDeTiempo = false;
        while(!pasadoDeTiempo && iterEmpleados <= listaSeparadores.size()) {
            int cantTareasPorEmpleado = listaSeparadores.get(iterEmpleados);
            int iterTareas = cantTareasBase;
            float tiempoTareasPorEmpleado = 0;
            float habilidadEmpleado = empleados.get(iterEmpleados).getHabilidad();
            while(!pasadoDeTiempo && iterTareas < cantTareasPorEmpleado + cantTareasBase) {
                int esfuerzoTarea = tareas.get(listaIdTareas.get(iterTareas)).getEsfuerzo();
                tiempoTareasPorEmpleado += esfuerzoTarea / ((float) 0.5 + habilidadEmpleado);
                pasadoDeTiempo = tiempoProyecto + tiempoTareasPorEmpleado <= F;
                iterTareas++;
            }
            int dedicacionEmpleado = empleados.get(iterEmpleados).getDedicacion();
            tiempoProyecto += (int) Math.ceil(tiempoTareasPorEmpleado / dedicacionEmpleado);
            pasadoDeTiempo = tiempoProyecto <= F;
            if (pasadoDeTiempo) {
                return false;  // solución no factible
            }
            cantTareasBase += cantTareasPorEmpleado;
            iterEmpleados++;
        }
        return !pasadoDeTiempo;
    }

    public int getFitness(Map<Integer, Tarea> tareas, Map<Integer, Empleado> empleados) {
        int costoProyecto = 0;     // en días
        int iterEmpleados = 1;      // id Empleado
        int cantTareasBase = 0;     // cant total de tareas de los empleados anteriores al actual
        while(iterEmpleados <= listaSeparadores.size()) {
            int cantTareasPorEmpleado = listaSeparadores.get(iterEmpleados);
            int iterTareas = cantTareasBase;
            float tiempoTareasPorEmpleado = 0;
            float habilidadEmpleado = empleados.get(iterEmpleados).getHabilidad();
            while(iterTareas < cantTareasPorEmpleado + cantTareasBase) {
                int esfuerzoTarea = tareas.get(listaIdTareas.get(iterTareas)).getEsfuerzo();
                tiempoTareasPorEmpleado += esfuerzoTarea / ((float) 0.5 + habilidadEmpleado);
                iterTareas++;
            }
            int dedicacionEmpleado = empleados.get(iterEmpleados).getDedicacion();
            int sueldoEmpleado = empleados.get(iterEmpleados).getSueldo();
            costoProyecto += ((int) Math.ceil(tiempoTareasPorEmpleado / dedicacionEmpleado)) * sueldoEmpleado;
            cantTareasBase += cantTareasPorEmpleado;
            iterEmpleados++;
        }
        return costoProyecto;
    }

    public static List<Solucion> cruzar(Solucion padre, Solucion madre) {
        return new ArrayList<>();
    }

    public Solucion mutar() {
        return this;
    }
}
