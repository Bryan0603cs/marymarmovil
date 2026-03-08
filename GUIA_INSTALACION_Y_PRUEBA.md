# Guía para ejecutar Mar y Mar Mobile en Android Studio

## 1. Ejecutarla en emulador
Esta es la forma más simple.

1. Abre Android Studio.
2. Carga el proyecto `MarymarMobile`.
3. Ve a **Device Manager**.
4. Crea un emulador si no tienes uno.
5. Pulsa **Run**.

Como la app consume un backend desplegado en internet, el emulador no necesita apuntar a tu PC local.

---

## 2. Ejecutarla en celular por cable USB
1. Activa **Opciones de desarrollador** en tu Android.
2. Activa **Depuración USB**.
3. Conecta el celular al computador por cable.
4. Acepta el mensaje de confianza en el teléfono.
5. En Android Studio, elige tu dispositivo en la parte superior.
6. Pulsa **Run**.

Esto instala la app directamente en el teléfono y podrás probar login, verificación por correo y menú.

---

## 3. Ejecutarla en celular por WiFi (Wireless Debugging)
Esto evita usar cable después del primer emparejamiento.

1. Abre Android Studio.
2. En el celular, activa **Opciones de desarrollador**.
3. Activa **Wireless debugging** o **Depuración inalámbrica**.
4. En Android Studio abre **Device Manager**.
5. Selecciona **Pair using Wi-Fi**.
6. Ingresa el código de emparejamiento que muestra el celular.
7. Cuando el equipo aparezca como conectado, pulsa **Run**.

---

## 4. Qué puedes probar
- Registro de usuario.
- Inicio de sesión.
- Recepción del código por correo.
- Validación del código.
- Visualización del menú con imágenes.
- Búsqueda por nombre o categoría.
- Visualización del perfil y cierre de sesión.

---

## 5. Qué no hace todavía
- No crea pedidos.
- No abre carrito.
- No muestra historial de pedidos.

Eso quedó intencionalmente fuera de esta versión para mantener la app alineada con la primera entrega del proyecto.

---

## 6. Si Android Studio no ejecuta la app
Revisa esto:
- Tener instalado un SDK de Android compatible (API 34).
- Tener un dispositivo o emulador seleccionado.
- Tener internet activo, porque la app consume el backend desplegado.
- Revisar que Gradle termine de sincronizar sin errores.
