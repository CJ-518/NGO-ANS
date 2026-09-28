const ROLES_LEGIBLES = {
    ADMINISTRADOR: 'Administrador',
    ATENCION: 'Atención',
    TECNICO: 'Técnico',
    VENDEDOR: 'Vendedor'
};

const nombreDeRol = (rol) => ROLES_LEGIBLES[rol] || rol || '';

// ---------- Mensajes ----------

function mostrarMensaje(texto, esError, idCaja = 'mensaje') {
    const caja = document.getElementById(idCaja);
    caja.textContent = texto;
    caja.style.display = 'block';
    caja.style.backgroundColor = esError ? '#fed7d7' : '#c6f6d5';
    caja.style.color = esError ? '#742a2a' : '#22543d';
}

function ocultarMensaje(idCaja = 'mensaje') {
    document.getElementById(idCaja).style.display = 'none';
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

    const tdAcciones = document.createElement('td');
    const btnClave = document.createElement('button');
    btnClave.type = 'button';
    btnClave.className = 'btn-primary';
    btnClave.style.cssText = 'padding: 6px 12px; font-size: 14px;';
    btnClave.textContent = 'Cambiar contraseña';
    btnClave.addEventListener('click', () => abrirModalClave(usuario));
    tdAcciones.append(btnClave);
    tr.append(tdAcciones);

    return tr;
}

function filaConMensaje(texto) {
    const tr = document.createElement('tr');
    const td = celda(texto);
    td.colSpan = 5;
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

// ---------- Cambio de contraseña (sin conocer la actual) ----------

let usuarioEnEdicion = null; // usuario al que se le está cambiando la contraseña

function abrirModalClave(usuario) {
    usuarioEnEdicion = usuario;
    ocultarMensaje('mensaje-lista');
    ocultarMensaje('mensaje-clave');
    document.getElementById('clave-form').reset();
    document.getElementById('modal-clave-usuario').textContent =
        `Usuario: ${usuario.nombre} (${usuario.correo})`;
    document.getElementById('modal-clave').style.display = 'flex';
    document.getElementById('nueva-clave').focus();
}

function cerrarModalClave() {
    document.getElementById('modal-clave').style.display = 'none';
    document.getElementById('clave-form').reset(); // no dejar contraseñas escritas en el formulario
    usuarioEnEdicion = null;
}

document.getElementById('modal-clave-cerrar').addEventListener('click', cerrarModalClave);
document.getElementById('clave-cancelar').addEventListener('click', cerrarModalClave);
document.getElementById('modal-clave').addEventListener('click', (event) => {
    // Un clic en el fondo oscuro (no dentro de la ventana) también cierra
    if (event.target.id === 'modal-clave') cerrarModalClave();
});
document.addEventListener('keydown', (event) => {
    if (event.key === 'Escape' && usuarioEnEdicion) cerrarModalClave();
});

document.getElementById('clave-form').addEventListener('submit', async function(event) {
    event.preventDefault();
    ocultarMensaje('mensaje-clave');

    if (!usuarioEnEdicion) return;

    const clave = document.getElementById('nueva-clave').value;
    const clave2 = document.getElementById('nueva-clave2').value;

    if (clave !== clave2) {
        mostrarMensaje('Las contraseñas no coinciden.', true, 'mensaje-clave');
        return;
    }

    const boton = this.querySelector('button[type="submit"]');
    boton.disabled = true; // evita enviar dos veces el mismo cambio

    try {
        const res = await fetch(`/api/usuarios/${usuarioEnEdicion.idUsuario}/clave`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ clave })
        });

        let datos = null;
        try { datos = await res.json(); } catch (e) { /* la respuesta puede no traer JSON */ }

        if (res.ok) {
            const cambiado = usuarioEnEdicion;
            cerrarModalClave();
            mostrarMensaje(`Contraseña actualizada para ${cambiado.nombre}. Deberá usar la nueva en su próximo inicio de sesión.`,
                false, 'mensaje-lista');
        } else if (res.status === 403) {
            mostrarMensaje('No tenés permiso para cambiar contraseñas.', true, 'mensaje-clave');
        } else if (res.status === 404) {
            cerrarModalClave();
            mostrarMensaje('El usuario ya no existe.', true, 'mensaje-lista');
            cargarUsuarios();
        } else {
            mostrarMensaje((datos && datos.error) || 'No se pudo cambiar la contraseña.', true, 'mensaje-clave');
        }
    } catch (error) {
        console.error('Error al cambiar la contraseña:', error);
        mostrarMensaje('Error de conexión al cambiar la contraseña.', true, 'mensaje-clave');
    } finally {
        boton.disabled = false;
    }
});

cargarUsuarios();
