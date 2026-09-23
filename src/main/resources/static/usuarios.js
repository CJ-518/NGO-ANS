const ROLES_LEGIBLES = {
    ADMINISTRADOR: 'Administrador',
    ATENCION: 'Atención',
    TECNICO: 'Técnico',
    VENDEDOR: 'Vendedor'
};

const nombreDeRol = (rol) => ROLES_LEGIBLES[rol] || rol || '';

// ---------- Mensajes ----------

function mostrarMensaje(texto, esError) {
    const caja = document.getElementById('mensaje');
    caja.textContent = texto;
    caja.style.display = 'block';
    caja.style.backgroundColor = esError ? '#fed7d7' : '#c6f6d5';
    caja.style.color = esError ? '#742a2a' : '#22543d';
}

function ocultarMensaje() {
    document.getElementById('mensaje').style.display = 'none';
}

// ---------- Listado ----------

function celda(texto) {
    const td = document.createElement('td');
    td.textContent = texto; // textContent evita interpretar HTML escrito por el usuario
    return td;
}

function filaDe(usuario) {
    const tr = document.createElement('tr');
    tr.append(celda(usuario.nombre), celda(usuario.correo), celda(nombreDeRol(usuario.rol)), celda(usuario.estado));
    return tr;
}

function filaConMensaje(texto) {
    const tr = document.createElement('tr');
    const td = celda(texto);
    td.colSpan = 4;
    tr.append(td);
    return tr;
}

async function cargarUsuarios() {
    const cuerpo = document.getElementById('usuarios-body');
    try {
        const res = await fetch('/api/usuarios');
        if (!res.ok) throw new Error('HTTP ' + res.status);
        const usuarios = await res.json();
        cuerpo.replaceChildren(...(usuarios.length ? usuarios.map(filaDe) : [filaConMensaje('Todavía no hay usuarios registrados.')]));
    } catch (error) {
        console.error('Error cargando los usuarios:', error);
        cuerpo.replaceChildren(filaConMensaje('No se pudo cargar la lista de usuarios.'));
    }
}

// ---------- Alta de usuario ----------

document.getElementById('usuario-form').addEventListener('submit', async function(event) {
    event.preventDefault();
    ocultarMensaje();

    const nombre = document.getElementById('nombre').value;
    const correo = document.getElementById('correo').value;
    const clave = document.getElementById('clave').value;
    const clave2 = document.getElementById('clave2').value;
    const rol = document.getElementById('rol').value;

    if (clave !== clave2) {
        mostrarMensaje('Las contraseñas no coinciden.', true);
        return;
    }

    const boton = this.querySelector('button[type="submit"]');
    boton.disabled = true; // evita enviar dos veces el mismo alta

    try {
        const res = await fetch('/api/usuarios', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ nombre, correo, clave, rol })
        });

        let datos = null;
        try { datos = await res.json(); } catch (e) { /* la respuesta puede no traer JSON */ }

        if (res.status === 201) {
            this.reset();
            mostrarMensaje(`Usuario creado: ${datos.nombre} (${nombreDeRol(datos.rol)}). Ya puede iniciar sesión con ${datos.correo}.`, false);
            cargarUsuarios();
        } else if (res.status === 403) {
            mostrarMensaje('No tenés permiso para crear usuarios.', true);
        } else {
            mostrarMensaje((datos && datos.error) || 'No se pudo crear el usuario.', true);
        }
    } catch (error) {
        console.error('Error al crear el usuario:', error);
        mostrarMensaje('Error de conexión al crear el usuario.', true);
    } finally {
        boton.disabled = false;
    }
});

cargarUsuarios();