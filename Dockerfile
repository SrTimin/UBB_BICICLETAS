# Usar una imagen base con Ubuntu
FROM ubuntu:20.04

# Instalar dependencias, Java 17, herramientas para generar QR, Python3, jq y wget
RUN apt-get update && apt-get install -y \
    wget \
    unzip \
    openjdk-17-jdk \
    qrencode \
    python3 \
    curl \
    jq \
    && rm -rf /var/lib/apt/lists/*

# Instalar ngrok
RUN wget https://bin.equinox.io/c/bNyj1mQVY4c/ngrok-v3-stable-linux-amd64.tgz -O /ngrok.tgz \
    && tar -xvzf /ngrok.tgz -C /usr/local/bin \
    && rm /ngrok.tgz

# Configurar authtoken de ngrok
RUN ngrok authtoken 2d0tNezaGEdtoReFTzEZ5c61RDy_7EhsAbfVNp9A5h3Qd75Je

# Establecer variables de entorno para Android SDK y Java 17
ENV ANDROID_SDK_ROOT=/usr/local/android-sdk
ENV JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
ENV PATH=$PATH:$ANDROID_SDK_ROOT/cmdline-tools/latest/bin:$ANDROID_SDK_ROOT/platform-tools:$JAVA_HOME/bin

# Descargar y descomprimir las herramientas de línea de comandos de Android SDK
RUN mkdir -p $ANDROID_SDK_ROOT/cmdline-tools \
    && wget https://dl.google.com/android/repository/commandlinetools-linux-9477386_latest.zip -O /cmdline-tools.zip \
    && unzip /cmdline-tools.zip -d $ANDROID_SDK_ROOT/cmdline-tools \
    && rm /cmdline-tools.zip \
    && mv $ANDROID_SDK_ROOT/cmdline-tools/cmdline-tools $ANDROID_SDK_ROOT/cmdline-tools/latest

# Aceptar licencias
RUN yes | sdkmanager --licenses

# Instalar las herramientas necesarias y los paquetes de SDK
RUN sdkmanager "platform-tools" "platforms;android-34" "build-tools;34.0.0"

# Configurar el directorio de trabajo
WORKDIR /app

# Copiar todos los archivos del proyecto al contenedor
COPY . .

# Hacer que el script gradlew sea ejecutable
RUN chmod +x ./gradlew

# Instalar dependencias del proyecto y construir la aplicación
RUN ./gradlew assembleDebug

# Exponer el puerto 8080
EXPOSE 8080

# Iniciar un servidor para servir el APK y mostrar el QR en los logs con depuración adicional
CMD ["sh", "-c", "cd /app/app/build/outputs/apk/debug && python3 -m http.server 8080 & ngrok http 8080 > /tmp/ngrok.log 2>&1 & sleep 10 && echo 'Verifying ngrok status...' && curl -s http://localhost:4040/api/tunnels && curl -s http://localhost:4040/api/tunnels | jq -r .tunnels[0].public_url | sed 's|$|/app-debug.apk|' | tee apk_url.txt && cat apk_url.txt && echo 'Scan the following QR to download the APK:' && qrencode -t ansiutf8 < apk_url.txt && tail -f /tmp/ngrok.log"]

