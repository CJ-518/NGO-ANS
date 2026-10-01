# NGO-ANS

Sistema interno de gestión para NGO SAECA, desarrollado como aplicación web full-stack con Java, Spring Boot y PostgreSQL. Combina un backend REST protegido con Spring Security y un frontend estático en HTML, CSS y JavaScript.

El sistema cubre dos líneas de negocio: **servicio técnico y garantías** (solicitudes, asignación de técnicos, diagnóstico, seguimiento y reclamos por demora) y **ventas** (catálogo con stock y registro de ventas). Los permisos y el flujo de trabajo se definen por roles y se validan en el backend.

---

## Stack tecnológico

| Capa | Tecnología |
| ---- | ---------- |
| Backend | Java 21, Spring Boot 4.1.1 |
| Web | Spring Web MVC |
| Persistencia | Spring Data JPA (Hibernate) |
| Validación | Bean Validation (`@Pattern` en `Cliente`) |
| Seguridad | Spring Security + BCrypt |
| PDF | Apache PDFBox (factura simple de venta) |
| Base de datos | PostgreSQL |
| Frontend | HTML5, CSS3, JavaScript nativo (sin framework) |
| Build | Maven Wrapper (`mvnw` / `mvnw.cmd`) |

---

## Roles del sistema

| Rol | Propósito principal | Permisos clave |
| --- | --- | --- |
| `ADMINISTRADOR` | Control total | Crea usuarios, elimina solicitudes, asigna técnicos, cambia estados, vende, da de alta artículos |
| `ATENCION` | Recepción y gestión operativa | Registra clientes y solicitudes, asigna técnicos, cambia el estado de las solicitudes y abre la factura de un producto al registrar una solicitud |
| `TECNICO` | Atención técnica | Ve solo las solicitudes que tiene asignadas; sobre ellas registra diagnóstico y las finaliza |
| `VENDEDOR` | Portal de ventas | Registra clientes y ventas, y consulta solo sus propias ventas |

Los permisos se aplican en dos niveles: `SecurityConfig` (reglas por ruta) y `@PreAuthorize` en los controladores. Además, el backend exige que un `TECNICO` solo pueda ver y avanzar las solicitudes que tiene asignadas: el listado, la búsqueda, el detalle y el seguimiento le devuelven únicamente las suyas (si pide otra por su número, recibe un error 403). Una solicitud es "suya" mientras sea su asignación vigente; si se reasigna a otro técnico, deja de aparecerle.

---

## Módulos principales

### 1. Login y sesión

- Formulario de login con sesión HTTP; las contraseñas se guardan con BCrypt.
- Solo pueden iniciar sesión los usuarios con `estado = 'ACTIVO'`.
- Después del login: `VENDEDOR` va al portal de ventas (`/portal-ventas.html`); el resto va al panel principal (`/index.html`).
- Todas las pantallas internas muestran el nombre, el rol y un botón **Cerrar sesión** en el encabezado.

### 2. Panel de solicitudes

- Tabla de solicitudes con número, cliente, documento, producto, técnico, estado y fecha. `ADMINISTRADOR` y `ATENCION` ven todas; el `TECNICO` ve solo las que tiene asignadas (y el contador **Solicitudes abiertas** cuenta únicamente esas).
- Búsqueda por número de documento del cliente (coincidencia parcial).
- Al hacer clic en una solicitud se abre su detalle, con el historial de seguimiento (diagnósticos y cierre) y, si el cliente reclamó la demora, la lista de sus reclamos con fecha y mensaje.
- Si el cliente reclamó y su solicitud todavía espera a un técnico (`RECIBIDA` o `ASIGNADA`), la celda del estado muestra el aviso **Reclamo del cliente** (con la cantidad si fueron varios). El aviso desaparece cuando la solicitud pasa a `EN DIAGNÓSTICO`.
- La columna **Acciones** depende del rol:
  - `ADMINISTRADOR`: botón **Eliminar** (borra también sus asignaciones, su seguimiento y sus reclamos).
  - `TECNICO`: botón **Actualizar estado** en las solicitudes que tiene asignadas y no están finalizadas.
  - `ATENCION`: no tiene columna de acciones (asigna y cambia el estado desde los selectores de la tabla).
