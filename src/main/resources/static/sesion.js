// Muestra en el encabezado quién tiene la sesión iniciada y con qué rol,
// y un botón para cerrar la sesión. Se incluye en todas las páginas internas.
(async function mostrarSesion() {
    const encabezado = document.querySelector('header');
    if (!encabezado) return;

    const ROLES_LEGIBLES = {
        ADMINISTRADOR: 'Administrador',
        ATENCION: 'Atención',
        TECNICO: 'Técnico',
        VENDEDOR: 'Vendedor'
    };

    let usuario;
    try {
        const res = await fetch('/api/usuario/actual');
        if (!res.ok) return;
        usuario = await res.json();
    } catch (error) {
        // Sin sesión válida (o sin conexión): no se muestra nada
        console.error('No se pudo obtener el usuario actual:', error);
        return;
    }

    // textContent en todos los textos: el nombre viene de la base de datos y no debe interpretarse como HTML
    const etiqueta = (texto) => {
        const span = document.createElement('span');
        span.textContent = texto;
        span.style.opacity = '0.8';
        return span;
    };

    const nombre = document.createElement('strong');
    nombre.id = 'sesion-nombre';
    nombre.textContent = usuario.nombre;
    nombre.title = usuario.correo || '';

    const rol = document.createElement('span');
    rol.id = 'sesion-rol';
    rol.textContent = ROLES_LEGIBLES[usuario.rol] || usuario.rol;
    rol.style.cssText = 'display: inline-block; padding: 3px 10px; border-radius: 12px; ' +
        'background-color: rgba(255, 255, 255, 0.18); font-weight: bold; font-size: 13px;';

    const cerrar = document.createElement('button');
    cerrar.id = 'sesion-cerrar';
    cerrar.type = 'button';
    cerrar.textContent = 'Cerrar sesión';
    cerrar.style.cssText = 'background: transparent; color: white; border: 1px solid rgba(255, 255, 255, 0.6); ' +
        'border-radius: 4px; padding: 4px 12px; cursor: pointer; font-size: 13px;';
    cerrar.addEventListener('click', async () => {
        try {
            await fetch('/logout', { method: 'POST' });
            window.location.href = 'login.html?logout';
        } catch (error) {
            console.error('Error al cerrar la sesión:', error);
            alert('No se pudo cerrar la sesión. Intentá de nuevo.');
        }
    });

    const caja = document.createElement('div');
    caja.id = 'sesion-info';
    caja.style.cssText = 'display: flex; align-items: center; gap: 8px; flex-wrap: wrap; font-size: 14px;';
    caja.append(etiqueta('Usuario:'), nombre, etiqueta('· Rol:'), rol, cerrar);

    encabezado.style.flexWrap = 'wrap';
    encabezado.style.gap = '10px';
    encabezado.append(caja);
})();