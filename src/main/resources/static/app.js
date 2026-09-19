document.addEventListener('DOMContentLoaded', () => {
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
        if (e.key === 'Escape') cerrarDetalle();
    });
});

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
                tableBody.innerHTML = '<tr><td colspan="7">No se encontraron solicitudes para ese documento.</td></tr>';
                return;
            }

            // Definir los estados oficiales del diagrama
            const estados = ['RECIBIDA', 'ASIGNADA', 'EN DIAGNÓSTICO', 'FINALIZADA'];

            data.forEach(solicitud => {
                const date = new Date(solicitud.fecha).toLocaleDateString('es-PY');
                
                // Generar el menú desplegable (Se agregó 'this' al onchange)
                let selectHtml = `<select class="estado-select" onchange="actualizarEstado(${solicitud.idSolicitud}, this.value, this)">`;
                estados.forEach(est => {
                    let isSelected = (est === solicitud.estadoActual) ? 'selected' : '';
                    selectHtml += `<option value="${est}" ${isSelected}>${est}</option>`;
                });
                selectHtml += `</select>`;
                
                const row = `<tr class="fila-solicitud" data-id="${solicitud.idSolicitud}">
                    <td>ST-${solicitud.idSolicitud}</td>
                    <td>${solicitud.cliente.nombre}</td>
                    <td>${solicitud.cliente.documento}</td>
                    <td>${solicitud.producto.tipoProducto}</td>
                    <td>${selectHtml}</td>
                    <td>${date}</td>
                    <td><button class="btn-primary btn-eliminar" data-id="${solicitud.idSolicitud}" style="background-color: #c53030; padding: 6px 12px; font-size: 14px;">Eliminar</button></td>
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
        } else {
            alert('Error al actualizar la base de datos');
        }
    } catch (error) {
        console.error('Error:', error);
    }
};