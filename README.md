# NGO-ANS · versión portable

Esta es la branch **`portable`** del sistema de gestión de NGO SAECA. Es la misma aplicación que en `main`, preparada para correr en una computadora **sin Java y sin PostgreSQL instalados**.

Para qué sirve cada módulo (solicitudes, garantías, ventas, roles, API) mirá el [README de `main`](../main/README.md). Este documento solo explica lo que cambia en la versión portable.

---

## Descarga y uso rápido

1. Entrá a la sección [**Releases**](../../releases) y descargá `NGO-SAECA.zip` de la versión más reciente con sufijo `-portable`.
2. Descomprimilo en cualquier carpeta (por ejemplo `C:\NGO-SAECA`).
3. Doble clic en **`iniciar.bat`**.
4. Se abre el navegador en <http://localhost:8080>. Si no se abre solo, entrá a esa dirección a mano.
5. Para cerrar el sistema, cerrá la ventana negra o presioná `Ctrl + C`.

El primer arranque tarda unos segundos más porque crea la base de datos. Los siguientes son más rápidos.

> **Windows puede mostrar un aviso de seguridad** ("Windows protegió su PC") porque el programa no está firmado. Elegí **Más información → Ejecutar de todas formas**. Si el aviso aparece al abrir el zip, hacé clic derecho sobre `NGO-SAECA.zip` → **Propiedades** → **Desbloquear** antes de descomprimir.

### Usuarios de prueba

