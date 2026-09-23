let rolActual = null;
let articulos = [];
// carrito: Map idArticulo -> { articulo, cantidad }
const carrito = new Map();

const formatoGs = (numero) => 'Gs. ' + Number(numero).toLocaleString('es-PY', { maximumFractionDigits: 0 });

function mostrarMensaje(elementoId, texto, tipo) {
    const el = document.getElementById(elementoId);
    el.textContent = texto;
    el.style.display = 'block';
    el.style.backgroundColor = tipo === 'error' ? '#fed7d7' : '#c6f6d5';
    el.style.color = tipo === 'error' ? '#742a2a' : '#22543d';
}

function ocultarMensaje(elementoId) {
    const el = document.getElementById(elementoId);
    el.style.display = 'none';
}

async function cargarUsuarioActual() {
    try {
        const res = await fetch('/api/usuario/actual');
        if (!res.ok) return;
        const u = await res.json();
        rolActual = u.rol;
        if (rolActual === 'ADMINISTRADOR') {
            document.getElementById('seccion-nuevo-articulo').style.display = 'block';
        }
    } catch (error) {
        console.error('Error cargando el usuario actual:', error);
    }
}

async function cargarArticulos() {
    try {
        const res = await fetch('/api/articulos');
        if (!res.ok) return;
        articulos = await res.json();
        pintarArticulos();
    } catch (error) {
        console.error('Error cargando el catálogo:', error);
    }
}

function pintarArticulos() {
    const tbody = document.getElementById('articulos-body');
    tbody.innerHTML = '';

    if (articulos.length === 0) {
        tbody.innerHTML = '<tr><td colspan="6" style="color: #a0aec0;">No hay artículos cargados en el catálogo.</td></tr>';
        return;
    }

    articulos.forEach((articulo) => {
        const fila = document.createElement('tr');

        const tdNombre = document.createElement('td');
        tdNombre.textContent = articulo.nombre;
        if (articulo.descripcion) tdNombre.title = articulo.descripcion;

        const tdCategoria = document.createElement('td');
        tdCategoria.textContent = articulo.categoria || '-';

        const tdPrecio = document.createElement('td');
        tdPrecio.textContent = formatoGs(articulo.precio);

        const tdStock = document.createElement('td');
        tdStock.textContent = articulo.stock;

        const tdCantidad = document.createElement('td');
        const inputCantidad = document.createElement('input');
        inputCantidad.type = 'text';
        inputCantidad.inputMode = 'numeric';
        inputCantidad.style.width = '70px';
        inputCantidad.value = articulo.stock > 0 ? '1' : '0';
        inputCantidad.disabled = articulo.stock <= 0;
        tdCantidad.appendChild(inputCantidad);

        const tdAccion = document.createElement('td');
        const btnAgregar = document.createElement('button');
        btnAgregar.type = 'button';
        btnAgregar.className = 'btn-primary';
        btnAgregar.textContent = articulo.stock > 0 ? 'Agregar' : 'Sin stock';
        btnAgregar.disabled = articulo.stock <= 0;
        btnAgregar.addEventListener('click', () => {
            const cantidad = parseInt(inputCantidad.value, 10);
            agregarAlCarrito(articulo, cantidad);
        });
        tdAccion.appendChild(btnAgregar);

        fila.append(tdNombre, tdCategoria, tdPrecio, tdStock, tdCantidad, tdAccion);
        tbody.appendChild(fila);
    });
}

function agregarAlCarrito(articulo, cantidad) {
    ocultarMensaje('venta-mensaje');

    if (!Number.isInteger(cantidad) || cantidad <= 0) {
        mostrarMensaje('venta-mensaje', 'Ingresá una cantidad válida.', 'error');
        return;
    }

    const enCarrito = carrito.get(articulo.idArticulo);
    const cantidadTotal = (enCarrito ? enCarrito.cantidad : 0) + cantidad;

    if (cantidadTotal > articulo.stock) {
        mostrarMensaje('venta-mensaje', `Solo hay ${articulo.stock} unidades disponibles de "${articulo.nombre}".`, 'error');
        return;
    }

    carrito.set(articulo.idArticulo, { articulo, cantidad: cantidadTotal });
    pintarCarrito();
}

function quitarDelCarrito(idArticulo) {
    carrito.delete(idArticulo);
    pintarCarrito();
}

function pintarCarrito() {
    const tbody = document.getElementById('carrito-body');
    tbody.innerHTML = '';

    if (carrito.size === 0) {
        tbody.innerHTML = '<tr id="carrito-vacio"><td colspan="5" style="color: #a0aec0;">El carrito está vacío.</td></tr>';
        document.getElementById('carrito-total').textContent = formatoGs(0);
        return;
    }

    let total = 0;
    carrito.forEach(({ articulo, cantidad }) => {
        const subtotal = articulo.precio * cantidad;
        total += subtotal;

        const fila = document.createElement('tr');

        const tdNombre = document.createElement('td');
        tdNombre.textContent = articulo.nombre;

        const tdCantidad = document.createElement('td');
        tdCantidad.textContent = cantidad;

        const tdPrecio = document.createElement('td');
        tdPrecio.textContent = formatoGs(articulo.precio);

        const tdSubtotal = document.createElement('td');
        tdSubtotal.textContent = formatoGs(subtotal);

        const tdAccion = document.createElement('td');
        const btnQuitar = document.createElement('button');
        btnQuitar.type = 'button';
        btnQuitar.className = 'btn-primary';
        btnQuitar.style.backgroundColor = '#c53030';
        btnQuitar.textContent = 'Quitar';
        btnQuitar.addEventListener('click', () => quitarDelCarrito(articulo.idArticulo));
        tdAccion.appendChild(btnQuitar);

        fila.append(tdNombre, tdCantidad, tdPrecio, tdSubtotal, tdAccion);
        tbody.appendChild(fila);
    });

    document.getElementById('carrito-total').textContent = formatoGs(total);
}

