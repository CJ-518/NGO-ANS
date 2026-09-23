# NGO-ANS

Sistema interno de gestión para NGO SAECA, desarrollado como aplicación web full-stack con Java, Spring Boot y PostgreSQL. El proyecto combina un backend REST protegido con Spring Security y un frontend estático en HTML, CSS y JavaScript.

El repositorio actual incluye dos líneas de negocio principales: soporte técnico/garantías y gestión de ventas. La seguridad, permisos y flujo de trabajo están definidos por roles y validaciones en backend.

---

## Stack tecnológico

| Capa | Tecnología |
| ---- | ---------- |
| Backend | Java 21, Spring Boot 4.1.1 |
| Web | Spring Web MVC |
| Persistencia | Spring Data JPA |
| Seguridad | Spring Security + BCrypt |
| Base de datos | PostgreSQL |
| Frontend | HTML5, CSS3, JavaScript nativo |
| Build | Maven Wrapper (`mvnw` / `mvnw.cmd`) |

---

## Roles del sistema

La aplicación usa roles con permisos diferenciados en la configuración de seguridad y en los controladores.

| Rol | Propósito principal | Permisos clave |
| --- | --- | --- |
| `ADMINISTRADOR` | Control total del sistema | administra usuarios, clientes, productos, solicitudes, ventas, stock y eliminación de registros |
| `ATENCION` | Recepción y gestión operativa | crea clientes, productos, solicitudes, asigna técnicos y cambia estados |
| `VENDEDOR` | Portal de ventas | registra ventas y consulta su propio historial |
| `TECNICO` | Atención técnica | registra diagnóstico y finaliza solicitudes asignadas |

La lógica está implementada con `hasRole(...)` y `hasAnyRole(...)` en `SecurityConfig`, y además se usan validaciones por `@PreAuthorize` en los controladores relevantes.

---

## Módulos principales

### 1. Login y autenticación

- La aplicación usa formulario de login con sesión HTTP.
- Las credenciales se guardan cifradas con BCrypt.
- El acceso a páginas y endpoints está protegido por Spring Security.
- La redirección post-login envía a:
  - `ADMINISTRADOR` / `ATENCION` / `TECNICO` → panel principal (`/index.html`)
  - `VENDEDOR` → portal de ventas (`/portal-ventas.html`)

### 2. Gestión de solicitudes y garantías

- Registro de clientes y productos.
- Alta de solicitudes de servicio técnico.
- Consulta y filtro de solicitudes por documento del cliente.
- Asignación de una solicitud a un técnico.
- Cambio de estado con seguimiento.
- Visualización del historial de diagnóstico y cierre.
- Validación de garantía basada en la fecha de venta y fecha de vencimiento del producto.

### 3. Portal de ventas

- Catálogo de artículos con stock.
- Registro de ventas por vendedor.
- Descuento automático de stock.
- Generación de productos a partir de la venta para que puedan entrar en flujo de garantías.
- Vista limitada por rol: `VENDEDOR` solo ve sus ventas; `ADMINISTRADOR` puede ver todas.

### 4. Administración de usuarios

- Solo `ADMINISTRADOR` puede acceder a la gestión de usuarios.
- Se gestionan usuarios con roles de atención, técnico y vendedor.
- El correo debe ser único y la contraseña se mantiene cifrada.

---

## Estados del flujo de trabajo

El sistema usa este flujo principal para solicitudes técnicas:

`RECIBIDA` → `ASIGNADA` → `EN DIAGNOSTICO` → `FINALIZADA`

La lógica exacta se encuentra en los modelos y endpoints de servicio. La solicitud nace en `RECIBIDA`, puede ser asignada por `ADMINISTRADOR` o `ATENCION`, y el `TECNICO` puede registrar diagnóstico y cierre para la solicitud que le fue asignada.

---

## Estructura del proyecto

```text
NGO-ANS/
├── db/
│   └── ngo_ans.sql
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── ngo/
│   │   │           └── sistema/
│   │   │               ├── Articulo.java
│   │   │               ├── ArticuloRepository.java
│   │   │               ├── Asignacion.java
│   │   │               ├── AsignacionController.java
│   │   │               ├── AsignacionRepository.java
│   │   │               ├── Cliente.java
│   │   │               ├── ClienteRepository.java
│   │   │               ├── CustomUserDetailsService.java
│   │   │               ├── DetalleVenta.java
│   │   │               ├── Garantia.java
│   │   │               ├── GarantiaRepository.java
│   │   │               ├── Producto.java
│   │   │               ├── ProductoRepository.java
│   │   │               ├── Rol.java
│   │   │               ├── RolRepository.java
│   │   │               ├── SecurityConfig.java
│   │   │               ├── Seguimiento.java
│   │   │               ├── SeguimientoController.java
│   │   │               ├── SeguimientoRepository.java
│   │   │               ├── ServicioAutorizado.java
│   │   │               ├── SistemaApplication.java
│   │   │               ├── SistemaController.java
│   │   │               ├── Solicitud.java
│   │   │               ├── SolicitudRepository.java
│   │   │               ├── Usuario.java
│   │   │               ├── UsuarioController.java
│   │   │               ├── UsuarioPrincipal.java
│   │   │               ├── UsuarioRepository.java
│   │   │               ├── Venta.java
│   │   │               ├── VentaController.java
│   │   │               └── VentaRepository.java
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
│       └── java/
│           └── com/
│               └── ngo/
│                   └── sistema/
│                       └── SistemaApplicationTests.java
├── iniciar.ps1
├── mvnw
├── mvnw.cmd
├── pom.xml
├── README.md
├── target/
│   ├── classes/
│   ├── generated-sources/
│   ├── generated-test-sources/
│   ├── maven-status/
│   ├── test-classes/
│   └── ...
└── .gitignore
```

