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
Al tratarse de un prototipo local, **este sistema no funcionará automáticamente en otra computadora** si se clona tal cual. Cada integrante debe tener su propia base de datos PostgreSQL local llamada `ngo_saeca`. Las credenciales ya no se editan manualmente en el archivo de configuración: el script `iniciar.ps1` las solicita de forma interactiva al arrancar el sistema (ver sección siguiente).

---

## Instrucciones para Ejecutar el Proyecto Localmente

Para levantar este proyecto en tu computadora de forma automática, sigue los siguientes pasos:

### 1. Requisitos Previos
* Tener instalado Java JDK 21 (o superior).
* Tener instalado PostgreSQL y crear una base de datos local llamada exactamente `ngo_saeca`.

### 2. Configurar la Base de Datos
* **Estructura:** Importa el esquema SQL ubicado en la ruta `db/ngo_ans.sql` dentro de tu base de datos local `ngo_saeca` usando pgAdmin o tu terminal preferida.

### 3. Ejecutar el Sistema (Script Interactivo)
El proyecto cuenta con un script de inicio automático en PowerShell que te solicitará tus credenciales locales de forma segura y abrirá el navegador por ti.

1. Abre **PowerShell** en la carpeta raíz del proyecto.
2. Ejecuta el siguiente comando:
   ```powershell
   .\iniciar.ps1
   ```
3. Ingresa tu usuario de PostgreSQL (por defecto es `postgres`) y tu contraseña cuando el script te lo solicite.

El sistema arrancará de forma silenciosa y abrirá automáticamente tu navegador web en [http://localhost:8080](http://localhost:8080). Para detener el servidor, simplemente presiona `Ctrl + C` en la terminal.

> **Alternativa manual (sin script):** si preferís no usar `iniciar.ps1`, también podés levantar la aplicación directamente con Maven — en Windows: `.\mvnw.cmd spring-boot:run`; en Linux/Mac: `./mvnw spring-boot:run`. En ese caso deberás configurar manualmente `spring.datasource.username` y `spring.datasource.password` en `src/main/resources/application.properties`.

### 4. Probar el Sistema

Una vez que la terminal indique que la aplicación ha iniciado, abre tu navegador web e ingresa a:

* **Panel Principal (Administración):** [http://localhost:8080](http://localhost:8080)