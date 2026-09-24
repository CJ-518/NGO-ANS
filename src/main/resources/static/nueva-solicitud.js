let clientes = [];               // Todos los clientes registrados (para sugerir documentos)
let clienteEncontrado = null;    // Cliente existente que coincide con el documento escrito (o null si es nuevo)
let productosCliente = [];       // Productos que ya le vendimos a ese cliente
let garantias = new Map();       // idProducto -> garantía (respuesta de /api/productos/{id}/garantia)
let productoSeleccionado = null; // Producto elegido en el menú desplegable
let indiceActivoDocumento = -1;  // Opción resaltada con el teclado en la lista de sugerencias de documento
let busquedaActual = 0;          // Identifica la búsqueda de cliente vigente, para descartar respuestas viejas
let documentoBuscado = null;     // Último documento ya buscado: evita repetir la búsqueda (y rearmar el menú) si no cambió

const normalizar = (texto) => (texto || '').trim().toLowerCase();
const selectProducto = document.getElementById('producto');

// ---------- Carga inicial y eventos ----------

(async function iniciar() {
    try {
        const res = await fetch('/api/clientes');
        if (!res.ok) throw new Error('HTTP ' + res.status);
        clientes = await res.json();
    } catch (error) {
        console.error('Error cargando los clientes:', error);
        clientes = [];
    }

    selectProducto.addEventListener('change', () => {
        const id = Number(selectProducto.value);
        mostrarProducto(productosCliente.find(p => p.idProducto === id) || null);
    });

    const inputDocumento = document.getElementById('documento');
    const listaDocumento = document.getElementById('sugerencias-documento');

    // Se filtran y muestran las sugerencias en cada tecleo, no solo al hacer click en el campo
    inputDocumento.addEventListener('input', () => {
        mostrarSugerenciasDocumento(inputDocumento.value);
    });

    // Navegación de la lista con el teclado (flechas, Enter para elegir, Escape para cerrar)
    inputDocumento.addEventListener('keydown', (e) => {
        const items = [...listaDocumento.children];
        if (items.length === 0) return;

        if (e.key === 'ArrowDown') {
            e.preventDefault();
            indiceActivoDocumento = (indiceActivoDocumento + 1) % items.length;
            resaltarSugerenciaActiva(items);
        } else if (e.key === 'ArrowUp') {
            e.preventDefault();
            indiceActivoDocumento = (indiceActivoDocumento - 1 + items.length) % items.length;
            resaltarSugerenciaActiva(items);
        } else if (e.key === 'Enter' && indiceActivoDocumento >= 0) {
            e.preventDefault();
            items[indiceActivoDocumento].dispatchEvent(new Event('mousedown'));
        } else if (e.key === 'Escape') {
            ocultarSugerenciasDocumento();
        }
    });

    // Al terminar de escribir el documento (Enter, Tab o click fuera), se busca el cliente
    inputDocumento.addEventListener('change', buscarCliente);

    // Un click fuera del campo o de la lista la cierra
    document.addEventListener('click', (e) => {
        if (e.target !== inputDocumento) ocultarSugerenciasDocumento();
    });
})();

// ---------- Sugerencias de documento (lista propia, no datalist nativo) ----------
//
// Se arma en el momento a partir de "clientes", buscando coincidencias tanto en el
// número de documento (solo si empieza con lo escrito) como en el nombre. Se ve el documento en primer plano (es lo
// que se completa en el campo) y el nombre al lado, para reconocer al cliente.
function mostrarSugerenciasDocumento(texto) {
    const contenedor = document.getElementById('sugerencias-documento');
    const escrito = normalizar(texto);
    indiceActivoDocumento = -1;

    if (!escrito) {
        ocultarSugerenciasDocumento();
        return;
    }

    const coincidencias = clientes
        .filter(c => normalizar(c.documento).startsWith(escrito) || normalizar(c.nombre).includes(escrito))
        .slice(0, 8);

    if (coincidencias.length === 0) {
        ocultarSugerenciasDocumento();
        return;
    }

    const items = coincidencias.map((c) => {
        const item = document.createElement('div');
        item.className = 'autocomplete-item';

        const doc = document.createElement('span');
        doc.className = 'autocomplete-doc';
        doc.textContent = c.documento;

        const nombre = document.createElement('span');
        nombre.className = 'autocomplete-nombre';
        nombre.textContent = c.nombre;

        item.append(doc, nombre);

        // mousedown (no click) para que se ejecute antes de que el input pierda el foco
        item.addEventListener('mousedown', (e) => {
            e.preventDefault();
            elegirSugerenciaDocumento(c);
        });
        return item;
    });

    contenedor.replaceChildren(...items);
    contenedor.style.display = 'block';
}

