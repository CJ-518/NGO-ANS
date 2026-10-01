// Lógica del panel principal (index.html): listado de solicitudes, búsqueda por documento,
// detalle con línea de tiempo, seguimiento y reclamos del cliente, asignación de técnicos,
// cambio de estado y eliminación. Qué botones y columnas se ven depende del rol del usuario logueado.

// Estado de la página: rol e id del usuario logueado (los completa cargarUsuarioActual).
let rolActual = null;
let idUsuarioActual = null;
let tecnicos = []; // técnicos activos, para el selector de asignación
let codigoPublicoActual = null; // de la solicitud que está abierta en el modal de detalle

/**
 * Consulta /api/usuario/actual y ajusta la pantalla según el rol:
 * muestra u oculta los botones "Nueva solicitud", "Usuarios" y "Portal de Ventas" y la columna "Acciones".
 */
async function cargarUsuarioActual() {
    try {
        const res = await fetch('/api/usuario/actual');
        if (!res.ok) return;
        const u = await res.json();
        rolActual = u.rol;
        idUsuarioActual = u.idUsuario;
        const span = document.getElementById('usuario-actual');
        if (span) span.textContent = `${u.nombre} (${u.rol})`;

        // El botón "Nueva solicitud" del header no lo puede usar un TECNICO ni un VENDEDOR
        // (el VENDEDOR trabaja desde el Portal de Ventas, no desde garantías/servicio técnico).
        const btnNueva = document.getElementById('btn-nueva-solicitud');
        if (btnNueva && (rolActual === 'TECNICO' || rolActual === 'VENDEDOR')) {
            btnNueva.style.display = 'none';
        }

        // Solo el ADMINISTRADOR puede administrar usuarios.
        const btnUsuarios = document.getElementById('btn-usuarios');
        if (btnUsuarios && rolActual === 'ADMINISTRADOR') {
            btnUsuarios.style.display = 'inline-block';
        }

        // El Portal de Ventas lo usan el VENDEDOR y el ADMINISTRADOR.
        const btnVentas = document.getElementById('btn-ventas');
        if (btnVentas && (rolActual === 'VENDEDOR' || rolActual === 'ADMINISTRADOR')) {
            btnVentas.style.display = 'inline-block';
        }

        // Autoasignar solicitudes: lo usan quienes pueden asignar técnicos (ADMINISTRADOR y ATENCION).
        const btnAutoasignar = document.getElementById('btn-autoasignar');
        if (btnAutoasignar && (rolActual === 'ADMINISTRADOR' || rolActual === 'ATENCION')) {
            btnAutoasignar.style.display = 'inline-block';
        }

        // La columna "Acciones" tiene botones solo para el ADMINISTRADOR (Eliminar) y el TECNICO
        // (Actualizar estado). Para el rol ATENCION no se muestra.
        const thAcciones = document.getElementById('th-acciones');
        if (thAcciones && !tieneColumnaAcciones()) {
            thAcciones.style.display = 'none';
        }
    } catch (error) {
        console.error('Error cargando el usuario actual:', error);
    }
}