- `ADMINISTRADOR` y `ATENCION` pueden asignar un técnico y cambiar el estado desde selectores en la propia tabla.
- El selector de técnicos muestra cuántas solicitudes **pendientes** tiene cada uno (por ejemplo, `Ana Gómez (2 pendientes)`) y ordena la lista de menos a más carga, para asignar primero a quien tiene menos trabajo; a igual carga, por nombre. Una solicitud cuenta como pendiente si el técnico es su asignación vigente (la última) y todavía no está `FINALIZADA`; el historial de asignaciones anteriores no cuenta. La lista se vuelve a pedir en cada recarga de la tabla, así que los números se actualizan al asignar o al finalizar una solicitud.
- Si el cambio de estado se hace desde el selector de estado de la tabla, la tabla no se recarga en ese momento: la carga de los técnicos se corrige en la próxima recarga (al asignar, buscar o refrescar).

### 3. Nueva solicitud

- Se escribe el documento del cliente: si existe, se autocompletan sus datos (y se pueden corregir); si no, se crea un cliente nuevo.
- Un menú desplegable ofrece los productos que ese cliente ya compró (tipo, marca, modelo y número de serie), y cada opción indica si la garantía está vigente o vencida.
- Antes de registrar se muestra el estado de la garantía del producto elegido.
- Al elegir un producto se muestra la **factura de su compra** (número, fecha, total y artículos), obtenida de la venta asociada al producto, con un enlace **Abrir factura** que muestra el PDF en una pestaña nueva. Si el producto no tiene venta asociada, se muestra un aviso.
- Solo pueden registrar solicitudes `ADMINISTRADOR` y `ATENCION`.

### 4. Garantías

La garantía **no se guarda aparte**: se calcula siempre a partir de la fecha de venta del producto más 1 año. Está vigente mientras esa fecha de fin no haya pasado. Se consulta con `GET /api/productos/{id}/garantia`.

### 5. Portal de ventas

- Catálogo de artículos activos con su stock.
- Registro de ventas con varios artículos; el precio se guarda en cada renglón y el stock se descuenta en la misma transacción.
- La venta puede tener un cliente asociado o ser a consumidor final.
- Si la venta tiene cliente, se genera un **producto por cada unidad vendida** (con número de serie `SN-XX-0000`, donde `XX` es el prefijo del tipo de producto y el número sigue al mayor ya usado), vinculado a la venta de la que salió, para que luego pueda entrar en el flujo de garantías. Las ventas a consumidor final no generan productos.
- `VENDEDOR` solo ve sus ventas; `ADMINISTRADOR` ve todas y es el único que puede dar de alta artículos.

### 6. Administración de usuarios

- Solo `ADMINISTRADOR` accede a `usuarios.html`.
- Permite crear usuarios con rol `ATENCION`, `TECNICO` o `VENDEDOR`.
- El correo debe ser único y la contraseña (mínimo 8 caracteres) se guarda cifrada.
- Cada fila de la tabla tiene el botón **Cambiar contraseña**: el administrador escribe la contraseña nueva (dos veces) y se guarda cifrada, **sin necesidad de conocer la actual**. Sirve para cualquier usuario, incluidos otros administradores y el propio. Se aplican las mismas reglas que en el alta (8 a 72 bytes). Las sesiones que el usuario ya tenía abiertas siguen vigentes hasta que cierre sesión; la contraseña nueva se exige en su próximo inicio de sesión.

### 7. Factura de venta en PDF

- Desde el Portal de Ventas, al registrar una venta se descarga automáticamente una factura simple en PDF (`GET /api/ventas/{id}/factura`), generada con Apache PDFBox por `FacturaService`.
- No es un comprobante fiscal timbrado por la SET: es un recibo de referencia con los datos de la venta (cliente, vendedor, artículos, cantidades y total).
- El `VENDEDOR` solo puede descargar la factura de sus propias ventas; el `ADMINISTRADOR` y el `ATENCION` pueden abrir cualquiera (el `ATENCION` lo hace desde **Nueva solicitud**, ver más arriba).
- Con `?abrir=true` el PDF se muestra en el navegador en lugar de descargarse; sin ese parámetro se descarga como archivo (es lo que usa el Portal de Ventas).
- Las fuentes estándar (Helvetica) no soportan tildes/ñ, así que el texto se guarda sin diacríticos dentro del PDF.

### 8. Seguimiento público de una solicitud

