# Veterinaria San Marcos — Backend

Backend del proyecto Veterinaria San Marcos, desarrollado con Spring Boot y MySQL. Permite registrar usuarios, iniciar sesión con JWT y administrar cuentas.

La aplicación se ejecuta en una instancia EC2 con Ubuntu. El [frontend](https://github.com/DavidMiranda816/Veterinaria-San-Marcos-2) se mantiene en un repositorio separado.

## Conexión con el frontend

El frontend y el backend pueden ejecutarse en instancias distintas. En el repositorio del frontend, configura Axios con una URL que el navegador pueda alcanzar:

```dotenv
VITE_API_URL=http://<IP_O_DOMINIO_DEL_BACKEND>:8080/api
```

No uses `localhost` en `VITE_API_URL` cuando el navegador abre el frontend desde otra máquina: `localhost` apuntaría a la máquina del usuario. La API escucha en el puerto `8080`.

El backend permite por defecto solicitudes desde `http://localhost:5173`. Para conectarlo desde otra dirección, configura `CORS_ALLOWED_ORIGINS` con el origen exacto del frontend, incluyendo esquema y puerto cuando corresponda, pero sin `/api` ni otras rutas. Para permitir más de uno, sepáralos por comas. También asegúrate de que las reglas de red de las instancias permitan que el navegador llegue al puerto `8080` del backend.

Esta API actualmente implementa autenticación (`POST /api/auth/registro` y `POST /api/auth/login`) y administración de usuarios (`/api/usuarios`, solo ADMIN). El frontend veterinario todavía usa datos de `localStorage` para productos, clientes, categorías y solicitudes, y el inicio de sesión usa credenciales locales; Axios por sí solo no conecta esas funciones porque sus endpoints aún no existen en este backend. La primera integración posible es reemplazar el inicio de sesión local por `POST /api/auth/login`; luego se pueden migrar los módulos que tengan endpoints disponibles.

## Tecnologías

- Java 21 y Spring Boot 4.1.2
- Spring Web MVC, Spring Data JPA y MySQL 8.4
- Spring Security, BCrypt y JWT Bearer
- Flyway para las migraciones
- Jakarta Validation y Lombok
- Swagger / OpenAPI
- Maven 3.9+

## Funcionalidades

Se comprobó con Postman el registro y el inicio de sesión; los datos guardados se revisaron en MySQL Workbench. Las contraseñas se almacenan con hash BCrypt y las credenciales incorrectas devuelven un mensaje genérico.

El registro público asigna siempre el rol `CLIENTE`. También se implementaron rutas protegidas para registrar administradores y para listar, modificar y desactivar usuarios. Estas operaciones requieren un token de un usuario con rol `ADMIN`.

## Endpoints

| Método | Ruta | Función | Acceso |
| --- | --- | --- | --- |
| POST | `/api/auth/registro` | Registrar usuario | Público |
| POST | `/api/auth/login` | Iniciar sesión y obtener JWT | Público |
| POST | `/api/auth/admin/registro` | Registrar administrador | ADMIN |
| GET | `/api/usuarios` | Listar usuarios | ADMIN |
| PUT | `/api/usuarios/{id}` | Modificar nombre y correo | ADMIN |
| PATCH | `/api/usuarios/{id}/desactivar` | Desactivar una cuenta | ADMIN |

Las rutas protegidas reciben el token en la cabecera `Authorization`:

```http
Authorization: Bearer <token>
```

El registro público no requiere autenticación. Si Postman conserva un token inválido o vencido, quita la autorización de esa solicitud (**No Auth**).

## Primer administrador

El primer administrador se habilita manualmente para evitar que el registro público permita asignar privilegios. Primero registra una cuenta de confianza mediante `POST /api/auth/registro`. Un operador autorizado con acceso a la base de datos debe asignarle el rol:

```sql
UPDATE usuarios SET rol = 'ADMIN' WHERE correo = 'correo-de-confianza';
```

Luego inicia sesión nuevamente con esa cuenta para obtener un token que incluya el rol `ADMIN`. Los administradores existentes pueden crear otros con `POST /api/auth/admin/registro`.

## Configuración y ejecución

Se necesita Java 21, Maven 3.9+ y una base MySQL llamada `veterinaria`, con un usuario que tenga permisos para crear tablas y gestionar sus datos. La aplicación usa MySQL en `127.0.0.1:3306` y la API escucha en el puerto `8080`. Para una base de datos en otra instancia, define `DB_URL` como `jdbc:mysql://<IP_O_DOMINIO_MYSQL>:3306/veterinaria` y permite el acceso de red desde el backend.

Crear un archivo `.env` en la raíz del proyecto:

```dotenv
DB_USERNAME=veterinaria_app
DB_PASSWORD='CONTRASENA_MYSQL'
JWT_SECRET='CLAVE_BASE64'
```

Para generar la clave JWT Base64 (de al menos 32 bytes una vez decodificada):

```bash
openssl rand -base64 32
```

Copiar el resultado en `JWT_SECRET`. El archivo `.env` está excluido del repositorio.

Desde la raíz del proyecto, ejecutar en Bash:

```bash
set -a
source .env
set +a
mvn spring-boot:run
```

En Windows también se puede iniciar con `mvn spring-boot:run`, configurando previamente las variables de entorno. Flyway crea la tabla `usuarios` mediante la migración V1 y JPA valida su estructura al iniciar.

La documentación OpenAPI está disponible en `http://localhost:8080/swagger-ui/index.html`. Para acceder desde el PC mientras se ejecuta en EC2, se puede reenviar el puerto `8080` mediante VS Code Remote SSH.

## Documentación y pruebas

Las pruebas manuales realizadas con Postman cubrieron el registro y el inicio de sesión; los datos guardados se revisaron en MySQL Workbench. La integración con el frontend se mantiene en el repositorio separado.
