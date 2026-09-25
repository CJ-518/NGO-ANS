// Página pública de seguimiento: el cliente entra con un link tipo
// seguimiento.html?codigo=XXXX (sin usuario ni contraseña) y acá se consulta
// /api/publico/seguimiento/{codigo}, que es de solo lectura y no expone datos
// del cliente ni de quién atendió cada paso.

const ESTADOS_MAP = {
    'RECIBIDA': 0,
    'ASIGNADA': 1,
    'EN DIAGNÓSTICO': 2,
    'FINALIZADA': 3
};

function mostrarError(mensaje) {
    document.getElementById('seguimiento-cargando').style.display = 'none';
    document.getElementById('seguimiento-contenido').style.display = 'none';
    const errorBox = document.getElementById('seguimiento-error');
    if (mensaje) {
        document.getElementById('seguimiento-error-mensaje').textContent = mensaje;
    }
    errorBox.style.display = 'block';
}

function dibujarRegistro(registro) {
    const bloque = document.createElement('div');
    bloque.style.cssText = 'padding: 8px 0; border-bottom: 1px solid #e2e8f0;';

    const estado = document.createElement('strong');
    estado.textContent = registro.estado;

    const cabecera = document.createElement('div');
    const cuando = registro.fecha ? new Date(registro.fecha).toLocaleString('es-PY') : '';
    cabecera.append(estado, ` · ${cuando}`);
    bloque.append(cabecera);

    if (registro.diagnostico) {
        const texto = document.createElement('div');
        texto.textContent = registro.diagnostico;
        texto.style.cssText = 'margin-top: 4px; white-space: pre-wrap; color: #2d3748;';
        bloque.append(texto);
    }
    return bloque;
}

async function cargarEstado() {
    const codigo = new URLSearchParams(window.location.search).get('codigo');
    if (!codigo) {
        mostrarError('Este link no incluye un código de seguimiento válido.');
        return;
    }

    try {
        const res = await fetch(`/api/publico/seguimiento/${encodeURIComponent(codigo)}`);
        if (!res.ok) {
            mostrarError();
            return;
        }
        const solicitud = await res.json();

        document.getElementById('detalle-titulo').innerText = `Solicitud ST-${solicitud.idSolicitud}`;
        document.getElementById('estado-actual').innerText = solicitud.estadoActual;
        document.getElementById('fecha-actualizacion').innerText =
            new Date(solicitud.fecha).toLocaleString('es-PY');
        document.getElementById('detalle-producto').textContent =
            `${solicitud.productoMarca} ${solicitud.productoModelo} (${solicitud.productoTipo})`;
        document.getElementById('detalle-descripcion').textContent = solicitud.descripcion;

        // Línea de tiempo
        const contenido = document.getElementById('seguimiento-contenido');
        const steps = contenido.querySelectorAll('.timeline-step');
        const lines = contenido.querySelectorAll('.timeline-line');

        const estadoActual = solicitud.estadoActual.toUpperCase();
        const limite = ESTADOS_MAP[estadoActual] !== undefined ? ESTADOS_MAP[estadoActual] : 0;

        for (let i = 0; i <= limite; i++) {
            if (steps[i]) steps[i].classList.add('active');
            if (i < limite && lines[i]) lines[i].classList.add('active');
        }

        // Historial
        const caja = document.getElementById('detalle-seguimiento');
        if (!solicitud.historial || solicitud.historial.length === 0) {
            caja.textContent = 'Todavía no hay registros de seguimiento.';
        } else {
            caja.replaceChildren(...solicitud.historial.map(dibujarRegistro));
        }

        document.getElementById('seguimiento-cargando').style.display = 'none';
        contenido.style.display = 'block';
    } catch (error) {
        console.error('Error consultando el seguimiento:', error);
        mostrarError('Hubo un problema de conexión. Probá de nuevo en un momento.');
    }
}

cargarEstado();