- Cada solicitud tiene un `codigoPublico` (UUID) generado automáticamente al crearse.
- Desde el detalle de una solicitud (`ADMINISTRADOR` o `ATENCION`), el botón **Copiar link de seguimiento** arma la URL `seguimiento.html?codigo=...` y la copia al portapapeles, para pasársela al cliente.
- `seguimiento.html` es una página pública (sin usuario ni contraseña) que consulta `GET /api/publico/seguimiento/{codigo}` y muestra el estado actual de la solicitud con una línea de tiempo y el historial de diagnósticos.
- Se usa el código aleatorio (no el id secuencial) para que no se pueda adivinar el link de otro cliente probando números, y la respuesta no expone datos del cliente ni el nombre de quién atendió cada paso.

#### Tiempo estimado de atención y reclamo

- Mientras la solicitud espera a un técnico (`RECIBIDA` o `ASIGNADA`), la página pública muestra cuánto falta para que empiecen a atenderla, con una cuenta regresiva y la fecha estimada. La atención empieza cuando el técnico la pasa a `EN DIAGNÓSTICO`; desde ese momento el recuadro desaparece.
- El tiempo estimado se cuenta desde la creación de la solicitud y vence pasados los minutos configurados en `ngo.atencion.plazo-minutos` (2880, o sea 48 horas, por defecto). El tiempo es corrido (se cuentan las 24 horas, sin descontar fines de semana ni horarios laborales) y el plazo es el mismo para todas las solicitudes.
- Si el plazo vence y la solicitud sigue sin atención, la página ofrece el botón **Reclamar atención**, con un mensaje opcional de hasta 500 caracteres. Cada reclamo se guarda en la tabla `reclamo` (entidad `Reclamo`) y lo ve el personal en el panel (ver más arriba).
- Entre un reclamo y el siguiente sobre la misma solicitud tienen que pasar al menos los minutos de `ngo.atencion.reclamo-intervalo-minutos` (720, o sea 12 horas, por defecto). Mientras tanto la página confirma que el reclamo fue recibido e indica desde cuándo se puede enviar otro.
- Todas las reglas (que la solicitud siga en espera, que el plazo haya vencido y que haya pasado el intervalo) las valida el servidor, no solo la página. Para reclamar no hace falta usuario ni contraseña: alcanza con el link de la solicitud.

---

## Flujo de estados de una solicitud

`RECIBIDA` → `ASIGNADA` → `EN DIAGNÓSTICO` → `FINALIZADA`

- La solicitud nace en `RECIBIDA`.
- Al asignar o reasignar un técnico pasa a `ASIGNADA`. Una solicitud `FINALIZADA` no se reabre.
- El técnico asignado la pasa a `EN DIAGNÓSTICO` (con un texto de diagnóstico opcional, máximo 2000 caracteres) o a `FINALIZADA`. Cada uno de esos cambios queda registrado en `seguimiento`.
- `ADMINISTRADOR` y `ATENCION` también pueden cambiar el estado manualmente desde el panel; ese cambio **no** genera registro de seguimiento.
- Mientras la solicitud está en `RECIBIDA` o `ASIGNADA` se considera que espera atención: es la etapa en que corre el tiempo estimado y en que el cliente puede reclamar si se vence.

---

## Conceptos del sistema

- **Cliente:** se identifica por su número de documento.
- **Producto:** un equipo concreto vendido a un cliente, con número de serie y fecha de venta. Es lo que se atiende en una solicitud.
- **Artículo:** un tipo de mercadería del catálogo de ventas, con precio y stock. No es lo mismo que un producto: cada unidad vendida a un cliente se convierte en un producto.
- **Solicitud:** pedido de servicio técnico de un cliente sobre uno de sus productos. Se le asigna un técnico y avanza por los estados del flujo.
- **Seguimiento:** registro de los diagnósticos y del cierre que hace el técnico en cada solicitud.
- **Tiempo estimado de atención:** plazo, contado desde que se crea la solicitud, en el que se espera que un técnico empiece a atenderla. Se configura en `application.properties`.
- **Reclamo:** aviso que envía el cliente desde su link de seguimiento cuando pasó el tiempo estimado y su solicitud sigue sin atención.

---

## Estructura del proyecto

