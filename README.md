# Sistema de Gestión de Garantías y Servicio Técnico - NGO SAECA

Este repositorio contiene el prototipo funcional de sistema de gestión de servicio técnico y garantías desarrollado para NGO SAECA, como parte del proyecto de análisis de sistemas.

## Características Principales
* **Panel Principal (`index.html`):** Visualización de solicitudes recientes y actualización en tiempo real del estado de los tickets con persistencia directa en la base de datos.
* **Registro de Solicitudes (`nueva-solicitud.html`):** Formulario para dar de alta nuevos clientes, productos y solicitudes técnicas de forma enlazada.
* **Portal de Seguimiento (`seguimiento.html`):** Vista orientada al cliente con una línea de tiempo visual que muestra el estado actual de su trámite.

## Tecnologías Utilizadas
* **Backend:** Java, Spring Boot (Spring Data JPA, Spring Web, Validation).
* **Base de Datos:** PostgreSQL con validación estricta de documentos y números de teléfono.
* **Frontend:** HTML5, CSS3 y JavaScript nativo (Fetch API).
* **Construcción:** Apache Maven.

---

## Nota Importante sobre el Prototipo y la Base de Datos
Al tratarse de un prototipo local, **este sistema no funcionará automáticamente en otra computadora** si se clona tal cual. El archivo de configuración contiene las credenciales de PostgreSQL específicas de la máquina donde fue creado. Para ejecutarlo en otro equipo, cada integrante deberá configurar su propio usuario y contraseña de base de datos en dicho archivo.

---

## Instrucciones para Ejecutar el Proyecto Localmente

Para levantar este proyecto en tu computadora, sigue los siguientes pasos:

### 1. Requisitos Previos
* Tener instalado Java JDK 21 (o superior).
* Tener instalado PostgreSQL y crear una base de datos local llamada exactamente `ngo_saeca`.

### 2. Configurar la Base de Datos y Credenciales
* **Estructura de la Base de Datos:** En este repositorio encontrarás el esquema de la base de datos exportado en formato plano SQL dentro de la ruta `db/ngo_ans.sql`. Importa este archivo en tu base de datos local `ngo_saeca` usando tu herramienta preferida (pgAdmin o terminal).
* **Configurar Credenciales:** Abre el archivo de configuración ubicado exactamente en la ruta `src/main/resources/application.properties` y modifica los valores de usuario y contraseña de PostgreSQL (`spring.datasource.username` y `spring.datasource.password`) para que coincidan con las credenciales locales de tu propia computadora.

### 3. Ejecutar la Aplicación
Abre una terminal (PowerShell o CMD) en la carpeta raíz del proyecto y ejecuta el comando correspondiente según tu sistema operativo:

* **En Windows (PowerShell):**
  ```powershell
  .\mvnw.cmd spring-boot:run
* **En Linux / Mac:**
  ```bash
  ./mvnw spring-boot:run
  ```



### 4. Probar el Sistema

Una vez que la terminal indique que la aplicación ha iniciado, abre tu navegador web e ingresa a:

* **Panel Principal (Administración):** [http://localhost:8080](http://localhost:8080)
* **Portal de Seguimiento (Cliente):** [http://localhost:8080/seguimiento.html](http://localhost:8080/seguimiento.html)
