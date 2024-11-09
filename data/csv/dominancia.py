import os
import pandas as pd
import numpy as np

def leer_resultados_primera_solucion(directorio):
    """
    Lee los archivos Excel resultados_NOMBRE_ALGORITMO.xlsx en el directorio especificado.
    
    Args:
        directorio (str): Ruta del directorio que contiene los archivos Excel.
        
    Returns:
        dict: Diccionario que contiene los DataFrames de los resultados por algoritmo.
    """
    resultados_por_algoritmo = {}
    
    for archivo in os.listdir(directorio):
        if archivo.startswith("resultados_") and archivo.endswith(".xlsx"):
            algoritmo = archivo.replace("resultados_", "").replace(".xlsx", "")
            ruta_archivo = os.path.join(directorio, archivo)
            df = pd.read_excel(ruta_archivo)
            resultados_por_algoritmo[algoritmo] = df
    
    return resultados_por_algoritmo

def determinar_dominancia(iteracion_i, iteracion_j):
    """
    Determina si una iteración de un algoritmo domina a otra iteración de otro algoritmo.
    Devuelve el resultado de la dominancia: "domina", "igual", o "dominado".
    """
    # Obtener las columnas comunes que no son 'Algoritmo' ni 'Time'
    columnas_comunes = set(iteracion_i.index) & set(iteracion_j.index) - {'Algoritmo', 'time'}
    
    # Contadores para determinar la relación de dominancia
    dominancia_i = 0
    dominancia_j = 0
    
    for col in columnas_comunes:
        if col == 'pd':
            if iteracion_i[col] > iteracion_j[col]:
                dominancia_i += 1
            elif iteracion_i[col] < iteracion_j[col]:
                dominancia_j += 1
        else:
            if iteracion_i[col] < iteracion_j[col]:
                dominancia_i += 1
            elif iteracion_i[col] > iteracion_j[col]:
                dominancia_j += 1
    
    if dominancia_i > 0 and dominancia_j == 0:
        return "domina"
    elif dominancia_i == 0 and dominancia_j > 0:
        return "dominado"
    else:
        return "igual"

def comparar_resultados(resultados_totales):
    """
    Compara los resultados de cada algoritmo entre sí y construye dos tablas de dominancia:
    1. Conteos totales de dominancia de cada algoritmo sobre el otro.
    2. Detalles de dominancia iteración a iteración.
    """
    algoritmos = list(resultados_totales.keys())
    num_algoritmos = len(algoritmos)
    
    # Tabla de conteos totales de dominancia
    tabla_conteos_dominancia = pd.DataFrame(np.zeros((num_algoritmos, num_algoritmos), dtype=int), columns=algoritmos, index=algoritmos)
    
    # Tabla de detalles de dominancia iteración a iteración
    detalles_dominancia = {}

    for i, nombre_algoritmo_i in enumerate(algoritmos):
        df_i = resultados_totales[nombre_algoritmo_i]
        detalles_dominancia[nombre_algoritmo_i] = {}
        for j, nombre_algoritmo_j in enumerate(algoritmos):
            if i == j:
                continue
            df_j = resultados_totales[nombre_algoritmo_j]
            num_iteraciones = min(len(df_i), len(df_j))
            dominancias = [determinar_dominancia(df_i.iloc[k].dropna(), df_j.iloc[k].dropna()) for k in range(num_iteraciones)]
            
            # Guardar detalles de dominancia iteración a iteración
            detalles_dominancia[nombre_algoritmo_i][nombre_algoritmo_j] = dominancias
            
            # Actualizar tabla de conteos totales de dominancia
            tabla_conteos_dominancia.loc[nombre_algoritmo_i, nombre_algoritmo_j] = sum(1 for d in dominancias if d == "domina")

    return tabla_conteos_dominancia, detalles_dominancia

# Directorio inicial
directorio_inicial = os.getcwd() + '/spec1/Optimizer/resultadosCh4/dominancia' # Reemplazar con el directorio deseado
print(f"Directorio inicial: {directorio_inicial}")

# Llamar a la función para leer los archivos Excel en el directorio
resultados_totales = leer_resultados_primera_solucion(directorio_inicial)

# Llamar a la función para comparar resultados
tabla_conteos_dominancia, detalles_dominancia = comparar_resultados(resultados_totales)

# Guardar la tabla de conteos totales de dominancia en un archivo Excel
ruta_excel_conteos_dominancia = os.path.join(directorio_inicial, 'tabla_conteos_dominancia.xlsx')
tabla_conteos_dominancia.to_excel(ruta_excel_conteos_dominancia, index=True)
print(f"Tabla de conteos totales de dominancia guardada en: {ruta_excel_conteos_dominancia}")

# Guardar los detalles de dominancia iteración a iteración en archivos Excel
for nombre_algoritmo_i, detalles_algoritmo in detalles_dominancia.items():
    df_detalles_algoritmo = pd.DataFrame(detalles_algoritmo)
    ruta_excel_detalles = os.path.join(directorio_inicial, f'detalles_dominancia_{nombre_algoritmo_i}.xlsx')
    df_detalles_algoritmo.to_excel(ruta_excel_detalles, index=False)
    print(f"Detalles de dominancia para {nombre_algoritmo_i} guardados en: {ruta_excel_detalles}")