# Sistema de Gestión de Garantías y Servicio Técnico — NGO SAECA

Prototipo funcional de un sistema de gestión de servicio técnico y garantías desarrollado para **NGO SAECA**, como parte del proyecto de Análisis de Sistemas.

La aplicación es un backend **Spring Boot** que expone una API REST y sirve un frontend estático (HTML, CSS y JavaScript nativo) sobre una base de datos **PostgreSQL**.

---

## Características principales

- **Panel principal (`index.html`)**
  - Listado de solicitudes recientes con número, cliente, documento, producto, estado y fecha.
  - Contador de solicitudes abiertas.
  - Cambio de estado en línea desde un selector, con persistencia inmediata en la base de datos.
  - Búsqueda de solicitudes por número de documento del cliente.
  - Eliminación de solicitudes (borra en cascada sus asignaciones y su seguimiento).
- **Detalle y seguimiento (modal del panel)**
  - Al hacer clic en una fila se abre el detalle de la solicitud con una **línea de tiempo visual**: Recibida → Asignada → Diagnóstico → Finalizada.
  - Muestra cliente, producto, problema reportado y última actualización.
- **Registro de solicitudes (`nueva-solicitud.html`)**
  - Alta enlazada de cliente, producto y solicitud técnica en un solo formulario.
  - Validación de documento (solo dígitos) y teléfono (dígitos con `+` opcional), tanto en el formulario como en la base de datos.
  - Al seleccionar un producto se consulta automáticamente su garantía vigente y se asocia a la solicitud.

### Estados de una solicitud

`RECIBIDA` → `ASIGNADA` → `EN DIAGNÓSTICO` → `FINALIZADA`

Toda solicitud nueva se crea en estado `RECIBIDA`.

---

## Tecnologías utilizadas

| Capa | Tecnología |
| --- | --- |
| Backend | Java 21, Spring Boot 4.1.1 (Spring Web MVC, Spring Data JPA, Validation) |
| Base de datos | PostgreSQL |
| Frontend | HTML5, CSS3, JavaScript nativo (Fetch API) |
| Construcción | Apache Maven (con wrapper `mvnw` / `mvnw.cmd`) |

---

## Estructura del proyecto

```
NGO-ANS/
├── db/
│   └── ngo_ans.sql                  # Volcado del esquema + datos de ejemplo
├── src/main/java/com/ngo/sistema/
│   ├── SistemaApplication.java      # Punto de entrada
│   ├── SistemaController.java       # API REST (/api)
│   ├── Cliente.java, Producto.java, Solicitud.java, Garantia.java,
│   │   Usuario.java, Rol.java, Asignacion.java, Seguimiento.java,
│   │   ServicioAutorizado.java      # Entidades JPA
│   └── *Repository.java             # Repositorios Spring Data
├── src/main/resources/
│   ├── application.properties       # Conexión a la BD vía variables de entorno
│   └── static/                      # index.html, nueva-solicitud.html, app.js,
│                                    # nueva-solicitud.js, style.css
├── iniciar.ps1                      # Script de arranque interactivo (Windows)
└── pom.xml
```

### Modelo de datos

Tablas: `cliente`, `producto`, `garantia`, `solicitud`, `seguimiento`, `asignacion`, `usuario`, `rol` y `servicio_autorizado`.

---

## API REST

Todos los endpoints cuelgan de `/api`.

| Método | Ruta | Descripción |
| --- | --- | --- |
| `POST` | `/api/clientes` | Registra un cliente |
| `POST` | `/api/productos` | Registra un producto |
| `GET` | `/api/productos` | Lista los productos |
| `GET` | `/api/productos/{id}/garantia` | Devuelve la garantía más reciente del producto (`404` si no tiene) |
| `POST` | `/api/solicitudes` | Crea una solicitud (enlaza la garantía del producto si no se indica) |
| `GET` | `/api/solicitudes` | Lista todas las solicitudes |
| `GET` | `/api/solicitudes/buscar?documento=` | Busca solicitudes por documento del cliente |
| `GET` | `/api/solicitudes/{id}` | Obtiene el detalle de una solicitud |
| `PUT` | `/api/solicitudes/{id}/estado` | Actualiza el estado; cuerpo: `{ "estado": "ASIGNADA" }` |
| `DELETE` | `/api/solicitudes/{id}` | Elimina la solicitud y sus registros relacionados |

