# Caso-DSY1104-Fullstack-Veterinaria-San-Marco-Backend

## Tecnologías

- Java 21 y Spring Boot 4.1.2
- Spring Web MVC, Spring Security y JWT Bearer
- Spring Data JPA y MySQL
- Flyway para migraciones de base de datos
- Maven (incluye Maven Wrapper)
- Springdoc OpenAPI para la documentación de la API

## Requisitos

- JDK 21
- MySQL en ejecución
- Base de datos `veterinaria` creada en MySQL

Configura estas variables de entorno antes de iniciar la aplicación:

- `DB_USERNAME`: usuario de MySQL
- `DB_PASSWORD`: contraseña de MySQL
- `JWT_SECRET`: clave JWT codificada en Base64, de al menos 32 bytes una vez decodificada

No publiques credenciales ni claves secretas en el repositorio.

## Levantar el proyecto

Desde la raíz del proyecto:

```bash
./mvnw spring-boot:run
```

En Windows:

```bat
mvnw.cmd spring-boot:run
```

La API queda disponible en `http://localhost:8080`. La configuración actual se conecta a MySQL en `127.0.0.1:3306` y Flyway aplica las migraciones al iniciar. La documentación interactiva está en `http://localhost:8080/swagger-ui/index.html`.

## Funcionalidad y endpoints

- `POST /api/auth/registro`: registro público; crea usuarios con rol `CLIENTE`.
- `POST /api/auth/login`: valida las credenciales y entrega un token JWT Bearer.
- `POST /api/auth/admin/registro`: registra administradores; requiere un token Bearer de un usuario `ADMIN`.
- `GET /api/usuarios`: lista usuarios; requiere rol `ADMIN`.
- `PUT /api/usuarios/{id}`: actualiza un usuario; requiere rol `ADMIN`.
- `PATCH /api/usuarios/{id}/desactivar`: desactiva un usuario; requiere rol `ADMIN`.

Los endpoints protegidos reciben el token en el encabezado:

```text
Authorization: Bearer <token>
```

## Pruebas manuales con Postman

Se usó Postman para validar la lógica de autenticación, registro y autorización con estas solicitudes:

1. Enviar `POST /api/auth/registro` sin autorización, con `nombre`, `correo` y `password`; debe crear una cuenta `CLIENTE`.
2. Enviar `POST /api/auth/login` con el correo y la contraseña; usar el token devuelto para las solicitudes protegidas.
3. Enviar `GET /api/usuarios` con un token `ADMIN`; un token de `CLIENTE` no debe autorizar esta operación.
4. Enviar `POST /api/auth/admin/registro` con un token `ADMIN`; la cuenta registrada debe quedar con rol `ADMIN`.
5. Repetir el registro con un correo ya utilizado para comprobar que la API rechaza duplicados.

Para el registro público, selecciona **No Auth** en Postman. Si adjuntas un token inválido o vencido, la solicitud puede responder `401 Unauthorized`.

## Administración de usuarios

- Para habilitar el primer administrador, registra una cuenta de confianza y un operador con acceso autorizado a la base de datos debe asignarle el rol `ADMIN` (`UPDATE usuarios SET rol = 'ADMIN' WHERE correo = 'correo-de-confianza';`). Después, los administradores existentes pueden crear otros mediante la ruta protegida.

El registro público no recibe ni acepta un rol desde la solicitud: siempre asigna `CLIENTE`.