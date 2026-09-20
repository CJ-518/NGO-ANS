# Sistema de Gestión de Garantías y Servicio Técnico — NGO SAECA

Prototipo funcional de un sistema de gestión de servicio técnico y garantías desarrollado para NGO SAECA, como parte del proyecto de Análisis de Sistemas.

La aplicación es un backend Spring Boot que expone una API REST protegida con Spring Security y sirve un frontend estático (HTML, CSS y JavaScript nativo) sobre una base de datos PostgreSQL.

---

## Características principales

- Inicio de sesión y roles (`login.html`)
  * Acceso con correo y contraseña; las contraseñas se guardan cifradas con BCrypt.
  * Tres roles con permisos distintos: `ADMINISTRADOR`, `FUNCIONARIO` y `TECNICO`.
  * Sin sesión iniciada no se puede abrir ninguna página ni consumir la API: el sistema redirige al login.
- Panel principal (`index.html`)
  * Listado de solicitudes recientes con número, cliente, documento, producto, estado y fecha.
  * Contador de solicitudes abiertas.
  * Búsqueda de solicitudes por número de documento del cliente (coincidencia parcial, mientras se escribe).
  * Cambio de estado en línea desde un selector, con persistencia inmediata en la base de datos.
  * Eliminación de solicitudes, con confirmación (borra en cascada sus asignaciones y su seguimiento).
  * Los botones y acciones disponibles dependen del rol del usuario.
- Detalle y seguimiento (modal del panel)
  * Al hacer clic en una fila se abre el detalle de la solicitud con una línea de tiempo visual: Recibida → Asignada → Diagnóstico → Finalizada.
  * Muestra cliente, producto, problema reportado y última actualización.
- Registro de solicitudes (`nueva-solicitud.html`)
  * Alta enlazada de cliente y solicitud técnica en un solo formulario.
  * El producto se elige entre los productos registrados en la base de datos; el formulario no crea productos.
  * Sugerencias en cascada: tipo de producto, marca, modelo y número de serie ofrecen sugerencias que se filtran entre sí, y al elegir un número de serie (o cuando queda un único producto posible) se completan los demás campos.
  * Al elegir un producto se muestra su garantía: fecha de venta, fecha de vencimiento y si está en garantía o vencida. Con la garantía vencida igualmente se puede registrar la solicitud. La garantía del producto se asocia automáticamente a la solicitud.
  * Validación de documento (solo dígitos) y teléfono (dígitos con `+` opcional), tanto en el formulario como en la base de datos.
- Administración de usuarios (`usuarios.html`)
  * Acceso exclusivo para `ADMINISTRADOR`: la interfaz oculta el botón Usuarios a los demás roles y el servidor rechaza `/api/usuarios/**` y `/usuarios.html` / `/usuarios.js` a quien no tenga ese rol.
  * Permite dar de alta usuarios con rol `FUNCIONARIO` o `TECNICO` (el rol `ADMINISTRADOR` no se crea desde la interfaz).
  * El formulario valida nombre, formato de correo y que la contraseña tenga al menos 8 caracteres, además de confirmar que las dos contraseñas ingresadas coincidan.
  * La contraseña se guarda cifrada con BCrypt; el correo debe ser único.
  * Muestra una tabla con nombre, correo, rol y estado de todos los usuarios registrados.

### Garantías

La garantía de un producto empieza el día en que fue vendido: `garantia.fecha_inicio` es la fecha de venta y `garantia.fecha_fin` es su vencimiento. La interfaz determina si una garantía está vigente comparando `fecha_fin` con la fecha actual; no depende de la columna `estado` de la tabla, que con el tiempo podría quedar desactualizada.

### Estados de una solicitud

`RECIBIDA` → `ASIGNADA` → `EN DIAGNÓSTICO` → `FINALIZADA`

Toda solicitud nueva se crea en estado `RECIBIDA`.

---

## Roles y permisos

Los permisos se aplican tanto en la interfaz como en el servidor.

