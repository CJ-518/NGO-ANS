// Página pública de seguimiento: el cliente entra con un link tipo
// seguimiento.html?codigo=XXXX (sin usuario ni contraseña) y acá se consulta
// /api/publico/seguimiento/{codigo}, que es de solo lectura y no expone datos
// del cliente ni de quién atendió cada paso. Mientras la solicitud espera a un técnico
// también muestra cuánto falta para que la atiendan y, si ese tiempo se pasa, deja
// enviar un reclamo (POST /api/publico/seguimiento/{codigo}/reclamo).

// Posición de cada estado en la línea de tiempo: 0 = Recibida ... 3 = Finalizada.
const ESTADOS_MAP = {
    'RECIBIDA': 0,
    'ASIGNADA': 1,
    'EN DIAGNÓSTICO': 2,
    'FINALIZADA': 3
};

// Cuenta regresiva de la espera: instante (según el reloj de esta página) en que vence el
// tiempo estimado, y temporizador que refresca el texto cada 15 segundos.
let vencimientoMs = null;
let temporizadorEspera = null;

/**
 * Lee el código de seguimiento de la URL (?codigo=...).
 * @returns {?string} el código, o null si el link no lo incluye
 */
function obtenerCodigo() {
    return new URLSearchParams(window.location.search).get('codigo');
}

/**
 * Oculta el "cargando" y el contenido y muestra el cuadro de error.
 * @param {string} [mensaje] Texto a mostrar; si se omite queda el mensaje por defecto del HTML.
 */
function mostrarError(mensaje) {
    document.getElementById('seguimiento-cargando').style.display = 'none';
    document.getElementById('seguimiento-contenido').style.display = 'none';
    const errorBox = document.getElementById('seguimiento-error');
    if (mensaje) {
        document.getElementById('seguimiento-error-mensaje').textContent = mensaje;
    }
    errorBox.style.display = 'block';
}

/**
 * Arma el bloque HTML de un paso del historial (estado, fecha y diagnóstico si lo hay).
 * Usa textContent para que el texto de la base nunca se interprete como HTML.
 * @param {{estado: string, fecha: string, diagnostico: ?string}} registro
 * @returns {HTMLElement}
 */
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

/**
 * Convierte una cantidad de minutos en un texto corto: "menos de 1 minuto", "35 min",
 * "3 h 20 min" o "1 día 3 h".
 * @param {number} minutos
 * @returns {string}
 */
function formatearDuracion(minutos) {
    const total = Math.floor(minutos);
    if (total < 1) return 'menos de 1 minuto';

    const dias = Math.floor(total / 1440);
    const horas = Math.floor((total % 1440) / 60);
    const mins = total % 60;

    if (dias > 0) {
        return `${dias} ${dias === 1 ? 'día' : 'días'}` + (horas > 0 ? ` ${horas} h` : '');
    }
    if (horas > 0) {
        return `${horas} h` + (mins > 0 ? ` ${mins} min` : '');
    }
    return `${mins} min`;
}

/**
 * Refresca el texto de la cuenta regresiva con el tiempo que falta hasta el vencimiento.
 * Cuando el tiempo estimado se cumple vuelve a consultar el estado, porque a partir de ese
 * momento el servidor habilita el reclamo.
 * @param {string} limite fecha estimada de atención, ya formateada para mostrar
 */
function actualizarCuentaRegresiva(limite) {
    const restanteMs = vencimientoMs - Date.now();
    if (restanteMs <= 0) {
        clearInterval(temporizadorEspera);
        cargarEstado();
        return;
    }
    document.getElementById('espera-texto').textContent =
        `Falta aproximadamente ${formatearDuracion(restanteMs / 60000)} para que un técnico empiece a atender ` +
        `tu solicitud (fecha estimada: ${limite}). Si pasa ese tiempo sin que te atiendan, vas a poder ` +
        'enviar un reclamo desde esta página.';
}

/**
 * Dibuja el recuadro de espera según la respuesta del servidor:
 * - dentro del tiempo estimado: cuenta regresiva hasta la fecha estimada;
 * - tiempo vencido: aviso con el formulario para reclamar, o la confirmación del último reclamo
 *   si todavía no se puede enviar otro;
 * - solicitud ya atendida: el recuadro se oculta.
 * Todo el texto se arma con textContent, nunca como HTML.
 * @param {object} solicitud respuesta de /api/publico/seguimiento/{codigo}
 */
