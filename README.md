# NGO-ANS

Sistema interno de gestión para NGO SAECA, desarrollado como aplicación web full-stack con Java, Spring Boot y PostgreSQL. Combina un backend REST protegido con Spring Security y un frontend estático en HTML, CSS y JavaScript.

El sistema cubre dos líneas de negocio: **servicio técnico y garantías** (solicitudes, asignación de técnicos, diagnóstico y seguimiento) y **ventas** (catálogo con stock y registro de ventas). Los permisos y el flujo de trabajo se definen por roles y se validan en el backend.

---

## Stack tecnológico

| Capa | Tecnología |
| ---- | ---------- |
| Backend | Java 21, Spring Boot 4.1.1 |
| Web | Spring Web MVC |
| Persistencia | Spring Data JPA (Hibernate) |
| Validación | Bean Validation (`@Pattern` en `Cliente`) |
| Seguridad | Spring Security + BCrypt |
| Base de datos | PostgreSQL |
| Frontend | HTML5, CSS3, JavaScript nativo (sin framework) |
| Build | Maven Wrapper (`mvnw` / `mvnw.cmd`) |

---

## Roles del sistema

| Rol | Propósito principal | Permisos clave |
| --- | --- | --- |
| `ADMINISTRADOR` | Control total | Crea usuarios, elimina solicitudes, asigna técnicos, cambia estados, vende, da de alta artículos y ajusta stock |
| `ATENCION` | Recepción y gestión operativa | Registra clientes y solicitudes, asigna técnicos y cambia el estado de las solicitudes |
| `TECNICO` | Atención técnica | Ve todas las solicitudes; sobre las que tiene asignadas, registra diagnóstico y las finaliza |
| `VENDEDOR` | Portal de ventas | Registra clientes y ventas, y consulta solo sus propias ventas |

Los permisos se aplican en dos niveles: `SecurityConfig` (reglas por ruta) y `@PreAuthorize` en los controladores. Además, el backend exige que un `TECNICO` solo pueda avanzar las solicitudes que tiene asignadas.

---

## Módulos principales

### 1. Login y sesión

- Formulario de login con sesión HTTP; las contraseñas se guardan con BCrypt.
- Solo pueden iniciar sesión los usuarios con `estado = 'ACTIVO'`.
- Después del login: `VENDEDOR` va al portal de ventas (`/portal-ventas.html`); el resto va al panel principal (`/index.html`).
- Todas las pantallas internas muestran el nombre, el rol y un botón **Cerrar sesión** en el encabezado.

### 2. Panel de solicitudes

- Tabla de solicitudes con número, cliente, documento, producto, técnico, estado y fecha.
- Búsqueda por número de documento del cliente (coincidencia parcial).
- Al hacer clic en una solicitud se abre su detalle, con el historial de seguimiento (diagnósticos y cierre).
- La columna **Acciones** depende del rol:
  - `ADMINISTRADOR`: botón **Eliminar** (borra también sus asignaciones y su seguimiento).
  - `TECNICO`: botón **Actualizar estado** en las solicitudes que tiene asignadas y no están finalizadas.
  - `ATENCION`: no tiene columna de acciones (asigna y cambia el estado desde los selectores de la tabla).
- `ADMINISTRADOR` y `ATENCION` pueden asignar un técnico y cambiar el estado desde selectores en la propia tabla.

### 3. Nueva solicitud

- Se escribe el documento del cliente: si existe, se autocompletan sus datos (y se pueden corregir); si no, se crea un cliente nuevo.
- Se ofrecen los productos que ese cliente ya compró, o se pueden buscar por marca, modelo, tipo y número de serie.
- Antes de registrar se muestra el estado de la garantía del producto.
- Solo pueden registrar solicitudes `ADMINISTRADOR` y `ATENCION`.

### 4. Garantías

La garantía **no se guarda en una tabla**: se calcula siempre a partir de `producto.fecha_venta` + 1 año. Está vigente mientras esa fecha de fin no haya pasado. Se consulta con `GET /api/productos/{id}/garantia`.