````text
NGO-ANS/
├── db/
│   └── ngo_ans.sql
├── src/
│   ├── main/
│   │   ├── java/com/ngo/sistema/
│   │   │   ├── Articulo.java
│   │   │   ├── ArticuloRepository.java
│   │   │   ├── Asignacion.java
│   │   │   ├── AsignacionController.java
│   │   │   ├── AsignacionRepository.java
│   │   │   ├── Cliente.java
│   │   │   ├── ClienteRepository.java
│   │   │   ├── CustomUserDetailsService.java
│   │   │   ├── DetalleVenta.java
│   │   │   ├── FacturaService.java
│   │   │   ├── Producto.java
│   │   │   ├── ProductoRepository.java
│   │   │   ├── PublicoController.java
│   │   │   ├── Reclamo.java
│   │   │   ├── ReclamoRepository.java
│   │   │   ├── Rol.java
│   │   │   ├── RolRepository.java
│   │   │   ├── SecurityConfig.java
│   │   │   ├── Seguimiento.java
│   │   │   ├── SeguimientoController.java
│   │   │   ├── SeguimientoRepository.java
│   │   │   ├── SistemaApplication.java
│   │   │   ├── SistemaController.java
│   │   │   ├── Solicitud.java
│   │   │   ├── SolicitudRepository.java
│   │   │   ├── Usuario.java
│   │   │   ├── UsuarioController.java
│   │   │   ├── UsuarioPrincipal.java
│   │   │   ├── UsuarioRepository.java
│   │   │   ├── Venta.java
│   │   │   ├── VentaController.java
│   │   │   └── VentaRepository.java
│   │   └── resources/
│   │       ├── application.properties
│   │       └── static/
│   │           ├── app.js
│   │           ├── index.html
│   │           ├── login.html
│   │           ├── nueva-solicitud.html
│   │           ├── nueva-solicitud.js
│   │           ├── portal-ventas.html
│   │           ├── portal-ventas.js
│   │           ├── seguimiento.html
│   │           ├── seguimiento.js
│   │           ├── sesion.js
│   │           ├── style.css
│   │           ├── usuarios.html
│   │           └── usuarios.js
│   └── test/
│       └── java/com/ngo/sistema/
│           └── SistemaApplicationTests.java
├── iniciar.ps1
├── mvnw
├── mvnw.cmd
├── pom.xml
├── README.md
└── .gitignore
````

La carpeta `target/` la genera Maven al compilar y no se versiona.

---

## Configuración de la base de datos

La aplicación espera una base PostgreSQL local llamada `ngo_saeca`. Para crearla con los datos de ejemplo:

````bash
createdb -U postgres ngo_saeca
psql -U postgres -d ngo_saeca -f db/ngo_ans.sql
````

El usuario y la contraseña no están en el repositorio: se pasan con las variables de entorno `DB_USER` (por defecto `postgres`) y `DB_PASSWORD`.

La tabla `reclamo` (reclamos de los clientes por la demora en la atención) no hace falta crearla a mano: con `spring.jpa.hibernate.ddl-auto=update`, Hibernate la crea sola al arrancar la aplicación.

---

## Datos de ejemplo y usuarios de prueba

Los datos de ejemplo incluyen los cuatro roles, clientes y productos ficticios, un historial de ventas y artículos en el catálogo.

| Correo | Rol |
| ------ | --- |
| `admin@ngosaeca.com.py` | `ADMINISTRADOR` |
| `atencion@ngosaeca.com.py` | `ATENCION` |
| `vendedor@ngosaeca.com.py` | `VENDEDOR` |
| `tecnico@ngosaeca.com.py` | `TECNICO` |
| `carlos.benitez@ngosaeca.com.py` | `VENDEDOR` |
| `lucia.fernandez@ngosaeca.com.py` | `VENDEDOR` |
| `marcos.duarte@ngosaeca.com.py` | `VENDEDOR` |
| `sofia.acosta@ngosaeca.com.py` | `VENDEDOR` |
| `andres.cabrera@ngosaeca.com.py` | `VENDEDOR` |

Las contraseñas no aparecen en el repositorio. Cambialas desde **Usuarios → Cambiar contraseña** antes de cualquier uso real.

### Historial de ventas incluido

Los productos de ejemplo figuran como vendidos, y cada uno tiene su venta con su factura en PDF, generada al vuelo. Cada venta aparece en **Ventas recientes** del vendedor y del administrador, y su factura se abre desde **Nueva solicitud** al elegir el producto. Los números de venta siguen el orden cronológico. Los precios de este historial son valores de ejemplo.

