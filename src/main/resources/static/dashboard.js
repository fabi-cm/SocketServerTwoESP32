document.addEventListener('DOMContentLoaded', function() {
    // Actualizar umbrales
    document.getElementById('threshold-form').addEventListener('submit', async function(e) {
        e.preventDefault();

        const x = parseFloat(document.getElementById('x-threshold').value);
        const y = parseFloat(document.getElementById('y-threshold').value);
        const z = parseFloat(document.getElementById('z-threshold').value);

        if (x >= y || y >= z) {
            alert('Los umbrales deben cumplir: x < y < z');
            return;
        }

        try {
            const response = await fetch('/led/thresholds', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ x, y, z })
            });

            if (response.ok) {
                alert('Umbrales actualizados correctamente');
                location.reload(); // Recargar para ver cambios
            } else {
                const error = await response.json();
                alert(`Error: ${error.error || 'Error desconocido'}`);
            }
        } catch (err) {
            alert('Error de conexión: ' + err.message);
        }
    });

    // Control manual de LEDs
    document.querySelectorAll('.led-button').forEach(button => {
        button.addEventListener('click', async function() {
            const led = this.dataset.led;

            try {
                // Obtener estado actual
                const response = await fetch('/actuator');
                const currentState = await response.text();

                // Parsear y cambiar el estado del LED específico
                const newState = currentState.split(',').map(item => {
                    if (item.startsWith(led)) {
                        return `${led}:${item.endsWith('0') ? '1' : '0'}`;
                    }
                    return item;
                }).join(',');

                // Enviar nuevo estado
                const updateResponse = await fetch('/actuator', {
                    method: 'POST',
                    body: newState
                });

                if (updateResponse.ok) {
                    location.reload(); // Recargar para ver cambios
                } else {
                    alert('Error al actualizar LED');
                }
            } catch (err) {
                alert('Error de conexión: ' + err.message);
            }
        });
    });

    // Conectar WebSocket para actualizaciones en tiempo real
    const socket = new WebSocket(`ws://${window.location.host}/actuator/ws`);

    socket.onmessage = function(event) {
        // Actualizar solo los elementos afectados sin recargar toda la página
        const newState = event.data;
        newState.split(',').forEach(item => {
            const [led, value] = item.split(':');
            const element = document.querySelector(`.led-status[data-led="${led}"]`);
            if (element) {
                element.textContent = value === '1' ? 'ON' : 'OFF';
                element.classList.toggle('on', value === '1');
                element.classList.toggle('off', value === '0');
            }
        });
    };

    // Opcional: Actualización periódica del estado (cada 5 segundos)
    setInterval(async () => {
        try {
            const response = await fetch('/');
            if (response.redirected) {
                window.location.href = response.url;
            }
        } catch (err) {
            console.log('Error de conexión:', err);
        }
    }, 5000);
});