// Inicialización: primero se averigua el rol y recién después se pinta la tabla y se registran los eventos.
document.addEventListener('DOMContentLoaded', async () => {
    await cargarUsuarioActual(); // hay que esperar a saber el rol ANTES de pintar la tabla
    cargarSolicitudes();

    // Buscador por documento: filtra mientras se escribe (con una pausa de 300 ms para no saturar al servidor).
    const inputDoc = document.getElementById('buscar-documento');
    let timer;

    // Buscar mientras se escribe (con una pequeña pausa para no saturar el servidor)
    inputDoc.addEventListener('input', () => {
        inputDoc.value = inputDoc.value.replace(/\D/g, ''); // El documento solo tiene números
        clearTimeout(timer);
        timer = setTimeout(() => cargarSolicitudes(inputDoc.value), 300);
    });

    document.getElementById('btn-limpiar').addEventListener('click', () => {
        inputDoc.value = '';
        cargarSolicitudes();
    });

    // Click en una fila -> abrir detalle (ignorando clicks sobre el selector de estado)
    document.getElementById('solicitudes-body').addEventListener('click', (e) => {
        const btnEliminar = e.target.closest('button.btn-eliminar');
        if (btnEliminar) {
            eliminarSolicitud(btnEliminar.dataset.id);
            return;
        }
        const btnAvance = e.target.closest('button.btn-avance');
        if (btnAvance) {
            abrirAvance(btnAvance.dataset.id);
            return;
        }
        if (e.target.closest('select')) return;
        const fila = e.target.closest('tr[data-id]');
        if (fila) abrirDetalle(fila.dataset.id);
    });

    // Cerrar el detalle: botón X, click fuera de la ventana o tecla Escape
    const modal = document.getElementById('modal-detalle');
    document.getElementById('modal-cerrar').addEventListener('click', cerrarDetalle);
    modal.addEventListener('click', (e) => {
        if (e.target === modal) cerrarDetalle();
    });
    document.addEventListener('keydown', (e) => {
        if (e.key === 'Escape') {
            cerrarDetalle();
            cerrarAvance();
        }
    });

    // Autoasignar: reparte las solicitudes sin técnico, de a una, al técnico con menos pendientes
    document.getElementById('btn-autoasignar').addEventListener('click', autoasignarSolicitudes);

    // Copiar el link público de seguimiento (para pasárselo al cliente)
    document.getElementById('btn-copiar-link').addEventListener('click', copiarLinkSeguimiento);

    // Ventana "Actualizar estado" del técnico
    document.getElementById('avance-cerrar').addEventListener('click', cerrarAvance);
    document.getElementById('avance-cancelar').addEventListener('click', cerrarAvance);
    document.getElementById('avance-guardar').addEventListener('click', guardarAvance);
    document.getElementById('avance-estado').addEventListener('change', actualizarCajaDiagnostico);
});

// La columna "Acciones" existe para quien tiene botones en ella
function tieneColumnaAcciones() {
    return rolActual === 'ADMINISTRADOR' || rolActual === 'TECNICO';
}

// ---------- Ventana "Actualizar estado" (TECNICO) ----------

// Id de la solicitud que se está actualizando en la ventana "Actualizar estado".
let avanceIdSolicitud = null;

/**
 * Devuelve el número de solicitud (ej. 28092026-01) que se muestra en la primera columna de la fila
 * de la tabla. Los botones solo conocen el id interno, así que el número se toma de la fila.
 * Si la fila no está en pantalla devuelve el id con "#" como respaldo.
 */
function numeroDeFila(id) {
    const celda = document.querySelector(`tr.fila-solicitud[data-id="${id}"] td`);
    return celda ? celda.textContent.trim() : `#${id}`;
}

/**
 * Abre la ventana "Actualizar estado" (uso del TECNICO) para la solicitud indicada.
 */
function abrirAvance(id) {
    avanceIdSolicitud = id;
    document.getElementById('avance-titulo').textContent = `Actualizar estado de la solicitud ${numeroDeFila(id)}`;
    document.getElementById('avance-estado').value = 'EN DIAGNÓSTICO';
    document.getElementById('avance-diagnostico').value = '';
    document.getElementById('avance-mensaje').style.display = 'none';
    actualizarCajaDiagnostico();
    document.getElementById('modal-avance').style.display = 'flex';
}

/**
 * Cierra la ventana "Actualizar estado".
 */
function cerrarAvance() {
    document.getElementById('modal-avance').style.display = 'none';
    avanceIdSolicitud = null;
}

// El texto del diagnóstico solo aplica cuando el nuevo estado es EN DIAGNÓSTICO
function actualizarCajaDiagnostico() {
    const esDiagnostico = document.getElementById('avance-estado').value === 'EN DIAGNÓSTICO';
    document.getElementById('avance-diagnostico-caja').style.display = esDiagnostico ? 'block' : 'none';
}