### 5. Portal de ventas

- Catálogo de artículos activos con su stock.
- Registro de ventas con varios artículos; el precio se guarda en cada renglón y el stock se descuenta en la misma transacción.
- La venta puede tener un cliente asociado o ser a consumidor final.
- Si la venta tiene cliente, se genera un **producto por cada unidad vendida** (con número de serie interno `VTA-{venta}-{artículo}-{n}`), para que luego pueda entrar en el flujo de garantías. Las ventas a consumidor final no generan productos.
- `VENDEDOR` solo ve sus ventas; `ADMINISTRADOR` ve todas y es el único que puede dar de alta artículos.

### 6. Administración de usuarios

- Solo `ADMINISTRADOR` accede a `usuarios.html`.
- Permite crear usuarios con rol `ATENCION`, `TECNICO` o `VENDEDOR`.
- El correo debe ser único y la contraseña (mínimo 8 caracteres) se guarda cifrada.

---

## Flujo de estados de una solicitud

`RECIBIDA` → `ASIGNADA` → `EN DIAGNÓSTICO` → `FINALIZADA`

- La solicitud nace en `RECIBIDA`.
- Al asignar o reasignar un técnico pasa a `ASIGNADA`. Una solicitud `FINALIZADA` no se reabre.
- El técnico asignado la pasa a `EN DIAGNÓSTICO` (con un texto de diagnóstico opcional, máximo 2000 caracteres) o a `FINALIZADA`. Cada uno de esos cambios queda registrado en `seguimiento`.
- `ADMINISTRADOR` y `ATENCION` también pueden cambiar el estado manualmente desde el panel; ese cambio **no** genera registro de seguimiento.

---

## Modelo de datos

| Tabla | Contenido |
| ----- | --------- |
| `cliente` | Nombre, documento (único, solo números), teléfono y correo |
| `producto` | Equipo vendido: marca, modelo, N° de serie (único), tipo, cliente dueño y fecha de venta |
| `solicitud` | Solicitud de servicio: cliente, producto, descripción y estado actual |
| `asignacion` | Historial de técnicos asignados a cada solicitud (la última es la vigente) |
| `seguimiento` | Registros de diagnóstico y cierre de cada solicitud |
| `usuario` / `rol` | Usuarios del sistema y sus roles |
| `articulo` | Catálogo de venta con precio y stock |
| `venta` / `detalle_venta` | Ventas y sus renglones |

`articulo` es independiente de `producto`: un artículo es un tipo de mercadería con stock; un producto es un equipo concreto con número de serie y dueño.

---

## Estructura del proyecto

```text
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
│   │   │   ├── Producto.java
│   │   │   ├── ProductoRepository.java
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
```

La carpeta `target/` la genera Maven al compilar y no se versiona.

---

## Configuración de la base de datos

La aplicación espera una base PostgreSQL local llamada `ngo_saeca`. El archivo `src/main/resources/application.properties` usa:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/ngo_saeca
spring.datasource.username=${DB_USER:postgres}
spring.datasource.password=${DB_PASSWORD}
```

El usuario y la contraseña no están en el repositorio: se pasan con las variables de entorno `DB_USER` (por defecto `postgres`) y `DB_PASSWORD`.

### Restaurar el respaldo

```bash
createdb -U postgres ngo_saeca
psql -U postgres -d ngo_saeca -f db/ngo_ans.sql
```

`db/ngo_ans.sql` es un volcado de `pg_dump` 18 con el esquema y los datos de ejemplo. Usá un cliente `psql` reciente: el archivo incluye los comandos `\restrict` / `\unrestrict` que agrega `pg_dump`.

> ⚠️ Este respaldo todavía define `articulo.id_articulo` con un `DEFAULT nextval(...)`, previo a la migración a `GENERATED ALWAYS AS IDENTITY` descripta más abajo. Si restaurás el respaldo en una base nueva, volvé a aplicar esa migración para mantener la protección contra inserción manual del id.

### Alta de artículos por SQL

Además del formulario **Nuevo artículo** del Portal de Ventas (solo `ADMINISTRADOR`), se puede cargar stock nuevo directo por SQL, útil para altas masivas. No hay que incluir `id_articulo`: la columna lo genera sola.

```sql
INSERT INTO public.articulo (nombre, descripcion, categoria, precio, stock, estado)
VALUES
    ('Ventilador de pie', 'Motor de 3 velocidades', 'Ventilación', 450000, 20, 'ACTIVO'),
    ('Cocina 4 hornallas', 'Gas natural/envasado', 'Cocina', 1800000, 6, 'ACTIVO');