function elegirSugerenciaDocumento(cliente) {
    document.getElementById('documento').value = cliente.documento;
    ocultarSugerenciasDocumento();
    buscarCliente();
}

function ocultarSugerenciasDocumento() {
    const contenedor = document.getElementById('sugerencias-documento');
    contenedor.replaceChildren();
    contenedor.style.display = 'none';
    indiceActivoDocumento = -1;
}

function resaltarSugerenciaActiva(items) {
    items.forEach((item, i) => item.classList.toggle('activo', i === indiceActivoDocumento));
}

// ---------- Cliente: autocompletar datos y productos que ya compró ----------

async function buscarCliente() {
    const documento = document.getElementById('documento').value.trim();

    // Al hacer click en el menú de productos, el campo Documento pierde el foco y dispara 'change'.
    // Si el documento es el mismo que ya se buscó, no se rehace todo: rearmar el menú justo cuando
    // se está abriendo hace que se vea mal en el primer click.
    if (documento === documentoBuscado) return;
    documentoBuscado = documento;

    const busqueda = ++busquedaActual; // si llega otra búsqueda mientras esta espera, esta se descarta

    clienteEncontrado = null;
    productosCliente = [];
    garantias = new Map();
    pintarProductos('Primero ingresá el documento del cliente');
    mostrarEstadoCliente(null);

    if (!documento) return;

    try {
        const res = await fetch(`/api/clientes/buscar?documento=${encodeURIComponent(documento)}`);
        if (busqueda !== busquedaActual) return;

        if (res.status === 404) {
            mostrarEstadoCliente(false); // documento no registrado: es un cliente nuevo
            pintarProductos('Cliente nuevo: todavía no tiene productos registrados');
            return;
        }
        if (!res.ok) throw new Error('HTTP ' + res.status);

        const cliente = await res.json();
        if (busqueda !== busquedaActual) return;
        clienteEncontrado = cliente;

        // Se completan los demás datos; el usuario igual puede corregirlos antes de registrar la solicitud.
        document.getElementById('nombre').value = cliente.nombre || '';
        document.getElementById('telefono').value = cliente.telefono || '';
        document.getElementById('correo').value = cliente.correo || '';
        mostrarEstadoCliente(true);

        await cargarProductosDelCliente(cliente.idCliente, busqueda);
    } catch (error) {
        console.error('Error buscando el cliente:', error);
        documentoBuscado = null; // permite reintentar
    }
}

function mostrarEstadoCliente(encontrado) {
    const caja = document.getElementById('cliente-estado');

    if (encontrado === null) {
        caja.style.display = 'none';
        return;
    }

    caja.style.display = 'block';
    if (encontrado) {
        caja.textContent = 'Cliente encontrado: se completaron sus datos.';
        caja.style.backgroundColor = '#c6f6d5';
        caja.style.color = '#22543d';
    } else {
        caja.textContent = 'Cliente nuevo: completá sus datos.';
        caja.style.backgroundColor = '#ebf8ff';
        caja.style.color = '#2c5282';
    }
}