/**
 * Muestra un mensaje de error dentro de la ventana "Actualizar estado".
 */
function mostrarMensajeAvance(texto) {
    const caja = document.getElementById('avance-mensaje');
    caja.textContent = texto;
    caja.style.display = 'block';
}

/**
 * Envía el cambio de estado del técnico: POST /diagnostico (con texto opcional) o /finalizar.
 * Al finalizar pide confirmación porque después ya no se puede cambiar.
 * Si el servidor responde 403 o 409 se recarga la tabla, porque los datos estaban desactualizados.
 */
async function guardarAvance() {
    const id = avanceIdSolicitud;
    if (!id) return;

    const estado = document.getElementById('avance-estado').value;
    const finaliza = estado === 'FINALIZADA';

    if (finaliza && !confirm(`¿Finalizar la solicitud ${numeroDeFila(id)}?\n\nUna vez finalizada, ya no podrás cambiar su estado.`)) {
        return;
    }

    const url = finaliza ? `/api/solicitudes/${id}/finalizar` : `/api/solicitudes/${id}/diagnostico`;
    const cuerpo = finaliza ? {} : { diagnostico: document.getElementById('avance-diagnostico').value };
    const filtro = document.getElementById('buscar-documento').value;
    const boton = document.getElementById('avance-guardar');
    boton.disabled = true; // evita enviar dos veces

    try {
        const response = await fetch(url, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(cuerpo)
        });

        if (response.ok) {
            cerrarAvance();
            cargarSolicitudes(filtro); // la tabla refleja el nuevo estado
        } else {
            let mensaje = null;
            try { mensaje = (await response.json()).error; } catch (e) { /* sin JSON */ }
            if (!mensaje) {
                mensaje = response.status === 403
                    ? 'No tenés permiso para cambiar el estado de esta solicitud.'
                    : 'No se pudo actualizar el estado.';
            }
            mostrarMensajeAvance(mensaje);
            if (response.status === 403 || response.status === 409) cargarSolicitudes(filtro); // datos desactualizados
        }
    } catch (error) {
        console.error('Error actualizando el estado:', error);
        mostrarMensajeAvance('Error de conexión al actualizar el estado.');
    } finally {
        boton.disabled = false;
    }
}

// ---------- Seguimiento (historial) en el detalle ----------

/**
 * Arma el bloque HTML de un registro del historial (estado, fecha, quién lo hizo y diagnóstico).
 * Usa textContent para que el texto de la base nunca se interprete como HTML.
 */
function dibujarRegistro(registro) {
    const bloque = document.createElement('div');
    bloque.style.cssText = 'padding: 8px 0; border-bottom: 1px solid #e2e8f0;';

    const estado = document.createElement('strong');
    estado.textContent = registro.estado;

    const cabecera = document.createElement('div');
    const cuando = registro.fecha ? new Date(registro.fecha).toLocaleString('es-PY') : '';
    // append() con un texto lo agrega como texto plano, no como HTML
    cabecera.append(estado, ` · ${cuando} · ${registro.usuario || ''}`);
    bloque.append(cabecera);

    if (registro.diagnostico) {
        const texto = document.createElement('div');
        texto.textContent = registro.diagnostico;
        texto.style.cssText = 'margin-top: 4px; white-space: pre-wrap; color: #2d3748;';
        bloque.append(texto);
    }
    return bloque;
}

/**
 * Muestra en el detalle los reclamos que el cliente envió desde su link de seguimiento (fecha y mensaje).
 * Si no hay ninguno, oculta la sección. Usa textContent para que el mensaje nunca se interprete como HTML.
 * @param {{fecha: string, mensaje: string}[]} reclamos
 */
