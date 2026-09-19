# Sistema de Gestión de Garantías y Servicio Técnico - NGO SAECA

Prototipo funcional de un sistema de gestión de servicio técnico y garantías desarrollado para NGO SAECA, como parte del proyecto de análisis de sistemas.

## Características Principales

* **Panel Principal (`index.html`):**
  * Listado de solicitudes con número, cliente, documento, producto, estado y fecha.
  * **Búsqueda por número de documento del cliente:** filtra la tabla mientras se escribe (coincidencia parcial, solo números).
  * **Actualización de estado en tiempo real:** cada solicitud tiene un selector de estado que se guarda directamente en la base de datos.
  * **Detalle de solicitud:** al hacer click en una fila se abre una ventana con el estado actual, una línea de tiempo visual del trámite y los datos del cliente, el producto y el problema reportado.
  * **Eliminación de solicitudes:** botón *Eliminar* por fila, con confirmación previa.
* **Registro de Solicitudes (`nueva-solicitud.html`):** formulario para dar de alta un cliente, un producto y su solicitud técnica de forma enlazada.

### Estados de una solicitud

`RECIBIDA` → `ASIGNADA` → `EN DIAGNÓSTICO` → `FINALIZADA`

Toda solicitud nueva se crea en estado `RECIBIDA`.

### Eliminación de solicitudes

Al eliminar una solicitud también se eliminan sus **asignaciones** y su **historial de seguimiento**, ya que dependen de ella. El cliente, el producto y la garantía asociados **no** se eliminan. La acción no se puede deshacer.

## Tecnologías Utilizadas

- **Backend:** Java 21, Spring Boot 4.1.1 (Spring Data JPA, Spring Web MVC, Validation).
- **Base de Datos:** PostgreSQL, con validación estricta de documentos y números de teléfono.
- **Frontend:** HTML5, CSS3 y JavaScript nativo (Fetch API).
- **Construcción:** Apache Maven (incluye Maven Wrapper).

## Estructura del Proyecto

```
NGO-ANS/
├── db/
│   └── ngo_ans.sql                  # Esquema de la base de datos (y datos iniciales)
├── src/main/
│   ├── java/com/ngo/sistema/
│   │   ├── SistemaApplication.java  # Punto de entrada de Spring Boot
│   │   ├── SistemaController.java   # API REST (/api/...)
│   │   ├── *Repository.java         # Acceso a datos (Cliente, Producto, Solicitud)
│   │   └── *.java                   # Entidades JPA (Cliente, Producto, Solicitud, Garantia,
│   │                                #   Asignacion, Seguimiento, Usuario, Rol, ServicioAutorizado)
│   └── resources/
│       ├── application.properties   # Configuración (lee DB_USER y DB_PASSWORD del entorno)
│       └── static/
│           ├── index.html, app.js               # Panel Principal
│           ├── nueva-solicitud.html, .js        # Registro de solicitudes
│           └── style.css                        # Estilos
├── iniciar.ps1                      # Script de inicio interactivo (Windows / PowerShell)
├── mvnw, mvnw.cmd                   # Maven Wrapper
└── pom.xml
```

## API REST

Todas las rutas cuelgan de `/api` y trabajan con JSON.

| Método | Ruta | Descripción |
| ------ | ---- | ----------- |
| `POST` | `/api/clientes` | Crea un cliente. |
| `POST` | `/api/productos` | Crea un producto. |
| `POST` | `/api/solicitudes` | Crea una solicitud (enlazada a un cliente y un producto existentes). |
| `GET` | `/api/solicitudes` | Lista todas las solicitudes. |
| `GET` | `/api/solicitudes/buscar?documento=123` | Lista las solicitudes cuyo cliente tenga un documento que contenga el valor indicado, de la más reciente a la más antigua. Sin valor devuelve todas. |
| `GET` | `/api/solicitudes/{id}` | Devuelve una solicitud por su número. |
| `PUT` | `/api/solicitudes/{id}/estado` | Actualiza el estado. Cuerpo: `{"estado": "ASIGNADA"}` |
| `DELETE` | `/api/solicitudes/{id}` | Elimina una solicitud (y sus asignaciones y seguimientos). Devuelve `204`, o `404` si no existe. |

### Validaciones

- **Documento del cliente:** solo números.
- **Teléfono del cliente:** solo números, con un signo `+` opcional al inicio.
- **Número de serie del producto:** único; no se puede registrar dos veces el mismo.

## Modelo de Datos

La base de datos incluye las tablas `cliente`, `producto`, `garantia`, `solicitud`, `asignacion`, `seguimiento`, `usuario`, `rol` y `servicio_autorizado`.

