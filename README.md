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

La API permite `GET`, `POST`, `PUT`, `PATCH`, `DELETE` y `OPTIONS`, y las cabeceras `Authorization` y `Content-Type`.

Esta API implementa autenticación (`/api/auth`), administración de usuarios (`/api/usuarios`), CRUD versionado de productos y categorías (`/api/v1/productos`, `/api/v1/categorias`; también disponibles en las rutas heredadas), y gestión de citas (`/api/v1/solicitudes`). Los pedidos del carrito no forman parte del módulo de citas.

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

El registro público asigna siempre el rol `CLIENTE`. Las operaciones del catálogo requieren una sesión vigente; crear, modificar y eliminar productos y categorías requiere además el rol `ADMIN`.

## Endpoints

| Método | Ruta | Función | Acceso |
| --- | --- | --- | --- |
| POST | `/api/auth/registro` | Registrar usuario | Público |
| POST | `/api/auth/login` | Iniciar sesión y obtener JWT | Público |
| GET | `/api/auth/me` | Revalidar la sesión y consultar el usuario actual | Cualquier usuario autenticado |
| POST | `/api/auth/admin/registro` | Registrar administrador | ADMIN |
| GET | `/api/usuarios` | Listar usuarios | ADMIN |
| GET | `/api/usuarios/{id}` | Consultar usuario para ficha y edición | ADMIN |
| PUT | `/api/usuarios/{id}` | Modificar nombre, correo y opcionalmente rol | ADMIN |
| PATCH | `/api/usuarios/{id}/desactivar` | Desactivar una cuenta | ADMIN |
| GET | `/api/productos` | Listar productos del catálogo | Usuario autenticado |
| GET | `/api/productos/{id}` | Consultar producto | Usuario autenticado |
| POST | `/api/productos` | Crear producto | ADMIN |
| PUT | `/api/productos/{id}` | Actualizar producto | ADMIN |
| DELETE | `/api/productos/{id}` | Eliminar producto | ADMIN |
| GET | `/api/categorias` | Listar categorías | Usuario autenticado |
| GET | `/api/categorias/{id}` | Consultar categoría | Usuario autenticado |
| POST | `/api/categorias` | Crear categoría | ADMIN |
| PUT | `/api/categorias/{id}` | Actualizar categoría | ADMIN |
| DELETE | `/api/categorias/{id}` | Eliminar categoría | ADMIN |
| GET | `/api/v1/solicitudes` | Listar citas | ADMIN/RECEPCIONISTA; CLIENTE solo propias |
| GET | `/api/v1/solicitudes/{id}` | Consultar cita por folio | ADMIN/RECEPCIONISTA; CLIENTE solo propia |
| POST | `/api/v1/solicitudes` | Crear cita propia | CLIENTE |
| PATCH | `/api/v1/solicitudes/{id}/estado` | Cambiar estado/reprogramar cita | ADMIN/RECEPCIONISTA |
| DELETE | `/api/v1/solicitudes/{id}` | Eliminar cita | ADMIN |

Las rutas de catálogo versionadas tienen los mismos métodos, cuerpos y permisos que las heredadas `/api/productos` y `/api/categorias`. Ambas formas se mantienen durante la transición del frontend.

### Contrato de citas

`/api/v1/solicitudes` representa exclusivamente **citas**, no pedidos de productos del carrito. El alta pública autenticada recibe `mascota`, `fechaHora` (ISO-8601 local y futura) y `motivo`; el cliente y su identidad se derivan del JWT y el servidor asigna el estado inicial `PENDIENTE`. La respuesta incluye el folio, el cliente, la mascota, la fecha, el motivo y el estado. No se acepta un `clienteId` del cuerpo.

Los estados permitidos son `PENDIENTE`, `CONFIRMADA`, `REPROGRAMADA`, `COMPLETADA` y `CANCELADA`. Las transiciones son: `PENDIENTE` → `CONFIRMADA`, `REPROGRAMADA` o `CANCELADA`; `CONFIRMADA` → `REPROGRAMADA`, `COMPLETADA` o `CANCELADA`; `REPROGRAMADA` → `CONFIRMADA` o `CANCELADA`. `COMPLETADA` y `CANCELADA` son terminales. Reprogramar requiere `fechaHora` futura en el mismo PATCH. Las validaciones y actualizaciones toman un bloqueo de escritura sobre la cita, dentro de una transacción.

Ejemplo de listado:

```json
[
  {
    "id": 15,
    "tipo": "CITA",
    "clienteId": 8,
    "clienteNombre": "Ana Pérez",
    "clienteCorreo": "ana@example.com",
    "mascota": "Luna",
    "fechaHora": "2026-12-10T10:30:00",
    "motivo": "Control anual",
    "estado": "PENDIENTE",
    "fechaCreacion": "2026-11-27T01:15:00",
    "fechaActualizacion": "2026-11-27T01:15:00"
  }
]
```

Ejemplo de detalle: `GET /api/v1/solicitudes/15` devuelve el mismo objeto individual (sin el arreglo). Si el folio no existe, o pertenece a otro cliente autenticado, devuelve `404` con `{"mensaje":"Solicitud no encontrada"}`.

Un cambio incompatible devuelve `409 Conflict` y conserva el estado original. Por ejemplo, completar directamente una cita pendiente:

```json
{
  "mensaje": "No se puede cambiar una cita de PENDIENTE a COMPLETADA"
}
```

El alta no define una regla de duplicidad que origine un `409`; el conflicto documentado corresponde a transiciones de estado incompatibles. El conflicto de correo duplicado sigue perteneciendo al registro/edición de cuentas, no a solicitudes.