---

## API REST

Todas las rutas requieren sesión iniciada, salvo `/login.html`, `/login`, `/style.css`, `/seguimiento.html`, `/seguimiento.js`, `GET /api/publico/**` y `POST /api/publico/seguimiento/{codigo}/reclamo`.

| Método | Ruta | Descripción | Acceso |
| ------ | ---- | ----------- | ------ |
| `GET` | `/api/usuario/actual` | Datos del usuario autenticado | autenticado |
| `GET` | `/api/clientes` | Clientes que tienen al menos un producto | autenticado |
| `GET` | `/api/clientes/buscar?documento=` | Busca un cliente por documento exacto (404 si no existe) | autenticado |
| `GET` | `/api/clientes/{id}/productos` | Productos de un cliente | autenticado |
| `POST` | `/api/clientes` | Crea un cliente | `ADMINISTRADOR`, `ATENCION`, `VENDEDOR` |
| `PUT` | `/api/clientes/{id}` | Actualiza nombre, teléfono y correo | `ADMINISTRADOR`, `ATENCION`, `VENDEDOR` |
| `GET` | `/api/productos/{id}/garantia` | Garantía calculada de un producto | autenticado |
| `GET` | `/api/productos/{id}/factura` | Factura (número, fecha, total y artículos) de la venta de la que salió el producto; 404 si no tiene venta asociada | `ADMINISTRADOR`, `ATENCION` |
| `GET` | `/api/solicitudes` | Lista de solicitudes (el `TECNICO` recibe solo las asignadas a él) | autenticado |
| `GET` | `/api/solicitudes/buscar?documento=` | Busca por documento del cliente (el `TECNICO` busca solo entre las suyas) | autenticado |
| `GET` | `/api/solicitudes/{id}` | Detalle de una solicitud (403 si es un `TECNICO` y no la tiene asignada) | autenticado |
| `GET` | `/api/solicitudes/{id}/seguimiento` | Historial de seguimiento (403 si es un `TECNICO` y no la tiene asignada) | autenticado |
| `POST` | `/api/solicitudes` | Registra una solicitud | `ADMINISTRADOR`, `ATENCION` |
| `PUT` | `/api/solicitudes/{id}/estado` | Cambia el estado manualmente | `ADMINISTRADOR`, `ATENCION` |
| `POST` | `/api/solicitudes/{id}/asignar` | Asigna un técnico | `ADMINISTRADOR`, `ATENCION` |
| `POST` | `/api/solicitudes/{id}/diagnostico` | Pasa a `EN DIAGNÓSTICO` (texto opcional) | `TECNICO` asignado |
| `POST` | `/api/solicitudes/{id}/finalizar` | Finaliza la solicitud | `TECNICO` asignado |
| `DELETE` | `/api/solicitudes/{id}` | Elimina la solicitud y su historial | `ADMINISTRADOR` |
| `GET` | `/api/tecnicos` | Técnicos activos con su cantidad de solicitudes pendientes, ordenados de menor a mayor carga | `ADMINISTRADOR`, `ATENCION` |
| `GET` | `/api/usuarios` | Lista de usuarios | `ADMINISTRADOR` |
| `POST` | `/api/usuarios` | Crea un usuario (`ATENCION`, `TECNICO` o `VENDEDOR`) | `ADMINISTRADOR` |
| `PUT` | `/api/usuarios/{id}/clave` | Cambia la contraseña de un usuario sin pedir la actual. Cuerpo: `{"clave": "..."}` (8 a 72 bytes). 404 si el usuario no existe | `ADMINISTRADOR` |
| `GET` | `/api/articulos` | Catálogo de artículos activos | autenticado |
| `POST` | `/api/articulos` | Alta de artículo | `ADMINISTRADOR` |
| `GET` | `/api/ventas` | Ventas (todas para el administrador, solo las propias para el vendedor) | `VENDEDOR`, `ADMINISTRADOR` |
| `POST` | `/api/ventas` | Registra una venta y descuenta stock | `VENDEDOR`, `ADMINISTRADOR` |
| `GET` | `/api/ventas/{id}/factura` | Factura simple en PDF de la venta (`?abrir=true` la muestra en el navegador en vez de descargarla) | `VENDEDOR` (propia), `ADMINISTRADOR` y `ATENCION` (cualquiera) |
| `GET` | `/api/publico/seguimiento/{codigo}` | Estado y seguimiento de una solicitud por su `codigoPublico`, sin datos del cliente. Incluye la espera de atención: `esperandoAtencion`, `fechaLimiteAtencion`, `segundosRestantes`, `plazoVencido`, `puedeReclamar`, `ultimoReclamo` y `proximoReclamo` | público (sin login) |
| `POST` | `/api/publico/seguimiento/{codigo}/reclamo` | Registra un reclamo del cliente por la demora. Cuerpo opcional: `{"mensaje": "..."}` (hasta 500 caracteres). Responde con el estado actualizado; 400 si el mensaje es muy largo, 404 si el código no existe, 409 si la solicitud ya fue atendida o todavía está dentro del plazo, 429 si el último reclamo es muy reciente | público (sin login) |

