/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.vector;
import ec.*;
import ec.app.p1e1.Empleado;
import ec.app.p1e1.Tarea;
import ec.util.*;

import java.io.*;

/* 
 * IntegerVectorSpeciesP1E1.java
 * 
 * Created: Tue Feb 20 13:26:00 2001
 * By: Sean Luke
 */

/**
 * IntegerVectorSpeciesP1E1 is a subclass of VectorSpecies with special constraints
 * for integral vectors, namely ByteVectorIndividual, ShortVectorIndividual,
 * IntegerVectorIndividualP1E1, and LongVectorIndividual.
 *
 * <p>IntegerVectorSpeciesP1E1 can specify a number of parameters globally, per-segment, and per-gene.
 * See <a href="VectorSpecies.html">VectorSpecies</a> for information on how to this works.
 *
 * <p>IntegerVectorSpeciesP1E1 defines a minimum and maximum gene value.  These values
 * are used during initialization and, depending on whether <tt>mutation-bounded</tt>
 * is true, also during various mutation algorithms to guarantee that the gene value
 * will not exceed these minimum and maximum bounds.
 *
 * <p>
 * IntegerVectorSpeciesP1E1 provides support for two ways of mutating a gene.
 * <ul>
 * <li><b>reset</b> Replacing the gene's value with a value uniformly drawn from the gene's
 * range (the default behavior).</li>
 * <li><b>random-walk</b>Replacing the gene's value by performing a random walk starting at the gene
 * value.  The random walk either adds 1 or subtracts 1 (chosen at random), then does a coin-flip
 * to see whether to continue the random walk.  When the coin-flip finally comes up false, the gene value
 * is set to the current random walk position.
 * </ul>
 *
 * <p>IntegerVectorSpeciesP1E1 performs gene initialization by resetting the gene.
 *
 *
 * <p><b>Parameters</b><br>
 * <table>
 <tr><td valign=top style="white-space: nowrap"><i>base</i>.<tt>min-gene</tt>&nbsp;&nbsp;&nbsp;<i>or</i><br>
 <tr><td valign=top style="white-space: nowrap"><i>base</i>.<tt>segment</tt>.<i>segment-number</i>.<tt>min-gene</tt>&nbsp;&nbsp;&nbsp;<i>or</i><br>
 <tr><td valign=top style="white-space: nowrap"><i>base</i>.<tt>min-gene</tt>.<i>gene-number</i><br>
 * <font size=-1>long (default=0)</font></td>
 * <td valign=top>(the minimum gene value)</td></tr>
 *
 <tr><td>&nbsp;
 <tr><td valign=top style="white-space: nowrap"><i>base</i>.<tt>max-gene</tt>&nbsp;&nbsp;&nbsp;<i>or</i><br>
 <tr><td valign=top style="white-space: nowrap"><i>base</i>.<tt>segment</tt>.<i>segment-number</i>.<tt>max-gene</tt>&nbsp;&nbsp;&nbsp;<i>or</i><br>
 <tr><td valign=top style="white-space: nowrap"><i>base</i>.<tt>max-gene</tt>.<i>gene-number</i><br>
 * <font size=-1>long &gt;= <i>base</i>.min-gene</font></td>
 * <td valign=top>(the maximum gene value)</td></tr>
 *
 <tr><td>&nbsp;
 <tr><td valign=top style="white-space: nowrap"><i>base</i>.<tt>mutation-type</tt>&nbsp;&nbsp;&nbsp;<i>or</i><br>
 <tr><td valign=top style="white-space: nowrap"><i>base</i>.<tt>segment</tt>.<i>segment-number</i>.<tt>mutation-type</tt>&nbsp;&nbsp;&nbsp;<i>or</i><br>
 <tr><td valign=top style="white-space: nowrap"><i>base</i>.<tt>mutation-prob</tt>.<i>gene-number</i><br>
 * <font size=-1><tt>reset</tt> or <tt>random-walk</tt> (default=<tt>reset</tt>)</font></td>
 * <td valign=top>(the mutation type)</td>
 * </tr>
 *
 <tr><td>&nbsp;
 <tr><td valign=top style="white-space: nowrap"><i>base</i>.<tt>random-walk-probability</tt>&nbsp;&nbsp;&nbsp;<i>or</i><br>
 <tr><td valign=top style="white-space: nowrap"><i>base</i>.<tt>segment</tt>.<i>segment-number</i>.<tt>random-walk-probability</tt>&nbsp;&nbsp;&nbsp;<i>or</i><br>
 <tr><td valign=top style="white-space: nowrap"><i>base</i>.<tt>random-walk-probability</tt>.<i>gene-number</i><br>
 <font size=-1>0.0 &lt;= double &lt;= 1.0 </font></td>
 *  <td valign=top>(the probability that a random walk will continue.  Random walks go up or down by 1.0 until the coin flip comes up false.)</td>
 * </tr>
 *
 <tr><td>&nbsp;
 <tr><td valign=top style="white-space: nowrap"><i>base</i>.<tt>mutation-bounded</tt>&nbsp;&nbsp;&nbsp;<i>or</i><br>
 <tr><td valign=top style="white-space: nowrap"><i>base</i>.<tt>segment</tt>.<i>segment-number</i>.<tt>mutation-bounded</tt>&nbsp;&nbsp;&nbsp;<i>or</i><br>
 <tr><td valign=top style="white-space: nowrap"><i>base</i>.<tt>mutation-bounded</tt>.<i>gene-number</i><br>
 *  <font size=-1>boolean (default=true)</font></td>
 *  <td valign=top>(whether mutation is restricted to only being within the min/max gene values.  Does not apply to SimulatedBinaryCrossover (which is always bounded))</td>
 * </tr>
 * </table>
 * @author Sean Luke, Rafal Kicinger
 * @version 1.0
 */