| Acción | `ADMINISTRADOR` | `FUNCIONARIO` | `TECNICO` |
| ------- | ---------------- | -------------- | --------- |
| Ver el panel, buscar y ver el detalle de solicitudes | ✔ | ✔ | ✔ |
| Consultar productos y garantías | ✔ | ✔ | ✔ |
| Registrar solicitudes nuevas (y sus clientes) | ✔ | ✔ | ✘ |
| Cambiar el estado de una solicitud | ✔ | ✔ | ✘ |
| Eliminar solicitudes | ✔ | ✘ | ✘ |
| Administrar usuarios (crear `FUNCIONARIO` / `TECNICO`) | ✔ | ✘ | ✘ |

Un `TECNICO` ve el estado de las solicitudes como texto fijo y no ve el botón Nueva solicitud. Solo un `ADMINISTRADOR` ve el botón Usuarios en el panel y puede acceder a `usuarios.html` / `/api/usuarios`.

---

## Tecnologías utilizadas

| Capa | Tecnología |
| ---- | ---------- |
| Backend | Java 21, Spring Boot 4.1.1 (Spring Web MVC, Spring Data JPA, Validation, Security) |
| Seguridad | Spring Security con login por formulario, sesión y contraseñas con BCrypt |
| Base de datos | PostgreSQL |
| Frontend | HTML5, CSS3, JavaScript nativo (Fetch API) |
| Construcción | Apache Maven (con wrapper `mvnw` / `mvnw.cmd`) |

---

## Estructura del proyecto

```text
NGO-ANS/
│
├── db/
│   └── ngo_ans.sql
│       └── Volcado del esquema y datos de ejemplo
│
├── src/
│   ├── main/
│   │   ├── java/com/ngo/sistema/
│   │   │   ├── SistemaApplication.java
│   │   │   ├── SistemaController.java
│   │   │   ├── UsuarioController.java
│   │   │   ├── AsignacionController.java
│   │   │   ├── SecurityConfig.java
│   │   │   ├── CustomUserDetailsService.java
│   │   │   ├── UsuarioPrincipal.java
│   │   │   ├── Cliente.java
│   │   │   ├── Producto.java
│   │   │   ├── Solicitud.java
│   │   │   ├── Garantia.java
│   │   │   ├── Usuario.java
│   │   │   ├── Rol.java
│   │   │   ├── Asignacion.java
│   │   │   ├── Seguimiento.java
│   │   │   ├── ServicioAutorizado.java
│   │   │   └── *Repository.java
│   │   │
│   │   └── resources/
│   │       ├── application.properties
│   │       └── static/
│   │           ├── login.html
│   │           ├── index.html
│   │           ├── nueva-solicitud.html
│   │           ├── usuarios.html
│   │           ├── app.js
│   │           ├── nueva-solicitud.js
│   │           ├── usuarios.js
│   │           ├── sesion.js
│   │           └── style.css
│   │
│   └── test/
│       └── java/com/ngo/sistema/
│
├── iniciar.ps1
├── mvnw
├── mvnw.cmd
├── pom.xml
├── README.md
└── target/
```

### Modelo de datos

Tablas: `cliente`, `producto`, `garantia`, `solicitud`, `seguimiento`, `asignacion`, `usuario`, `rol` y `servicio_autorizado`.

La interfaz actual trabaja con `cliente`, `producto`, `garantia`, `solicitud`, `usuario` y `rol`. Las tablas `asignacion`, `seguimiento` y `servicio_autorizado` están modeladas pero todavía no se gestionan desde la interfaz.

---

## API REST

Todos los endpoints cuelgan de `/api` y requieren sesión iniciada. La columna Acceso indica qué rol puede usarlos.

