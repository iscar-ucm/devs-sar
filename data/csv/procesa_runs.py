import os
import pandas as pd

def procesar_archivos_runs_en_directorio(directorio):
    rendimiento_totales = {}

    for root, dirs, files in os.walk(directorio):
        for archivo in files:
            if archivo == 'runs.csv':
                # Obtener el nombre de la carpeta que contiene el archivo
                algoritmo = os.path.basename(os.path.normpath(root))

                ruta_archivo = os.path.join(root, archivo)

                # Cargar el archivo CSV en un DataFrame de pandas
                df = pd.read_csv(ruta_archivo)

                # Filtrar las filas donde la columna "sol" es igual a 1
                sol1_df = df[df['sol'] == 1]

                # Filtrar las columnas que son variables Pareto (incluyendo el tiempo)
                pareto_cols = df.columns[2:]

                # Crear un diccionario para guardar los resultados del algoritmo
                resultados_algoritmo = {'Algoritmo': algoritmo}
                rendimiento_algoritmo = {'Algoritmo': algoritmo}
                
                # Calcular la media, varianza y desviación típica para cada columna Pareto
                for col in pareto_cols:
                    if col.lower() in ['nfz', 'collision']:
                        # Calcular el número de runs en los que el valor del Pareto es distinto de 0
                        rendimiento_algoritmo[col + '_num_runs'] = sol1_df[sol1_df[col] != 0].shape[0]
                        resultados_algoritmo[col] = sol1_df[col].values
                    elif col.lower() == 'myo':
                        # No hacer nada para "myo"
                        pass
                    else:
                        rendimiento_algoritmo[col + '_med'] = sol1_df[col].mean()
                        rendimiento_algoritmo[col + '_var'] = sol1_df[col].var()
                        rendimiento_algoritmo[col + '_std'] = sol1_df[col].std()
                        resultados_algoritmo[col] = sol1_df[col].values

                # Guardar los resultados para este archivo en un diccionario
                rendimiento_totales[ruta_archivo] = rendimiento_algoritmo

                # Guardar el DataFrame en un archivo Excel
                resultados_df = pd.DataFrame(resultados_algoritmo)
                nombre_excel = f"resultados_{algoritmo}.xlsx"
                ruta_excel = os.path.join(root, nombre_excel)
                resultados_df.to_excel(ruta_excel, index=False)

    return rendimiento_totales

# Directorio inicial
directorio_inicial = os.getcwd() + '/spec1/Optimizer/annsim25/annsim25_s3_cy'  # Reemplazar para cada escenario
#directorio_inicial = os.getcwd() + '/spec1/Islands/annsim25/annsim25_s3_mix_full'  # Reemplazar para cada escenario
print(f"Directorio: {directorio_inicial}")

# Llamamos a la función para procesar todos los archivos en el directorio
rendimiento_totales = procesar_archivos_runs_en_directorio(os.path.abspath(directorio_inicial))

# Crear un DataFrame a partir de los resultados
df_resultados = pd.DataFrame.from_dict(rendimiento_totales, orient='index')

# Guardar el DataFrame en un archivo Excel en el directorio inicial
ruta_excel = os.path.join(directorio_inicial, 'rendimiento_algoritmos.xlsx')
df_resultados.to_excel(ruta_excel, index=False)

print(f"Resultados guardados en: {ruta_excel}")
