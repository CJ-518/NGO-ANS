document.getElementById('solicitud-form').addEventListener('submit', async function(event) {
    event.preventDefault(); // Prevent standard HTML form submission

    // 1. Gather Client Data
    const clienteData = {
        documento: document.getElementById('documento').value,
        nombre: document.getElementById('nombre').value,
        telefono: document.getElementById('telefono').value,
        correo: document.getElementById('correo').value
    };

    // 2. Gather Product Data
    const productoData = {
        nroSerie: document.getElementById('nroSerie').value,
        marca: document.getElementById('marca').value,
        modelo: document.getElementById('modelo').value,
        tipoProducto: document.getElementById('tipoProducto').value
    };

    try {
        // Step A: Save Cliente
        const clienteRes = await fetch('/api/clientes', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(clienteData)
        });
        const clienteSalvado = await clienteRes.json();

        // Step B: Save Producto
        const productoRes = await fetch('/api/productos', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(productoData)
        });
        const productoSalvado = await productoRes.json();

        // Step C: Save Solicitud (Linking A and B)
        const solicitudData = {
            cliente: { idCliente: clienteSalvado.idCliente },
            producto: { idProducto: productoSalvado.idProducto },
            descripcion: document.getElementById('descripcion').value
        };

        const solicitudRes = await fetch('/api/solicitudes', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(solicitudData)
        });
        const solicitudSalvada = await solicitudRes.json();

        // Success! Alert user and redirect
        alert(`¡Éxito! Solicitud registrada con el número ST-${solicitudSalvada.idSolicitud}`);
        window.location.href = 'index.html';

    } catch (error) {
        console.error('Error al registrar:', error);
        alert('Ocurrió un error al registrar la solicitud. Revisa la consola.');
    }
});