| Método | Ruta | Descripción | Acceso |
| ------ | ---- | ----------- | ------ |
| `GET` | `/api/usuario/actual` | Devuelve nombre, correo y rol del usuario conectado | Cualquier usuario |
| `POST` | `/api/clientes` | Registra un cliente | `ADMINISTRADOR`, `FUNCIONARIO` |
| `POST` | `/api/productos` | Registra un producto | `ADMINISTRADOR`, `FUNCIONARIO` |
| `GET` | `/api/productos` | Lista los productos | Cualquier usuario |
| `GET` | `/api/productos/{id}/garantia` | Devuelve la garantía más reciente del producto (`404` si no tiene) | Cualquier usuario |
| `POST` | `/api/solicitudes` | Crea una solicitud (enlaza la garantía del producto si no se indica) | `ADMINISTRADOR`, `FUNCIONARIO` |
| `GET` | `/api/solicitudes` | Lista todas las solicitudes | Cualquier usuario |
| `GET` | `/api/solicitudes/buscar?documento=` | Busca solicitudes por documento del cliente | Cualquier usuario |
| `GET` | `/api/solicitudes/{id}` | Obtiene el detalle de una solicitud | Cualquier usuario |
| `PUT` | `/api/solicitudes/{id}/estado` | Actualiza el estado; cuerpo: `{ "estado": "ASIGNADA" }` | `ADMINISTRADOR`, `FUNCIONARIO` |
| `DELETE` | `/api/solicitudes/{id}` | Elimina la solicitud y sus registros relacionados | `ADMINISTRADOR` |
| `GET` | `/api/usuarios` | Lista los usuarios registrados (nombre, correo, rol, estado) | `ADMINISTRADOR` |
| `POST` | `/api/usuarios` | Crea un usuario con rol `FUNCIONARIO` o `TECNICO`; la contraseña se guarda cifrada con BCrypt | `ADMINISTRADOR` |

Además, el servidor expone `POST /login` (inicio de sesión) y `/logout` (cierre de sesión).

---

## Nota importante sobre el prototipo y la base de datos

Al tratarse de un prototipo local, este sistema no funcionará automáticamente en otra computadora si se clona tal cual. Cada integrante debe tener su propia base de datos PostgreSQL local llamada `ngo_saeca`, con el esquema importado; el volcado ya incluye usuarios de prueba para iniciar sesión.

Las credenciales de la base de datos no se escriben dentro de `src/main/resources/application.properties`. La aplicación lee dos variables de entorno (`DB_USER` y `DB_PASSWORD`) y el script `iniciar.ps1` las solicita de forma interactiva al arrancar.

---

## Instrucciones para ejecutar el proyecto localmente

### 1. Clonar el repositorio

```bash
git clone https://github.com/CJ-518/NGO-ANS.git
cd NGO-ANS
```

### 2. Requisitos previos

- Java JDK 21 o superior.
- PostgreSQL instalado, con una base de datos local llamada exactamente `ngo_saeca`.
- No hace falta instalar Maven: el proyecto incluye el wrapper (`mvnw` / `mvnw.cmd`).

### 3. Configurar la base de datos

Importá el esquema SQL de `db/ngo_ans.sql` en tu base `ngo_saeca`, usando pgAdmin o la terminal:

```bash
psql -U postgres -d ngo_saeca -f db/ngo_ans.sql
```

El volcado incluye la estructura completa y datos de ejemplo: clientes, un catálogo de productos con sus garantías, algunas solicitudes de muestra, los tres roles del sistema y tres usuarios de prueba.

### 4. Usuarios de acceso

El sistema exige iniciar sesión. El volcado del paso anterior ya incluye un usuario de prueba por cada rol:

| Correo | Rol |
| ------ | --- |
| `admin@ngosaeca.com.py` | `ADMINISTRADOR` |
| `funcionario@ngosaeca.com.py` | `FUNCIONARIO` |
| `tecnico@ngosaeca.com.py` | `TECNICO` |

Las contraseñas se guardan cifradas con BCrypt y no figuran en el repositorio. Para crear usuarios nuevos o restablecer una contraseña, podés usar el SQL correspondiente en tu base local.

> Para poder iniciar sesión, el usuario debe tener `estado = 'ACTIVO'`, y el nombre de su rol debe ser exactamente `ADMINISTRADOR`, `FUNCIONARIO` o `TECNICO`.

### 5. Ejecutar el sistema (script interactivo)

El proyecto incluye un script de inicio en PowerShell que pide las credenciales locales de la base de datos y abre el navegador automáticamente.

1. Abrí PowerShell en la carpeta raíz del proyecto.
2. Ejecutá:

```powershell
./iniciar.ps1
```

3. Ingresá tu usuario de PostgreSQL (por defecto `postgres`) y la contraseña cuando el script los solicite.

El sistema arranca en modo silencioso y abre el navegador en <http://localhost:8080>. Para detener el servidor, presioná `Ctrl + C` en la terminal.

