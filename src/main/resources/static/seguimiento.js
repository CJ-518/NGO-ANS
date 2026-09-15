document.getElementById('btn-buscar').addEventListener('click', async () => {
    const id = document.getElementById('nro-solicitud').value;
    if (!id) {
        alert("Por favor ingrese un número de solicitud");
        return;
    }

    try {
        const response = await fetch(`/api/solicitudes/${id}`);
        
        if (!response.ok || response.status === 204) {
            alert("No se encontró ninguna solicitud con ese número.");
            document.getElementById('resultado-container').style.display = 'none';
            return;
        }

        const solicitud = await response.json();
        if (!solicitud) return;

        // Mostrar el contenedor
        document.getElementById('resultado-container').style.display = 'block';

        // Llenar datos básicos
        document.getElementById('estado-actual').innerText = solicitud.estadoActual;
        
        const fecha = new Date(solicitud.fecha).toLocaleString('es-PY');
        document.getElementById('fecha-actualizacion').innerText = fecha;

        // Llenar tabla de detalles
        const historialBody = document.getElementById('historial-body');
        historialBody.innerHTML = `
            <tr>
                <td style="width: 200px; font-weight: bold;">Cliente</td>
                <td>${solicitud.cliente.nombre}</td>
            </tr>
            <tr>
                <td style="font-weight: bold;">Producto</td>
                <td>${solicitud.producto.marca} ${solicitud.producto.modelo} (SN: ${solicitud.producto.nroSerie})</td>
            </tr>
            <tr>
                <td style="font-weight: bold;">Problema reportado</td>
                <td>${solicitud.descripcion}</td>
            </tr>
        `;

        // Lógica visual dinámica para la línea de tiempo
        const steps = document.querySelectorAll('.timeline-step');
        const lines = document.querySelectorAll('.timeline-line');
        
        // 1. Apagar todos los círculos y líneas
        steps.forEach(s => s.classList.remove('active'));
        lines.forEach(l => l.classList.remove('active'));

        // 2. Mapear el texto del estado a un número
        const estadosMap = {
            'RECIBIDA': 0,
            'ASIGNADA': 1,
            'EN DIAGNÓSTICO': 2,
            'FINALIZADA': 3
        };
        
        const estadoActual = solicitud.estadoActual.toUpperCase();
        const limite = estadosMap[estadoActual] !== undefined ? estadosMap[estadoActual] : 0;

        // 3. Encender los círculos hasta el estado actual
        for (let i = 0; i <= limite; i++) {
            if (steps[i]) steps[i].classList.add('active');
            
            // Encender la línea conectora (si no es el último paso actual)
            if (i < limite && lines[i]) lines[i].classList.add('active');
        }

    } catch (error) {
        console.error("Error consultando la solicitud:", error);
    }
});