async function cargarProductosDelCliente(idCliente, busqueda) {
    let lista = [];
    try {
        const res = await fetch(`/api/clientes/${idCliente}/productos`);
        if (!res.ok) throw new Error('HTTP ' + res.status);
        lista = await res.json();
    } catch (error) {
        console.error('Error cargando los productos del cliente:', error);
        pintarProductos('No se pudieron cargar los productos del cliente');
        return;
    }

    // La garantía de cada producto se pide al servidor (fuente única del cálculo), en paralelo
    const pares = await Promise.all(lista.map(async (p) => {
        try {
            const res = await fetch(`/api/productos/${p.idProducto}/garantia`);
            return [p.idProducto, res.ok ? await res.json() : null];
        } catch (error) {
            console.error('Error consultando la garantía:', error);
            return [p.idProducto, null];
        }
    }));

    if (busqueda !== busquedaActual) return; // el usuario ya cambió de cliente

    productosCliente = lista;
    garantias = new Map(pares);
    pintarProductos();

    // Un solo producto: queda elegido directamente
    if (productosCliente.length === 1) {
        selectProducto.value = String(productosCliente[0].idProducto);
        mostrarProducto(productosCliente[0]);
    }
}

// ---------- Menú desplegable de productos ----------

const esVigente = (garantia) => garantia && garantia.estado === 'VIGENTE';

function etiquetaGarantia(garantia) {
    if (!garantia) return 'Garantía sin datos';
    return esVigente(garantia) ? '✔ En garantía' : '✖ Garantía vencida';
}

// Arma las opciones del menú con los productos del cliente. Cada opción muestra si la garantía
// está activa o no. Si no hay productos, deja el menú deshabilitado con el mensaje recibido.
function pintarProductos(mensajeVacio) {
    productoSeleccionado = null;
    mostrarProducto(null);

    const opciones = [];

    if (productosCliente.length === 0) {
        const vacia = document.createElement('option');
        vacia.value = '';
        vacia.textContent = mensajeVacio || 'Este cliente no tiene productos registrados';
        opciones.push(vacia);
    } else {
        const placeholder = document.createElement('option');
        placeholder.value = '';
        placeholder.textContent = productosCliente.length === 1
            ? 'Producto del cliente'
            : `Elegí un producto (${productosCliente.length} disponibles)`;
        opciones.push(placeholder);

        productosCliente.forEach((p) => {
            const garantia = garantias.get(p.idProducto);
            const opcion = document.createElement('option');
            opcion.value = p.idProducto;
            opcion.textContent = `${p.tipoProducto} · ${p.marca} ${p.modelo} (N/S ${p.nroSerie}) — ${etiquetaGarantia(garantia)}`;
            if (garantia) opcion.className = esVigente(garantia) ? 'garantia-vigente' : 'garantia-vencida';
            opciones.push(opcion);
        });
    }

    selectProducto.replaceChildren(...opciones);
    selectProducto.value = '';
    selectProducto.disabled = productosCliente.length === 0;
}

// ---------- Producto elegido y garantía ----------

function mostrarProducto(producto) {
    productoSeleccionado = producto;

    const caja = document.getElementById('info-producto');
    if (!producto) {
        caja.style.display = 'none';
        caja.replaceChildren();
        return;
    }

    caja.style.display = 'block';
    dibujarGarantia(caja, garantias.get(producto.idProducto));
}

function dibujarGarantia(caja, garantia) {
    if (!garantia) {
        caja.replaceChildren(linea('No se pudo consultar la garantía del producto.'));
        return;
    }

    const inicio = fechaLocal(garantia.fechaInicio);
    const fin = fechaLocal(garantia.fechaFin);
    const vigente = esVigente(garantia);

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
        alert('Elegí un producto del menú desplegable (primero ingresá el documento de un cliente registrado).');
        selectProducto.focus();
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
        // Paso A: guardar el cliente.
        // Si ya lo habíamos encontrado por documento, se actualiza (evita duplicarlo);
        // si es un cliente nuevo, se crea.
        let clienteSalvado;
        if (clienteEncontrado && clienteEncontrado.idCliente) {
            const clienteRes = await fetch(`/api/clientes/${clienteEncontrado.idCliente}`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(clienteData)
            });
            if (!clienteRes.ok) throw new Error('No se pudo actualizar el cliente (HTTP ' + clienteRes.status + ')');
            clienteSalvado = await clienteRes.json();
        } else {
            const clienteRes = await fetch('/api/clientes', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(clienteData)
            });
            if (!clienteRes.ok) throw new Error('No se pudo guardar el cliente (HTTP ' + clienteRes.status + ')');
            clienteSalvado = await clienteRes.json();
            clientes.push(clienteSalvado); // queda disponible como sugerencia sin recargar la página
        }

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
