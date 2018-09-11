/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/



package ec;
import java.util.*;
import java.io.*;
import java.lang.reflect.Array;

import ec.app.p1e1.Empleado;
import ec.app.p1e1.Tarea;
import ec.app.p1e1.p1e1;
import ec.simple.SimpleFitness;
import ec.util.*;
import ec.vector.IntegerVectorIndividualP1E1;
import ec.vector.IntegerVectorSpeciesP1E1;

/* 
 * Subpopulation.java
 * 
 * Created: Tue Aug 10 20:34:14 1999
 * By: Sean Luke
 */

/**
 * Subpopulation is a group which is basically an array of Individuals.
 * There is always one or more Subpopulations in the Population.  Each
 * Subpopulation has a Species, which governs the formation of the Individuals
 * in that Subpopulation.  Subpopulations also contain a Fitness prototype
 * which is cloned to form Fitness objects for individuals in the subpopulation.
 *
 * <p>An initial subpopulation is populated with new random individuals 
 * using the populate(...) method.  This method typically populates
 * by filling the array with individuals created using the Subpopulations' 
 * species' emptyClone() method, though you might override this to create
 * them with other means, by loading from text files for example.
 *
 * <p>In a multithreaded area of a run, Subpopulations should be considered
 * immutable.  That is, once they are created, they should not be modified,
 * nor anything they contain.  This protocol helps ensure read-safety under
 * multithreading race conditions.
 *

 <p><b>Parameters</b><br>
 <table>
 <tr><td valign=top><i>base</i>.<tt>size</tt><br>
 <font size=-1>int &gt;= 1</font></td>
 <td valign=top>(total number of individuals in the subpopulation)</td></tr>

 <tr><td valign=top><i>base</i>.<tt>species</tt><br>
 <font size=-1>classname, inherits and != ec.Species</font></td>
 <td valign=top>(the class of the subpopulations' Species)</td></tr>

 <tr><td valign=top><i>base</i>.<tt>fitness</tt><br>
 <font size=-1>classname, inherits and != ec.Fitness</font></td>
 <td valign=top>(the class for the prototypical Fitness for individuals in this subpopulation)</td></tr>

 <tr><td valign=top><i>base</i>.<tt>file</tt><br>
 <font size=-1>String</font></td>
 <td valign=top>(pathname of file from which the population is to be loaded.  If not defined, or empty, then the population will be initialized at random in the standard manner)</td></tr>

 <tr><td valign=top><i>base</i>.<tt>duplicate-retries</tt><br>
 <font size=-1>int &gt;= 0</font></td>
 <td valign=top>(during initialization, when we produce an individual which already exists in the subpopulation, the number of times we try to replace it with something unique.  Ignored if we're loading from a file.)</td></tr>
 </table>

 <p><b>Default Base</b><br>
 ec.subpop

 <p><b>Parameter bases</b><br>
 <table>
 <tr><td valign=top><i>base</i>.<tt>species</tt></td>
 <td>species (the subpopulations' species)</td></tr>

 </table>


 * @author Sean Luke
 * @version 1.0 
 */


