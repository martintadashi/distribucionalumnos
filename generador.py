import sys
import random
import math
from collections import defaultdict

try:
	#Constantes
	mayor_duracion_horas_tarea=200
	menor_duracion_horas_tarea=12
	mayor_disponibilidad_diaria_horas_empleado=10
	menor_disponibilidad_diaria_horas_empleado=1
	sueldo_hora_base_empleado_sin_experiencia=110
	sueldo_valor_maximo_bono_por_buen_empleado_hora=23
	deadline_dias=0
	
	#Sanity check
	if (len(sys.argv)<4):
		print "Ejecutar con la siguiente linea: ./generador <cant_tereas> <cant_empleados> <archivo_salida>"
		sys.exit(1)

	cantidad_tareas=int(sys.argv[1])
	cantidad_empleados=int(sys.argv[2])
	archivo_salida=sys.argv[3]

	if (cantidad_tareas<1):
		print "La cantidad de tareas debe ser mayor o igual a 1."
		sys.exit(1)

	
	if (cantidad_empleados<1):
		print "La cantidad de empleados debe ser mayor o igual a 1."
		sys.exit(1)

	if (cantidad_empleados>cantidad_tareas):
		print "La cantidad de empleados debe ser menor a la cantidad de tareas."
		sys.exit(1)


	#VARIABLES:
	
	#habilidades de los empleados valor entre 0 y 1
	habilidad_empleados=[]	
	sueldo_semanal_empleado=[]	
	dedicacion_diaria_diponible_empleado=[]	
	esfuerzo_requerido_tarea=[]	
	
	for i in range(0,cantidad_empleados):
		dedicacion_diaria_diponible_empleado.append(random.randint(menor_disponibilidad_diaria_horas_empleado,mayor_disponibilidad_diaria_horas_empleado))
	
	for i in range(0,cantidad_empleados):
		habilidad_empleados.append(round(random.uniform(0, 1),2))

	#los empleados cobran un bono (random) de rendimiento
	#empleados con habilidad 0 cobran el sueldo de horas base por la cantidad de horas semanales
	#empleados con habilidad mayor a 0 cobran un plus por habilidad proporcional a su habilidad
	#empleado con habilidad 1 cobra aproximadamente el doble que el de experiencia 0
	for i in range(0,cantidad_empleados):
		bono_por_buen_empleado=random.randint(0,sueldo_valor_maximo_bono_por_buen_empleado_hora)
		sueldo_base=sueldo_hora_base_empleado_sin_experiencia+bono_por_buen_empleado	
		plus_por_experiencia=sueldo_hora_base_empleado_sin_experiencia*habilidad_empleados[i]
		sueldo_semanal_empleado.append(int(math.ceil((sueldo_base+plus_por_experiencia)*dedicacion_diaria_diponible_empleado[i])))
	
	for i in range(0,cantidad_tareas):
		esfuerzo_requerido_tarea.append(random.randint(menor_duracion_horas_tarea,mayor_duracion_horas_tarea))
	
	######################
	#calculo del deadline
	######################

	
	tiempo_ocupado_empleado = {}
	for indice_empleado in range(0,cantidad_empleados):
		tiempo_ocupado_empleado[indice_empleado]=0
	
	tiempo_en_horas=0
	tiempo_en_dias_con_decimales=0
	indice_empleado_menor_tiempo=0
	for indice_tarea in range(0,cantidad_tareas):
		minimo_tiempo=sys.float_info.max
		for indice_empleado in range(0,cantidad_empleados):
			tiempo_en_horas=(esfuerzo_requerido_tarea[indice_tarea] / (0.5 + habilidad_empleados[indice_empleado]))		
			tiempo_en_dias_con_decimales= tiempo_en_horas/dedicacion_diaria_diponible_empleado[indice_empleado]	
			tiempo_en_el_que_termina_si_se_le_da_esta_tarea=tiempo_ocupado_empleado[indice_empleado]+tiempo_en_dias_con_decimales
			if(tiempo_en_el_que_termina_si_se_le_da_esta_tarea < minimo_tiempo):
				indice_empleado_menor_tiempo=indice_empleado
				minimo_tiempo=tiempo_en_el_que_termina_si_se_le_da_esta_tarea
		tiempo_ocupado_empleado[indice_empleado_menor_tiempo]=minimo_tiempo


	deadline_dias=0
	#busca maxima duracion
	for indice_empleado in range(0,cantidad_empleados):
		if(tiempo_ocupado_empleado[indice_empleado]>deadline_dias):
			deadline_dias=tiempo_ocupado_empleado[indice_empleado]
	
	#print tiempo_ocupado_empleado
	#se redondean los dias para pagar el dia entero a los que trabajan menos
	deadline_dias=int(math.ceil(deadline_dias))*1.2
	
	#archivo de salida de empleados
	archivo = open ("%s_empleados"%archivo_salida, "w")
	archivo.write("e%d"%1)
	for i in range(1,cantidad_empleados):
		#nombre del empleado
		archivo.write(" e%d"%(i+1))
	archivo.write("\n")
	archivo.write("%d"%dedicacion_diaria_diponible_empleado[0])	
	for i in range(1,cantidad_empleados):
		archivo.write(" %d"%dedicacion_diaria_diponible_empleado[i])
	archivo.write("\n")	
	archivo.write("%.2f"%habilidad_empleados[0])
	for i in range(1,cantidad_empleados):
		archivo.write(" %.2f"%habilidad_empleados[i])
	archivo.write("\n")	
	archivo.write("%d"%sueldo_semanal_empleado[0])
	for i in range(1,cantidad_empleados):
		archivo.write(" %d"%sueldo_semanal_empleado[i])				
		
	#archivo de salida de tareas
	archivo = open ("%s_tareas"%archivo_salida, "w")
	archivo.write("%d"%deadline_dias)
	archivo.write("\n")
	archivo.write("t%d"%1)
	for i in range(1,cantidad_tareas):
		#nombre de la tarea
		archivo.write(" t%d"%(i+1))
	archivo.write("\n")	
	archivo.write("%d"%esfuerzo_requerido_tarea[0])
	for i in range(1,cantidad_tareas):
		archivo.write(" %d"%esfuerzo_requerido_tarea[i])


	#archivo cantidad de tareas
	archivo = open ("%s_cantidad_tareas"%archivo_salida, "w")
	archivo.write("%d"%cantidad_tareas)	

	#archivo cantidad de trabajos
	archivo = open ("%s_cantidad_empleados"%archivo_salida, "w")
	archivo.write("%d"%cantidad_empleados)	


except IOError as error:
	print error