Autenticación de Spring Security: `POST /login` y `POST /logout`.

---

## Ejecución local

### Requisitos

- Java 21
- PostgreSQL con la base `ngo_saeca` creada y el respaldo restaurado (ver arriba)

### Opción recomendada: script de inicio

En PowerShell, desde la raíz del proyecto:

````powershell
./iniciar.ps1
````

El script pide el usuario y la contraseña de PostgreSQL, define `DB_USER` y `DB_PASSWORD`, levanta la aplicación con Maven Wrapper y abre el navegador en `http://localhost:8080`.

### Opción manual

Windows (PowerShell):

````powershell
$env:DB_USER = "postgres"
$env:DB_PASSWORD = "tu_contrasena"
./mvnw.cmd spring-boot:run
````

Linux / macOS:

````bash
export DB_USER="postgres"
export DB_PASSWORD="tu_contrasena"
./mvnw spring-boot:run
````

### Probar el sistema

1. Abrí <http://localhost:8080>; sin sesión te lleva al login.
2. Iniciá sesión con uno de los usuarios de prueba.
3. En el panel principal (`/index.html`) probá la búsqueda por documento y hacé clic en una fila para ver el detalle y el seguimiento.
4. Con `ATENCION` o `ADMINISTRADOR`, entrá a **Nueva solicitud**, escribí el documento de un cliente, elegí uno de sus productos, revisá el estado de la garantía y abrí la factura de su compra con **Abrir factura** antes de registrar.
5. Asigná un técnico desde la tabla (la lista muestra cuántas solicitudes pendientes tiene cada uno y ofrece primero a quien tiene menos); después iniciá sesión como `TECNICO`: solo verá las solicitudes que le asignaron, y puede actualizar su estado desde **Actualizar estado**.
6. Con `VENDEDOR` o `ADMINISTRADOR`, probá una venta desde el **Portal de Ventas**.
7. Para probar el tiempo estimado, abrí el detalle de una solicitud `RECIBIDA` o `ASIGNADA`, copiá el link para el cliente y abrilo en otra ventana (sin sesión): verás la cuenta regresiva. Para una demostración, poné `ngo.atencion.plazo-minutos=2` y `ngo.atencion.reclamo-intervalo-minutos=1` en `application.properties` y reiniciá la aplicación: el link muestra la cuenta regresiva de 2 minutos y, al llegar a cero, aparece el botón **Reclamar atención** (después de enviarlo se puede reclamar de nuevo al minuto). Al enviarlo, la solicitud muestra el aviso **Reclamo del cliente** en el panel y el mensaje en su detalle.

---

## Notas para desarrollo

- **Reiniciar tras cada cambio:** Maven copia los archivos estáticos al arrancar, así que cualquier cambio en `src/` requiere reiniciar la aplicación. Después, recargá el navegador con `Ctrl + F5`.
- **Logs:** la terminal muestra solo errores críticos. Si algo falla (por ejemplo, un error de PostgreSQL al guardar o eliminar), el detalle aparece ahí.
- **Tests:** por ahora solo existe `SistemaApplicationTests` (`contextLoads`).
- **Tiempo de atención:** `ngo.atencion.plazo-minutos` (minutos hasta que vence el tiempo estimado, 2880 por defecto) y `ngo.atencion.reclamo-intervalo-minutos` (minutos mínimos entre dos reclamos de una misma solicitud, 720 por defecto) se cambian en `application.properties`. El plazo se evalúa con la fecha de creación de cada solicitud, así que un cambio vale también para las ya existentes.