```

Se puede pegar directo en la Query Tool de pgAdmin4 o en `psql`. **Preferí pgAdmin4 (o guardar el script como archivo `.sql` en UTF-8) antes que pegarlo en una terminal de Windows sin UTF-8**: pegar texto con tildes/ñ en una terminal mal configurada puede corromper los caracteres (por ejemplo `Climatización` guardado como `ClimatizaciÃ³n`).

`articulo.id_articulo` es `GENERATED ALWAYS AS IDENTITY`: Postgres rechaza cualquier `INSERT` que indique `id_articulo` a mano (salvo `OVERRIDING SYSTEM VALUE`, que no se usa acá). Así se evita desincronizar la secuencia insertando ids manualmente.

---

## Datos de ejemplo y usuarios de prueba

El respaldo incluye los cuatro roles, un usuario por rol, clientes y productos ficticios, y un artículo de ejemplo en el catálogo.

| Correo | Rol |
| ------ | --- |
| `admin@ngosaeca.com.py` | `ADMINISTRADOR` |
| `atencion@ngosaeca.com.py` | `ATENCION` |
| `vendedor@ngosaeca.com.py` | `VENDEDOR` |
| `tecnico@ngosaeca.com.py` | `TECNICO` |

Las contraseñas están cifradas con BCrypt dentro del respaldo, por lo que no aparecen en el repositorio.

### Restablecer la contraseña de un usuario

Desde `psql`, con la extensión `pgcrypto` (genera hashes BCrypt compatibles con Spring Security):

```sql
CREATE EXTENSION IF NOT EXISTS pgcrypto;

