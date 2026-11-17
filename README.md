# Veterinaria San Marcos — Backend

Backend del proyecto Veterinaria San Marcos, desarrollado con Spring Boot y MySQL. Permite registrar usuarios, iniciar sesión con JWT y administrar cuentas.

La aplicación se ejecuta en una instancia EC2 con Ubuntu. El [frontend](https://github.com/DavidMiranda816/Veterinaria-San-Marcos-2) se mantiene en un repositorio separado.

## Tecnologías

- Java 21 y Spring Boot 4.1.2.
- Spring Data JPA y MySQL 8.4.
- Spring Security, BCrypt y JWT.
- Flyway para las migraciones.
- Jakarta Validation y Lombok.
- Swagger / OpenAPI.

## Funcionalidades

Se comprobó el registro de usuarios en MySQL, el almacenamiento de contraseñas con hash BCrypt y el inicio de sesión con JWT. Las credenciales incorrectas devuelven un mensaje genérico.

El registro asigna el rol `CLIENTE` desde el servidor. También se implementaron las rutas y la lógica para listar, modificar y desactivar usuarios, restringidas al rol `ADMIN`; sus pruebas funcionales están pendientes.

## Endpoints

| Método | Ruta | Función | Acceso |
| --- | --- | --- | --- |
| POST | `/api/auth/registro` | Registrar usuario | Público |
| POST | `/api/auth/login` | Iniciar sesión y obtener JWT | Público |
| GET | `/api/usuarios` | Listar usuarios | ADMIN |
| PUT | `/api/usuarios/{id}` | Modificar nombre y correo | ADMIN |
| PATCH | `/api/usuarios/{id}/desactivar` | Desactivar una cuenta | ADMIN |

Las rutas protegidas reciben el token en la cabecera:

```http
Authorization: Bearer TOKEN
```

## Configuración y ejecución

Se necesita Java 21 y una base MySQL llamada `veterinaria`, con un usuario que tenga permisos para crear tablas y gestionar sus datos.

Crear un archivo `.env` en la raíz del proyecto:

```dotenv
DB_USERNAME=veterinaria_app
DB_PASSWORD='CONTRASENA_MYSQL'
JWT_SECRET='CLAVE_BASE64'
```

Para generar la clave JWT:

```bash
openssl rand -base64 32
```

Copiar el resultado en `JWT_SECRET`. El archivo `.env` está excluido del repositorio.

Desde la raíz del proyecto, ejecutar en Bash:

```bash
chmod +x mvnw
set -a
source .env
set +a
./mvnw spring-boot:run
```

Flyway crea la tabla `usuarios` mediante la migración V1. JPA valida su estructura al iniciar.

La API utiliza el puerto `8080`. Para acceder desde el PC mientras se ejecuta en EC2, se puede reenviar ese puerto mediante VS Code Remote SSH.

## Documentación y pruebas

- Swagger UI: [http://127.0.0.1:8080/swagger-ui.html](http://127.0.0.1:8080/swagger-ui.html).
- OpenAPI: [http://127.0.0.1:8080/v3/api-docs](http://127.0.0.1:8080/v3/api-docs).
- Las pruebas realizadas de registro y login se hicieron con Postman; los datos guardados se revisaron en MySQL Workbench.

La integración con el frontend sigue pendiente.