La interfaz actual trabaja con `cliente`, `producto` y `solicitud`. Las demás tablas ya existen en el esquema y como entidades, pero todavía no tienen pantallas ni endpoints propios.

---

## Nota Importante sobre el Prototipo y la Base de Datos

Al tratarse de un prototipo local, **este sistema no funcionará automáticamente en otra computadora** si se clona tal cual. Cada integrante debe tener su propia base de datos PostgreSQL local llamada `ngo_saeca`.

Las credenciales no se editan dentro de `src/main/resources/application.properties`. La aplicación lee dos variables de entorno (`DB_USER` y `DB_PASSWORD`), y el script `iniciar.ps1` las solicita de forma interactiva al arrancar el sistema (ver sección siguiente). Esto evita que se compartan credenciales en archivos del proyecto.

---

## Instrucciones para Ejecutar el Proyecto Localmente

### 1. Clonar el Repositorio

```
git clone https://github.com/CJ-518/NGO-ANS.git
cd NGO-ANS
```
> Si preferís usar SSH en lugar de HTTPS:
>
> ```
> git clone git@github.com:CJ-518/NGO-ANS.git
> ```

### 2. Requisitos Previos

- Java JDK 21 (o superior).
- PostgreSQL, con una base de datos local llamada exactamente `ngo_saeca`.

### 3. Configurar la Base de Datos

Importá el esquema SQL ubicado en `db/ngo_ans.sql` dentro de tu base de datos local `ngo_saeca`, usando pgAdmin o la terminal:

```
psql -U postgres -d ngo_saeca -f db/ngo_ans.sql
```

### 4. Ejecutar el Sistema (Script Interactivo)

El proyecto cuenta con un script de inicio automático en PowerShell que solicita tus credenciales locales de forma segura y abre el navegador por vos.

1. Abrí **PowerShell** en la carpeta raíz del proyecto.
2. Ejecutá:

```
.\iniciar.ps1
```

3. Ingresá tu usuario de PostgreSQL (por defecto es `postgres`) y tu contraseña cuando el script te lo pida.

El sistema arrancará de forma silenciosa y abrirá automáticamente tu navegador en <http://localhost:8080>. Para detener el servidor, presioná `Ctrl + C` en la terminal.

> **Alternativa manual (sin script):** si preferís no usar `iniciar.ps1`, podés levantar la aplicación directamente con Maven y definir las variables de entorno antes de iniciar.
>
> En Windows (PowerShell):
>
> ```
> $env:DB_USER = "postgres"
> $env:DB_PASSWORD = "tu_contrasena"
> .\mvnw.cmd spring-boot:run
> ```
>
> En Linux/Mac:
>
> ```
> export DB_USER="postgres"
> export DB_PASSWORD="tu_contrasena"
> ./mvnw spring-boot:run
> ```
>
> En este caso no se editan los valores de `spring.datasource.username` ni `spring.datasource.password` dentro de `application.properties`; simplemente se pasan como variables de entorno.

### 5. Probar el Sistema

Una vez que la terminal indique que la aplicación ha iniciado, abrí tu navegador en:

- **Panel Principal:** <http://localhost:8080>
- **Nueva solicitud:** <http://localhost:8080/nueva-solicitud.html>

Para probar la búsqueda, registrá una solicitud y escribí el número de documento del cliente (o parte de él) en el campo de búsqueda del panel.

## Notas para Desarrollo

- **Reiniciar tras cada cambio:** Maven copia los archivos estáticos (`index.html`, `app.js`, `style.css`, etc.) al arrancar, así que cualquier cambio en `src/` requiere reiniciar la aplicación. Después, recargá el navegador con `Ctrl + F5` para evitar la caché.
- **Esquema de la base de datos:** `spring.jpa.hibernate.ddl-auto=update` hace que Hibernate agregue tablas o columnas nuevas si faltan, pero no reemplaza la importación inicial de `db/ngo_ans.sql`.
- **Logs:** la terminal está configurada para mostrar solo errores críticos. Si algo falla (por ejemplo, un error de PostgreSQL al guardar o eliminar), el detalle aparece ahí.

## Limitaciones Conocidas

- El sistema **no tiene autenticación**: cualquier persona con acceso a la aplicación puede ver, modificar y eliminar solicitudes.
- Cada solicitud nueva crea también un cliente y un producto nuevos; no se reutilizan registros existentes.
- La garantía, la asignación de técnicos y el historial de seguimiento todavía no se gestionan desde la interfaz.