function dibujarReclamos(reclamos) {
    const caja = document.getElementById('detalle-reclamos-caja');
    if (!reclamos || reclamos.length === 0) {
        caja.style.display = 'none';
        return;
    }

    const bloques = reclamos.map(reclamo => {
        const bloque = document.createElement('div');
        bloque.style.cssText = 'padding: 8px 0; border-bottom: 1px solid #e2e8f0;';

        const cabecera = document.createElement('strong');
        cabecera.textContent = new Date(reclamo.fecha).toLocaleString('es-PY');

        const texto = document.createElement('div');
        texto.textContent = reclamo.mensaje || 'Sin mensaje.';
        texto.style.cssText = 'margin-top: 4px; white-space: pre-wrap; color: #2d3748;';

        bloque.append(cabecera, texto);
        return bloque;
    });
    document.getElementById('detalle-reclamos').replaceChildren(...bloques);
    caja.style.display = 'block';
}

/**
 * Carga el historial de la solicitud (GET /api/solicitudes/{id}/seguimiento) en el detalle.
 */
async function cargarSeguimiento(id) {
    const caja = document.getElementById('detalle-seguimiento');
    try {
        const res = await fetch(`/api/solicitudes/${id}/seguimiento`);
        if (!res.ok) throw new Error('HTTP ' + res.status);
        const registros = await res.json();
        if (registros.length === 0) {
            caja.textContent = 'Todavía no hay registros de seguimiento.';
        } else {
            caja.replaceChildren(...registros.map(dibujarRegistro));
        }
    } catch (error) {
        console.error('Error cargando el seguimiento:', error);
        caja.textContent = 'No se pudo cargar el seguimiento.';
    }
}

// Evita que un texto de la base de datos se interprete como HTML
function escaparHtml(texto) {
    return String(texto ?? '')
        .replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;').replace(/'/g, '&#39;');
}

/**
 * Carga la lista de técnicos activos con su cantidad de pendientes (GET /api/tecnicos).
 * Si falla queda una lista vacía.
 */
async function cargarTecnicos() {
    try {
        const res = await fetch('/api/tecnicos');
        if (!res.ok) throw new Error('HTTP ' + res.status);
        tecnicos = await res.json();
    } catch (error) {
        console.error('Error cargando los técnicos:', error);
        tecnicos = [];
    }
}

/**
 * Asigna un técnico a una solicitud (POST /api/solicitudes/{id}/asignar) y recarga la tabla,
 * que refleja el técnico y el nuevo estado (RECIBIDA pasa a ASIGNADA).
 */
async function asignarTecnico(idSolicitud, idTecnico) {
    const filtro = document.getElementById('buscar-documento').value;

    try {
        const response = await fetch(`/api/solicitudes/${idSolicitud}/asignar`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ idTecnico: Number(idTecnico) })
        });

        if (!response.ok) {
            let mensaje = 'No se pudo asignar el técnico.';
            if (response.status === 403) {
                mensaje = 'No tenés permiso para asignar técnicos.';
            } else {
                try { mensaje = (await response.json()).error || mensaje; } catch (e) { /* sin JSON */ }
            }
            alert(mensaje);
        }
    } catch (error) {
        console.error('Error asignando el técnico:', error);
        alert('Error de conexión al asignar el técnico.');
    }

    // Se recarga la tabla: refleja el técnico asignado y el nuevo estado (RECIBIDA pasa a ASIGNADA)
    cargarSolicitudes(filtro);
}

/**
 * Autoasigna todas las solicitudes que no tienen técnico (POST /api/solicitudes/autoasignar).
 * El servidor las asigna de a una: a cada solicitud le corresponde el técnico que en ese momento tiene
 * menos pendientes, y las cantidades se recalculan antes de asignar la siguiente. Al terminar se
 * muestra un resumen y se recarga la tabla (con los técnicos y sus cantidades actualizadas).
 */