Los cuerpos coinciden con los formularios administrativos del frontend: un producto recibe `nombre`, `categoria`, `precio` y `stock`; `descripcion` e `imagen` son opcionales para la ficha pública. `categoria` es el nombre en texto que usa actualmente el formulario, no un ID. Una categoría recibe `nombre` y `descripcion`. Las respuestas agregan el `id` numérico generado por el backend.

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
| `GET /api/usuarios`, `GET /api/usuarios/{id}` | `ADMIN` |
| `PUT /api/usuarios/{id}`, `PATCH /api/usuarios/{id}/desactivar` | `ADMIN` |
| `PUT /api/usuarios/{id}` con `rol` | `ADMIN`; acepta `ADMIN`, `RECEPCIONISTA` o `CLIENTE`. Si se omite, conserva el rol. |
| `GET /api/productos/**`, `GET /api/categorias/**`, `GET /api/v1/productos/**`, `GET /api/v1/categorias/**` | Cualquier usuario autenticado y activo |
| `POST`, `PUT`, `DELETE` en `/api/productos/**` y `/api/categorias/**` | `ADMIN` |
| `GET /api/v1/solicitudes/**` | `ADMIN` y `RECEPCIONISTA` ven todas; `CLIENTE` solo sus citas |
| `POST /api/v1/solicitudes` | `CLIENTE`; el servidor establece la propiedad desde la sesión |
| `PATCH /api/v1/solicitudes/{id}/estado` | `ADMIN` y `RECEPCIONISTA` |
| `DELETE /api/v1/solicitudes/{id}` | `ADMIN` |
| Rutas no declaradas en la configuración de seguridad | Denegadas por defecto |

### Roles y diferencias con el frontend

| Rol | Contrato |
| --- | --- |
| `ADMIN` | Gestiona usuarios, asigna roles y administra productos y categorías. |
| `RECEPCIONISTA` | Gestiona las citas del día (listar, consultar, confirmar, reprogramar, completar y cancelar según las transiciones). No elimina citas, administra usuarios ni modifica el catálogo. |
| `CLIENTE` | Identificador canónico del dueño/cliente; no se acepta `DUENO` como alias. Puede crear citas y consultar solo las propias. |
| `VENDEDOR`, `ENCARGADO` | Roles heredados de otro proyecto; se conservan en la base de datos, pero no se pueden asignar mediante la edición de usuarios ni reciben permisos sobre rutas no declaradas. |

La autorización usa el estado y rol actuales de la cuenta en la base de datos, no los roles incluidos en un JWT anterior. Por eso una cuenta desactivada recibe `401` y un cambio de rol tiene efecto inmediato en las rutas protegidas. El registro público siempre crea `CLIENTE`; el endpoint administrativo crea `ADMIN`.

El frontend está en un repositorio separado y aún necesita alineación: su función `estaAutenticado()` depende de `obtenerTokenAdmin()`, que solo reconoce `ADMIN`; el cliente deberá habilitar menú, rutas y acciones de solicitudes para `RECEPCIONISTA` y consumir `/api/v1/solicitudes` en lugar de persistir las citas localmente. Los pedidos del carrito permanecen fuera de este contrato. Además, las vistas actuales del catálogo son públicas, pero sus GET exigen Bearer; al conectar Axios, el frontend deberá proteger esas vistas o rediseñar explícitamente la excepción.

Swagger UI y OpenAPI permanecen públicos como excepción técnica para consultar y probar la documentación. El resto de rutas de negocio requiere autenticación según la matriz.

El registro público no requiere autenticación. Si Postman conserva un token inválido o vencido, quita la autorización de esa solicitud (**No Auth**).

## Primer administrador

El primer administrador se habilita manualmente para evitar que el registro público permita asignar privilegios. Primero registra una cuenta de confianza mediante `POST /api/auth/registro`. Un operador autorizado con acceso a la base de datos debe asignarle el rol:

```sql
UPDATE usuarios SET rol = 'ADMIN' WHERE correo = 'correo-de-confianza';
```

Luego inicia sesión nuevamente con esa cuenta para obtener un token que incluya el rol `ADMIN`. Los administradores existentes pueden crear otros con `POST /api/auth/admin/registro` o asignar roles canónicos al editar una cuenta.

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

En Windows también se puede iniciar con `mvn spring-boot:run`, configurando previamente las variables de entorno. Flyway crea las tablas de usuarios, categorías y productos mediante migraciones independientes (V1–V4), y V5 agrega `RECEPCIONISTA` sin retirar los roles heredados; JPA valida su estructura al iniciar.

### Swagger y OpenAPI en vivo

Con el backend en ejecución, la documentación interactiva está en `http://localhost:8080/swagger-ui/index.html` y la especificación OpenAPI JSON en `http://localhost:8080/v3/api-docs`. Swagger UI muestra los contratos, DTO, validaciones, permisos y respuestas documentadas por los controladores. Para probar rutas protegidas, selecciona **Authorize** e ingresa el JWT (sin anteponer `Bearer`); la interfaz agregará el esquema automáticamente. Para acceder desde el PC mientras se ejecuta en EC2, se puede reenviar el puerto `8080` mediante VS Code Remote SSH.

## Documentación y pruebas

Las pruebas manuales realizadas con Postman cubrieron el registro y el inicio de sesión; los datos guardados se revisaron en MySQL Workbench. La integración con el frontend se mantiene en el repositorio separado.