---

## Configuración de la base de datos

La aplicación espera una base PostgreSQL local llamada `ngo_saeca`.

El archivo `src/main/resources/application.properties` usa estas variables:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/ngo_saeca
spring.datasource.username=${DB_USER:postgres}
spring.datasource.password=${DB_PASSWORD}
```

Esto significa que el usuario y contraseña no se guardan hardcodeados en el repositorio. Se inyectan como variables de entorno `DB_USER` y `DB_PASSWORD`.

---

## Datos de ejemplo y usuarios de prueba

El script SQL en `db/ngo_ans.sql` incluye el esquema y datos de ejemplo. Entre ellos se cargan roles y usuarios de prueba.

Los correos incluidos en la base son:

| Correo | Rol |
| ------ | --- |
| `admin@ngosaeca.com.py` | `ADMINISTRADOR` |
| `atencion@ngosaeca.com.py` | `ATENCION` |
| `vendedor@ngosaeca.com.py` | `VENDEDOR` |
| `tecnico@ngosaeca.com.py` | `TECNICO` |

Las contraseñas quedan cifradas con BCrypt dentro del respaldo SQL, por lo que no se exponen en el repositorio. Para crear nuevos usuarios o restablecer contraseñas, se debe hacer desde la base local o a través del flujo del sistema cuando corresponda.

---

## API REST principal

Todos los endpoints bajo `/api` requieren autenticación, salvo los recursos públicos del login y assets estáticos.

| Método | Ruta | Descripción | Acceso |
| ------ | ---- | ----------- | ------ |
| `GET` | `/api/usuario/actual` | Devuelve datos del usuario autenticado | autenticado |
| `POST` | `/api/clientes` | Crea un cliente | `ADMINISTRADOR`, `ATENCION`, `VENDEDOR` |
| `GET` | `/api/clientes` | Lista clientes con productos vinculados | autenticado |
| `GET` | `/api/clientes/buscar` | Busca cliente por documento | autenticado |
| `POST` | `/api/productos` | Crea un producto | `ADMINISTRADOR`, `ATENCION` |
| `GET` | `/api/productos` | Lista productos | autenticado |
| `GET` | `/api/productos/{id}/garantia` | Consulta garantía de un producto | autenticado |
| `POST` | `/api/solicitudes` | Registra una solicitud | `ADMINISTRADOR`, `ATENCION` |
| `GET` | `/api/solicitudes` | Lista solicitudes | autenticado |
| `GET` | `/api/solicitudes/buscar` | Busca por documento del cliente | autenticado |
| `GET` | `/api/solicitudes/{id}` | Detalle de una solicitud | autenticado |
| `POST` | `/api/solicitudes/{id}/asignar` | Asigna un técnico | `ADMINISTRADOR`, `ATENCION` |
| `GET` | `/api/solicitudes/{id}/seguimiento` | Historial de seguimiento | autenticado |
| `POST` | `/api/solicitudes/{id}/diagnostico` | Registra diagnóstico | `TECNICO` |
| `POST` | `/api/solicitudes/{id}/finalizar` | Finaliza solicitud | `TECNICO` |
| `PUT` | `/api/solicitudes/{id}/estado` | Cambia estado de la solicitud | `ADMINISTRADOR`, `ATENCION` |
| `DELETE` | `/api/solicitudes/{id}` | Elimina solicitud | `ADMINISTRADOR` |
| `GET` | `/api/usuarios` | Lista usuarios | `ADMINISTRADOR` |
| `POST` | `/api/usuarios` | Crea usuario | `ADMINISTRADOR` |
| `GET` | `/api/articulos` | Catálogo de artículos | autenticado |
| `POST` | `/api/articulos` | Alta de artículo | `ADMINISTRADOR` |
| `PUT` | `/api/articulos/{id}/stock` | Ajusta stock | `ADMINISTRADOR` |
| `GET` | `/api/ventas` | Consulta ventas | `VENDEDOR`, `ADMINISTRADOR` |
| `POST` | `/api/ventas` | Registra venta | `VENDEDOR`, `ADMINISTRADOR` |

Además, los endpoints de autenticación expuestos por Spring Security son:

- `POST /login`
- `POST /logout`

---

## Ejecución local

### Opción recomendada: script de inicio

En PowerShell, desde la raíz del proyecto:

```powershell
./iniciar.ps1
```

El script solicita el usuario y contraseña de PostgreSQL, exporta las variables de entorno `DB_USER` y `DB_PASSWORD`, levanta la aplicación con Maven Wrapper y abre el navegador en:

```text
http://localhost:8080
```

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

---

## Notas importantes

- El proyecto es un prototipo académico/local y no está pensado para ejecutarse sin una base PostgreSQL local.
- La versión actual incluye tanto gestión de soporte técnico como portal de ventas, por lo que los permisos están divididos por rol.
- El frontend es estático y se entrega a través de Spring Boot; la aplicación no usa un cliente SPA separado.
- La validación de seguridad y permisos se hace principalmente en `SecurityConfig` y en los controladores con `@PreAuthorize`.

---

## Sugerencias de uso rápido

1. Crear la base `ngo_saeca` y restaurar el contenido de `db/ngo_ans.sql`.
2. Iniciar la app con `./iniciar.ps1`.
3. Ingresar con uno de los usuarios cargados en la base.
4. Usar `ADMINISTRADOR` para administrar usuarios y permisos.
5. Usar `ATENCION` para registrar clientes y solicitudes.
6. Usar `VENDEDOR` para registrar ventas.
7. Usar `TECNICO` para diagnosticar y cerrar solicitudes asignadas.

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