async function autoasignarSolicitudes() {
    const boton = document.getElementById('btn-autoasignar');
    const filtro = document.getElementById('buscar-documento').value;
    boton.disabled = true; // evita enviar dos veces

    try {
        const response = await fetch('/api/solicitudes/autoasignar', { method: 'POST' });

        if (response.ok) {
            const resultado = await response.json();
            if (resultado.asignadas === 0) {
                alert('No hay solicitudes sin técnico para asignar.');
            } else {
                // Resumen: cuántas solicitudes recibió cada técnico
                const porTecnico = {};
                resultado.detalle.forEach(d => {
                    porTecnico[d.tecnico] = (porTecnico[d.tecnico] || 0) + 1;
                });
                const lineas = Object.entries(porTecnico)
                    .map(([nombre, cantidad]) => `• ${nombre}: ${cantidad}`)
                    .join('\n');
                alert(`Se asignaron ${resultado.asignadas} ${resultado.asignadas === 1 ? 'solicitud' : 'solicitudes'}:\n\n${lineas}`);
            }
        } else {
            let mensaje = 'No se pudieron autoasignar las solicitudes.';
            if (response.status === 403) {
                mensaje = 'No tenés permiso para asignar técnicos.';
            } else {
                try { mensaje = (await response.json()).error || mensaje; } catch (e) { /* sin JSON */ }
            }
            alert(mensaje);
        }
    } catch (error) {
        console.error('Error autoasignando las solicitudes:', error);
        alert('Error de conexión al autoasignar las solicitudes.');
    } finally {
        boton.disabled = false;
    }

    cargarSolicitudes(filtro);
}

/**
 * Elimina una solicitud (solo ADMINISTRADOR) después de pedir confirmación.
 * También se borran sus asignaciones y su seguimiento.
 */
async function eliminarSolicitud(id) {
    const confirmado = confirm(
        `¿Eliminar la solicitud ${numeroDeFila(id)}?\n\n` +
        'También se borrarán sus asignaciones y su historial de seguimiento. ' +
        'Esta acción no se puede deshacer.'
    );
    if (!confirmado) return;

    const filtro = document.getElementById('buscar-documento').value;

    try {
        const response = await fetch(`/api/solicitudes/${id}`, { method: 'DELETE' });

        if (response.ok) {
            // Ajustar el contador de inmediato (si no hay filtro, se recalcula al recargar)
            const total = document.getElementById('total-abiertas');
            total.innerText = Math.max(0, parseInt(total.innerText, 10) - 1);
            cargarSolicitudes(filtro);
        } else if (response.status === 404) {
            alert('La solicitud ya no existe.');
            cargarSolicitudes(filtro);
        } else if (response.status === 403) {
            alert('No tenés permiso para eliminar solicitudes.');
        } else {
            alert('No se pudo eliminar la solicitud.');
        }
    } catch (error) {
        console.error('Error eliminando la solicitud:', error);
        alert('Error de conexión al eliminar la solicitud.');
    }
}

/**
 * Cierra la ventana de detalle de la solicitud.
 */
function cerrarDetalle() {
    document.getElementById('modal-detalle').style.display = 'none';
}

// Arma el link público (sin login) para que el cliente vea el estado de su solicitud,
// y lo copia al portapapeles.
async function copiarLinkSeguimiento() {
    if (!codigoPublicoActual) {
        alert('Esta solicitud todavía no tiene un link de seguimiento.');
        return;
    }
    const link = `${window.location.origin}/seguimiento.html?codigo=${codigoPublicoActual}`;
    try {
        await navigator.clipboard.writeText(link);
        const boton = document.getElementById('btn-copiar-link');
        const textoOriginal = boton.textContent;
        boton.textContent = '¡Link copiado!';
        setTimeout(() => { boton.textContent = textoOriginal; }, 2000);
    } catch (error) {
        console.error('Error copiando el link:', error);
        prompt('Copiá el link para el cliente:', link);
    }
}