public class Subpopulation implements Cloneable, Setup
    {
    private static final long serialVersionUID = 1;

    /* A new subpopulation should be loaded from this resource name if it is non-null;
       otherwise they should be created at random.  */
    public boolean loadInds;
    public Parameter file;

    /** The species for individuals in this subpopulation. */
    public Species species;

    public IntBag[] parents;
        
    /** The subpopulation's individuals. */
    public ArrayList<Individual> individuals;
    
    /** initial expected size of the subpopulation*/
    public int initialSize;

    /** Do we allow duplicates? */
    public int numDuplicateRetries;
    
    /** What is our fill behavior beyond files? */
    public int extraBehavior;
    
    public static final String P_SUBPOPULATION = "subpop";
    public static final String P_FILE = "file";
    public static final String P_SUBPOPSIZE = "size";  // parameter for number of subpops or pops
    public static final String P_SPECIES = "species";
    public static final String P_RETRIES = "duplicate-retries";
    public static final String P_EXTRA_BEHAVIOR = "extra-behavior";
    public static final String V_TRUNCATE = "truncate";
    public static final String V_WRAP = "wrap";
    public static final String V_FILL = "fill";

    public static final String NUM_INDIVIDUALS_PREAMBLE = "Number of Individuals: ";
    public static final String INDIVIDUAL_INDEX_PREAMBLE = "Individual Number: ";

    public static final int TRUNCATE = 0;
    public static final int WRAP = 1;
    public static final int FILL = 2;
        
    public Parameter defaultBase()
        {
        return ECDefaults.base().push(P_SUBPOPULATION);
        }

    /** Returns an instance of Subpopulation just like it had been before it was
        populated with individuals. You may need to override this if you override
        Subpopulation.   <b>IMPORTANT NOTE</b>: if the size of the array in
        Subpopulation has been changed, then the clone will take on the new array
        size.  This helps some evolution strategies.
    */
    
    public Subpopulation emptyClone()
        {
        try
            {
            Subpopulation p = (Subpopulation)clone();
            p.species = species;  // don't throw it away...maybe this is a bad idea...
            p.individuals = new ArrayList<Individual>();  // empty
            return p;   
            }
        catch (CloneNotSupportedException e) { throw new InternalError(); } // never happens
        }
        
    /** Truncates the Subpopulation to a new size. The Subpopulation is truncated such that 
        the higher indexed individuals may be deleted.  
    */
    public void truncate(int toThis)
        {
        for(int i = individuals.size() - 1; i >= toThis; i--)
            individuals.remove(i);
        assert(individuals.size() == toThis);
        }

    /** Sets all Individuals in the Subpopulation to null, preparing it to be reused. */
    public void clear()
        {
        individuals.clear();
        }

    public void setup(final EvolutionState state, final Parameter base)
        {
        Parameter def = defaultBase();

        // do we load from a file?
        file = base.push(P_FILE);
        loadInds = state.parameters.exists(file,null);
        
        // how big should our subpopulation be?
        // Note that EvolutionState.setup() has similar code, so if you change this, change it there too.
        
        initialSize = state.parameters.getInt(base.push(P_SUBPOPSIZE),def.push(P_SUBPOPSIZE),1);
        if (initialSize<=0)
            state.output.fatal(
                "Subpopulation size must be an integer >= 1.\n",
                base.push(P_SUBPOPSIZE),def.push(P_SUBPOPSIZE));
        
        // what species do we use?

        species = (Species) state.parameters.getInstanceForParameter(
            base.push(P_SPECIES),def.push(P_SPECIES),
            Species.class);
        species.setup(state,base.push(P_SPECIES));

        // How often do we retry if we find a duplicate?
        numDuplicateRetries = state.parameters.getInt(
            base.push(P_RETRIES),def.push(P_RETRIES),0);
        if (numDuplicateRetries < 0) state.output.fatal(
            "The number of retries for duplicates must be an integer >= 0.\n",
            base.push(P_RETRIES),def.push(P_RETRIES));
        
        individuals = new ArrayList<Individual>();
        
        extraBehavior = TRUNCATE;
        if (loadInds)
            {
            String extra = state.parameters.getStringWithDefault(base.push(P_EXTRA_BEHAVIOR), def.push(P_EXTRA_BEHAVIOR), null);
                
            if (extra == null)  // uh oh
                state.output.warning("Subpopulation is reading from a file, but no " + P_EXTRA_BEHAVIOR + 
                    " provided.  By default, subpopulation will be truncated to fit the file size.");
            else if (extra.equalsIgnoreCase(V_TRUNCATE))
                extraBehavior=TRUNCATE;  // duh
            else if (extra.equalsIgnoreCase(V_FILL))
                extraBehavior=FILL;
            else if (extra.equalsIgnoreCase(V_WRAP))
                extraBehavior=WRAP;
            else state.output.fatal("Subpopulation given a bad " + P_EXTRA_BEHAVIOR + ": " + extra,
                base.push(P_EXTRA_BEHAVIOR),def.push(P_EXTRA_BEHAVIOR));
            }
        }



    public void populate(EvolutionState state, int thread)
        {
        int len = initialSize;                         // original length of individual ArrayList
        int start = 0;                          // where to start filling new individuals in -- may get modified if we read some individuals in
        
        // should we load individuals from a file? -- duplicates are permitted
        if (loadInds)
            {
            InputStream stream = state.parameters.getResource(file,null);
            if (stream == null)
                state.output.fatal("Could not load subpopulation from file", file);
            
            try { readSubpopulation(state, new LineNumberReader(new InputStreamReader(stream))); }
            catch (IOException e) { state.output.fatal("An IOException occurred when trying to read from the file " + state.parameters.getString(file, null) + ".  The IOException was: \n" + e,
                    file, null); }
            
            if (len < individuals.size())
                {
                state.output.message("Old subpopulation was of size " + len + ", expanding to size " + individuals.size());
                return;
                }
            else if (len > individuals.size())   // the population was shrunk, there's more space yet
                {
                // What do we do with the remainder?
                if (extraBehavior == TRUNCATE)
                    {
                    state.output.message("Old subpopulation was of size " + len + ", truncating to size " + individuals.size());
                    return;  // we're done
                    }
                else if (extraBehavior == WRAP)
                    {
                    state.output.message("Only " + individuals.size() + " individuals were read in.  Subpopulation will stay size " + len + 
                        ", and the rest will be filled with copies of the read-in individuals.");
                    start = individuals.size();
                    int count = 0;
                    for(int i = start; i < len; ++i)
                        {
                        individuals.add((Individual) individuals.get(count).clone());
                        if(++count >= start) count = 0;
                        }
                    return;
                    }
                else // if (extraBehavior == FILL)
                    {
                    state.output.message("Only " + individuals.size() + " individuals were read in.  Subpopulation will stay size " + len + 
                        ", and the rest will be filled using randomly generated individuals.");
                    
                    // mark the start position for filling in
                    start = individuals.size();
                    // now go on to fill the rest below...
                    }                       
                }
            else // exactly right number, we're done
                {
                return;
                }
            }

        //ACA GENERA LOS INDIVIDUOS RANDOM
        // populating the remainder with random individuals
        HashMap h = null;
        if (numDuplicateRetries >= 1)
            h = new HashMap((len - start) / 2);  // seems reasonable
        System.out.println("COMIENZO DE MOSTRAR LOS INDIVIDUOS ORIGINALES");

        for(int x=start;x<len;x++){
            System.out.println("INDIVIDUO: "+x);
            Individual newInd = null;
            newInd = species.newIndividual(state, thread);
            IntegerVectorSpeciesP1E1 t_spe = (IntegerVectorSpeciesP1E1) newInd.species;
            Tarea[] tareas = t_spe.getTareas();
            Empleado[] empleados = t_spe.getEmpleados();
            /*
            System.out.println("\nEmpleados: ");
            for (Empleado e : empleados) {
                System.out.print((int)e.getId() + " ,");
            }
            System.out.println("\n Tareas: ");
            for (Tarea t : tareas) {
                System.out.print(t.getId() + " ,");
            }
            System.out.println("");
            */


            for(int tries=0; 
                tries <= /* Yes, I see that*/ numDuplicateRetries; 
                tries++)
                {

                    newInd = null;
                    //System.out.println("Sete en null: ");
                    newInd = species.newIndividual(state, thread);
                    newInd= generarIndividuoRRAcotadoPorF(state,tareas, empleados, t_spe ,newInd);
                    evaluoAMano(state,tareas, empleados, t_spe ,newInd);

                    System.out.println("Generado en Species: ");
                    //devuelvo el nuevo individuo
                    newInd.printIndividualForHumans(state,0);

                    //System.out.println("Obtenido en Subpopulation despues del new: "+newInd.genotypeToStringForHumans());
                    //newInd.printIndividualForHumans(state,0);

                    if (numDuplicateRetries >= 1)
                    {
                    // check for duplicates
                    Object o = h.get(newInd);
                    if (o == null) // found nothing, we're safe
                        // hash it and go
                        {
                        h.put(newInd,newInd);
                        break;
                        }
                    }
                }  // oh well, we tried to cut down the duplicates
            individuals.add(newInd);
            //Muestro ppoblacion inicial
//            System.out.println("Obtenido en Subpopulation.");
//            newInd.printIndividualForHumans(state,0);
            }
        System.out.println("TERMINO DE MOSTRAR LOS INDIVIDUOS ORIGINALES");
        }

        private void evaluoAMano(EvolutionState state, Tarea[] tareas, Empleado[] empleados, IntegerVectorSpeciesP1E1 t_spe, Individual newInd) {
            //Evaluo a mano

            int costoProyecto = 0;
            int posMax = 0;

            int[] empleadosPorTarea = ((IntegerVectorIndividualP1E1) newInd).genome;

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
                    System.out.print("Se penaliza   \t|\t El individuo demora "+diasDeTrabajoParaCadaEmpleado[posMax]+"/"+t_spe.getF() +" se agrega costo: "+(t_spe.getHorasTotal()*t_spe.getMaxSueldoReal())+ " a su costo: "+ costoProyecto);
                    costoProyecto+=t_spe.getHorasTotal()*t_spe.getMaxSueldoReal()*(diasDeTrabajoParaCadaEmpleado[posMax]/t_spe.getF());
                    System.out.println(" total: "+costoProyecto);
                } else{
                    System.out.println("NO SE PENALIZA\t|\t El individuo demora "+diasDeTrabajoParaCadaEmpleado[posMax]+"/"+t_spe.getF()+" y tiene costo: "+costoProyecto);
                }
                System.out.println("\tAl individuo: ");



                boolean ideal = diasDeTrabajoParaCadaEmpleado[posMax] <= t_spe.getF(); // factible??
                //ideal = ideal && (((SimpleFitness) ind2.fitness).getMinFitness() > costoProyecto);
                ideal =  ideal && (t_spe.getMaxSueldoReal()*diasDeTrabajoParaCadaEmpleado[posMax]) == costoProyecto;
                //ideal = ideal || state.generation - state.getBestFitnessGeneration() > (int) state.numGenerations/10;
                //System.out.println("state.generation: " + state.generation + ",getBestFitnessGeneration(): " + state.getBestFitnessGeneration() + ", state.numGenerations: " + state.numGenerations);
                //System.out.println("ideal= "+ideal+"  horasDeTrabajoParaCadaEmpleado[posMax]"+ horasDeTrabajoParaCadaEmpleado[posMax]);
                ((SimpleFitness) newInd.fitness).setFitness(state, costoProyecto * (-1), ideal);
                newInd.evaluated = true;

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
            /*
            System.out.println("\n Genotipo: ");
            for (int iGenotipo = 0; iGenotipo < nuevoGenotype.length; iGenotipo++) {
                System.out.print(nuevoGenotype[iGenotipo]+" ");
            }

            System.out.println("");
             */

        }

        private Individual generarIndividuoRRAcotadoPorF(EvolutionState state, Tarea[] tareas, Empleado[] empleados, IntegerVectorSpeciesP1E1 t_spe, Individual newInd) {
            Random r = new Random();
            //int cantEmpleados= ((IntegerVectorIndividualP1E1) newInd).getCantidadEmpleados();
            //int cantTareas= ((IntegerVectorIndividualP1E1) newInd).getCantidadTareas();
            //System.out.println("CantEmpleados: "+cantEmpleados+" CantTareas: "+cantTareas);
            //int[] nuevoGenotype=new int[cantTareas];

            float[] horasEmpleado = new float[empleados.length];
            int iterEmpleados = 0;
            int[] nuevoGenotype=new int[tareas.length];
            int empleadosProbados=0;
            boolean asigne=false;
            ArrayList <Integer> Tareas=new ArrayList<Integer>();
            //System.out.print("\tTareas          :");
            for (int iTareas = 0; iTareas < tareas.length; iTareas++) {
                Tareas.add(iTareas);
                //System.out.print(iTareas);
            }    //System.out.println();
            //System.out.print("\tTareas Sorteadas:");

            //GENERO UN NUEVO GENOTYPE

            //Recorro todas las tareas
            for (int iterTarea = 0; iterTarea < tareas.length; iterTarea++) {
                //Sorteo una tarea aleatoria entre las que quedan
                int indiceTareaAleatoria = r.nextInt(Tareas.size());
                int tareaAleatoria = Tareas.get(indiceTareaAleatoria);
                //System.out.println("Sorteo la tarea: "+ tareaAleatoria);

                //Calculo el empleado con el que quiero probar
                iterEmpleados = (iterTarea + empleadosProbados) % empleados.length;
                //Calculo el esfuerzo de la tarea para el empleado con el que quiero probar
                float esfuerzo = tareas[tareaAleatoria].getEsfuerzo() / ((float) 0.5 + empleados[iterEmpleados].getHabilidad());
                //Calculo si es posiblea agregarle la tarea
                boolean agregar = ((int) Math.ceil(horasEmpleado[iterEmpleados] + esfuerzo / empleados[iterEmpleados].getDedicacion())) <= t_spe.getF();
                //Itero hasta que pueda asignar
                while (!asigne) {
                    //System.out.println("Intento agregar a la tarea "+tareaAleatoria+" el empleado "+iterEmpleados);
                    //Si puedo asignar al empleado actual
                    if (agregar && (empleadosProbados < empleados.length)) {//cumple condicion
                        //asigno tarea, actualizo ezfuerso del empleado,
                        nuevoGenotype[tareaAleatoria] = iterEmpleados;
                        //System.out.println("\tAsigno en "+tareaAleatoria+" el empleado "+iterEmpleados);
                        horasEmpleado[iterEmpleados] += esfuerzo;
                        //Seteo valores para la siguiente tarea
                        empleadosProbados = 0;
                        asigne = true;
                    }
                    //Si ya recorri a todos los empleados, le asigno al primero que probe y fallo
                    else if ((empleadosProbados == empleados.length)) {
                        //asigno tarea, actualizo ezfuerso del empleado,
                        nuevoGenotype[tareaAleatoria] = iterEmpleados;
                        //System.out.println("\tAsigno en "+tareaAleatoria+" el empleado "+iterEmpleados);
                        horasEmpleado[iterEmpleados] += esfuerzo;
                        //Seteo valores para la siguiente tarea
                        empleadosProbados = 0;
                        asigne = true;
                    }
                    //Si me quedan empleados por probar
                    else {
                        //Actualizo empleados probados
                        empleadosProbados++;
                        //Calculo el empleado a probar despues
                        iterEmpleados = (iterTarea + empleadosProbados) % empleados.length;
                        //Actualizo el ezfuerzo de esa tarea
                        esfuerzo = tareas[tareaAleatoria].getEsfuerzo() / ((float) 0.5 + empleados[iterEmpleados].getHabilidad());
                        //Reviso si puedo agregar
                        agregar = ((int) Math.ceil(horasEmpleado[iterEmpleados] + esfuerzo / empleados[iterEmpleados].getDedicacion())) <= t_spe.getF();


                    }
                }
                asigne = false;
                //System.out.println("Elimino la tarea: "+ tareaAleatoria);
                Tareas.remove(indiceTareaAleatoria);
            }
            //Borro el genotype viejo y cargo el nuevo
            ((IntegerVectorIndividualP1E1) newInd).cargarGenotype(state,nuevoGenotype);
            return newInd;
        }


        /** Prints an entire subpopulation in a form readable by humans.
        @deprecated Verbosity no longer has meaning
    */
    public final void printSubpopulationForHumans(final EvolutionState state,
        final int log, 
        final int verbosity)
        {
        printSubpopulationForHumans(state, log);
        }
        
    /** Prints an entire subpopulation in a form readable by humans but also parseable by the computer using readSubpopulation(EvolutionState, LineNumberReader). 
        @deprecated Verbosity no longer has meaning
    */
    public final void printSubpopulation(final EvolutionState state,
        final int log, 
        final int verbosity)
        {
        printSubpopulation(state, log);
        }
        
    /** Prints an entire subpopulation in a form readable by humans, with a verbosity of Output.V_NO_GENERAL. */
    boolean warned = false;
    public void printSubpopulationForHumans(final EvolutionState state,
        final int log)
        {
        state.output.println(NUM_INDIVIDUALS_PREAMBLE + individuals.size(), log);
        for(int i = 0 ; i < individuals.size(); i++)
            {
            state.output.println(INDIVIDUAL_INDEX_PREAMBLE + Code.encode(i), log);
            if (individuals.get(i) != null)
                individuals.get(i).printIndividualForHumans(state, log);
            else if (!warned)
                {
                state.output.warnOnce("Null individuals found in subpopulation");
                warned = true;  // we do this rather than relying on warnOnce because it is much faster in a tight loop
                }
            }
        }
        
    /** Prints an entire subpopulation in a form readable by humans but also parseable by the computer using readSubpopulation(EvolutionState, LineNumberReader) with a verbosity of Output.V_NO_GENERAL. */
    public void printSubpopulation(final EvolutionState state, final int log)
        {
        state.output.println(NUM_INDIVIDUALS_PREAMBLE + Code.encode(individuals.size()), log);
        for(int i = 0 ; i < individuals.size(); i++)
            {
            state.output.println(INDIVIDUAL_INDEX_PREAMBLE + Code.encode(i), log);
            individuals.get(i).printIndividual(state, log);
            }
        }
        
    /** Prints an entire subpopulation in a form readable by humans but also parseable by the computer using readSubpopulation(EvolutionState, LineNumberReader). */
    public void printSubpopulation(final EvolutionState state,
        final PrintWriter writer)
        {
        writer.println(NUM_INDIVIDUALS_PREAMBLE + Code.encode(individuals.size()));
        for(int i = 0 ; i < individuals.size(); i++)
            {
            writer.println(INDIVIDUAL_INDEX_PREAMBLE + Code.encode(i));
            individuals.get(i).printIndividual(state, writer);
            }
        }
    
    /** Reads a subpopulation from the format generated by printSubpopulation(....).  If the number of individuals is not identical, the individuals array will
        be deleted and replaced with a new array, and a warning will be generated as individuals will have to be created using newIndividual(...) rather
        than readIndividual(...).  */
    public void readSubpopulation(final EvolutionState state, final LineNumberReader reader) throws IOException
        {
        // read in number of individuals and check to see if this appears to be a valid subpopulation
        int numIndividuals = Code.readIntegerWithPreamble(NUM_INDIVIDUALS_PREAMBLE, state, reader);

        if (numIndividuals < 1)
            state.output.fatal("On reading subpopulation from text stream, the subpopulation size must be >= 1.  The provided value was: " + numIndividuals + ".");

        // read in individuals
        if (numIndividuals != individuals.size())
            {
            state.output.warnOnce("On reading subpopulation from text stream, the current subpopulation size (" + individuals.size() + " didn't match the number of individuals in the file (" + numIndividuals +
                ") and so the subpopulation size will change.");
            }
                
        individuals = new ArrayList<Individual>();
        for(int i = 0 ; i < numIndividuals; i++)
            {
            int j = Code.readIntegerWithPreamble(INDIVIDUAL_INDEX_PREAMBLE, state, reader);
            // sanity check
            if (j!=i) state.output.warnOnce("On reading subpopulation from text stream, some individual indexes in the subpopulation did not match.  " +
                "The first was individual " + i + ", which is listed in the file as " + j);
            individuals.add(species.newIndividual(state, reader));
            }
        }
        
    /** Writes a subpopulation in binary form, in a format readable by readSubpopulation(EvolutionState, DataInput). */
    public void writeSubpopulation(final EvolutionState state,
        final DataOutput dataOutput) throws IOException
        {
        dataOutput.writeInt(individuals.size());
        for(int i = 0 ; i < individuals.size(); i++)
            individuals.get(i).writeIndividual(state, dataOutput);
        }
    
    /** Reads a subpopulation in binary form, from the format generated by writeSubpopulation(...).  If the number of individuals is not identical, the individuals array will
        be deleted and replaced with a new array, and a warning will be generated as individuals will have to be created using newIndividual(...) rather
        than readIndividual(...) */
    public void readSubpopulation(final EvolutionState state,
        final DataInput dataInput) throws IOException
        {
        int numIndividuals = dataInput.readInt();
        if (numIndividuals != individuals.size())
            {
            state.output.warnOnce("On reading subpopulation from binary stream, the subpopulation size was incorrect.\n" + 
                "Had to resize and use newIndividual() instead of readIndividual().");
            
            individuals = new ArrayList<Individual>();
            for(int i = 0 ; i < numIndividuals; i++)
                individuals.add(species.newIndividual(state, dataInput));
            }
        else for(int i = 0 ; i < individuals.size(); i++)
                 individuals.get(i).readIndividual(state, dataInput);
        }
    }
