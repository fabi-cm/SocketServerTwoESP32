<#-- Definición correcta de la macro statusIcon -->
<#function statusIcon connected>
    <#return connected?then("✅", "❌")>
</#function>

<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>📡 IoT Monitoring Dashboard</title>
    <link rel="stylesheet" href="/static/styles.css">
</head>
<body>
    <div class="dashboard-container">
        <header class="dashboard-header">
            <h1>📡 IoT Monitoring Dashboard</h1>
        </header>

        <div class="dashboard-section">
            <div class="node-card">
                <h2>Sensor Node (ESP32 @ 192.168.0.12)
                    <span class="status-badge">[Status: ${statusIcon(sensorStatus)}
                        <#if sensorStatus>ON<#else>OFFLINE</#if>]</span>
                </h2>
                <div class="divider"></div>

                <div class="sensor-data">
                    <p>📏 Distance: <span id="distance-value">${lastDistance!} cm</span></p>
                    <p>🟢 Interval Active: <span id="current-interval">${currentInterval!}</span></p>
                    <p>⚙️ Change Intervals: [x=${thresholds.x}] [y=${thresholds.y}] [z=${thresholds.z}]</p>
                </div>
            </div>

            <div class="node-card">
                <h2>Actuator Node (ESP32 @ 192.168.0.11)
                    <span class="status-badge">[Status: ${statusIcon(actuatorStatus)}
                    <#if actuatorStatus>ON<#else>OFFLINE</#if>]</span>
                </h2>
                <div class="divider"></div>

                <div class="actuator-data">
                    <p>🔴 rLED: <span class="led-status ${ledStatus["tled"]?then("on", "off")}">${ledStatus["tled"]?then("ON", "OFF")}</span></p>
                    <p>🟡 yLED: <span class="led-status ${ledStatus["yled"]?then("on", "off")}">${ledStatus["yled"]?then("ON", "OFF")}</span></p>
                    <p>🟢 gLED: <span class="led-status ${ledStatus["gled"]?then("on", "off")}">${ledStatus["gled"]?then("ON", "OFF")}</span></p>
                </div>
            </div>
        </div>

        <div class="controls-section">
            <h2>⚙️ System Controls</h2>

            <div class="control-group">
                <h3>Set Intervals (x < y < z):</h3>
                <form id="threshold-form">
                    <div class="input-group">
                        <label>x:</label>
                        <input type="number" id="x-threshold" value="${thresholds.x}" step="0.1" required>
                    </div>
                    <div class="input-group">
                        <label>y:</label>
                        <input type="number" id="y-threshold" value="${thresholds.y}" step="0.1" required>
                    </div>
                    <div class="input-group">
                        <label>z:</label>
                        <input type="number" id="z-threshold" value="${thresholds.z}" step="0.1" required>
                    </div>
                    <button type="submit">Update Intervals</button>
                </form>
            </div>

            <div class="control-group">
                <h3>Manual Toggle LEDs (for test)</h3>
                <div class="led-controls">
                    <button class="led-button red" data-led="tled">Toggle rLED</button>
                    <button class="led-button yellow" data-led="yled">Toggle yLED</button>
                    <button class="led-button green" data-led="gled">Toggle gLED</button>
                </div>
            </div>
        </div>

        <div class="logs-section">
            <h2>📜 Logs (Últimos eventos)</h2>
            <div class="log-container">
                <#list logs as log>
                    <p class="log-entry">${log}</p>
                </#list>
            </div>
        </div>
    </div>

    <script src="/static/dashboard.js"></script>
</body>
</html>