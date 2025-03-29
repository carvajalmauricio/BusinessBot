# BusinessBot - Asistente Inteligente para Gestión de Negocios

![Versión](https://img.shields.io/badge/versión-1.0.0-blue)
![Android](https://img.shields.io/badge/Android-API%2024+-green)
![Python](https://img.shields.io/badge/Python-3.7+-orange)
![Flask](https://img.shields.io/badge/Flask-2.0+-lightblue)
![Firebase](https://img.shields.io/badge/Firebase-Latest-yellow)
![LangChain](https://img.shields.io/badge/LangChain-Latest-blueviolet)

BusinessBot es una solución integral que permite a pequeñas y medianas empresas gestionar su inventario y establecer un canal de comunicación omnicanal con sus clientes a través de un asistente virtual inteligente.

## 📱 Aplicación Móvil

La aplicación móvil de BusinessBot está desarrollada en Java para Android y ofrece una interfaz intuitiva para la gestión empresarial con las siguientes características:

### Características Principales

- **Autenticación Segura**: Inicio de sesión mediante Google Authentication
- **Registro de Negocio**: Configuración inicial del perfil de la empresa
- **Gestión de Inventario**: 
  - Carga masiva de productos mediante archivos Excel
  - Adición manual de productos individuales
  - Visualización y edición de inventario existente
- **Panel de Ventas**: Registro y seguimiento de transacciones
- **Configuración del Chatbot**:
  - Personalización del tono de respuesta
  - Establecimiento de horarios de atención
  - Información de contacto y localización
  - Detalles adicionales sobre el negocio

### Tecnologías Utilizadas en la App

- Java para Android
- Firebase Authentication para inicio de sesión seguro
- Firebase Realtime Database para almacenamiento en la nube
- OkHttp para comunicación con la API REST
- RecyclerView para visualización de datos
- Material Design para la interfaz de usuario

## 🤖 API y Backend

El backend está desarrollado en Python y utiliza varias tecnologías para proporcionar inteligencia y procesamiento a la aplicación:

### Características del Backend

- **API REST con Flask**: Endpoints para todas las funcionalidades de la aplicación
- **Procesamiento de Datos**: Manejo de archivos Excel para la gestión de inventario
- **Chatbot Inteligente**: 
  - Integración con OpenAI para comprender y generar respuestas naturales
  - Personalización del tono según las preferencias del negocio
  - Gestión de disponibilidad según horarios configurados
- **Integración con Facebook**: Canal de comunicación con clientes a través de Messenger
- **Orquestación con LangChain**: Manejo avanzado de conversaciones y contexto

### Tecnologías Utilizadas en el Backend

- Python como lenguaje principal
- Flask para la creación de API REST
- OpenAI API para procesamiento de lenguaje natural
- LangChain para la gestión de flujos de conversación
- Facebook Messenger API para la comunicación con clientes
- Pandas para el procesamiento de datos (archivos Excel)

## 🚀 Instalación y Configuración

### Requisitos Previos

- Android Studio para la compilación de la aplicación móvil
- Python 3.7+ para el backend
- Firebase cuenta y configuración
- Claves API para OpenAI y Facebook

### Configuración del Backend

1. Clonar el repositorio:
   ```bash
   git clone https://github.com/tu-usuario/BusinessBot.git
   ```

2. Instalar dependencias:
   ```bash
   cd BusinessBot/ApiFlask
   pip install -r requirements.txt
   ```

3. Configurar variables de entorno:
   ```bash
   export OPENAI_API_KEY=tu_clave_api
   export FACEBOOK_API_KEY=tu_clave_api
   ```

4. Iniciar el servidor Flask:
   ```bash
   python app.py
   ```

### Configuración de la App Android

1. Abrir el proyecto en Android Studio:
   ```bash
   cd BusinessBot/BusinessApp
   ```

2. Sincronizar con Gradle

3. Configurar Google Services:
   - Crear proyecto en Firebase
   - Descargar el archivo google-services.json
   - Colocarlo en la carpeta app/

4. Compilar y ejecutar en un dispositivo/emulador

## 💼 Casos de Uso

1. **Negocio Minorista**:
   - Gestión de inventario de productos físicos
   - Atención al cliente automatizada para consultas frecuentes
   - Promoción de productos en stock a través del chatbot

2. **Servicios Profesionales**:
   - Gestión de citas y disponibilidad
   - Respuestas automáticas a consultas sobre servicios
   - Mantenimiento de información actualizada

3. **Restaurantes y Cafeterías**:
   - Gestión de menú e ingredientes
   - Toma de pedidos a través del chatbot
   - Información sobre horarios y ubicación

## 🔍 Arquitectura del Sistema

```
BusinessBot/
├── ApiFlask/                # Backend en Python
│   └── app/                 # Aplicación Flask
│       ├── static/          # Recursos estáticos
│       └── templates/       # Plantillas HTML
│
└── BusinessApp/             # Aplicación Android
    ├── app/                 # Código principal de la aplicación
    │   ├── src/main/        # Fuentes de la aplicación
    │   │   ├── java/        # Código Java
    │   │   └── res/         # Recursos de la aplicación
    │   └── build.gradle     # Configuración de dependencias
    └── build.gradle         # Configuración del proyecto
```

## 📊 Flujo de Comunicación

1. **Usuario → App Android**: El usuario interactúa con la aplicación móvil
2. **App Android → API Flask**: La aplicación envía peticiones al backend
3. **API Flask → LangChain**: Las solicitudes de conversación se procesan a través de LangChain
4. **LangChain → OpenAI**: Se generan respuestas naturales basadas en el contexto
5. **API Flask → Facebook**: Se establece comunicación con los clientes a través de Messenger
6. **API Flask → App Android**: Se devuelven respuestas y datos actualizados a la aplicación

## 🛠️ Tecnologías y Dependencias

### App Android

- androidx.appcompat:appcompat
- com.google.android.material:material
- androidx.constraintlayout:constraintlayout
- com.google.firebase:firebase-auth
- com.google.firebase:firebase-database
- com.squareup.okhttp3:okhttp
- androidx.recyclerview:recyclerview
- com.google.code.gson:gson

### Backend Python

- Flask
- OpenAI
- LangChain
- pandas
- Facebook Messenger API
- requests
- python-dotenv

## 👥 Contribuciones

Las contribuciones son bienvenidas. Para contribuir:

1. Haz un fork del repositorio
2. Crea una rama para tu característica (`git checkout -b feature/nueva-caracteristica`)
3. Haz commit de tus cambios (`git commit -m 'Añade nueva característica'`)
4. Haz push a la rama (`git push origin feature/nueva-caracteristica`)
5. Abre un Pull Request

## 📄 Licencia

Este proyecto está licenciado bajo la Licencia MIT - ver el archivo LICENSE para más detalles.

Desarrollado por Innova Web - Soluciones tecnológicas para negocios.
