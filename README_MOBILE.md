# Marymar Mobile (JDK 21 + Gradle 8.6)

## Cadena recomendada para esta variante
- Android Studio reciente
- JDK 21 para ejecutar Gradle en Android Studio
- Gradle 8.6 configurado como instalación local en Android Studio
- Android Gradle Plugin 8.4.2

## Importante
Gradle se ejecuta con JDK 21, pero la app sigue compilando a bytecode JVM 17 para evitar problemas de compatibilidad en Android.

## Backend
La app ya consume el backend desplegado:
https://marymar-backend-env.eba-kqyuhemz.us-east-2.elasticbeanstalk.com/

## Ajustes clave
- Login + validación de código
- Registro con aceptaHabeasData
- Menú de productos con imágenes
- Perfil y cierre de sesión
- Pedidos no habilitados en esta fase