/**
 * Abre el detalle de una solicitud: vuelve a consultarla para mostrar el dato más reciente y completa
 * los datos, la línea de tiempo y el seguimiento.
 * @param {string|number} id número de la solicitud
 */
async function abrirDetalle(id) {
    try {
        // Se consulta de nuevo para mostrar el estado más reciente
        const response = await fetch(`/api/solicitudes/${id}`);
        if (!response.ok) {
            alert(response.status === 403
                ? 'Esta solicitud no está asignada a tu usuario.'
                : 'No se pudo cargar la solicitud.');
            return;
        }
        const texto = await response.text();
        if (!texto) {
            alert('No se encontró ninguna solicitud con ese número.');
            return;
        }
        const solicitud = JSON.parse(texto);

        // Datos básicos
        document.getElementById('detalle-titulo').innerText = `Solicitud ${solicitud.numero}`;
        document.getElementById('estado-actual').innerText = solicitud.estadoActual;
        document.getElementById('fecha-actualizacion').innerText =
            new Date(solicitud.fecha).toLocaleString('es-PY');

        // Detalles (textContent evita interpretar HTML escrito por el usuario)
        document.getElementById('detalle-cliente').textContent = solicitud.cliente.nombre;
        document.getElementById('detalle-telefono').textContent = solicitud.cliente.telefono || '—';
        document.getElementById('detalle-producto').textContent =
            `${solicitud.producto.marca} ${solicitud.producto.modelo} (SN: ${solicitud.producto.nroSerie})`;
        document.getElementById('detalle-descripcion').textContent = solicitud.descripcion;
        codigoPublicoActual = solicitud.codigoPublico;
        dibujarReclamos(solicitud.reclamosCliente);

        // Línea de tiempo
        const modal = document.getElementById('modal-detalle');
        const steps = modal.querySelectorAll('.timeline-step');
        const lines = modal.querySelectorAll('.timeline-line');

        steps.forEach(s => s.classList.remove('active'));
        lines.forEach(l => l.classList.remove('active'));

        // Posición de cada estado en la línea de tiempo (0 = Recibida ... 3 = Finalizada).
        const estadosMap = {
            'RECIBIDA': 0,
            'ASIGNADA': 1,
            'EN DIAGNÓSTICO': 2,
            'FINALIZADA': 3
        };
        const estadoActual = solicitud.estadoActual.toUpperCase();
        const limite = estadosMap[estadoActual] !== undefined ? estadosMap[estadoActual] : 0;

        for (let i = 0; i <= limite; i++) {
            if (steps[i]) steps[i].classList.add('active');
            if (i < limite && lines[i]) lines[i].classList.add('active');
        }

        await cargarSeguimiento(id);

        modal.style.display = 'flex';
    } catch (error) {
        console.error('Error consultando la solicitud:', error);
    }
}

/**
 * Trae las solicitudes (todas, o filtradas por documento) y dibuja la tabla.
 * El contenido de cada fila depende del rol: ADMINISTRADOR/ATENCION ven selectores de estado y técnico;
 * el TECNICO ve solo sus solicitudes y el botón "Actualizar estado"; el ADMINISTRADOR además ve "Eliminar".
 * @param {string} [documento=""] filtro por número de documento del cliente
 */
