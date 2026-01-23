# trabajoGrupal_B2_S15
<img width="755" height="469" alt="image" src="https://github.com/user-attachments/assets/1c4446a1-3a8d-40a9-b015-97170afd97bc" />

# Cargador de Datos CSV a Base de Datos

Este proyecto es una aplicación desarrollada en Scala que realiza la lectura de archivos CSV y persiste la información en una base de datos relacional utilizando el stack tecnológico de Typelevel.

---

## Tecnologías Utilizadas

* Scala 3
* Cats Effect 3 (Manejo de efectos IO)
* FS2 (Procesamiento de flujos de datos)
* Doobie (Acceso a base de datos funcional)
* HikariCP (Pool de conexiones)
* fs2-data-csv (Parseo de CSV con tipado fuerte)

---

## Requisitos Previos

### Base de Datos

Debe existir una tabla llamada estudiantes con la siguiente estructura:

```sql
CREATE TABLE estudiantes (
    nombre VARCHAR(255),
    edad INT,
    calificacion INT,
    genero VARCHAR(50)
);
```
### Archivo de Datos
El sistema busca un archivo CSV en la ruta: src/main/resources/data/estudiantes.csv

El archivo debe incluir cabeceras que coincidan con los atributos de la entidad:
```sql
Fragmento de código
nombre,edad,calificacion,genero
Ejemplo Nombre,20,85,Masculino
```
### Configuración
La aplicación requiere un archivo de configuración en la ruta src/main/resources/application.conf:
```sql
Fragmento de código
db {
  driver = "org.postgresql.Driver"
  url = "jdbc:postgresql://localhost:5432/tu_base_de_datos"
  user = "tu_usuario"
  password = "tu_password"
}
````
## Estructura del Código
Modelo de Datos: Case class Estudiante que mapea los campos del CSV.

Objeto Database: Configura el HikariTransactor como un recurso gestionado.

EstudianteDAO: Implementa la lógica de inserción SQL utilizando doobie.

Main (IOApp): Orquestador que une el stream de lectura con la persistencia.

## Funcionamiento del Proceso
El flujo de ejecución se define de la siguiente manera:

Lectura del archivo mediante FS2 Files.

Decodificación de filas basada en cabeceras (decodeUsingHeaders).

Conversión de los datos a una lista de objetos en memoria.

Inserción transaccional de todos los registros en la base de datos.

## Ejecución
Para iniciar la aplicación, utilice el comando:
```sql
Bash
sbt run
````
## Manejo de Errores
El programa implementa un manejo de errores global mediante el método handleErrorWith. Si ocurre un fallo en la lectura del archivo o en la conexión a la base de datos, se capturará la excepción y se mostrará un mensaje descriptivo en la consola sin interrumpir de forma abrupta el sistema de ejecución.