UPDATE usuario
SET clave = crypt('NuevaClave123', gen_salt('bf', 10))
WHERE correo = 'admin@ngosaeca.com.py';
```

---

## API REST

Todas las rutas requieren sesión iniciada, salvo `/login.html`, `/login` y `/style.css`.

| Método | Ruta | Descripción | Acceso |
| ------ | ---- | ----------- | ------ |
| `GET` | `/api/usuario/actual` | Datos del usuario autenticado | autenticado |
| `GET` | `/api/clientes` | Clientes que tienen al menos un producto | autenticado |
| `GET` | `/api/clientes/buscar?documento=` | Busca un cliente por documento exacto (404 si no existe) | autenticado |
| `GET` | `/api/clientes/{id}/productos` | Productos de un cliente | autenticado |
| `POST` | `/api/clientes` | Crea un cliente | `ADMINISTRADOR`, `ATENCION`, `VENDEDOR` |
| `PUT` | `/api/clientes/{id}` | Actualiza nombre, teléfono y correo | `ADMINISTRADOR`, `ATENCION`, `VENDEDOR` |
| `GET` | `/api/productos` | Lista de productos | autenticado |
| `GET` | `/api/productos/{id}/garantia` | Garantía calculada de un producto | autenticado |
| `GET` | `/api/solicitudes` | Lista de solicitudes | autenticado |
| `GET` | `/api/solicitudes/buscar?documento=` | Busca por documento del cliente | autenticado |
| `GET` | `/api/solicitudes/{id}` | Detalle de una solicitud | autenticado |
| `GET` | `/api/solicitudes/{id}/seguimiento` | Historial de seguimiento | autenticado |
| `POST` | `/api/solicitudes` | Registra una solicitud | `ADMINISTRADOR`, `ATENCION` |
| `PUT` | `/api/solicitudes/{id}/estado` | Cambia el estado manualmente | `ADMINISTRADOR`, `ATENCION` |
| `POST` | `/api/solicitudes/{id}/asignar` | Asigna un técnico | `ADMINISTRADOR`, `ATENCION` |
| `POST` | `/api/solicitudes/{id}/diagnostico` | Pasa a `EN DIAGNÓSTICO` (texto opcional) | `TECNICO` asignado |
| `POST` | `/api/solicitudes/{id}/finalizar` | Finaliza la solicitud | `TECNICO` asignado |
| `DELETE` | `/api/solicitudes/{id}` | Elimina la solicitud y su historial | `ADMINISTRADOR` |
| `GET` | `/api/tecnicos` | Técnicos activos, para el selector de asignación | `ADMINISTRADOR`, `ATENCION` |
| `GET` | `/api/usuarios` | Lista de usuarios | `ADMINISTRADOR` |
| `POST` | `/api/usuarios` | Crea un usuario (`ATENCION`, `TECNICO` o `VENDEDOR`) | `ADMINISTRADOR` |
| `GET` | `/api/articulos` | Catálogo de artículos activos | autenticado |
| `POST` | `/api/articulos` | Alta de artículo | `ADMINISTRADOR` |
| `PUT` | `/api/articulos/{id}/stock` | Ajusta el stock de un artículo | `ADMINISTRADOR` |
| `GET` | `/api/ventas` | Ventas (todas para el administrador, solo las propias para el vendedor) | `VENDEDOR`, `ADMINISTRADOR` |
| `POST` | `/api/ventas` | Registra una venta y descuenta stock | `VENDEDOR`, `ADMINISTRADOR` |

Autenticación de Spring Security: `POST /login` y `POST /logout`.

---

## Ejecución local

### Requisitos

- Java 21
- PostgreSQL con la base `ngo_saeca` creada y el respaldo restaurado (ver arriba)

### Opción recomendada: script de inicio

En PowerShell, desde la raíz del proyecto:

```powershell
./iniciar.ps1
```

El script pide el usuario y la contraseña de PostgreSQL, define `DB_USER` y `DB_PASSWORD`, levanta la aplicación con Maven Wrapper y abre el navegador en `http://localhost:8080`.

### Opción manual

Windows (PowerShell):

```powershell
$env:DB_USER = "postgres"
$env:DB_PASSWORD = "tu_contrasena"
./mvnw.cmd spring-boot:run
```

Linux / macOS:

```bash
export DB_USER="postgres"
export DB_PASSWORD="tu_contrasena"
./mvnw spring-boot:run
```

### Probar el sistema

1. Abrí <http://localhost:8080>; sin sesión te lleva al login.
2. Iniciá sesión con uno de los usuarios de prueba.
3. En el panel principal (`/index.html`) probá la búsqueda por documento y hacé clic en una fila para ver el detalle y el seguimiento.
4. Con `ATENCION` o `ADMINISTRADOR`, entrá a **Nueva solicitud**, escribí el documento de un cliente, elegí uno de sus productos y revisá el estado de la garantía antes de registrar.
5. Asigná un técnico desde la tabla; después iniciá sesión como `TECNICO` para actualizar el estado desde **Actualizar estado**.
6. Con `VENDEDOR` o `ADMINISTRADOR`, probá una venta desde el **Portal de Ventas**.

---

## Notas para desarrollo

- **Reiniciar tras cada cambio:** Maven copia los archivos estáticos al arrancar, así que cualquier cambio en `src/` requiere reiniciar la aplicación. Después, recargá el navegador con `Ctrl + F5`.
- **Esquema:** `spring.jpa.hibernate.ddl-auto=update` agrega tablas o columnas nuevas si faltan, pero **nunca borra** las que sobran. Si eliminás una entidad, la tabla hay que borrarla a mano.
- **`articulo.id_articulo` es IDENTITY:** se migró de `DEFAULT nextval(...)` a `GENERATED ALWAYS AS IDENTITY` para que no se pueda insertar el id a mano por error (ver "Alta de artículos por SQL"). `Articulo.java` no necesitó cambios: ya usaba `GenerationType.IDENTITY`.
- **Logs:** la terminal muestra solo errores críticos. Si algo falla (por ejemplo, un error de PostgreSQL al guardar o eliminar), el detalle aparece ahí.
- **Tests:** por ahora solo existe `SistemaApplicationTests` (`contextLoads`).

