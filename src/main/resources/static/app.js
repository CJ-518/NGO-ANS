let rolActual = null;
let idUsuarioActual = null;
let tecnicos = []; // técnicos activos, para el selector de asignación

async function cargarUsuarioActual() {
    try {
        const res = await fetch('/api/usuario/actual');
        if (!res.ok) return;
        const u = await res.json();
        rolActual = u.rol;
        idUsuarioActual = u.idUsuario;
        const span = document.getElementById('usuario-actual');
        if (span) span.textContent = `${u.nombre} (${u.rol})`;

        // El botón "Nueva solicitud" del header no lo puede usar un TECNICO.
        const btnNueva = document.getElementById('btn-nueva-solicitud');
        if (btnNueva && rolActual === 'TECNICO') {
            btnNueva.style.display = 'none';
        }

        // Solo el ADMINISTRADOR puede administrar usuarios.
        const btnUsuarios = document.getElementById('btn-usuarios');
        if (btnUsuarios && rolActual === 'ADMINISTRADOR') {
            btnUsuarios.style.display = 'inline-block';
        }

        // La columna "Acciones" tiene botones solo para el ADMINISTRADOR (Eliminar) y el TECNICO
        // (Actualizar estado). Para el FUNCIONARIO no se muestra.
        const thAcciones = document.getElementById('th-acciones');
        if (thAcciones && !tieneColumnaAcciones()) {
            thAcciones.style.display = 'none';
        }

        // Quienes pueden asignar (ADMINISTRADOR y FUNCIONARIO) necesitan la lista de técnicos.
        if (rolActual === 'ADMINISTRADOR' || rolActual === 'FUNCIONARIO') {
            await cargarTecnicos();
        }
    } catch (error) {
        console.error('Error cargando el usuario actual:', error);
    }
}

