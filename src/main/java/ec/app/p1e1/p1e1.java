package ec.app.p1e1;

import ec.*;
import ec.simple.*;
import ec.vector.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
                    //System.out.print("Factibilizo\t");
                    int cantidadFactibilizaciones=0;
                    boolean factible=esFactible(state,  ind2, horasDeTrabajoParaCadaEmpleado, empleados, tareas,t_spe);
                    while(cantidadFactibilizaciones<10 && !factible){
                        //System.out.println("Intento "+cantidadFactibilizaciones+" factibilizar el individuo: "+ind2.genotypeToStringForHumans());
                        factibilizar(state,  ind2, horasDeTrabajoParaCadaEmpleado, empleados, tareas,t_spe);
                        factible=esFactible(state,  ind2, horasDeTrabajoParaCadaEmpleado, empleados, tareas,t_spe);
                        cantidadFactibilizaciones++;
                    }
                    if(!factible){
                        //System.out.print("Se penaliza   \t|\t El individuo demora "+diasDeTrabajoParaCadaEmpleado[posMax]+"/"+t_spe.getF() +" se agrega costo: "+(t_spe.getHorasTotal()*t_spe.getMaxSueldoReal())+ " a su costo: "+ costoProyecto);
                        costoProyecto+=t_spe.getHorasTotal()*t_spe.getMaxSueldoReal()*(diasDeTrabajoParaCadaEmpleado[posMax]/t_spe.getF());
                        //System.out.println(" total: "+costoProyecto);
                    }else{
                        //System.out.println("FUNCIONO el Factibilizo");
                    }



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

    private boolean esFactible(EvolutionState state, IntegerVectorIndividualP1E1 ind2, float[] horasDeTrabajoParaCadaEmpleado, Empleado[] empleados, Tarea[] tareas, IntegerVectorSpeciesP1E1 t_spe) {
        boolean factible=true;
        int iEmpleados=0;
        //recorro hasta ver todos los usuarios o encontrar que no es factible
        while(iEmpleados<empleados.length && factible){
            if(Math.ceil(horasDeTrabajoParaCadaEmpleado[iEmpleados] / empleados[iEmpleados].getDedicacion())>t_spe.getF()){
                factible=false;
                //System.out.println("\tNo factible por el empleado: "+iEmpleados);
                //System.out.println("\t\tHoras trabajadas: "+horasDeTrabajoParaCadaEmpleado[iEmpleados]+" Dedicacion diaria: "+empleados[iEmpleados].getDedicacion());
                //System.out.println("\t\tDias trabajados: "+(horasDeTrabajoParaCadaEmpleado[iEmpleados]/empleados[iEmpleados].getDedicacion())+"/"+t_spe.getF());

            }
            else{
                iEmpleados++;
            }
        }
        return factible;
    }

    private void factibilizar(EvolutionState state, IntegerVectorIndividualP1E1 ind2, float[] horasDeTrabajoParaCadaEmpleado, Empleado[] empleados, Tarea[] tareas, IntegerVectorSpeciesP1E1 t_spe) {
        Random r = new Random();
        //Recorro cada empleado


        for (int iEmpleado = 0; iEmpleado < horasDeTrabajoParaCadaEmpleado.length; iEmpleado++) {
            List<Integer> tareasEmpleado=new ArrayList<Integer>();

            //Mientras este pasado de horas
            if (Math.ceil(horasDeTrabajoParaCadaEmpleado[iEmpleado]/empleados[iEmpleado].getDedicacion())>t_spe.getF()){
                //System.out.println("\tIntento corregir al empleado: "+iEmpleado+ " con dedicacion: "+empleados[iEmpleado].getDedicacion()+ " y habilidad: "+empleados[iEmpleado].getHabilidad());
                //System.out.print("\tTareas del empleado ("+(horasDeTrabajoParaCadaEmpleado[iEmpleado]/empleados[iEmpleado].getDedicacion())+"): ");
                //Consigo todas sus tareas
                for (int iTareas = 0; iTareas < tareas.length; iTareas++) {
                    if (ind2.genome[iTareas]==iEmpleado){
                        tareasEmpleado.add(iTareas);
                        //System.out.print(" "+iTareas+"("+tareas[iTareas].getEsfuerzo()+")");
                    }
                }
                //System.out.println("");

                int contadorCorrecciones=0;
                while(Math.ceil(horasDeTrabajoParaCadaEmpleado[iEmpleado]/empleados[iEmpleado].getDedicacion())>t_spe.getF()
                        && contadorCorrecciones<tareas.length
                        && tareasEmpleado.size()>0
                        ){
                    //Elijo tarea al azar
                    int indiceTareaAleatoria = r.nextInt(tareasEmpleado.size());
                    int tareaAleatoria= tareasEmpleado.get(indiceTareaAleatoria);
                    //System.out.print("\t\tIntento reasignar la tarea: "+tareaAleatoria+" ("+tareas[tareaAleatoria].getEsfuerzo()+") pruebo con:");
                    //Actualizo sus horas trabajadas
                    horasDeTrabajoParaCadaEmpleado[iEmpleado]-=tareas[tareaAleatoria].getEsfuerzo()/(0.5+empleados[iEmpleado].getHabilidad());
                    //Elijo un empleado nuevo
                    int iNuevoEmpleado=(iEmpleado+1)%empleados.length;
                    float limiteHoras=empleados[iNuevoEmpleado].getDedicacion()*t_spe.getF();
                    float holgura=limiteHoras-horasDeTrabajoParaCadaEmpleado[iNuevoEmpleado];
                    boolean empleadoDisponible=holgura>= tareas[tareaAleatoria].getEsfuerzo();
                    while(iNuevoEmpleado!=iEmpleado && !empleadoDisponible){
                        limiteHoras=empleados[iNuevoEmpleado].getDedicacion()*t_spe.getF();
                        holgura=limiteHoras-horasDeTrabajoParaCadaEmpleado[iNuevoEmpleado];
                        //System.out.print(" "+iNuevoEmpleado+ "("+holgura+")");
                        //Paso al siguiente empleado
                        iNuevoEmpleado= (iNuevoEmpleado+1)%empleados.length;
                        empleadoDisponible=holgura>= tareas[tareaAleatoria].getEsfuerzo();

                    }
                    //System.out.print("\n");
                    //Le asigno la tarea
                    //System.out.println("\t\tGenoma:"+ind2.genome[tareaAleatoria]);
                    ind2.genome[tareaAleatoria]=iNuevoEmpleado;
                    //System.out.println("\t\tLa asigno al empleado:"+iNuevoEmpleado);
                    //System.out.println("\t\tGenoma:"+ind2.genome[tareaAleatoria]);

                    //Actualizo sus horas trabajadas
                    horasDeTrabajoParaCadaEmpleado[iNuevoEmpleado]+=tareas[tareaAleatoria].getEsfuerzo()/(0.5+empleados[iNuevoEmpleado].getHabilidad());
                    //Retiro la lista de tarea del iEmpleado
                    tareasEmpleado.remove(indiceTareaAleatoria);
                    //
                    contadorCorrecciones++;
                }

            }
        }

        //Encontrar quienes se pasan de F
        //Repartir tareas hasta que dejen de pasarse


    }
}