---

## Nota importante sobre el prototipo y la base de datos

Al tratarse de un prototipo local, **este sistema no funcionará automáticamente en otra computadora** si se clona tal cual. Cada integrante debe tener su propia base de datos PostgreSQL local llamada `ngo_saeca`.

Las credenciales **no se escriben** dentro de `src/main/resources/application.properties`. La aplicación lee dos variables de entorno (`DB_USER` y `DB_PASSWORD`) y el script `iniciar.ps1` las solicita de forma interactiva al arrancar. Así se evita compartir credenciales en archivos del proyecto.

---

## Instrucciones para ejecutar el proyecto localmente

### 1. Clonar el repositorio

```bash
git clone https://github.com/CJ-518/NGO-ANS.git
cd NGO-ANS
```

> Si preferís SSH:
>
> ```bash
> git clone git@github.com:CJ-518/NGO-ANS.git
> ```

### 2. Requisitos previos

- Java JDK 21 o superior.
- PostgreSQL instalado, con una base de datos local llamada exactamente `ngo_saeca`.
- No hace falta instalar Maven: el proyecto incluye el wrapper (`mvnw` / `mvnw.cmd`).

### 3. Configurar la base de datos

Importá el esquema SQL de `db/ngo_ans.sql` en tu base `ngo_saeca`, usando pgAdmin o la terminal:

```bash
psql -U postgres -d ngo_saeca -f db/ngo_ans.sql
```

El volcado incluye la estructura completa y algunos registros de ejemplo en `cliente`, `producto` y `garantia`.

### 4. Ejecutar el sistema (script interactivo)

El proyecto incluye un script de inicio en PowerShell que pide las credenciales locales y abre el navegador automáticamente.

1. Abrí **PowerShell** en la carpeta raíz del proyecto.
2. Ejecutá:

   ```powershell
   .\iniciar.ps1
   ```

3. Ingresá tu usuario de PostgreSQL (por defecto `postgres`) y la contraseña cuando el script las solicite.

El sistema arranca en modo silencioso y abre el navegador en <http://localhost:8080>. Para detener el servidor, presioná `Ctrl + C` en la terminal.

> **Alternativa manual (sin script).** Definí las variables de entorno y levantá la aplicación con Maven.
>
> Windows (PowerShell):
>
> ```powershell
> $env:DB_USER = "postgres"
> $env:DB_PASSWORD = "tu_contrasena"
> .\mvnw.cmd spring-boot:run
> ```
>
> Linux / macOS:
>
> ```bash
> export DB_USER="postgres"
> export DB_PASSWORD="tu_contrasena"
> ./mvnw spring-boot:run
> ```

### 5. Probar el sistema

Cuando la terminal indique que la aplicación inició, abrí:

- **Panel principal:** <http://localhost:8080>
- **Nueva solicitud:** <http://localhost:8080/nueva-solicitud.html>

---

## Solución de problemas

| Síntoma | Causa probable |
| --- | --- |
| `FATAL: password authentication failed` | Usuario o contraseña de PostgreSQL incorrectos en `DB_USER` / `DB_PASSWORD`. |
| `database "ngo_saeca" does not exist` | Falta crear la base local con ese nombre exacto. |
| `Port 8080 was already in use` | Otro proceso ocupa el puerto; cerralo o cambiá `server.port` en `application.properties`. |
| El panel carga vacío | La base está creada pero sin datos: importá `db/ngo_ans.sql`. |
| No se puede ejecutar `iniciar.ps1` | Política de ejecución de PowerShell: `Set-ExecutionPolicy -Scope Process RemoteSigned`. |

---

## Estado del proyecto

Prototipo académico con fines demostrativos. No incluye autenticación de usuarios ni despliegue en producción; las tablas `usuario`, `rol`, `asignacion` y `servicio_autorizado` están modeladas en la base pero todavía no se gestionan desde la interfaz.