---

## Solución de problemas

| Síntoma | Causa probable |
| ------- | -------------- |
| `FATAL: password authentication failed` | Usuario o contraseña de PostgreSQL incorrectos en `DB_USER` / `DB_PASSWORD`. |
| `database "ngo_saeca" does not exist` | Falta crear la base local con ese nombre exacto. |
| `Port 8080 was already in use` | Otro proceso ocupa el puerto; cerralo o cambiá `server.port` en `application.properties`. |
| "Correo o contraseña incorrectos" | El correo no existe, la contraseña no coincide o el usuario no está activo. |
| No recuerdo la contraseña de un usuario | Un `ADMINISTRADOR` la cambia desde **Usuarios → Cambiar contraseña** (no pide la actual). |
| Entro, pero no puedo crear, cambiar el estado o eliminar | El rol del usuario no permite esa acción. Los nombres de rol válidos son `ADMINISTRADOR`, `ATENCION`, `TECNICO` y `VENDEDOR`. |
| El panel carga vacío | Todavía no hay solicitudes: se crean desde **Nueva solicitud**. Un `TECNICO` solo ve las que tiene asignadas, así que también lo verá vacío si todavía no le asignaron ninguna. |
| En Nueva solicitud aparece "No se encontró la factura de este producto en el sistema" | El producto no tiene una venta asociada. Los generados por el Portal de Ventas y los de ejemplo sí la tienen. |
| Un técnico no ve una solicitud, o no ve el botón "Actualizar estado" | La solicitud no está asignada a ese técnico (o se reasignó a otro), o ya está `FINALIZADA`. |
| El cliente no ve el botón para reclamar | La solicitud todavía está dentro del tiempo estimado, ya la está atendiendo un técnico (`EN DIAGNÓSTICO` o `FINALIZADA`), o el cliente ya reclamó hace menos de `ngo.atencion.reclamo-intervalo-minutos` minutos. |
| Una solicitud antigua permite reclamar apenas se abre el link | Su fecha de creación ya superó el plazo y todavía espera a un técnico: el tiempo estimado se cuenta desde que se creó la solicitud. |
| No se puede ejecutar `iniciar.ps1` | Política de ejecución de PowerShell: `Set-ExecutionPolicy -Scope Process RemoteSigned`. |

---

## Estado del proyecto

Prototipo académico con fines demostrativos, no preparado para producción. Limitaciones conocidas:

- **Seguridad:** la protección CSRF está desactivada (la API solo la consume el propio frontend); debe reactivarse antes de cualquier despliegue real.
- **Usuarios:** el administrador puede crearlos y cambiar la contraseña de cualquiera desde la interfaz, pero todavía no hay forma de editarlos ni desactivarlos. Cambiar una contraseña no cierra las sesiones que el usuario ya tenía abiertas.
- **Roles:** no se pueden administrar desde la interfaz; son los cuatro fijos del sistema.
- **Productos:** solo se generan a partir de ventas con cliente; no hay pantalla para crearlos ni editarlos. Su número de serie es interno (`SN-XX-0000`), no el de fábrica.
- **Artículos:** desde la interfaz solo se pueden dar de alta; no hay forma de ajustar el stock, editar o desactivar artículos.
- **Estados:** el cambio manual de estado hecho por `ADMINISTRADOR` o `ATENCION` no queda registrado en el seguimiento, y el backend no valida que el valor enviado sea uno de los cuatro estados oficiales.
- **Reclamos:** el cliente solo puede enviarlos desde su link, y el personal los ve en el panel; no hay notificación al equipo ni forma de marcar un reclamo como respondido (el aviso se va solo cuando la solicitud pasa a `EN DIAGNÓSTICO`). Cualquiera que tenga el link puede reclamar, y lo único que limita la repetición es el tiempo mínimo entre reclamos de la misma solicitud.
- **Tiempo estimado:** es un plazo fijo y único para todas las solicitudes, contado en tiempo corrido; no considera la carga de los técnicos, los fines de semana ni los horarios laborales. La cuenta regresiva de la página usa el reloj del navegador del cliente.
- **Datos de ejemplo:** el respaldo incluye usuarios de prueba; esas cuentas y sus contraseñas deben reemplazarse antes de cualquier uso real.
