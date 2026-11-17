# Veterinaria San Marcos · Backend

API REST del proyecto académico de Veterinaria San Marcos, desarrollada para registrar usuarios, autenticar sus credenciales y administrar sus cuentas. Se ejecuta con Spring Boot en una instancia EC2 con Ubuntu y utiliza MySQL para almacenar los datos.

El frontend se mantiene en un repositorio separado: [Veterinaria-San-Marcos-2](https://github.com/DavidMiranda816/Veterinaria-San-Marcos-2).

## Tecnologías

| Tecnología | Uso |
| --- | --- |
| Java 21 y Spring Boot 4.1.2 | Desarrollo y ejecución del backend |
| Spring Web MVC | Endpoints REST |
| Spring Data JPA e Hibernate | Persistencia y consultas |
| MySQL 8.4 | Base de datos relacional |
| Flyway | Creación y evolución de tablas mediante migraciones SQL |
| Spring Security y BCrypt | Control de acceso y hash de contraseñas |
| JWT con firma HS256 | Autenticación de solicitudes |
| Jakarta Validation | Validación de las entradas |
| Lombok | Generación de métodos y builders |
| Springdoc OpenAPI 3.2.0 | Documentación y Swagger UI |
| Maven Wrapper | Compilación y ejecución sin instalar Maven por separado |

## Funcionalidades

- Registro público con rol `CLIENTE` asignado por el servidor.
- Contraseñas almacenadas con BCrypt; nunca se devuelven en las respuestas.
- Inicio de sesión con correo y contraseña, consultando la tabla de usuarios.
- Emisión de JWT con identificador, roles y vencimiento.
- Error genérico para credenciales incorrectas y cuentas inactivas.
- Listado, modificación y desactivación de usuarios restringidos a `ADMIN`.
- Validación de entradas, correos únicos y respuestas de error centralizadas.

## Organización del código

Paquete principal: `cl.veterinariasanmarcos.veterinaria_backend`.

| Paquete o ruta | Responsabilidad |
| --- | --- |
| `controller` | Recibir solicitudes HTTP y devolver respuestas |
| `services` | Aplicar reglas de negocio, generar hashes y emitir tokens |
| `repository` | Consultar y guardar entidades mediante JPA |
| `model` | Representar las tablas con entidades |
| `dto` | Definir datos de entrada y salida |
| `mapper` | Transformar DTO y entidades con builders |
| `config` | Configurar seguridad, firma y validación JWT |
| `exception` | Centralizar errores de la API |
| `src/main/resources/db/migration` | Mantener las migraciones SQL |

El controlador llama al servicio, que utiliza el repositorio para acceder a MySQL. El mapper convierte las entidades en DTO de respuesta y excluye el hash.

## Requisitos

- JDK 21.
- Git.
- MySQL 8.4 disponible.
- Acceso a internet durante la primera compilación para descargar dependencias.
- Bash para los comandos de configuración indicados.

En Ubuntu se pueden instalar las herramientas con:

```bash
sudo apt update
sudo apt install -y openjdk-21-jdk-headless git mysql-server openssl
sudo systemctl enable --now mysql
```

La ejecución documentada utiliza MySQL y Spring Boot en la misma instancia EC2.

## Descargar el proyecto

Mientras la implementación permanezca en la rama de desarrollo, utilizar:

```bash
git clone --branch feat/migracion-spring-boot https://github.com/DavidMiranda816/Caso-DSY1104-Fullstack-Veterinaria-San-Marco-Backend.git
cd Caso-DSY1104-Fullstack-Veterinaria-San-Marco-Backend
chmod +x mvnw
```

## Preparar MySQL

Entrar como administrador local:

```bash
sudo mysql
```

Ejecutar una vez, sustituyendo la contraseña de ejemplo:

```sql
CREATE DATABASE veterinaria CHARACTER SET utf8mb4;

CREATE USER 'veterinaria_app'@'localhost'
IDENTIFIED BY 'REEMPLAZAR_POR_UNA_CONTRASENA_PROPIA';

GRANT ALL PRIVILEGES ON veterinaria.*
TO 'veterinaria_app'@'localhost';

EXIT;
```

Esta cuenta tiene permisos sobre la base del proyecto para que Flyway cree las tablas y JPA gestione sus datos. Es una cuenta de conexión a MySQL, distinta de las cuentas de la aplicación.

## Variables de entorno

Crear un archivo `.env` en la raíz, junto a `pom.xml`:

```dotenv
DB_USERNAME=veterinaria_app
DB_PASSWORD='REEMPLAZAR_POR_LA_CONTRASENA_MYSQL'
JWT_SECRET='REEMPLAZAR_POR_UNA_CLAVE_BASE64'
```

Generar una clave JWT de 32 bytes:

```bash
openssl rand -base64 32
```

Copiar el resultado en `JWT_SECRET` y conservarlo entre reinicios. No se debe utilizar una clave de ejemplo compartida.

`.env` contiene secretos en texto plano y está excluido mediante `.gitignore`. No subirlo al repositorio. Como estos ejemplos se cargan con Bash, una contraseña que contenga una comilla simple requiere ajustar su escape.

```bash
chmod 600 .env
git check-ignore .env
```

Spring Boot no carga este archivo automáticamente. Antes de ejecutar comandos Maven en una terminal nueva:

```bash
set -a
source .env
set +a
```

### Configuración de la aplicación

`src/main/resources/application.properties` utiliza:

```properties
spring.datasource.url=jdbc:mysql://127.0.0.1:3306/veterinaria
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}

spring.jpa.hibernate.ddl-auto=validate
spring.jpa.open-in-view=false

spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration

app.jwt.secret=${JWT_SECRET}
app.jwt.issuer=veterinaria-backend
app.jwt.duration-seconds=3600
```

Si MySQL se ejecuta en otra máquina, ajustar la URL de conexión.

## Ejecutar

Con las variables exportadas:

```bash
./mvnw spring-boot:run
```

La API escucha por defecto en `http://127.0.0.1:8080`. Detener el proceso con `Ctrl+C`.

Para compilar:

```bash
./mvnw clean compile
```

Para ejecutar las pruebas automatizadas incluidas:

```bash
./mvnw test
```

La prueba de contexto necesita MySQL y las variables de entorno configuradas. Las pruebas manuales de Postman descritas más abajo no equivalen a una suite automatizada completa.

## Migraciones y persistencia

Flyway aplica `V1__crear_tabla_usuarios.sql` al iniciar contra una base vacía y registra la ejecución en `flyway_schema_history`.

La tabla `usuarios` contiene:

| Columna | Propósito |
| --- | --- |
| `id` | Clave primaria con `AUTO_INCREMENT`, asociada a `GenerationType.IDENTITY` |
| `nombre` | Nombre del usuario |
| `correo` | Correo único |
| `password_hash` | Hash BCrypt |
| `rol` | `ADMIN` o `CLIENTE` |
| `activo` | Estado de la cuenta |
| `fecha_creacion` | Fecha y hora del registro |

Flyway administra la estructura; `ddl-auto=validate` hace que Hibernate compruebe su correspondencia con las entidades. No es necesario crear la tabla manualmente.

Para cambios posteriores en una base compartida, agregar nuevas migraciones, por ejemplo `V2__descripcion.sql`, conservando las ya aplicadas.

## Endpoints

| Método | Ruta | Acceso | Respuesta correcta |
| --- | --- | --- | --- |
| POST | `/api/auth/registro` | Público | `201 Created` |
| POST | `/api/auth/login` | Público | `200 OK` con JWT |
| GET | `/api/usuarios` | JWT de ADMIN | `200 OK` |
| PUT | `/api/usuarios/{id}` | JWT de ADMIN | `200 OK` |
| PATCH | `/api/usuarios/{id}/desactivar` | JWT de ADMIN | `204 No Content` |

### Registro

```http
POST /api/auth/registro
Content-Type: application/json
```

```json
{
  "nombre": "Usuario de prueba",
  "correo": "usuario@example.com",
  "password": "EjemploPruebas123!"
}
```

El servidor asigna el rol `CLIENTE`, activa la cuenta y registra la fecha. La contraseña requiere al menos ocho caracteres y no puede superar los 72 bytes admitidos por BCrypt. El correo se guarda sin espacios externos y en minúsculas.

La respuesta contiene `id`, `nombre`, `correo`, `rol`, `activo` y `fechaCreacion`; no incluye contraseña ni hash.

### Inicio de sesión

```http
POST /api/auth/login
Content-Type: application/json
```

```json
{
  "correo": "usuario@example.com",
  "password": "EjemploPruebas123!"
}
```

La respuesta contiene:

- `token`: JWT firmado.
- `tipo`: `Bearer`.
- `usuario`: datos públicos de la cuenta.

Enviar el token en las solicitudes protegidas:

```http
Authorization: Bearer TOKEN_OBTENIDO_EN_EL_LOGIN
```

Los tokens duran 3600 segundos. Se comprueban su firma, emisor y vigencia. No existe un endpoint público que asigne el rol `ADMIN`.

### Actualización

```http
PUT /api/usuarios/ID
Content-Type: application/json
Authorization: Bearer TOKEN_ADMIN
```

```json
{
  "nombre": "Nombre actualizado",
  "correo": "actualizado@example.com"
}
```

Se modifican nombre y correo. Esta operación no cambia contraseña ni rol.

### Desactivación

```http
PATCH /api/usuarios/ID/desactivar
Authorization: Bearer TOKEN_ADMIN
```

La cuenta permanece en MySQL con `activo=false` y no puede iniciar nuevas sesiones. Los JWT emitidos previamente continúan vigentes hasta su vencimiento; no hay revocación inmediata de tokens.

### Errores

| Código | Situación |
| --- | --- |
| 400 | Datos inválidos o contraseña de registro superior a 72 bytes |
| 401 | Login incorrecto, cuenta inactiva o autenticación JWT ausente/inválida |
| 403 | Token válido sin el rol ADMIN requerido |
| 404 | Usuario solicitado inexistente |
| 409 | Correo repetido o conflicto de integridad |

Para credenciales incorrectas, el login devuelve:

```json
{
  "mensaje": "Correo o contraseña incorrectos"
}
```

Los errores de autenticación producidos por el filtro de seguridad pueden tener un formato distinto de los errores gestionados por los controladores.

## Swagger y pruebas desde el equipo local

Con el backend ejecutándose, abrir:

- [Swagger UI](http://127.0.0.1:8080/swagger-ui.html).
- [Especificación OpenAPI](http://127.0.0.1:8080/v3/api-docs).

Si el backend está en EC2 y Postman o el navegador están en el PC, reenviar el puerto `8080` desde la pestaña **Puertos** de VS Code Remote SSH. Utilizar la dirección local que indique VS Code y mantener la conexión abierta.

La aplicación utiliza HTTP en el puerto 8080; no tiene TLS configurado directamente. Para estas pruebas locales se utiliza el túnel SSH.

Los controladores administrativos declaran `bearerAuth`. Falta incorporar al código subido la definición de ese esquema en OpenAPI y verificar el botón **Authorize**. Postman permite probar la autorización desde **Authorization → Bearer Token**.

## Verificación de la entrega

### Comprobado manualmente durante el desarrollo

- Arranque de Spring Boot y conexión a MySQL.
- Validación de V1 por Flyway y reconocimiento del repositorio JPA.
- Protección del listado sin token con respuesta 401.
- Registro con respuesta 201 y rol CLIENTE.
- Registro visible en MySQL con contraseña almacenada como hash BCrypt.
- Login correcto con respuesta 200 y JWT.
- Login con contraseña incorrecta con respuesta 401 y mensaje genérico.

### Pendiente de completar o documentar

| Prueba o tarea | Resultado que se debe comprobar |
| --- | --- |
| Reiniciar el backend y repetir login sin registrar nuevamente | La cuenta persiste y el login devuelve 200 |
| Login con correo inexistente | Mismo 401 y mensaje genérico |
| Listado con token CLIENTE | 403 |
| Listado con token ADMIN | 200 sin hashes |
| Modificar usuario | Datos actualizados en MySQL |
| Desactivar una cuenta de prueba | 204 y nuevos logins rechazados |
| Token alterado o vencido | 401 |
| Registro duplicado y entradas inválidas | 409 y 400, respectivamente |
| Swagger con autorización JWT | Documentación accesible y ruta protegida ejecutable |
| Aportes del equipo | Commits reales de cada integrante |

Las rutas de listar, modificar y desactivar cuentan con controlador y lógica de servicio. Su implementación supera el inicio de desarrollo requerido, pero no se presentan como pruebas manuales aprobadas hasta verificarlas.

La integración con el frontend y su configuración CORS continúan pendientes.

## Git y entrega

Los cambios de Spring Boot se mantienen en `feat/migracion-spring-boot`. Deben revisarse e integrarse a `main` antes de entregar esa rama como versión final.

Cada integrante debe registrar sus aportes reales desde su propia identidad de Git. No se incluyen credenciales, claves privadas, wallets ni tokens en los commits.

Antes de subir cambios:

```bash
git status --short
git diff --cached
```

Conservar como evidencias las respuestas de Postman, la consulta de MySQL y el historial de commits, sin publicar secretos.