function dibujarEspera(solicitud) {
    clearInterval(temporizadorEspera);
    vencimientoMs = null;

    const caja = document.getElementById('espera-caja');
    if (!solicitud.esperandoAtencion) {
        caja.style.display = 'none';
        return;
    }

    const titulo = document.getElementById('espera-titulo');
    const texto = document.getElementById('espera-texto');
    const formulario = document.getElementById('reclamo-formulario');
    const limite = new Date(solicitud.fechaLimiteAtencion).toLocaleString('es-PY');

    if (!solicitud.plazoVencido) {
        caja.style.cssText = 'padding: 15px; margin-bottom: 20px; border-radius: 4px; border-left: 5px solid #63b3ed; background-color: #ebf8ff; color: #2c5282;';
        titulo.textContent = 'Tiempo estimado de espera';
        formulario.style.display = 'none';

        vencimientoMs = Date.now() + solicitud.segundosRestantes * 1000;
        actualizarCuentaRegresiva(limite);
        temporizadorEspera = setInterval(() => actualizarCuentaRegresiva(limite), 15000);
        return;
    }

    caja.style.cssText = 'padding: 15px; margin-bottom: 20px; border-radius: 4px; border-left: 5px solid #ed8936; background-color: #fffaf0; color: #7b341e;';
    if (solicitud.puedeReclamar) {
        titulo.textContent = 'Pasó el tiempo estimado de atención';
        texto.textContent =
            `Estimábamos que un técnico empezaría a atender tu solicitud antes del ${limite} y todavía no ` +
            'pudimos hacerlo. Pedimos disculpas por la demora: podés enviar un reclamo para que el equipo ' +
            'de NGO SAECA revise tu solicitud.';
        formulario.style.display = 'block';
    } else {
        titulo.textContent = 'Recibimos tu reclamo';
        texto.textContent =
            `Enviaste tu reclamo el ${new Date(solicitud.ultimoReclamo).toLocaleString('es-PY')} y el equipo de ` +
            `NGO SAECA lo está revisando. A partir del ${new Date(solicitud.proximoReclamo).toLocaleString('es-PY')} ` +
            'vas a poder enviar otro reclamo si seguís sin recibir atención.';
        formulario.style.display = 'none';
    }
}

/**
 * Envía el reclamo del cliente (POST /api/publico/seguimiento/{codigo}/reclamo, con el mensaje opcional)
 * y, si el servidor lo acepta, redibuja la página con el estado que devuelve. Si responde 409 o 429
 * (la solicitud ya fue atendida o ya hay un reclamo reciente) se vuelve a consultar el estado,
 * porque los datos de la página estaban desactualizados.
 */
async function enviarReclamo() {
    const boton = document.getElementById('reclamo-enviar');
    const errorBox = document.getElementById('reclamo-error');
    errorBox.style.display = 'none';
    boton.disabled = true; // evita enviar dos veces

    try {
        const res = await fetch(`/api/publico/seguimiento/${encodeURIComponent(obtenerCodigo())}/reclamo`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ mensaje: document.getElementById('reclamo-mensaje').value })
        });

        if (res.ok) {
            document.getElementById('reclamo-mensaje').value = '';
            mostrarSolicitud(await res.json());
        } else {
            let mensaje = null;
            try { mensaje = (await res.json()).error; } catch (e) { /* sin JSON */ }
            errorBox.textContent = mensaje || 'No se pudo enviar el reclamo. Probá de nuevo en un momento.';
            errorBox.style.display = 'block';
            if (res.status === 409 || res.status === 429) cargarEstado(); // datos desactualizados
        }
    } catch (error) {
        console.error('Error enviando el reclamo:', error);
        errorBox.textContent = 'Hubo un problema de conexión. Probá de nuevo en un momento.';
        errorBox.style.display = 'block';
    } finally {
        boton.disabled = false;
    }
}

/**
 * Completa la página con la respuesta del servidor: estado actual, espera de atención, línea de
 * tiempo, datos del producto e historial.
 * @param {object} solicitud respuesta de /api/publico/seguimiento/{codigo}
 */
function mostrarSolicitud(solicitud) {
    document.getElementById('detalle-titulo').innerText = `Solicitud ${solicitud.numero}`;
    document.getElementById('estado-actual').innerText = solicitud.estadoActual;
    document.getElementById('fecha-actualizacion').innerText =
        new Date(solicitud.fecha).toLocaleString('es-PY');
    document.getElementById('detalle-producto').textContent =
        `${solicitud.productoMarca} ${solicitud.productoModelo} (${solicitud.productoTipo})`;
    document.getElementById('detalle-descripcion').textContent = solicitud.descripcion;

    dibujarEspera(solicitud);

    // Línea de tiempo
    const contenido = document.getElementById('seguimiento-contenido');
    const steps = contenido.querySelectorAll('.timeline-step');
    const lines = contenido.querySelectorAll('.timeline-line');

    steps.forEach(s => s.classList.remove('active'));
    lines.forEach(l => l.classList.remove('active'));

    const estadoActual = solicitud.estadoActual.toUpperCase();
    const limite = ESTADOS_MAP[estadoActual] !== undefined ? ESTADOS_MAP[estadoActual] : 0;

    // Marca como activos los pasos hasta el estado actual y las líneas que los unen.
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
}

/**
 * Lee el código de la URL (?codigo=...), consulta /api/publico/seguimiento/{codigo} y completa la
 * página: estado actual, espera de atención, línea de tiempo, datos del producto e historial.
 */
async function cargarEstado() {
    const codigo = obtenerCodigo();
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
        mostrarSolicitud(await res.json());
    } catch (error) {
        console.error('Error consultando el seguimiento:', error);
        mostrarError('Hubo un problema de conexión. Probá de nuevo en un momento.');
    }
}

// Botón del formulario de reclamo
document.getElementById('reclamo-enviar').addEventListener('click', enviarReclamo);

// Al cargar la página se consulta el estado de inmediato.
cargarEstado();