---

## Solución de problemas

| Síntoma | Causa probable |
| ------- | -------------- |
| `FATAL: password authentication failed` | Usuario o contraseña de PostgreSQL incorrectos en `DB_USER` / `DB_PASSWORD`. |
| `database "ngo_saeca" does not exist` | Falta crear la base local con ese nombre exacto. |
| `Port 8080 was already in use` | Otro proceso ocupa el puerto; cerralo o cambiá `server.port` en `application.properties`. |
| Errores con `\restrict` al restaurar el respaldo | El cliente `psql` es antiguo; usá una versión reciente. |
| "Correo o contraseña incorrectos" | El correo no existe en `usuario`, la contraseña no coincide o el usuario no tiene `estado = 'ACTIVO'`. |
| No recuerdo la contraseña de un usuario | Restablecela con el SQL de la sección "Restablecer la contraseña de un usuario". |
| Entro, pero no puedo crear, cambiar el estado o eliminar | El rol del usuario no permite esa acción. Los nombres de rol válidos son `ADMINISTRADOR`, `ATENCION`, `TECNICO` y `VENDEDOR`. |
| El panel carga vacío | Hay que crear solicitudes desde **Nueva solicitud**; el respaldo no trae solicitudes cargadas. |
| El formulario no sugiere productos | La tabla `producto` está vacía: restaurá `db/ngo_ans.sql` o registrá una venta con cliente. |
| Un técnico no ve el botón "Actualizar estado" | La solicitud no está asignada a ese técnico o ya está `FINALIZADA`. |
| No se puede ejecutar `iniciar.ps1` | Política de ejecución de PowerShell: `Set-ExecutionPolicy -Scope Process RemoteSigned`. |
| Las tildes/ñ quedan mal guardadas (`ClimatizaciÃ³n`) al cargar artículos por SQL | La terminal donde se pegó el script no estaba en UTF-8. Usá pgAdmin4 (Query Tool) o un archivo `.sql` guardado en UTF-8 en vez de pegar en una terminal de Windows sin configurar. |
| `INSERT` a `articulo` falla con "cannot insert a non-DEFAULT value into column id_articulo" | Es esperado: la columna es `GENERATED ALWAYS AS IDENTITY`. No incluyas `id_articulo` en el `INSERT`. |

---

## Estado del proyecto

Prototipo académico con fines demostrativos, no preparado para producción. Limitaciones conocidas:

- **Seguridad:** la protección CSRF está desactivada (la API solo la consume el propio frontend); debe reactivarse antes de cualquier despliegue real.
- **Usuarios:** el administrador puede crearlos, pero todavía no hay forma de editarlos, desactivarlos ni restablecer su contraseña desde la interfaz.
- **Roles:** se administran directamente en la base de datos.
- **Productos:** solo se generan a partir de ventas con cliente; no hay pantalla para crearlos ni editarlos. Su número de serie es interno (`VTA-...`), no el de fábrica.
- **Artículos:** existe el endpoint para ajustar el stock (`PUT /api/articulos/{id}/stock`), pero la interfaz todavía no tiene un botón para usarlo, ni para editar o desactivar artículos. Como alternativa, se puede cargar stock nuevo por SQL (ver "Alta de artículos por SQL").
- **Estados:** el cambio manual de estado hecho por `ADMINISTRADOR` o `ATENCION` no queda registrado en el seguimiento, y el backend no valida que el valor enviado sea uno de los cuatro estados oficiales.
- **Datos de ejemplo:** el respaldo incluye usuarios de prueba; esas cuentas y sus contraseñas deben reemplazarse antes de cualquier uso real.
