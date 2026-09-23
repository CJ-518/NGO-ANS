// Campos del producto: coinciden con los nombres del JSON y con los id del formulario
const CAMPOS = ['tipoProducto', 'marca', 'modelo', 'nroSerie'];

let productos = [];              // Catálogo completo de productos registrados
let productoSeleccionado = null; // Producto que coincide exactamente con los cuatro campos

let clientes = [];               // Todos los clientes registrados (para sugerir documentos)
let clienteEncontrado = null;    // Cliente existente que coincide con el documento escrito (o null si es nuevo)
let productosCliente = [];       // Productos que ya le vendimos a ese cliente
let indiceActivoDocumento = -1;  // Opción resaltada con el teclado en la lista de sugerencias de documento

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

    try {
        const res = await fetch('/api/clientes');
        if (!res.ok) throw new Error('HTTP ' + res.status);
        clientes = await res.json();
    } catch (error) {
        console.error('Error cargando los clientes:', error);
        clientes = [];
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

    actualizarSugerencias();
})();

// ---------- Sugerencias de documento (lista propia, no datalist nativo) ----------
//
// Se arma en el momento a partir de "clientes", buscando coincidencias tanto en el
// número de documento como en el nombre. Se ve el documento en primer plano (es lo
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
        .filter(c => normalizar(c.documento).includes(escrito) || normalizar(c.nombre).includes(escrito))
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

    clienteEncontrado = null;
    productosCliente = [];
    limpiarProducto(); // se limpia lo elegido para el cliente/documento anterior
    pintarProductosCliente();
    mostrarEstadoCliente(null);

    if (!documento) return;

    try {
        const res = await fetch(`/api/clientes/buscar?documento=${encodeURIComponent(documento)}`);

        if (res.status === 404) {
            mostrarEstadoCliente(false); // documento no registrado: es un cliente nuevo
            return;
        }
        if (!res.ok) throw new Error('HTTP ' + res.status);

        clienteEncontrado = await res.json();

        // Se completan los demás datos; el usuario igual puede corregirlos antes de registrar la solicitud.
        document.getElementById('nombre').value = clienteEncontrado.nombre || '';
        document.getElementById('telefono').value = clienteEncontrado.telefono || '';
        document.getElementById('correo').value = clienteEncontrado.correo || '';
        mostrarEstadoCliente(true);

        await cargarProductosDelCliente(clienteEncontrado.idCliente);
    } catch (error) {
        console.error('Error buscando el cliente:', error);
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

async function cargarProductosDelCliente(idCliente) {
    try {
        const res = await fetch(`/api/clientes/${idCliente}/productos`);
        if (!res.ok) throw new Error('HTTP ' + res.status);
        productosCliente = await res.json();
    } catch (error) {
        console.error('Error cargando los productos del cliente:', error);
        productosCliente = [];
    }
    pintarProductosCliente();

    // Un solo producto: se autocompleta directo. Varios: se muestran para elegir cuál (pintarProductosCliente).
    if (productosCliente.length === 1) {
        elegirProductoDeCliente(productosCliente[0]);
    }
}

// Dibuja los botones con los productos de este cliente. Se oculta todo si no hay ninguno.
// El producto ya elegido (productoSeleccionado) queda resaltado.
function pintarProductosCliente() {
    const contenedor = document.getElementById('productos-cliente');
    const lista = document.getElementById('productos-cliente-lista');
    lista.replaceChildren();

    if (productosCliente.length === 0) {
        contenedor.style.display = 'none';
        return;
    }

    document.getElementById('productos-cliente-titulo').textContent =
        productosCliente.length === 1
            ? 'Producto de este cliente (completado automáticamente)'
            : `Este cliente tiene ${productosCliente.length} productos: elegí cuál corresponde`;

    productosCliente.forEach((p) => {
        const elegido = productoSeleccionado && productoSeleccionado.idProducto === p.idProducto;

        const boton = document.createElement('button');
        boton.type = 'button';
        boton.textContent = `${p.tipoProducto} · ${p.marca} ${p.modelo} (N/S ${p.nroSerie})`;
        boton.style.cssText = 'padding: 6px 12px; border-radius: 20px; cursor: pointer; font-size: 13px; ' +
            (elegido
                ? 'border: 1px solid #1a365d; background-color: #1a365d; color: #fff;'
                : 'border: 1px solid #1a365d; background-color: #fff; color: #1a365d;');
        boton.addEventListener('click', () => elegirProductoDeCliente(p));
        lista.appendChild(boton);
    });

    contenedor.style.display = 'block';
}

// Un clic en un producto del cliente completa los cuatro campos, igual que si se hubiera
// escrito y encontrado una única coincidencia en la búsqueda manual.
function elegirProductoDeCliente(producto) {
    CAMPOS.forEach(c => { document.getElementById(c).value = producto[c]; });
    actualizarSugerencias();
    mostrarProducto(producto);
    pintarProductosCliente(); // refresca cuál queda resaltado como elegido
}

// Limpia los cuatro campos del producto y lo deselecciona (se usa al cambiar de cliente).
function limpiarProducto() {
    CAMPOS.forEach(c => { document.getElementById(c).value = ''; });
    actualizarSugerencias();
    mostrarProducto(null);
}

// ---------- Sugerencias (búsqueda manual por tipo / marca / modelo / N° de serie) ----------

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