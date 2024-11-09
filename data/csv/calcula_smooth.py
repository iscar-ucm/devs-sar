import os
import pandas as pd
import numpy as np
import shutil

def eliminar_carpetas_no_01(directorio):
    for root, dirs, _ in os.walk(directorio):
        for d in dirs:
            dir_path = os.path.join(root, d)
            # Verifica si la carpeta no es "01" y está a dos niveles por debajo del directorio inicial
            if d != '01' and len(os.path.relpath(dir_path, directorio).split(os.sep)) == 3:
                try:
                    shutil.rmtree(dir_path)
                    print(f"Carpeta eliminada: {dir_path}")
                except Exception as e:
                    print(f"No se pudo eliminar la carpeta {dir_path}: {e}")

class CntrlSignal:
    def __init__(self, cHeading):
        self.cHeading = cHeading

def calcular_suavizado_cheading(cntrl_signals, heading_inicial):
    smooth_value = 0
    prev_cntrl = cntrl_signals[0].cHeading - heading_inicial
    while prev_cntrl < -180.0:
        prev_cntrl += 360.0
    while prev_cntrl > 180.0:
        prev_cntrl -= 360.0


    for i in range(1, len(cntrl_signals)):
        act_cntrl = cntrl_signals[i].cHeading

        # Calculate angular difference
        act_cntrl -= cntrl_signals[i-1].cHeading
        while act_cntrl < -180.0:
            act_cntrl += 360.0
        while act_cntrl > 180.0:
            act_cntrl -= 360.0

        # Calculate signs
        act_cntrl_sign = 1 if act_cntrl >= 0 else -1
        prev_cntrl_sign = 1 if prev_cntrl >= 0 else -1

        # Check sign change
        if act_cntrl_sign + prev_cntrl_sign == 0:
            smooth_value += (abs(act_cntrl) + abs(prev_cntrl)) ** 2

        prev_cntrl = act_cntrl

    # Round the smooth_value
    smooth_value = round(smooth_value * smooth_factor) / smooth_factor

    return smooth_value

def guardar_smooth_total_en_archivo(smooth_total, uavs_dir):
    archivo_smooth = os.path.join(uavs_dir, 'smooth.csv')
    with open(archivo_smooth, 'w') as f:
        f.write(str(smooth_total))

def buscar_archivos_cntrl_en_directorio(directorio):
    smooth_total = 0  # Inicializa la suma total de smooth_value
    for root, _, _ in os.walk(directorio):
        if 'uavs' in root:  # Solo buscamos en los subdirectorios que contienen 'uavs'
            smooth_total = 0
            for nombre_uav in nombres_uav:
                uav_cntrl_path = os.path.join(root, nombre_uav)
                if os.path.exists(uav_cntrl_path):
                    for subdir, _, files in os.walk(uav_cntrl_path):
                        for archivo in files:
                            if archivo.endswith('Cntrl.csv'):
                                ruta_archivo_cntrl = os.path.join(subdir, archivo)
                                with open(ruta_archivo_cntrl, 'r') as file:
                                    primera_linea = file.readline().strip()
                                    if primera_linea != "CELEVATION,CHEADING,CSPEED,TIME":
                                        print(f"El archivo {archivo} en {subdir} no tiene la estructura esperada en la primera línea.")
                                        continue
                                    cntrl_signals = []
                                    for line in file:
                                        valores = line.strip().split(',')
                                        cntrl_signals.append(CntrlSignal(float(valores[1])))

                                    # Calcular el smooth_value para el UAV actual
                                    smooth_value = calcular_suavizado_cheading(cntrl_signals, valores_iniciales_heading[nombre_uav])
                                    smooth_total += smooth_value  # Suma el smooth_value al total

            # Guarda el smooth_total en el archivo solo después de procesar todos los archivos en 'uavs'
            if smooth_total != 0:
                guardar_smooth_total_en_archivo(smooth_total, root)

def calcular_estadisticas_smooth(directorio_inicial):
    for dirpath, dirs, _ in os.walk(directorio_inicial):
        if dirpath != directorio_inicial:
            valores_smooth = []
            for subdir, _, files in os.walk(dirpath):
                for archivo in files:
                    if archivo.endswith('smooth.csv'):
                        archivo_smooth = os.path.join(subdir, archivo)
                        try:
                            df = pd.read_csv(archivo_smooth, header=None)
                            valores_smooth.extend(df.iloc[:, 0].values)
                        except Exception as e:
                            print(f"No se pudo leer el archivo {archivo_smooth}: {e}")
            if valores_smooth:
                media = np.mean(valores_smooth)
                varianza = np.var(valores_smooth)
                desviacion_estandar = np.std(valores_smooth)
                nombre_directorio = os.path.basename(dirpath)
                ruta_resultados = os.path.join(dirpath, f'{nombre_directorio}_resultados_smooth.txt')
                with open(ruta_resultados, 'w') as archivo_resultados:
                    archivo_resultados.write(f"Media: {media}\n")
                    archivo_resultados.write(f"Varianza: {varianza}\n")
                    archivo_resultados.write(f"Desviación Estándar: {desviacion_estandar}\n")
                print(f"Resultados guardados en: {ruta_resultados}")

# Directorio inicial
directorio_inicial = os.path.join(os.getcwd(), 'spec1', 'Optimizer', 'resultadosCh4', 'smooth' , 'op_s6_cy')  # Reemplazar con tu directorio
print(f"Directorio: {directorio_inicial}")

# Preparar el directorio
eliminar_carpetas_no_01(directorio_inicial)

# Definir nombres de UAVs y valores iniciales de heading
nombres_uav = ["uav_1", "uav_2", "uav_3"]                           # Reemplazar con los nombres de los UAVs del escenario
valores_iniciales_heading = {"uav_1": 90.0, "uav_2": 90.0, "uav_3": 90.0} # Reemplazar con el heading inicial de los UAVs del escenario

# Definir smooth factor
smooth_factor = 0.001  # Remplazar con el factor utilizado para el escenario

# Llamamos a la función para buscar archivos 'Cntrl.csv'
buscar_archivos_cntrl_en_directorio(directorio_inicial)

# Llamamos a la función para calcular la media, varianza y desviación estándar
calcular_estadisticas_smooth(directorio_inicial)
