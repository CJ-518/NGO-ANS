document.addEventListener('DOMContentLoaded', () => {
    cargarSolicitudes();
});

function cargarSolicitudes() {
    fetch('/api/solicitudes')
        .then(response => response.json())
        .then(data => {
            const tableBody = document.getElementById('solicitudes-body');
            tableBody.innerHTML = ''; // Limpiar la tabla antes de cargar
            document.getElementById('total-abiertas').innerText = data.length;

            // Definir los estados oficiales del diagrama
            const estados = ['RECIBIDA', 'ASIGNADA', 'EN DIAGNÓSTICO', 'FINALIZADA'];

            data.forEach(solicitud => {
                const date = new Date(solicitud.fecha).toLocaleDateString('es-PY');
                
                // Generar el menú desplegable (Se agregó 'this' al onchange)
                let selectHtml = `<select class="estado-select" onchange="actualizarEstado(${solicitud.idSolicitud}, this.value, this)">`;
                estados.forEach(est => {
                    let isSelected = (est === solicitud.estadoActual) ? 'selected' : '';
                    selectHtml += `<option value="${est}" ${isSelected}>${est}</option>`;
                });
                selectHtml += `</select>`;
                
                const row = `<tr>
                    <td>ST-${solicitud.idSolicitud}</td>
                    <td>${solicitud.cliente.nombre}</td>
                    <td>${solicitud.producto.tipoProducto}</td>
                    <td>${selectHtml}</td>
                    <td>${date}</td>
                </tr>`;
                tableBody.innerHTML += row;
            });
        })
        .catch(error => console.error('Error fetching data:', error));
}

// Función global (Se agregó selectElement como parámetro)
window.actualizarEstado = async function(id, nuevoEstado, selectElement) {
    try {
        const response = await fetch(`/api/solicitudes/${id}/estado`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ estado: nuevoEstado })
        });
        
        if (response.ok) {
            console.log(`Solicitud ${id} actualizada a ${nuevoEstado}`);
            
            // Animación visual corregida
            selectElement.style.backgroundColor = '#e6fffa'; // Verde claro
            setTimeout(() => selectElement.style.backgroundColor = 'white', 1000); // Vuelve a blanco
        } else {
            alert('Error al actualizar la base de datos');
        }
    } catch (error) {
        console.error('Error:', error);
    }
};