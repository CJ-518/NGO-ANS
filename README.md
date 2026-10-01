# NGO-ANS · versión portable

Esta es la branch **`portable`** del sistema de gestión de NGO SAECA. Es la misma aplicación que en `main`, preparada para correr en una computadora **sin Java y sin PostgreSQL instalados**.

Para qué sirve cada módulo (solicitudes, garantías, ventas, roles) mirá el [README de `main`](../main/README.md). Este documento solo explica cómo usar la versión portable.

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
| Datos de ejemplo | Se restauran a mano desde `db/ngo_ans.sql` | Se cargan solos en el primer arranque |

El funcionamiento del sistema (pantallas, roles, permisos) es el mismo.

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

Los valores por defecto están dentro del programa. Para cambiarlos, creá la carpeta `config` junto a `iniciar.bat` y dentro un archivo `application.properties` solo con las líneas que quieras modificar:

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

Reiniciá el programa para que tome los cambios. También se puede mover la carpeta de datos con la variable de entorno `NGO_DATA_DIR` (por defecto `./data`).

---

## Solución de problemas

| Síntoma | Causa probable |
| ------- | -------------- |
| La ventana se cierra enseguida o muestra un error de Java | Falta la carpeta `runtime/` (se descomprimió mal o se movió solo `iniciar.bat`). Descomprimí el zip completo y no separes los archivos. |
| `Port 8080 was already in use` | Otro programa usa ese puerto. Cerralo o cambiá `server.port` en `config\application.properties`. |
| `Database may be already in use` | Hay otra copia del programa abierta sobre la misma carpeta `data/`. Cerrala. |
| El navegador no se abre | Entrá a mano a <http://localhost:8080>. El navegador se abre 10 segundos después de iniciar; en equipos lentos puede tardar más. |
| "Correo o contraseña incorrectos" | El correo no existe, la contraseña no coincide o el usuario está inactivo. Un `ADMINISTRADOR` puede cambiar la contraseña desde **Usuarios → Cambiar contraseña**, o reactivar al usuario desde **Usuarios → Activar**. |
| Quiero empezar de cero | Cerrá el programa y borrá la carpeta `data/`. |

Para el resto de los síntomas del sistema (permisos por rol, facturas, reclamos, etc.) mirá la sección de solución de problemas del [README de `main`](../main/README.md#solución-de-problemas).