public class IntegerVectorSpeciesP1E1 extends VectorSpeciesP1E1
    {
    public final static String RUTA_TAREAS="ruta-tareas";
    public final static String RUTA_CANT_TAREAS="ruta-cantidad-tareas";
    public final static String RUTA_CANT_EMPLEADOS="ruta-cantidad-empleados";
    public final static String RUTA_EMPLEADOS="ruta-empleados";

    public final static String P_MINGENE = "min-gene";
    public final static String P_MAXGENE = "max-gene";

    public final static String P_NUM_SEGMENTS = "num-segments";

    public final static String P_SEGMENT_TYPE = "segment-type";

    public final static String P_SEGMENT_START = "start";

    public final static String P_SEGMENT_END = "end";

    public final static String P_SEGMENT = "segment";

    public final static String P_MUTATIONTYPE = "mutation-type";

    public final static String P_RANDOM_WALK_PROBABILITY = "random-walk-probability";

    public final static String P_MUTATION_BOUNDED = "mutation-bounded";

    public final static String V_RESET_MUTATION = "reset";

    public final static String V_RANDOM_WALK_MUTATION = "random-walk";

    public final static int C_RESET_MUTATION = 0;

    public final static int C_RANDOM_WALK_MUTATION = 1;

    /** Min-gene value, per gene.
        This array is one longer than the standard genome length.
        The top element in the array represents the parameters for genes in
        genomes which have extended beyond the genome length.  */
    protected long[] minGene;

    /** Max-gene value, per gene.
        This array is one longer than the standard genome length.
        The top element in the array represents the parameters for genes in
        genomes which have extended beyond the genome length.  */
    protected long[] maxGene;


    /** Mutation type, per gene.
        This array is one longer than the standard genome length.
        The top element in the array represents the parameters for genes in
        genomes which have extended beyond the genome length.  */
    protected int[] mutationType;

    /** The continuation probability for Integer Random Walk Mutation, per gene.
        This array is one longer than the standard genome length.
        The top element in the array represents the parameters for genes in
        genomes which have extended beyond the genome length.  */
    protected double[] randomWalkProbability;

    /** Whether mutation is bounded to the min- and max-gene values, per gene.
        This array is one longer than the standard genome length.
        The top element in the array represents the parameters for genes in
        genomes which have extended beyond the genome length.  */
    protected boolean[] mutationIsBounded;

    /** Whether the mutationIsBounded value was defined, per gene.
        Used internally only.
        This array is one longer than the standard genome length.
        The top element in the array represents the parameters for genes in
        genomes which have extended beyond the genome length.  */
    boolean mutationIsBoundedDefined;

    protected int [] empleados_por_tarea;
    protected Empleado[] empleados;
    protected Tarea[] tareas;
    protected int F;

    protected float maxSueldoReal = 0;
    protected float minSueldoReal = 0;
    protected float horasTotal = 0;
    protected int cantTareas = 0;
    protected int cantEmpleados = 0;

    public Empleado[] getEmpleados() {
        return empleados;
    }

    public void setEmpleados(Empleado[] empleados) {
        empleados = empleados;
    }

    public Tarea[] getTareas() {
        return tareas;
    }

    public float getHorasTotal() {
        return horasTotal;
    }

    public void setTareas(Tarea[] tareas) {
        tareas = tareas;
    }

    public int [] getEmpleadosPorCadaTarea(){
        return empleados_por_tarea;
    }

    public int getCantEmpleados() {
        return (empleados == null) ? 0 : empleados.length;
    }

    public int getCantTareas() {
        return (tareas == null) ? 0 : tareas.length;
    }

    public int getF() {
        return F;
    }

    public float getMaxSueldoReal() {
            return maxSueldoReal;
        }

    public float getMinSueldoReal() {
        return minSueldoReal;
    }

    public long maxGene(int gene)
        {
        long[] m = maxGene;
        if (m.length <= gene)
            gene = m.length - 1;
        return m[gene];
        }

    public long minGene(int gene)
        {
        long[] m = minGene;
        if (m.length <= gene)
            gene = m.length - 1;
        return m[gene];
        }

    public int mutationType(int gene)
        {
        int[] m = mutationType;
        if (m.length <= gene)
            gene = m.length - 1;
        return m[gene];
        }

    public double randomWalkProbability(int gene)
        {
        double[] m = randomWalkProbability;
        if (m.length <= gene)
            gene = m.length - 1;
        return m[gene];
        }

    public boolean mutationIsBounded(int gene)
        {
        boolean[] m = mutationIsBounded;
        if (m.length <= gene)
            gene = m.length - 1;
        return m[gene];
        }

    public boolean inNumericalTypeRange(double geneVal)
        {
        if (i_prototype instanceof ByteVectorIndividual)
            return (geneVal <= Byte.MAX_VALUE && geneVal >= Byte.MIN_VALUE);
        else if (i_prototype instanceof ShortVectorIndividual)
            return (geneVal <= Short.MAX_VALUE && geneVal >= Short.MIN_VALUE);
        else if (i_prototype instanceof IntegerVectorIndividualP1E1)
            return (geneVal <= Integer.MAX_VALUE && geneVal >= Integer.MIN_VALUE);
        else if (i_prototype instanceof LongVectorIndividual)
            return true;  // geneVal is valid for all longs
        else return false;  // dunno what the individual is...
        }

    public boolean inNumericalTypeRange(long geneVal)
        {
        if (i_prototype instanceof ByteVectorIndividual)
            return (geneVal <= Byte.MAX_VALUE && geneVal >= Byte.MIN_VALUE);
        else if (i_prototype instanceof ShortVectorIndividual)
            return (geneVal <= Short.MAX_VALUE && geneVal >= Short.MIN_VALUE);
        else if (i_prototype instanceof IntegerVectorIndividualP1E1)
            return (geneVal <= Integer.MAX_VALUE && geneVal >= Integer.MIN_VALUE);
        else if (i_prototype instanceof LongVectorIndividual)
            return true;  // geneVal is valid for all longs
        else return false;  // dunno what the individual is...
        }
    
    public void setup(final EvolutionState state, final Parameter base) {
        Parameter def = defaultBase();

        setupGenome(state, base);

        // create the arrays
        minGene = new long[genomeSize + 1];
        maxGene = new long[genomeSize + 1];
        mutationType = fill(new int[genomeSize + 1], -1);
        mutationIsBounded = new boolean[genomeSize + 1];
        randomWalkProbability = new double[genomeSize + 1];

        // Cargo los empleados y las tareas de los archivos
        if (tareas == null || tareas.length == 0) {

            try {
                String ruta_cant_empleados = state.parameters.getStringWithDefault(base.push(RUTA_CANT_EMPLEADOS), def.push(RUTA_CANT_EMPLEADOS), null);
                //System.out.println("Ruta del archivo de RUTA_CANT_EMPLEADOS: " + ruta_cant_empleados);
                File fin = new File(ruta_cant_empleados);
                FileInputStream fis = new FileInputStream(fin);

                BufferedReader br = new BufferedReader(new InputStreamReader(fis));
                cantEmpleados = Integer.parseInt(br.readLine());
                br.close();
            } catch (Exception e) {
                state.output.fatal(e + ", error al leer el archivo de cant empleados");
            }

            try {
                String ruta_cant_tareas = state.parameters.getStringWithDefault(base.push(RUTA_CANT_TAREAS), def.push(RUTA_CANT_TAREAS), null);
                //System.out.println("Ruta del archivo de RUTA_CANT_TAREAS: " + ruta_cant_tareas);
                File fin = new File(ruta_cant_tareas);
                FileInputStream fis = new FileInputStream(fin);

                BufferedReader br = new BufferedReader(new InputStreamReader(fis));
                cantTareas = Integer.parseInt(br.readLine());
                br.close();
            } catch (Exception e) {
                state.output.fatal(e + ", error al leer el archivo de cant tareas");
            }

            tareas = new Tarea[cantTareas];

            try {
                String ruta_tareas = state.parameters.getStringWithDefault(base.push(RUTA_TAREAS), def.push(RUTA_TAREAS), null);
                //System.out.println("Ruta del archivo RUTA_TAREAS: " + ruta_tareas);
                File fin = new File(ruta_tareas);
                FileInputStream fis = new FileInputStream(fin);

                BufferedReader br = new BufferedReader(new InputStreamReader(fis));

                // 1er linea
                F = Integer.parseInt(br.readLine());


                // 2da linea
                int index = 0;
                String[] line_tokens = br.readLine().split(" ");
                for (String id : line_tokens) {
                    tareas[index] = new Tarea(Integer.parseInt((String) id.substring(1,id.length())));
                    index++;
                }

                // 3er linea
                index = 0;
                line_tokens = br.readLine().split(" ");
                for (String dias : line_tokens) {
                    tareas[index].setEsfuerzo(Integer.parseInt(dias));
                    index++;
                }

                br.close();
                for (Tarea t:tareas){
                    horasTotal+=t.getEsfuerzo();
                }
            } catch (Exception e) {
                state.output.fatal(e + ", error al leer el archivo de tareas");
            }
        }

        if (empleados == null || empleados.length == 0) {
            empleados = new Empleado[cantEmpleados];
            try {
                String ruta_empleados = state.parameters.getStringWithDefault(base.push(RUTA_EMPLEADOS), def.push(RUTA_EMPLEADOS), null);
                //System.out.println("Ruta del archivo RUTA_EMPLEADOS: " +ruta_empleados);
                File fin = new File(ruta_empleados);
                FileInputStream fis = new FileInputStream(fin);

                BufferedReader br = new BufferedReader(new InputStreamReader(fis));

                // 1er linea
                int index = 0;
                String[] line_tokens = br.readLine().split(" ");
                for (String id : line_tokens) {
                    empleados[index] = new Empleado(Integer.parseInt((String) id.substring(1,id.length())));
                    index++;
                }

                // 2da linea
                index = 0;
                line_tokens = br.readLine().split(" ");
                for (String dedicacion : line_tokens) {
                    empleados[index].setDedicacion(Integer.parseInt(dedicacion));
                    index++;
                }

                // 3er linea
                index = 0;
                line_tokens = br.readLine().split(" ");
                for (String habilidad : line_tokens) {
                    empleados[index].setHabilidad(Float.parseFloat(habilidad));
                    index++;
                }

                // 4ta linea
                index = 0;
                line_tokens = br.readLine().split(" ");
                for (String sueldo : line_tokens) {
                    empleados[index].setSueldo(Integer.parseInt(sueldo));
                    index++;
                }

                br.close();
                float sueldoReal;
                for (int iEmpleados = 0; iEmpleados < empleados.length; iEmpleados++) {
                    sueldoReal= empleados[iEmpleados].getSueldo()/((0.5F+empleados[iEmpleados].getHabilidad())*empleados[iEmpleados].getDedicacion());
                    //System.out.println("Sueldo real empleado "+iEmpleados+ ": "+sueldoReal);
                    if (sueldoReal> maxSueldoReal){
                        maxSueldoReal=sueldoReal;
                    }
                    if (sueldoReal< minSueldoReal){
                        minSueldoReal=sueldoReal;
                    }
                }

            } catch (Exception e) {
                state.output.fatal(e + ", error al leer el archivo de empleados");
            }
        }
        

        // LOADING GLOBAL MIN/MAX GENES
        long _minGene = 0;
        long _maxGene = cantEmpleados-1;
        if (_maxGene < _minGene)
            state.output.fatal("IntegerVectorSpeciesP1E1 must have a default min-gene which is <= the default max-gene",
                base.push(P_MAXGENE),def.push(P_MAXGENE));
        fill(minGene, _minGene);
        fill(maxGene, _maxGene);


        /// MUTATION
        
        
        String mtype = state.parameters.getStringWithDefault(base.push(P_MUTATIONTYPE), def.push(P_MUTATIONTYPE), null);
        int _mutationType = C_RESET_MUTATION;
        if (mtype == null)
            state.output.warning("No global mutation type given for IntegerVectorSpeciesP1E1, assuming 'reset' mutation",
                base.push(P_MUTATIONTYPE), def.push(P_MUTATIONTYPE));
        else if (mtype.equalsIgnoreCase(V_RESET_MUTATION))
            _mutationType = C_RESET_MUTATION; // redundant
        else if (mtype.equalsIgnoreCase(V_RANDOM_WALK_MUTATION))
            _mutationType = C_RANDOM_WALK_MUTATION;
        else
            state.output.fatal("IntegerVectorSpeciesP1E1 given a bad mutation type: "
                + mtype, base.push(P_MUTATIONTYPE), def.push(P_MUTATIONTYPE));
        fill(mutationType, _mutationType);

        if (_mutationType == C_RANDOM_WALK_MUTATION)
            {
            double _randomWalkProbability = state.parameters.getDoubleWithMax(base.push(P_RANDOM_WALK_PROBABILITY),def.push(P_RANDOM_WALK_PROBABILITY), 0.0, 1.0);
            if (_randomWalkProbability <= 0)
                state.output.fatal("If it's going to use random walk mutation as its global mutation type, IntegerVectorSpeciesP1E1 must a random walk mutation probability between 0.0 and 1.0.",
                    base.push(P_RANDOM_WALK_PROBABILITY), def.push(P_RANDOM_WALK_PROBABILITY));
            fill(randomWalkProbability, _randomWalkProbability);

            if (!state.parameters.exists(base.push(P_MUTATION_BOUNDED), def.push(P_MUTATION_BOUNDED)))
                state.output.warning("IntegerVectorSpeciesP1E1 is using gaussian, polynomial, or integer randomwalk mutation as its global mutation type, but " + P_MUTATION_BOUNDED + " is not defined.  Assuming 'true'");
            boolean _mutationIsBounded = state.parameters.getBoolean(base.push(P_MUTATION_BOUNDED), def.push(P_MUTATION_BOUNDED), true);
            fill(mutationIsBounded, _mutationIsBounded);
            mutationIsBoundedDefined = true;
            }


        super.setup(state, base);


        // VERIFY
        for(int x=0; x< genomeSize + 1; x++)
            {
            if (maxGene[x] < minGene[x])
                state.output.fatal("IntegerVectorSpeciesP1E1 must have a min-gene["+x+"] which is <= the max-gene["+x+"]");
            
            // check to see if these longs are within the data type of the particular individual
            if (!inNumericalTypeRange(minGene[x]))
                state.output.fatal("This IntegerVectorSpeciesP1E1 has a prototype of the kind: " 
                    + i_prototype.getClass().getName() +
                    ", but doesn't have a min-gene["+x+"] value within the range of this prototype's genome's data types");
            if (!inNumericalTypeRange(maxGene[x]))
                state.output.fatal("This IntegerVectorSpeciesP1E1 has a prototype of the kind: " 
                    + i_prototype.getClass().getName() +
                    ", but doesn't have a max-gene["+x+"] value within the range of this prototype's genome's data types");
            }

                
 
            
        /*
        //Debugging
        for(int i = 0; i < minGene.length; i++)
        System.out.println("Min: " + minGene[i] + ", Max: " + maxGene[i]);
        */
        }



    protected void loadParametersForGene(EvolutionState state, int index, Parameter base, Parameter def, String postfix)
        {       
        super.loadParametersForGene(state, index, base, def, postfix);
        
        boolean minValExists = state.parameters.exists(base.push(P_MINGENE).push(postfix), def.push(P_MINGENE).push(postfix));
        boolean maxValExists = state.parameters.exists(base.push(P_MAXGENE).push(postfix), def.push(P_MAXGENE).push(postfix));
        
        if ((maxValExists && !(minValExists)))
            state.output.warning("Max Gene specified but not Min Gene", base.push(P_MINGENE).push(postfix), def.push(P_MINGENE).push(postfix));
                
        if ((minValExists && !(maxValExists)))
            state.output.warning("Min Gene specified but not Max Gene", base.push(P_MAXGENE).push(postfix), def.push(P_MINGENE).push(postfix));

        if (minValExists)
            {        
            long minVal = state.parameters.getLongWithDefault(base.push(P_MINGENE).push(postfix), def.push(P_MINGENE).push(postfix), 0);

            //check if the value is in range
            if (!inNumericalTypeRange(minVal))
                state.output.error("Min Gene Value out of range for data type " + i_prototype.getClass().getName(),
                    base.push(P_MINGENE).push(postfix), 
                    base.push(P_MINGENE).push(postfix));
            else minGene[index] = minVal;

            if (dynamicInitialSize)
                state.output.warnOnce("Using dynamic initial sizing, but per-gene or per-segment min-gene declarations.  This is probably wrong.  You probably want to use global min/max declarations.",
                    base.push(P_MINGENE).push(postfix), 
                    base.push(P_MINGENE).push(postfix));
            }
            
        if (maxValExists)
            {
            long maxVal = state.parameters.getLongWithDefault(base.push(P_MAXGENE).push(postfix), def.push(P_MAXGENE).push(postfix), 0);
                
            //check if the value is in range
            if (!inNumericalTypeRange(maxVal))
                state.output.error("Max Gene Value out of range for data type " + i_prototype.getClass().getName(),
                    base.push(P_MAXGENE).push(postfix), 
                    base.push(P_MAXGENE).push(postfix));
            else maxGene[index] = maxVal;

            if (dynamicInitialSize)
                state.output.warnOnce("Using dynamic initial sizing, but per-gene or per-segment max-gene declarations.  This is probably wrong.  You probably want to use global min/max declarations.",
                    base.push(P_MAXGENE).push(postfix), 
                    base.push(P_MAXGENE).push(postfix));
            }


        /// MUTATION

        String mtype = state.parameters.getStringWithDefault(base.push(P_MUTATIONTYPE).push(postfix), def.push(P_MUTATIONTYPE).push(postfix), null);
        int mutType = -1;
        if (mtype == null) { }  // we're cool
        else if (mtype.equalsIgnoreCase(V_RESET_MUTATION))
            mutType = mutationType[index] = C_RESET_MUTATION; 
        else if (mtype.equalsIgnoreCase(V_RANDOM_WALK_MUTATION))
            {
            mutType = mutationType[index] = C_RANDOM_WALK_MUTATION;
            state.output.warnOnce("Integer Random Walk Mutation used in IntegerVectorSpeciesP1E1.  Be advised that during initialization these genes will only be set to integer values.");
            }
        else
            state.output.error("IntegerVectorSpeciesP1E1 given a bad mutation type: " + mtype, 
                base.push(P_MUTATIONTYPE).push(postfix), def.push(P_MUTATIONTYPE).push(postfix));


        if (mutType == C_RANDOM_WALK_MUTATION)
            {
            if (state.parameters.exists(base.push(P_RANDOM_WALK_PROBABILITY).push(postfix),def.push(P_RANDOM_WALK_PROBABILITY).push(postfix)))
                {
                randomWalkProbability[index] = state.parameters.getDoubleWithMax(base.push(P_RANDOM_WALK_PROBABILITY).push(postfix),def.push(P_RANDOM_WALK_PROBABILITY).push(postfix), 0.0, 1.0);
                if (randomWalkProbability[index] <= 0)
                    state.output.error("If it's going to use random walk mutation as a per-gene or per-segment type, IntegerVectorSpeciesP1E1 must a random walk mutation probability between 0.0 and 1.0.",
                        base.push(P_RANDOM_WALK_PROBABILITY).push(postfix), def.push(P_RANDOM_WALK_PROBABILITY).push(postfix));
                }
            else
                state.output.error("If IntegerVectorSpeciesP1E1 is going to use polynomial mutation as a per-gene or per-segment type, either the global or per-gene/per-segment random walk mutation probability must be defined.",
                    base.push(P_RANDOM_WALK_PROBABILITY).push(postfix), def.push(P_RANDOM_WALK_PROBABILITY).push(postfix));

            if (state.parameters.exists(base.push(P_MUTATION_BOUNDED).push(postfix), def.push(P_MUTATION_BOUNDED).push(postfix)))
                {
                mutationIsBounded[index] = state.parameters.getBoolean(base.push(P_MUTATION_BOUNDED).push(postfix), def.push(P_MUTATION_BOUNDED).push(postfix), true);
                }
            else if (!mutationIsBoundedDefined)
                state.output.fatal("If IntegerVectorSpeciesP1E1 is going to use gaussian, polynomial, or integer random walk mutation as a per-gene or per-segment type, the mutation bounding must be defined.",
                    base.push(P_MUTATION_BOUNDED).push(postfix), def.push(P_MUTATION_BOUNDED).push(postfix));

            }           
        }



    
    }

