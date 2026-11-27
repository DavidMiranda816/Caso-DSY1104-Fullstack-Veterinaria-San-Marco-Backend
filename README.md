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

Esta API implementa registro y autenticación (`/api/auth`), revalidación de sesión y administración de usuarios (`/api/usuarios`, solo ADMIN). El frontend veterinario todavía usa datos de `localStorage` para productos, clientes, categorías y solicitudes; sus endpoints aún no existen en este backend.

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
| GET | `/api/auth/me` | Revalidar la sesión y consultar el usuario actual | Cualquier usuario autenticado |
| POST | `/api/auth/admin/registro` | Registrar administrador | ADMIN |
| POST | `/api/usuarios` | Crear una cuenta de Kaiju con nombre, correo, contraseña y rol | ADMIN |
| GET | `/api/usuarios` | Listar usuarios | ADMIN |
| GET | `/api/usuarios/{id}` | Consultar usuario para ficha y edición | ADMIN |
| PUT | `/api/usuarios/{id}` | Modificar nombre y correo | ADMIN |
| PATCH | `/api/usuarios/{id}/desactivar` | Desactivar una cuenta | ADMIN |

Las rutas protegidas reciben el token en la cabecera `Authorization`:

```http
Authorization: Bearer <token>
```

### Matriz de permisos

| Operación | Acceso |
| --- | --- |
| `POST /api/auth/registro` | Público; el servidor asigna `CLIENTE` |
| `POST /api/auth/login` | Público |
| `GET /api/auth/me` | Cualquier usuario autenticado y activo |
| `POST /api/auth/admin/registro` | `ADMIN` |
| `POST /api/usuarios`, `GET /api/usuarios`, `GET /api/usuarios/{id}` | `ADMIN` |
| `PUT /api/usuarios/{id}`, `PATCH /api/usuarios/{id}/desactivar` | `ADMIN` |
| Catálogo y solicitudes | Rutas no implementadas aquí; permisos por acordar antes de integrarlas |

`CLIENTE` es el identificador canónico del rol de cliente/dueño; no se emite ni se acepta `DUENO` como alias. La autorización usa el estado y rol actuales de la cuenta en la base de datos, no los roles incluidos en un JWT anterior. Por eso una cuenta desactivada recibe `401` y un cambio de rol tiene efecto inmediato en las rutas protegidas. Los roles distintos de `ADMIN` no pueden gestionar usuarios; no se asignan permisos administrativos a `RECEPCIONISTA` ni a otros roles sin acuerdo para cada módulo.

El registro público no requiere autenticación. Si Postman conserva un token inválido o vencido, quita la autorización de esa solicitud (**No Auth**).

## Cliente móvil Kaiju Inventory

[Kaiju Inventory](https://github.com/DavidMiranda816/Caso_DSY1105_Aplicaciones_Moviles_KAIJU) usa esta misma API. Enviando el JWT de un administrador a `POST /api/usuarios`, puede crear cuentas con `rol` igual a `ADMIN`, `VENDEDOR` o `ENCARGADO`. La migración V2 habilita los dos últimos roles. El registro público de Veterinaria sigue asignando `CLIENTE`.

Kaiju usa `PUT /api/usuarios/{id}` para editar el nombre y el correo y `PATCH /api/usuarios/{id}/desactivar` para dar de baja la cuenta. Una cuenta desactivada sale del listado de cuentas activas de Kaiju y pierde el acceso.

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

### Swagger y OpenAPI en vivo

Con el backend en ejecución, la documentación interactiva está en `http://localhost:8080/swagger-ui/index.html` y la especificación OpenAPI JSON en `http://localhost:8080/v3/api-docs`. Swagger UI muestra los contratos, DTO, validaciones, permisos y respuestas documentadas por los controladores. Para probar rutas protegidas, selecciona **Authorize** e ingresa el JWT (sin anteponer `Bearer`); la interfaz agregará el esquema automáticamente. Para acceder desde el PC mientras se ejecuta en EC2, se puede reenviar el puerto `8080` mediante VS Code Remote SSH.

## Documentación y pruebas

Las pruebas manuales realizadas con Postman cubrieron el registro y el inicio de sesión; los datos guardados se revisaron en MySQL Workbench. La integración con el frontend se mantiene en el repositorio separado.
