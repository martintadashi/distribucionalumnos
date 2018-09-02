package ec.app.p1e1;

import ec.*;
import ec.simple.*;
import ec.vector.*;

public class p1e1 extends Problem implements SimpleProblemForm
{
    public void evaluate(final EvolutionState state, final Individual ind, final int subpopulation, final int threadnum) {

        if (ind.evaluated)
            return;

        if (!(ind instanceof IntegerVectorIndividual)) {

            state.output.fatal("Error. No es un vector de enteros!", null);

        } else {

            IntegerVectorIndividual ind2 = (IntegerVectorIndividual) ind;
            IntegerVectorSpecies t_spe = (IntegerVectorSpecies) ind2.species;

            int[] empleadosPorTarea = t_spe.getEmpleadosPorCadaTarea();

            // Fitness
            int costoProyecto = 0;
            int posMax = 0;
            Tarea[] tareas = ind2.getTareas();
            Empleado[] empleados = ind2.getEmpleados();

            if (!(ind2.fitness instanceof SimpleFitness)) {
                state.output.fatal("Error. No es un SimpleFitness", null);
            }

            if (tareas.length == empleadosPorTarea.length && tareas.length > empleados.length) {
                float[] horasDeTrabajoParaCadaEmpleado = new float[t_spe.getCantEmpleados()];
                for (int i = 0; i < horasDeTrabajoParaCadaEmpleado.length; i++) {
                    horasDeTrabajoParaCadaEmpleado[i] = (float) 0.0;
                }

                for (int i = 0; i < empleadosPorTarea.length; i++) {
                    Tarea tarea = tareas[i];
                    Empleado empleado = empleados[empleadosPorTarea[i]];
                    float horasTrabajadasPorTarea = tarea.getEsfuerzo() / ((float) 0.5 + empleado.getHabilidad());
                    horasDeTrabajoParaCadaEmpleado[empleadosPorTarea[i]] += horasTrabajadasPorTarea;
                }

                for (int i = 0; i < horasDeTrabajoParaCadaEmpleado.length; i++) {
                    Empleado empleado = empleados[i];
                    horasDeTrabajoParaCadaEmpleado[empleadosPorTarea[i]] = ((int) Math.ceil(horasDeTrabajoParaCadaEmpleado[empleadosPorTarea[i]] / empleado.getDedicacion()));
                    costoProyecto += horasDeTrabajoParaCadaEmpleado[empleadosPorTarea[i]] * empleado.getSueldo();
                    if (horasDeTrabajoParaCadaEmpleado[i] > horasDeTrabajoParaCadaEmpleado[posMax]) {
                        posMax = i;
                    }
                }
                // horasDeTrabajoParaCadaEmpleado pasa a ser en días en vez de horas.

                boolean ideal = horasDeTrabajoParaCadaEmpleado[posMax] <= t_spe.getF(); // factible??
                ideal = ideal && (((SimpleFitness) ind2.fitness).getMinFitness() > costoProyecto);

                ((SimpleFitness) ind2.fitness).setFitness(state, costoProyecto * (-1), ideal);
                ind2.evaluated = true;
            }
        }
    }
}
