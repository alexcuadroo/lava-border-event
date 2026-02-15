# Extreme Rising Lava

Un mod sincronizado de eventos para Minecraft NeoForge que combina la contracción del WorldBorder con capas de lava ascendentes y una bossbar dinámica.

## 📋 Características

- **WorldBorder Sincronizado**: Reduce progresivamente el área jugable del mundo
- **Lava Ascendente**: Las capas de lava suben gradualmente, obligando a los jugadores a subir
- **Bossbar Dinámica**: Visualización en tiempo real del progreso del evento
- **Sistema de Configuración**: Personalizable mediante archivos de configuración
- **Comandos Administrativos**: Control total del evento mediante comandos

## 🛠️ Requisitos

- **Java 21+** (NeoForge requiere Java 21)
- **Gradle** 8.0+ (incluido con el wrapper)
- **Minecraft 1.21.8** (compatible con rango 1.21.1 - 1.22)
- **NeoForge 21.8.40+**

## 📦 Instalación

### Método 1: Compilar desde Código Fuente

#### Requisitos Previos
- Git
- Java 21 JDK

#### Pasos

1. **Clonar el repositorio**
   ```bash
   git clone https://github.com/tu-usuario/lava-border-event.git
   cd lava-border-event
   ```

2. **Compilar el mod**
   ```bash
   # En Windows
   gradlew.bat build
   
   # En Linux/macOS
   ./gradlew build
   ```

3. **Localizar el JAR compilado**
   ```
   El mod compilado estará en: build/libs/extremerisinglava-1.0.0.jar
   ```

4. **Instalar el mod**
   - Copia el archivo JAR a la carpeta `mods` de tu servidor o cliente Minecraft
   - La carpeta `mods` se encuentra en:
     - **Cliente**: `.minecraft/mods/`
     - **Servidor**: `./mods/`

### Método 2: Descargar Release Precompilado

1. Ve a la sección [Releases](https://github.com/tu-usuario/lava-border-event/releases)
2. Descarga la versión más reciente del mod
3. Coloca el JAR en tu carpeta `mods`

## 🚀 Uso

### Configuración Inicial

El mod genera un archivo de configuración automáticamente en:
```
config/extremerisinglava-common.toml
```

Edita este archivo para personalizar:
- Velocidad de contracción del WorldBorder
- Velocidad de ascenso de la lava
- Altura inicial de la lava
- Durabilidad total del evento

### Comandos Disponibles

#### Iniciar el Evento
```
/extremelava start
```
Inicia el evento de Extreme Rising Lava con los parámetros configurados.

#### Detener el Evento
```
/extremelava stop
```
Detiene el evento en progreso.

#### Ver Estado
```
/extremelava status
```
Muestra información sobre el evento actual (estado, tiempo transcurrido, jugadores vivos).

#### Reanudar/Pausar
```
/extremelava pause
/extremelava resume
```
Pausa o reanuda el evento.

#### Configuración en Tiempo Real
```
/extremelava config <parámetro> <valor>
```
Ajusta parámetros durante el evento.

## 🔧 Desarrollo

### Estructura del Proyecto

```
src/main/java/com/extremerisinglava/
├── ExtremeRisingLavaMod.java      # Clase principal del mod
├── command/
│   └── ModCommands.java           # Definición de comandos
├── config/
│   └── ModConfig.java             # Sistema de configuración
└── event/
    └── EventManager.java          # Lógica principal del evento

src/main/resources/
└── META-INF/
    └── neoforge.mods.toml         # Metadatos del mod
```

### Ejecución en Modo Desarrollo

#### Cliente de Prueba
```bash
# En Windows
gradlew.bat runClient

# En Linux/macOS
./gradlew runClient
```

#### Servidor de Prueba
```bash
# En Windows
gradlew.bat runServer

# En Linux/macOS
./gradlew runServer
```

#### Generar Datos (Datagen)
```bash
gradlew runData
```

### Dependencias Principales

- **NeoForge 21.8.40**: Framework de modding
- **Minecraft 1.21.8**: API del juego
- **Java 21**: Runtime

### Estructura del Código

#### EventManager.java
Gestiona la lógica principal:
- Control del WorldBorder
- Generación de lava
- Actualización de la bossbar
- Sincronización de eventos

#### ModCommands.java
Define los comandos administrativos:
- Registro de comandos
- Parsing de argumentos
- Validación de permisos

#### ModConfig.java
Sistema de configuración:
- Cargar archivos TOML
- Valores por defecto
- Validación de parámetros

### Construyendo el Mod

```bash
# Compilación completa
gradlew build

# Solo compilar JAR
gradlew jar

# Limpiar artefactos
gradlew clean

# Compilar y ejecutar pruebas
gradlew test build
```

## 🐛 Solución de Problemas

### El mod no aparece en el servidor
- Verifica que el JAR esté en la carpeta correcta: `./mods/`
- Reinicia el servidor después de agregarlo
- Revisa los logs: `logs/latest.log`

### Error: "Java version mismatch"
- Asegúrate de tener Java 21 instalado
- Configura la variable de entorno `JAVA_HOME` a Java 21

### El evento no inicia
- Verifica que tienes permisos de operador (OP)
- Revisa la configuración en `config/extremerisinglava-common.toml`
- Comprueba los logs del servidor

### WorldBorder se comporta erratically
- Verifica que no hay otros mods conflictivos
- Reduce la velocidad de contracción en la configuración
- Asegúrate de que el servidor tiene suficientes recursos

## 📊 Rendimiento

### Optimizaciones

- Eventos de tick optimizados
- Cacheo de bordes del mundo
- Actualizaciones asincrónicas de bossbar
- Minimización de llamadas de API

### Requisitos de Recursos

- **CPU**: Bajo impacto (~1-2% en servidor típico)
- **RAM**: ~50MB adicionales
- **Ancho de banda**: Mínimo (solo actualizaciones periódicas)

## 🤝 Contribuciones

Las contribuciones son bienvenidas. Para cambios importantes:

1. Haz un fork del proyecto
2. Crea una rama para tu feature (`git checkout -b feature/AmazingFeature`)
3. Commit tus cambios (`git commit -m 'Add some AmazingFeature'`)
4. Push a la rama (`git push origin feature/AmazingFeature`)
5. Abre un Pull Request

## 📄 Licencia

Este proyecto está bajo la licencia MIT - ver el archivo [LICENSE](LICENSE) para detalles.

## 👨‍💻 Autor

**alexcuadroo**

- GitHub: [@alexcuadroo](https://github.com/alexcuadroo)

## 📬 Soporte

¿Encontraste un bug o tienes una sugerencia?

- **Issues**: [Crear un issue](https://github.com/tu-usuario/lava-border-event/issues)
- **Discussiones**: [Discusiones](https://github.com/tu-usuario/lava-border-event/discussions)
- **Email**: contacto@ejemplo.com

## 🔗 Enlaces Útiles

- [NeoForge Documentation](https://docs.neoforged.net/)
- [Minecraft Wiki](https://minecraft.wiki/)
- [Gradle Documentation](https://gradle.org/documentation/)
- [Java 21 Documentation](https://docs.oracle.com/en/java/javase/21/)

---

**Última actualización**: 15 de febrero de 2026
**Versión**: 1.0.0
