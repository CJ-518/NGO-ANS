// Campos del producto: coinciden con los nombres del JSON y con los id del formulario
const CAMPOS = ['tipoProducto', 'marca', 'modelo', 'nroSerie'];

let productos = [];              // Productos registrados en la base de datos
let productoSeleccionado = null; // Producto que coincide exactamente con los cuatro campos

const normalizar = (texto) => (texto || '').trim().toLowerCase();
const valorDe = (campo) => normalizar(document.getElementById(campo).value);

// ---------- Carga inicial y eventos ----------

(async function iniciar() {
    try {
        const res = await fetch('/api/productos');
        if (!res.ok) throw new Error('HTTP ' + res.status);
        productos = await res.json();
    } catch (error) {
        console.error('Error cargando los productos:', error);
        alert('No se pudo cargar la lista de productos registrados.');
    }

    CAMPOS.forEach(campo => {
        const input = document.getElementById(campo);

        input.addEventListener('input', (e) => {
            alEscribir();
            // Elegir una opción de la lista dispara 'input' sin haber tipeado nada
            if (!e.inputType || e.inputType === 'insertReplacementText') alConfirmar();
        });

        // Al terminar de editar un campo (Enter, Tab o click fuera)
        input.addEventListener('change', alConfirmar);
    });

    actualizarSugerencias();
})();

// ---------- Sugerencias ----------

// Productos que coinciden con lo escrito en los campos (ignorando el campo "excluir")
function coincidencias(excluir) {
    return productos.filter(p =>
        CAMPOS.every(campo => {
            if (campo === excluir) return true;
            const escrito = valorDe(campo);
            return !escrito || normalizar(p[campo]).includes(escrito);
        })
    );
}

// Cada campo sugiere los valores que existen en los productos compatibles con los OTROS campos
function actualizarSugerencias() {
    CAMPOS.forEach(campo => {
        const valores = [...new Set(coincidencias(campo).map(p => p[campo]))]
            .sort((a, b) => a.localeCompare(b, 'es'));

        const opciones = valores.map(valor => {
            const opcion = document.createElement('option');
            opcion.value = valor;
            return opcion;
        });
        document.getElementById('lista-' + campo).replaceChildren(...opciones);
    });
}

function alEscribir() {
    actualizarSugerencias();
    const exacto = productos.find(p => CAMPOS.every(c => normalizar(p[c]) === valorDe(c))) || null;
    mostrarProducto(exacto);
}

// Si solo queda un producto posible, se completan todos los campos
function alConfirmar() {
    const hayFiltro = CAMPOS.some(c => valorDe(c));
    const candidatos = coincidencias(null);
    if (hayFiltro && candidatos.length === 1) {
        const p = candidatos[0];
        CAMPOS.forEach(c => { document.getElementById(c).value = p[c]; });
        actualizarSugerencias();
        mostrarProducto(p);
    }
}

// ---------- Producto elegido y garantía ----------

async function mostrarProducto(producto) {
    if (producto === productoSeleccionado) return;
    productoSeleccionado = producto;

    const caja = document.getElementById('info-producto');
    if (!producto) {
        caja.style.display = 'none';
        caja.replaceChildren();
        return;
    }

    caja.style.display = 'block';
    caja.replaceChildren(linea('Consultando garantía...'));

    let garantia = null;
    let error = false;
    try {
        const res = await fetch(`/api/productos/${producto.idProducto}/garantia`);
        if (res.ok) garantia = await res.json();
        else if (res.status !== 404) error = true; // 404 = el producto no tiene garantía
    } catch (e) {
        console.error('Error consultando la garantía:', e);
        error = true;
    }

    // Si el usuario cambió de producto mientras se consultaba, se descarta este resultado
    if (productoSeleccionado !== producto) return;
    dibujarGarantia(caja, garantia, error);
}

function dibujarGarantia(caja, garantia, error) {
    if (error) {
        caja.replaceChildren(linea('No se pudo consultar la garantía del producto.'));
        return;
    }
    if (!garantia) {
        caja.replaceChildren(linea('Este producto no tiene garantía registrada.'));
        return;
    }

    const inicio = fechaLocal(garantia.fechaInicio);
    const fin = fechaLocal(garantia.fechaFin);
    const hoy = new Date();
    hoy.setHours(0, 0, 0, 0);
    const vigente = fin >= hoy;

    const insignia = document.createElement('span');
    insignia.textContent = vigente ? 'EN GARANTÍA' : 'GARANTÍA VENCIDA';
    insignia.style.cssText = 'display: inline-block; margin-left: 10px; padding: 3px 10px; border-radius: 12px; font-weight: bold; font-size: 13px; ' +
        (vigente ? 'background-color: #c6f6d5; color: #22543d;' : 'background-color: #fed7d7; color: #742a2a;');

    const fila = linea(`Vendido el ${formatear(inicio)} · Garantía hasta el ${formatear(fin)}`);
    fila.appendChild(insignia);
    caja.replaceChildren(fila);
}

function linea(texto) {
    const div = document.createElement('div');
    div.textContent = texto;
    return div;
}

// Las fechas llegan como "2026-03-10"; se arman como fecha local para evitar desfases de zona horaria
function fechaLocal(valor) {
    if (Array.isArray(valor)) return new Date(valor[0], valor[1] - 1, valor[2]);
    const [anio, mes, dia] = String(valor).split('-').map(Number);
    return new Date(anio, mes - 1, dia);
}

function formatear(fecha) {
    return fecha.toLocaleDateString('es-PY');
}

// ---------- Registro de la solicitud ----------

document.getElementById('solicitud-form').addEventListener('submit', async function(event) {
    event.preventDefault(); // Evita el envío estándar del formulario

    if (!productoSeleccionado) {
        alert('Elegí un producto registrado. Podés buscarlo por tipo, marca, modelo o número de serie.');
        document.getElementById('nroSerie').focus();
        return;
    }

    // 1. Datos del cliente
    const clienteData = {
        documento: document.getElementById('documento').value,
        nombre: document.getElementById('nombre').value,
        telefono: document.getElementById('telefono').value,
        correo: document.getElementById('correo').value
    };

    try {
        // Paso A: guardar el cliente
        const clienteRes = await fetch('/api/clientes', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(clienteData)
        });
        if (!clienteRes.ok) throw new Error('No se pudo guardar el cliente (HTTP ' + clienteRes.status + ')');
        const clienteSalvado = await clienteRes.json();

        // Paso B: guardar la solicitud enlazada al cliente y al producto existente.
        // La garantía del producto la enlaza el servidor automáticamente.
        const solicitudData = {
            cliente: { idCliente: clienteSalvado.idCliente },
            producto: { idProducto: productoSeleccionado.idProducto },
            descripcion: document.getElementById('descripcion').value
        };

        const solicitudRes = await fetch('/api/solicitudes', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(solicitudData)
        });
        if (!solicitudRes.ok) throw new Error('No se pudo guardar la solicitud (HTTP ' + solicitudRes.status + ')');
        const solicitudSalvada = await solicitudRes.json();

        alert(`¡Éxito! Solicitud registrada con el número ST-${solicitudSalvada.idSolicitud}`);
        window.location.href = 'index.html';

    } catch (error) {
        console.error('Error al registrar:', error);
        alert('Ocurrió un error al registrar la solicitud. Revisá la consola.');
    }
});