async function confirmarVenta() {
    ocultarMensaje('venta-mensaje');

    if (carrito.size === 0) {
        mostrarMensaje('venta-mensaje', 'Agregá al menos un artículo antes de confirmar la venta.', 'error');
        return;
    }

    const items = Array.from(carrito.values()).map(({ articulo, cantidad }) => ({
        idArticulo: articulo.idArticulo,
        cantidad
    }));

    try {
        const res = await fetch('/api/ventas', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ items })
        });

        if (!res.ok) {
            const cuerpo = await res.json().catch(() => ({}));
            mostrarMensaje('venta-mensaje', cuerpo.error || 'No se pudo registrar la venta.', 'error');
            return;
        }

        carrito.clear();
        pintarCarrito();
        mostrarMensaje('venta-mensaje', 'Venta registrada correctamente.', 'exito');
        await Promise.all([cargarArticulos(), cargarVentas()]);
    } catch (error) {
        console.error('Error registrando la venta:', error);
        mostrarMensaje('venta-mensaje', 'Error de conexión al registrar la venta.', 'error');
    }
}

async function cargarVentas() {
    try {
        const res = await fetch('/api/ventas');
        if (!res.ok) return;
        const ventas = await res.json();
        pintarVentas(ventas);
    } catch (error) {
        console.error('Error cargando las ventas:', error);
    }
}

function pintarVentas(ventas) {
    const tbody = document.getElementById('ventas-body');
    tbody.innerHTML = '';

    if (ventas.length === 0) {
        tbody.innerHTML = '<tr><td colspan="5" style="color: #a0aec0;">Todavía no hay ventas registradas.</td></tr>';
        return;
    }

    ventas.forEach((venta) => {
        const fila = document.createElement('tr');

        const cantidadArticulos = (venta.detalles || []).reduce((suma, d) => suma + d.cantidad, 0);
        const fecha = venta.fecha ? new Date(venta.fecha).toLocaleString('es-PY') : '-';

        [venta.idVenta, fecha, venta.vendedor ? venta.vendedor.nombre : '-', cantidadArticulos, formatoGs(venta.total)]
            .forEach((valor) => {
                const td = document.createElement('td');
                td.textContent = valor;
                fila.appendChild(td);
            });

        tbody.appendChild(fila);
    });
}

async function crearArticulo(evento) {
    evento.preventDefault();
    ocultarMensaje('articulo-mensaje');

    const nombre = document.getElementById('art-nombre').value.trim();
    const categoria = document.getElementById('art-categoria').value.trim();
    const descripcion = document.getElementById('art-descripcion').value.trim();
    const precio = parseFloat(document.getElementById('art-precio').value.replace(',', '.'));
    const stock = parseInt(document.getElementById('art-stock').value, 10);

    if (!nombre) {
        mostrarMensaje('articulo-mensaje', 'El nombre es obligatorio.', 'error');
        return;
    }
    if (Number.isNaN(precio) || precio < 0) {
        mostrarMensaje('articulo-mensaje', 'Ingresá un precio válido.', 'error');
        return;
    }
    if (Number.isNaN(stock) || stock < 0) {
        mostrarMensaje('articulo-mensaje', 'Ingresá un stock inicial válido.', 'error');
        return;
    }

    try {
        const res = await fetch('/api/articulos', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ nombre, categoria, descripcion, precio, stock })
        });

        if (!res.ok) {
            const cuerpo = await res.json().catch(() => ({}));
            mostrarMensaje('articulo-mensaje', cuerpo.error || 'No se pudo crear el artículo.', 'error');
            return;
        }

        document.getElementById('articulo-form').reset();
        document.getElementById('art-stock').value = '0';
        mostrarMensaje('articulo-mensaje', 'Artículo agregado al catálogo.', 'exito');
        await cargarArticulos();
    } catch (error) {
        console.error('Error creando el artículo:', error);
        mostrarMensaje('articulo-mensaje', 'Error de conexión al crear el artículo.', 'error');
    }
}

document.addEventListener('DOMContentLoaded', async () => {
    await cargarUsuarioActual();
    await Promise.all([cargarArticulos(), cargarVentas()]);

    document.getElementById('btn-confirmar-venta').addEventListener('click', confirmarVenta);

    const formArticulo = document.getElementById('articulo-form');
    if (formArticulo) formArticulo.addEventListener('submit', crearArticulo);
});