package ec.app.p1e1;

import ec.*;
import ec.simple.*;
import ec.vector.*;

public class p1e1 extends Problem implements SimpleProblemForm
{
    public void evaluate(final EvolutionState state, final Individual ind, final int subpopulation, final int threadnum) {

        if (ind.evaluated)
            return;

        if (!(ind instanceof IntegerVectorIndividualP1E1)) {

            state.output.fatal("Error. No es un vector de enteros!", null);

        } else {
            IntegerVectorIndividualP1E1 ind2 = (IntegerVectorIndividualP1E1) ind;
            IntegerVectorSpeciesP1E1 t_spe = (IntegerVectorSpeciesP1E1) ind2.species;

            int[] empleadosPorTarea = ((IntegerVectorIndividualP1E1) ind).genome;

            // Fitness
            int costoProyecto = 0;
            int posMax = 0;
            Tarea[] tareas = t_spe.getTareas();
            Empleado[] empleados = t_spe.getEmpleados();

            if (!(ind2.fitness instanceof SimpleFitness)) {
                state.output.fatal("Error. No es un SimpleFitness", null);
            }
            //Calculo del fitnes
//            System.out.println("tareas.length: "+tareas.length);
//            System.out.println(" empleadosPorTarea.length: "+ empleadosPorTarea.length);
//            System.out.println("empleados.length: "+empleados.length);
            if (tareas.length == empleadosPorTarea.length && tareas.length > empleados.length) {
                //System.out.println("Entramos al evaluate.else.calculo");

                float[] horasDeTrabajoParaCadaEmpleado = new float[t_spe.getCantEmpleados()];
                //Inicializa las horas trabajadas en cero
                for (int i = 0; i < horasDeTrabajoParaCadaEmpleado.length; i++) {
                    horasDeTrabajoParaCadaEmpleado[i] = (float) 0.0;
                }
                //Calcula las horas de cada empleado
                for (int i = 0; i < empleadosPorTarea.length; i++) {
                    Tarea tarea = tareas[i];
                    //System.out.println("i= "+i+"   empleadosPorTarea[i]= "+empleadosPorTarea[i]);
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
                //Penalizo las soluciones no factibles con un valor que asegure sean peores que las factubles
                //TODO conseguir el mejor valor para esto
                if(horasDeTrabajoParaCadaEmpleado[posMax]>t_spe.getF()){
                    costoProyecto+=t_spe.getHorasTotal()*t_spe.getMaxSueldoReal();
                }

                // horasDeTrabajoParaCadaEmpleado pasa a ser en días en vez de horas.

                boolean ideal = horasDeTrabajoParaCadaEmpleado[posMax] <= t_spe.getF(); // factible??
                //ideal = ideal && (((SimpleFitness) ind2.fitness).getMinFitness() > costoProyecto);
                ideal = ideal && 3700 == costoProyecto;
                //System.out.println("ideal= "+ideal+"  horasDeTrabajoParaCadaEmpleado[posMax]"+ horasDeTrabajoParaCadaEmpleado[posMax]);
                ((SimpleFitness) ind2.fitness).setFitness(state, costoProyecto * (-1), false);
                ind2.evaluated = true;
            }
        }
    }
}
