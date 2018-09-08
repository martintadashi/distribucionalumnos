#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import sys
import random
import math

try:
    #Sanity check
	if (len(sys.argv)<4):
		print "Ejecutar con la siguiente linea: ./validador <ruta_tareas> <ruta_empleados> <ruta_solucion>"
		sys.exit(1)

	ruta_tareas=sys.argv[1]
	ruta_empleados=sys.argv[2]
	ruta_solucion=sys.argv[3]
	habilidad_empleados=[]
	sueldo_diario_empleado=[]
	dedicacion_diaria_diponible_empleado=[]
	esfuerzo_requerido_tarea=[]
	nombres_tareas=[]
	nombre_empleados=[]
	costo_proyecto=0
	tiempo_solucion_proyecto=0
	
	# se sabe que son 3 lineas
	archivo_tareas=open(ruta_tareas)
	lineasTareas=archivo_tareas.readlines()
	deadLine=lineasTareas[0]
	nombres_tareas=lineasTareas[1].strip().split(" ")
	esfuerzo_requerido_tarea=lineasTareas[2].strip().split(" ")
	
	# se sabe que son 4 lineas
	archivo_empleados=open(ruta_empleados)
	lineasEmpleados=archivo_empleados.readlines()
	nombre_empleados=lineasEmpleados[0].strip().split(" ")
	dedicacion_diaria_diponible_empleado=lineasEmpleados[1].strip().split(" ")
	habilidad_empleados=lineasEmpleados[2].strip().split(" ")
	sueldo_diario_empleado=lineasEmpleados[3].strip().split(" ")
	
	# se convierten a entero
	dedicacion_diaria_diponible_empleado = map(int, dedicacion_diaria_diponible_empleado)
	sueldo_diario_empleado = map(int, sueldo_diario_empleado)
	deadLine=int(deadLine)
	esfuerzo_requerido_tarea = map(int, esfuerzo_requerido_tarea)
	
	# se convierte a float
	habilidad_empleados = map(float, habilidad_empleados)	
		
	# se levanta la solucion del archivo
	matriz_solucion=[]

	archivo_solucion=open(ruta_solucion)
	
	solucion_empleados_tareas=[]
	solucion_empleados=[]
	solucion_tareas=[]
	
	for line in archivo_solucion.readlines():
		empleados_tareas=line.strip().split(" ") 
		if( not (empleados_tareas[0] in nombre_empleados)):
			print "No existe en la instancia el empleado: " + empleados_tareas[0] 
			sys.exit(1)
		solucion_empleados.append(empleados_tareas[0])				
		for i in range(1,len(empleados_tareas)):
			if( not (empleados_tareas[i] in nombres_tareas)):
				print "No existe en la instancia la terea: " + empleados_tareas[i]
				sys.exit(1)
			solucion_tareas.append(empleados_tareas[i])
	

	for i in range(0,len(solucion_empleados)):
		for j in range(i+1,len(solucion_empleados)):
			if(solucion_empleados[i]==solucion_empleados[j]):
				print "Empleado repetido: " + solucion_empleados[i]
				sys.exit(1)
	
	for i in range(0,len(solucion_tareas)):
		for j in range(i+1,len(solucion_tareas)):
			if(solucion_tareas[i]==solucion_tareas[j]):
				print "Tarea asignada más de una vez: " + solucion_tareas[i]
				sys.exit(1)
	
	
	# todas las tareas asignadas
	if(len(solucion_tareas)!=len(nombres_tareas)):
		print "La cantidad de tareas asignadas es distinta a la cantidad de tareas de la instancia."
		sys.exit(1)
	
	for i in range(0,len(nombres_tareas)):
		if( not (nombres_tareas[i] in solucion_tareas)):
			print "Tarea no asignada " + nombres_tareas[i] 
			sys.exit(1)
	
	costo_total=0	
	archivo_solucion=open(ruta_solucion)
	maximo_tiempo=0	
	tiempo_en_dias=0
	# se hacen los calculos con la informacion en los archivos	
	for line in archivo_solucion.readlines():
		empleados_tareas=line.strip().split(" ") 
		indice_empleado = nombre_empleados.index(empleados_tareas[0])
		tiempo_en_horas=0
		for i in range(1,len(empleados_tareas)):
			indice_tarea = nombres_tareas.index(empleados_tareas[i])
			tiempo_en_horas=tiempo_en_horas+(esfuerzo_requerido_tarea[indice_tarea] / (0.5 + habilidad_empleados[indice_empleado]))
		tiempo_en_dias= int(math.ceil(tiempo_en_horas/dedicacion_diaria_diponible_empleado[indice_empleado]))
		costo_total=costo_total+sueldo_diario_empleado[indice_empleado]*tiempo_en_dias
		if(tiempo_en_dias>maximo_tiempo):
			maximo_tiempo=tiempo_en_dias

	if(maximo_tiempo<=deadLine):
		# todo OK!
		print costo_total,maximo_tiempo
	else:
		print "No cumple con el tiempo máximo de finalización ({0} > {1}).".format(maximo_tiempo, deadLine)
		sys.exit(1)

except IOError as error:
    print error