document.addEventListener('DOMContentLoaded', async () => {
    await cargarUsuarioActual(); // hay que esperar a saber el rol ANTES de pintar la tabla
    cargarSolicitudes();

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

let avanceIdSolicitud = null;

function abrirAvance(id) {
    avanceIdSolicitud = id;
    document.getElementById('avance-titulo').textContent = `Actualizar estado de ST-${id}`;
    document.getElementById('avance-estado').value = 'EN DIAGNÓSTICO';
    document.getElementById('avance-diagnostico').value = '';
    document.getElementById('avance-mensaje').style.display = 'none';
    actualizarCajaDiagnostico();
    document.getElementById('modal-avance').style.display = 'flex';
}

function cerrarAvance() {
    document.getElementById('modal-avance').style.display = 'none';
    avanceIdSolicitud = null;
}

// El texto del diagnóstico solo aplica cuando el nuevo estado es EN DIAGNÓSTICO
function actualizarCajaDiagnostico() {
    const esDiagnostico = document.getElementById('avance-estado').value === 'EN DIAGNÓSTICO';
    document.getElementById('avance-diagnostico-caja').style.display = esDiagnostico ? 'block' : 'none';
}

function mostrarMensajeAvance(texto) {
    const caja = document.getElementById('avance-mensaje');
    caja.textContent = texto;
    caja.style.display = 'block';
}

async function guardarAvance() {
    const id = avanceIdSolicitud;
    if (!id) return;

    const estado = document.getElementById('avance-estado').value;
    const finaliza = estado === 'FINALIZADA';

    if (finaliza && !confirm(`¿Finalizar la solicitud ST-${id}?\n\nUna vez finalizada, ya no podrás cambiar su estado.`)) {
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

async function eliminarSolicitud(id) {
    const confirmado = confirm(
        `¿Eliminar la solicitud ST-${id}?\n\n` +
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

function cerrarDetalle() {
    document.getElementById('modal-detalle').style.display = 'none';
}

async function abrirDetalle(id) {
    try {
        // Se consulta de nuevo para mostrar el estado más reciente
        const response = await fetch(`/api/solicitudes/${id}`);
        if (!response.ok) {
            alert('No se pudo cargar la solicitud.');
            return;
        }
        const texto = await response.text();
        if (!texto) {
            alert('No se encontró ninguna solicitud con ese número.');
            return;
        }
        const solicitud = JSON.parse(texto);

        // Datos básicos
        document.getElementById('detalle-titulo').innerText = `Solicitud ST-${solicitud.idSolicitud}`;
        document.getElementById('estado-actual').innerText = solicitud.estadoActual;
        document.getElementById('fecha-actualizacion').innerText =
            new Date(solicitud.fecha).toLocaleString('es-PY');

        // Detalles (textContent evita interpretar HTML escrito por el usuario)
        document.getElementById('detalle-cliente').textContent = solicitud.cliente.nombre;
        document.getElementById('detalle-producto').textContent =
            `${solicitud.producto.marca} ${solicitud.producto.modelo} (SN: ${solicitud.producto.nroSerie})`;
        document.getElementById('detalle-descripcion').textContent = solicitud.descripcion;

        // Línea de tiempo
        const modal = document.getElementById('modal-detalle');
        const steps = modal.querySelectorAll('.timeline-step');
        const lines = modal.querySelectorAll('.timeline-line');

        steps.forEach(s => s.classList.remove('active'));
        lines.forEach(l => l.classList.remove('active'));

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

function cargarSolicitudes(documento = '') {
    const url = documento
        ? `/api/solicitudes/buscar?documento=${encodeURIComponent(documento)}`
        : '/api/solicitudes';

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
                tableBody.innerHTML = `<tr><td colspan="${columnas}">No se encontraron solicitudes para ese documento.</td></tr>`;
                return;
            }

            // Definir los estados oficiales del diagrama
            const estados = ['RECIBIDA', 'ASIGNADA', 'EN DIAGNÓSTICO', 'FINALIZADA'];

            // Puede cambiar el estado: ADMINISTRADOR y FUNCIONARIO. TECNICO solo lo ve (texto fijo).
            const puedeCambiarEstado = rolActual === 'ADMINISTRADOR' || rolActual === 'FUNCIONARIO';
            // Asignar técnicos: mismos roles que pueden cambiar el estado.
            const puedeAsignar = rolActual === 'ADMINISTRADOR' || rolActual === 'FUNCIONARIO';

            data.forEach(solicitud => {
                const date = new Date(solicitud.fecha).toLocaleDateString('es-PY');

                let estadoHtml;
                if (puedeCambiarEstado) {
                    // Generar el menú desplegable (Se agregó 'this' al onchange)
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

                // Técnico asignado: ADMINISTRADOR y FUNCIONARIO lo eligen con un selector; el TECNICO solo lo ve
                const asignado = solicitud.tecnicoAsignado;
                let tecnicoHtml;
                if (puedeAsignar) {
                    let selectTecnico = `<select class="estado-select" onchange="asignarTecnico(${solicitud.idSolicitud}, this.value)">`;
                    if (!asignado) {
                        selectTecnico += `<option value="" selected disabled>Sin asignar</option>`;
                    }
                    tecnicos.forEach(t => {
                        const seleccionado = asignado && asignado.idUsuario === t.idUsuario ? 'selected' : '';
                        selectTecnico += `<option value="${t.idUsuario}" ${seleccionado}>${escaparHtml(t.nombre)}</option>`;
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

                // Columna Acciones: el ADMINISTRADOR elimina; el TECNICO actualiza el estado de SUS solicitudes
                // (asignadas a él y todavía no finalizadas); el FUNCIONARIO no tiene columna.
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

                const row = `<tr class="fila-solicitud" data-id="${solicitud.idSolicitud}">
                    <td>ST-${solicitud.idSolicitud}</td>
                    <td>${solicitud.cliente.nombre}</td>
                    <td>${solicitud.cliente.documento}</td>
                    <td>${solicitud.producto.tipoProducto}</td>
                    <td>${tecnicoHtml}</td>
                    <td>${estadoHtml}</td>
                    <td>${date}</td>
                    ${celdaAcciones}
                </tr>`;
                tableBody.innerHTML += row;
            });
        })
        .catch(error => console.error('Error fetching data:', error));
}

// Función global (Se agregó selectElement como parámetro)
window.actualizarEstado = async function(id, nuevoEstado, selectElement) {
    try {
        const response = await fetch(`/api/solicitudes/${id}/estado`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ estado: nuevoEstado })
        });

        if (response.ok) {
            console.log(`Solicitud ${id} actualizada a ${nuevoEstado}`);

            // Animación visual corregida
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