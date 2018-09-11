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
            //consigo el individuo y lo casteo como la clase con la que lo voy a trabajar
            IntegerVectorIndividualP1E1 ind2 = (IntegerVectorIndividualP1E1) ind;
            //Consigo la especie del individuo
            IntegerVectorSpeciesP1E1 t_spe = (IntegerVectorSpeciesP1E1) ind2.species;
            //Creo un arreglo en el que tener el genoma del individuo
            int[] empleadosPorTarea = ((IntegerVectorIndividualP1E1) ind).genome;
            // Fitness
            int costoProyecto = 0;
            int posMax = 0;
            //Cargo las tareas y los empleados
            Tarea[] tareas = t_spe.getTareas();
            Empleado[] empleados = t_spe.getEmpleados();
            //System.out.println("Empleados.length: "+empleados.length);
//            for (int iEmpleado = 0; iEmpleado < empleados.length; iEmpleado++) {
//                System.out.println("\t"+empleados[iEmpleado].toString());
//            }

            //Corroboro que el fitnes de mi individuo extienda de SimpleFitness
            if (!(ind2.fitness instanceof SimpleFitness)) {
                state.output.fatal("Error. No es un SimpleFitness", null);
            }
            //Si llegue hasta aca el individuo es correcto


//            System.out.println("tareas.length: "+tareas.length);
//            System.out.println(" empleadosPorTarea.length: "+ empleadosPorTarea.length);
//            System.out.println("empleados.length: "+empleados.length);

            //Corroboro que el tamaño de los arreglos fitness
            if (tareas.length == empleadosPorTarea.length && tareas.length > empleados.length) {
                //System.out.println("INICIA INDIVIDUO");

                float[] horasDeTrabajoParaCadaEmpleado = new float[t_spe.getCantEmpleados()];
                float[] diasDeTrabajoParaCadaEmpleado = new float[t_spe.getCantEmpleados()];

                //Inicializa las horas trabajadas en cero
                for (int i = 0; i < horasDeTrabajoParaCadaEmpleado.length; i++) {
                    horasDeTrabajoParaCadaEmpleado[i] = (float) 0.0;
                    diasDeTrabajoParaCadaEmpleado[i] = (float) 0.0;
                }
                //Recorro cada Calcula las horas de cada empleado
                for (int i = 0; i < empleadosPorTarea.length; i++) {
                    //Consigo la tarea con la que voy a trabajar
                    Tarea tarea = tareas[i];
                    //System.out.println("empleadosPorTarea["+i+"]= "+empleadosPorTarea[i]);
                    //Encuentro al empleado que tiene asignada esa tarea
                    Empleado empleado = empleados[empleadosPorTarea[i]];
                    //Sumo las horas que le va a tomar al empleado (segun su habilidad) completar el esfuerzo de la tarea
                    //System.out.println("Tarea: "+tarea.toString());
                    //System.out.println("Empleado: "+empleado.toString());
                    float horasTrabajadasPorTarea = tarea.getEsfuerzo() / ((float) 0.5 + empleado.getHabilidad());
                    horasDeTrabajoParaCadaEmpleado[empleadosPorTarea[i]] += horasTrabajadasPorTarea;
                }
                //Con todas las horas cargadas en todos los empleados calculo los dias que trabaja cada uno y cuanto nos cuesta
                for (int i = 0; i < diasDeTrabajoParaCadaEmpleado.length; i++) {
                    Empleado empleado = empleados[i];
                    diasDeTrabajoParaCadaEmpleado[i] = ((int) Math.ceil(horasDeTrabajoParaCadaEmpleado[i] / empleado.getDedicacion()));
                    costoProyecto += diasDeTrabajoParaCadaEmpleado[i] * empleado.getSueldo();
                    //System.out.println("\t Dias trabajados por el empleado "+i+" : "+diasDeTrabajoParaCadaEmpleado[i]);
                    //Guardo la posicion del empleado que trabaje mas dias
                    if (diasDeTrabajoParaCadaEmpleado[i] > diasDeTrabajoParaCadaEmpleado[posMax]) {
                        posMax = i;
                    }
                }

                //Penalizo las soluciones no factibles con un valor que asegure sean peores que las factubles
                if(diasDeTrabajoParaCadaEmpleado[posMax]>t_spe.getF()){
                    //System.out.print("Se penaliza   \t|\t El individuo demora "+diasDeTrabajoParaCadaEmpleado[posMax]+"/"+t_spe.getF() +" se agrega costo: "+(t_spe.getHorasTotal()*t_spe.getMaxSueldoReal())+ " a su costo: "+ costoProyecto);
                    costoProyecto+=t_spe.getHorasTotal()*t_spe.getMaxSueldoReal();
                    //System.out.println(" total: "+costoProyecto);
                } else{
                    //System.out.println("NO SE PENALIZA\t|\t El individuo demora "+diasDeTrabajoParaCadaEmpleado[posMax]+"/"+t_spe.getF()+" y tiene costo: "+costoProyecto);
                }
                //System.out.println("\tAl individuo: ");



                boolean ideal = diasDeTrabajoParaCadaEmpleado[posMax] <= t_spe.getF(); // factible??
                //ideal = ideal && (((SimpleFitness) ind2.fitness).getMinFitness() > costoProyecto);
                ideal =  ideal && (t_spe.getMaxSueldoReal()*diasDeTrabajoParaCadaEmpleado[posMax]) == costoProyecto;
                //ideal = ideal || state.generation - state.getBestFitnessGeneration() > (int) state.numGenerations/10;
                //System.out.println("state.generation: " + state.generation + ",getBestFitnessGeneration(): " + state.getBestFitnessGeneration() + ", state.numGenerations: " + state.numGenerations);
                //System.out.println("ideal= "+ideal+"  horasDeTrabajoParaCadaEmpleado[posMax]"+ horasDeTrabajoParaCadaEmpleado[posMax]);
                ((SimpleFitness) ind2.fitness).setFitness(state, costoProyecto * (-1), ideal);
                ind2.evaluated = true;

                //ind.printIndividualForHumans(state,0);
                /*
                System.out.print("\t Dias trabajados: (");

                for (int i = 0; i < horasDeTrabajoParaCadaEmpleado.length; i++) {
                    System.out.print(diasDeTrabajoParaCadaEmpleado[i]+", ");
                }
                System.out.println(")");
                System.out.println("FINALIZA INDIVIDUO");
                */
            }
        }
    }
}