Son los mismos de `main`: `admin@ngosaeca.com.py`, `atencion@ngosaeca.com.py`, `tecnico@ngosaeca.com.py` y `vendedor@ngosaeca.com.py`, entre otros. La lista completa y los roles están en el [README de `main`](../main/README.md#datos-de-ejemplo-y-usuarios-de-prueba). Las contraseñas no están en el repositorio; cambialas desde **Usuarios → Cambiar contraseña** antes de un uso real.

---

## Qué cambia respecto a `main`

| | `main` | `portable` |
| --- | --- | --- |
| Base de datos | PostgreSQL instalado aparte | **H2 embebida** (un archivo local) |
| Java | Java 21 instalado | **Incluido** en la carpeta `runtime/` |
| Arranque | `iniciar.ps1` (pide usuario y contraseña de PostgreSQL) | `iniciar.bat` (no pide nada) |
| Esquema de tablas | Lo crea Hibernate (`ddl-auto=update`) sobre un respaldo de PostgreSQL | Lo crea `db/h2/schema.sql` |
| Datos de ejemplo | `db/ngo_ans.sql` (se restaura a mano) | `db/h2/datos.sql` (se cargan solos en el primer arranque) |

El código de la aplicación (controladores, entidades, pantallas) **es el mismo**. Los archivos propios de esta branch son:

- `pom.xml`: usa el driver de H2 en lugar del de PostgreSQL y fija el nombre del jar en `ngo-saeca.jar`.
- `src/main/resources/application.properties`: apunta a la base H2 local.
- `src/main/resources/db/h2/schema.sql` y `datos.sql`: tablas y datos de ejemplo.
- `src/main/java/com/ngo/sistema/DatosIniciales.java`: crea las tablas y carga los datos solo si la base está vacía.
- `iniciar.bat`: lanza la aplicación con el Java incluido.
- `crear-release.ps1`: genera el paquete portable.
- `.github/workflows/release-portable.yml`: publica el release automáticamente al subir un tag.

---

## Dónde se guardan los datos

Todo se guarda en la carpeta **`data/`**, al lado de `iniciar.bat` (archivo `ngo_saeca.mv.db`). Se crea sola en el primer arranque.

- **Respaldo:** cerrá el programa y copiá la carpeta `data/` a otro lugar.
- **Restaurar:** cerrá el programa y reemplazá `data/` por la copia.
- **Volver a los datos de ejemplo:** cerrá el programa y borrá la carpeta `data/`. En el próximo arranque se crea de nuevo y se cargan los datos de ejemplo. **Se pierde todo lo que se haya registrado.**
- **Actualizar a una versión nueva:** descomprimí el zip nuevo en otra carpeta y copiá tu `data/` a ella. No pises tu `data/` con la del zip.

No abras dos copias del programa sobre la misma carpeta `data/` a la vez: la base de datos queda bloqueada para la segunda.

---

## Configuración

Los valores por defecto están dentro del jar. Para cambiarlos sin recompilar, creá la carpeta `config` junto a `iniciar.bat` y dentro un archivo `application.properties` solo con las líneas que quieras modificar:

```
NGO-SAECA/
├── iniciar.bat
├── app/
├── runtime/
├── config/
│   └── application.properties   ← tus cambios
└── data/
```

Ejemplos de contenido:

```properties
# Cambiar el puerto
server.port=9090

# Tiempo estimado de atención (minutos) y espera entre reclamos
ngo.atencion.plazo-minutos=2880
ngo.atencion.reclamo-intervalo-minutos=720
```

También se puede mover la carpeta de datos con la variable de entorno `NGO_DATA_DIR` (por defecto `./data`).

---

## Generar el release (para quien mantiene el proyecto)

Requisitos: **JDK 21** (no un JRE) en la máquina de desarrollo y acceso a internet para que Maven descargue las dependencias.

```powershell
.\crear-release.ps1
```

El script compila el jar, crea un Java reducido con `jlink` y deja el resultado en `release\NGO-SAECA\` y `release\NGO-SAECA.zip`. Probalo antes de publicarlo, preferiblemente en una máquina sin Java ni PostgreSQL.

### Publicarlo en Releases

Con el workflow incluido, alcanza con subir un tag que empiece con `v`:

```powershell
git tag v1.0.1-portable
git push origin v1.0.1-portable
```

GitHub Actions compila en un runner Windows y publica el release con `NGO-SAECA.zip` adjunto. También se puede publicar a mano:

```powershell
gh release create v1.0.1-portable release/NGO-SAECA.zip --title "NGO SAECA portable" --notes "Incluye Java y base de datos embebida" --latest=false
```

El release solo corre en el **mismo sistema operativo** donde se genera (generado en Windows, corre en Windows). El zip no se versiona en el repositorio: va como adjunto del release.

---

## Mantener la branch al día con `main`

Cuando haya cambios nuevos en `main`, traelos a esta branch:

```powershell
git checkout portable
git merge main
```

Si en `main` se cambió el esquema de la base de datos (una tabla o columna nueva), hay que reflejarlo también en `src/main/resources/db/h2/schema.sql`, porque esta branch no deja que Hibernate cree las tablas. Los archivos `pom.xml` y `application.properties` pueden dar conflicto si se modificaron las mismas líneas en ambas branches.

---

## Solución de problemas

| Síntoma | Causa probable |
| ------- | -------------- |
| La ventana se cierra enseguida o muestra un error de Java | Falta la carpeta `runtime/` (se descomprimió mal o se movió solo `iniciar.bat`). Descomprimí el zip completo y no separes los archivos. |
| `Port 8080 was already in use` | Otro programa usa ese puerto. Cerralo o cambiá `server.port` en `config\application.properties`. |
| `Database may be already in use` | Hay otra copia del programa abierta sobre la misma carpeta `data/`. Cerrala. |
| El navegador no se abre | Entrá a mano a <http://localhost:8080>. El navegador se abre 10 segundos después de iniciar; en equipos lentos puede tardar más. |
| `ClassNotFoundException` al arrancar | Falta un módulo de Java en el release. Agregalo a la variable `$modulos` de `crear-release.ps1` y generá el release de nuevo. |
| "Correo o contraseña incorrectos" | El correo no existe, la contraseña no coincide o el usuario no está activo. Un `ADMINISTRADOR` puede cambiar la contraseña desde **Usuarios → Cambiar contraseña**. |
| Quiero empezar de cero | Cerrá el programa y borrá la carpeta `data/`. |

Para el resto de los síntomas del sistema (permisos por rol, facturas, reclamos, etc.) mirá la sección de solución de problemas del [README de `main`](../main/README.md#solución-de-problemas).