async function cargarSolicitudes(documento = '') {
    const url = documento
        ? `/api/solicitudes/buscar?documento=${encodeURIComponent(documento)}`
        : '/api/solicitudes';

    // Quienes pueden asignar (ADMINISTRADOR y ATENCION) necesitan la lista de técnicos con su
    // carga actual. Se pide en cada recarga para que las cantidades y el orden estén al día
    // (cambian al asignar una solicitud o al finalizarla).
    if (rolActual === 'ADMINISTRADOR' || rolActual === 'ATENCION') {
        await cargarTecnicos();
    }

    // Se piden las solicitudes y se dibuja una fila por cada una.
    fetch(url)
        .then(response => response.json())
        .then(data => {
            const tableBody = document.getElementById('solicitudes-body');
            tableBody.innerHTML = ''; // Limpiar la tabla antes de cargar

            // El contador refleja el total; no cambia al filtrar
            if (!documento) {
                document.getElementById('total-abiertas').innerText = data.length;
            }

            if (data.length === 0) {
                const columnas = tieneColumnaAcciones() ? 8 : 7;
                const vacio = documento
                    ? 'No se encontraron solicitudes para ese documento.'
                    : (rolActual === 'TECNICO' ? 'No tenés solicitudes asignadas.' : 'No hay solicitudes registradas.');
                tableBody.innerHTML = `<tr><td colspan="${columnas}">${vacio}</td></tr>`;
                return;
            }

            // Definir los estados oficiales del diagrama
            const estados = ['RECIBIDA', 'ASIGNADA', 'EN DIAGNÓSTICO', 'FINALIZADA'];

            // Puede cambiar el estado: ADMINISTRADOR y ATENCION. TECNICO solo lo ve (texto fijo).
            const puedeCambiarEstado = rolActual === 'ADMINISTRADOR' || rolActual === 'ATENCION';
            // Asignar técnicos: mismos roles que pueden cambiar el estado.
            const puedeAsignar = rolActual === 'ADMINISTRADOR' || rolActual === 'ATENCION';

            data.forEach(solicitud => {
                const date = new Date(solicitud.fecha).toLocaleDateString('es-PY');

                let estadoHtml;
                if (puedeCambiarEstado) {
                    // Menú desplegable de estados: 'this' permite que actualizarEstado ilumine el selector al guardar
                    let selectHtml = `<select class="estado-select" onchange="actualizarEstado(${solicitud.idSolicitud}, this.value, this)">`;
                    estados.forEach(est => {
                        let isSelected = (est === solicitud.estadoActual) ? 'selected' : '';
                        selectHtml += `<option value="${est}" ${isSelected}>${est}</option>`;
                    });
                    selectHtml += `</select>`;
                    estadoHtml = selectHtml;
                } else {
                    // TECNICO: solo lectura, mismo look que el badge de estado
                    estadoHtml = `<span class="estado-select" style="display:inline-block; cursor: default;">${solicitud.estadoActual}</span>`;
                }

                // Técnico asignado: ADMINISTRADOR y ATENCION lo eligen con un selector; el TECNICO solo lo ve
                const asignado = solicitud.tecnicoAsignado;
                let tecnicoHtml;
                if (puedeAsignar) {
                    let selectTecnico = `<select class="estado-select" onchange="asignarTecnico(${solicitud.idSolicitud}, this.value)">`;
                    if (!asignado) {
                        selectTecnico += `<option value="" selected disabled>Sin asignar</option>`;
                    }
                    tecnicos.forEach(t => {
                        const seleccionado = asignado && asignado.idUsuario === t.idUsuario ? 'selected' : '';
                        selectTecnico += `<option value="${t.idUsuario}" ${seleccionado}>${escaparHtml(t.nombre)} (${t.pendientes} ${t.pendientes === 1 ? 'pendiente' : 'pendientes'})</option>`;
                    });
                    // Si el técnico asignado ya no está activo, igualmente se muestra su nombre
                    if (asignado && !tecnicos.some(t => t.idUsuario === asignado.idUsuario)) {
                        selectTecnico += `<option value="${asignado.idUsuario}" selected disabled>${escaparHtml(asignado.nombre)}</option>`;
                    }
                    selectTecnico += `</select>`;
                    tecnicoHtml = selectTecnico;
                } else {
                    tecnicoHtml = asignado
                        ? escaparHtml(asignado.nombre)
                        : `<span style="color: #718096;">Sin asignar</span>`;
                }

                // Aviso de reclamo: el cliente reclamó porque pasó el tiempo estimado y la solicitud todavía
                // espera a un técnico (RECIBIDA o ASIGNADA). Va en la celda del estado y no en la del número,
                // porque numeroDeFila() toma el número de solicitud del texto de la primera celda.
                const cantidadReclamos = (solicitud.reclamosCliente || []).length;
                const hayReclamo = cantidadReclamos > 0 && ['RECIBIDA', 'ASIGNADA'].includes(solicitud.estadoActual);
                const reclamoHtml = hayReclamo
                    ? `<div style="margin-top: 6px;"><span title="El cliente reclamó la demora en la atención" style="display: inline-block; padding: 2px 6px; border: 1px solid #ed8936; border-radius: 4px; background-color: #fffaf0; color: #7b341e; font-size: 12px; font-weight: bold;">Reclamo del cliente${cantidadReclamos > 1 ? ` (${cantidadReclamos})` : ''}</span></div>`
                    : '';

                // Columna Acciones: el ADMINISTRADOR elimina; el TECNICO actualiza el estado de SUS solicitudes
                // (asignadas a él y todavía no finalizadas); el rol ATENCION no tiene columna.
                let celdaAcciones = '';
                if (rolActual === 'ADMINISTRADOR') {
                    celdaAcciones = `<td><button class="btn-primary btn-eliminar" data-id="${solicitud.idSolicitud}" style="background-color: #c53030; padding: 6px 12px; font-size: 14px;">Eliminar</button></td>`;
                } else if (rolActual === 'TECNICO') {
                    const esMia = asignado && idUsuarioActual !== null && asignado.idUsuario === idUsuarioActual;
                    const puedeAvanzar = esMia && solicitud.estadoActual !== 'FINALIZADA';
                    celdaAcciones = puedeAvanzar
                        ? `<td><button class="btn-primary btn-avance" data-id="${solicitud.idSolicitud}" style="padding: 6px 12px; font-size: 14px;">Actualizar estado</button></td>`
                        : '<td></td>';
                }

                // NOTA: nombre y documento del cliente y el tipo de producto se insertan como HTML sin escapar
                // (el nombre del técnico sí pasa por escaparHtml). Conviene escaparlos también con escaparHtml().
                const row = `<tr class="fila-solicitud" data-id="${solicitud.idSolicitud}">
                    <td>${solicitud.numero}</td>
                    <td>${solicitud.cliente.nombre}</td>
                    <td>${solicitud.cliente.documento}</td>
                    <td>${solicitud.producto.tipoProducto}</td>
                    <td>${tecnicoHtml}</td>
                    <td>${estadoHtml}${reclamoHtml}</td>
                    <td>${date}</td>
                    ${celdaAcciones}
                </tr>`;
                tableBody.innerHTML += row;
            });
        })
        .catch(error => console.error('Error fetching data:', error));
}

/**
 * Cambia el estado desde el selector de la tabla (PUT /api/solicitudes/{id}/estado).
 * Es global (window) porque se invoca desde el atributo onchange del HTML generado.
 * Si el cambio se guarda, el selector se ilumina un segundo en verde claro.
 */
// Función global; recibe el selector (selectElement) para poder iluminarlo cuando el cambio se guarda
window.actualizarEstado = async function(id, nuevoEstado, selectElement) {
    try {
        const response = await fetch(`/api/solicitudes/${id}/estado`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ estado: nuevoEstado })
        });

        if (response.ok) {
            console.log(`Solicitud ${id} actualizada a ${nuevoEstado}`);

            // Animación visual: el selector se ilumina un segundo
            selectElement.style.backgroundColor = '#e6fffa'; // Verde claro
            setTimeout(() => selectElement.style.backgroundColor = 'white', 1000); // Vuelve a blanco
        } else if (response.status === 403) {
            alert('No tenés permiso para cambiar el estado de una solicitud.');
        } else {
            alert('Error al actualizar la base de datos');
        }
    } catch (error) {
        console.error('Error:', error);
    }
};