> Alternativa manual (sin script). Definí las variables de entorno y levantá la aplicación con Maven.
>
> Windows (PowerShell):
>
> ```powershell
> $env:DB_USER = "postgres"
> $env:DB_PASSWORD = "tu_contrasena"
> ./mvnw.cmd spring-boot:run
> ```
>
> Linux / macOS:
>
> ```bash
> export DB_USER="postgres"
> export DB_PASSWORD="tu_contrasena"
> ./mvnw spring-boot:run
> ```

### 6. Probar el sistema

Cuando la terminal indique que la aplicación inició, abrí <http://localhost:8080>. Como todavía no hay sesión, te va a llevar a la pantalla de login:

1. Iniciá sesión con uno de los usuarios del paso 4.
2. En el panel principal (<http://localhost:8080/index.html>) vas a ver las solicitudes; probá la búsqueda por documento y hacé clic en una fila para ver su detalle.
3. En Nueva solicitud (<http://localhost:8080/nueva-solicitud.html>), escribí en cualquier campo del producto, elegí uno de los sugeridos y revisá el estado de su garantía antes de registrar la solicitud.

Para cerrar la sesión existe la ruta `/logout`.

---

## Notas para desarrollo

- Reiniciar tras cada cambio: Maven copia los archivos estáticos (`index.html`, `app.js`, `style.css`, etc.) al arrancar, así que cualquier cambio en `src/` requiere reiniciar la aplicación. Después, recargá el navegador con `Ctrl + F5`.
- Esquema de la base de datos: `spring.jpa.hibernate.ddl-auto=update` hace que Hibernate agregue tablas o columnas nuevas si faltan, pero no reemplaza la importación inicial de `db/ngo_ans.sql`.
- Logs: la terminal muestra solo errores críticos. Si algo falla (por ejemplo, un error de PostgreSQL al guardar o eliminar), el detalle aparece ahí.

---

## Solución de problemas

| Síntoma | Causa probable |
| ------- | -------------- |
| `FATAL: password authentication failed` | Usuario o contraseña de PostgreSQL incorrectos en `DB_USER` / `DB_PASSWORD`. |
| `database "ngo_saeca" does not exist` | Falta crear la base local con ese nombre exacto. |
| `Port 8080 was already in use` | Otro proceso ocupa el puerto; cerralo o cambiá `server.port` en `application.properties`. |
| No recuerdo la contraseña de un usuario | Restablecela con el SQL del paso 4. |
| "Correo o contraseña incorrectos" | El correo no existe en `usuario`, la contraseña no coincide, o el usuario no tiene `estado = 'ACTIVO'`. |
| Entro, pero no puedo crear, cambiar el estado o eliminar | El rol del usuario no permite esa acción, o el nombre del rol no es exactamente `ADMINISTRADOR`, `FUNCIONARIO` o `TECNICO`. |
| El panel carga vacío | La base está creada pero sin datos: importá `db/ngo_ans.sql`. |
| El formulario no sugiere productos | La tabla `producto` está vacía: importá `db/ngo_ans.sql`. |
| No se puede ejecutar `iniciar.ps1` | Política de ejecución de PowerShell: `Set-ExecutionPolicy -Scope Process RemoteSigned`. |

---

## Estado del proyecto

Prototipo académico con fines demostrativos, no preparado para producción. Limitaciones conocidas:

- Seguridad: la protección CSRF está desactivada (la API solo la consume el propio frontend); debe reactivarse antes de cualquier despliegue real.
- Usuarios: un `ADMINISTRADOR` ya puede crear `FUNCIONARIO` y `TECNICO` desde `usuarios.html`, pero todavía no hay forma de editarlos, desactivarlos ni restablecer su contraseña desde la interfaz.
- Roles, productos y garantías se siguen administrando directamente en la base de datos; no hay pantallas para crearlos o editarlos.
- La interfaz todavía no tiene un botón para cerrar sesión ni muestra el nombre del usuario conectado (el cierre de sesión existe en la ruta `/logout`).
- Cada solicitud nueva crea también un cliente nuevo; no se reutilizan clientes existentes, y el cliente de la solicitud no tiene por qué coincidir con el dueño de la garantía del producto.
- La asignación de técnicos, el diagnóstico y el historial de seguimiento todavía no se gestionan desde la interfaz.
- Los datos de ejemplo del volcado incluyen registros y usuarios de prueba; esas cuentas y sus contraseñas deben reemplazarse antes de cualquier uso real.