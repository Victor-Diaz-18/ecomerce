// Ejecuta el Maven Wrapper correcto según el sistema operativo (mvnw.cmd en Windows, ./mvnw en Linux/macOS).
const { spawnSync } = require('node:child_process');
const path = require('node:path');

const root = path.join(__dirname, '..');
const isWindows = process.platform === 'win32';
const wrapper = path.join(root, isWindows ? 'mvnw.cmd' : 'mvnw');

const result = spawnSync(isWindows ? `"${wrapper}"` : wrapper, process.argv.slice(2), { cwd: root, stdio: 'inherit', shell: isWindows });
process.exit(result.status ?? 1);
