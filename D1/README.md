# TiendaApp - API REST con JWT y roles

API REST de una tienda en línea hecha con **Spring Boot 3 (Web MVC + Data JPA) y MySQL**. La seguridad está implementada **a mano**: JWT, un filtro (`JwtValidationFilter`) y un interceptor de roles (`@RequiresRole` + `RoleInterceptor`). No usa Spring Security completo; solo `spring-security-crypto` para `BCryptPasswordEncoder`.

- Puerto `9090`, contexto `/api/v1/tienda` -> URL base: `http://localhost:9090/api/v1/tienda`
- JWT con JJWT 0.12.5 (subject = username, claims `userId` y `rolId`)
- Roles: `ADMIN=1`, `CLIENTE=2`, `VENDEDOR=3`

## Requisitos

- JDK 17 o superior (recomendado 17 o 21)
- MySQL 8
- Postman o Thunder Client (para la colección)
- No hace falta instalar Maven: el proyecto trae el Maven Wrapper (`mvnw.cmd`). La primera vez descarga Maven y las dependencias, así que necesita internet.

## 1. Crear la base de datos

Desde la carpeta del proyecto:

```powershell
mysql -u root -p -e "source tienda.sql"
```

(Con MySQL Workbench: *File > Open SQL Script* y ejecutar.) El script crea el esquema `tienda_db`, las tablas, los tres roles y el usuario ADMIN inicial. Se puede ejecutar más de una vez sin duplicar datos.

## 2. Variables de entorno

| Variable | Obligatoria | Valor por defecto | Para qué sirve |
|---|---|---|---|
| `JWT_SECRET` | **Sí** | (ninguno: sin ella la app no arranca) | Secreto para firmar los JWT. Base64 de al menos 32 bytes |
| `JWT_EXPIRATION_MINUTES` | No | `10` | Minutos de vida del token |
| `DB_URL` | No | `jdbc:mysql://localhost:3306/tienda_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC` | Conexión a MySQL |
| `DB_USERNAME` | No | `root` | Usuario de MySQL |
| `DB_PASSWORD` | No | (vacío) | Clave de MySQL |

Generar el secreto:

```bash
openssl rand -base64 32
```

Si no tienes `openssl` (Windows), en PowerShell:

```powershell
$b = New-Object byte[] 32; [Security.Cryptography.RandomNumberGenerator]::Create().GetBytes($b); [Convert]::ToBase64String($b)
```

Configurar las variables (PowerShell; valen solo para esa ventana):

```powershell
$env:DB_USERNAME = "root"
$env:DB_PASSWORD = "tu_clave_de_mysql"
$env:JWT_SECRET = "pega-aqui-el-secreto-generado"
$env:JWT_EXPIRATION_MINUTES = "10"
```

> No guardes el secreto ni la clave de la base de datos dentro del repositorio.

## 3. Compilar y ejecutar

```powershell
.\mvnw.cmd -DskipTests package
.\mvnw.cmd spring-boot:run
```

o, después de compilar: `java -jar target\tienda-0.0.1-SNAPSHOT.jar`

(En Linux/macOS usa `./mvnw`.)

## Credenciales del ADMIN inicial

| Campo | Valor |
|---|---|
| username | `admin` (también sirve el email `admin@tienda.com`) |
| password | `Admin2026!` |

**Solo para demostración académica.** En la base se guarda el hash BCrypt, no la contraseña. Cámbiala antes de usar el proyecto fuera del entorno académico.

- `POST /auth/register` siempre crea un **CLIENTE**.
- Los **VENDEDOR** (y otros ADMIN) los crea un ADMIN con `POST /users` indicando `rolId` (3 = VENDEDOR).

## Flujo de seguridad (resumen)

1. `POST /auth/login` valida credenciales y devuelve un **JWT** (subject = username, claims `userId` y `rolId`, firmado con HS256, vence a los 10 minutos).
2. El cliente envía `Authorization: Bearer <token>` en cada petición protegida.
3. **`JwtValidationFilter`** (OncePerRequestFilter registrado con `FilterRegistrationBean`) deja pasar las rutas públicas; en las demás verifica firma y expiración. Si falta el token, está mal formado, venció o no es válido responde **401**. Si es válido guarda `username`, `userId` y `rolId` como atributos del request.
4. **`RoleInterceptor`** lee `@RequiresRole` del método (o de la clase). Si el rol del token no está permitido responde **403**.
5. El **servicio** comprueba la propiedad del recurso usando el `userId` del token (nunca un `sellerId` o `clientId` del body). Si el recurso es de otro usuario responde **403**.

**Rutas públicas** (sin token): todo `/auth/**`, `GET /products` y `GET /products/{id}` (id numérico; `/products/mine` sí exige token).

## Endpoints

Todas las respuestas tienen la forma `{ "message": "...", "data": ... }`.

| Método | Ruta | Acceso | Códigos |
|---|---|---|---|
| POST | `/auth/register` | Público | 201, 400, 409 |
| POST | `/auth/login` | Público | 200, 400, 401 |
| POST | `/auth/refreshtoken` | Bearer token válido | 200, 401 |
| GET | `/users/me` | ADMIN, CLIENTE, VENDEDOR | 200, 401 |
| GET | `/users` | ADMIN | 200, 401, 403 |
| POST | `/users` | ADMIN | 201, 400, 401, 403, 409 |
| PATCH | `/users/{id}/role` | ADMIN | 200, 400, 401, 403, 404 |
| PATCH | `/users/{id}/status` | ADMIN | 200, 400, 401, 403, 404 |
| GET | `/categories` | ADMIN, CLIENTE, VENDEDOR | 200, 401 |
| POST | `/categories` | ADMIN | 201, 400, 401, 403, 409 |
| DELETE | `/categories/{id}` | ADMIN | 200, 401, 403, 404, 409 |
| GET | `/products?categoryId=` | Público | 200 |
| GET | `/products/{id}` | Público | 200, 404 |
| GET | `/products/mine` | VENDEDOR | 200, 401, 403 |
| POST | `/products` | VENDEDOR | 201, 400, 401, 403, 404 |
| PUT | `/products/{id}` | VENDEDOR (solo suyos), ADMIN | 200, 400, 401, 403, 404 |
| DELETE | `/products/{id}` | VENDEDOR (solo suyos), ADMIN | 200, 401, 403, 404, 409 |
| POST | `/orders` | CLIENTE | 201, 400, 401, 403, 404 |
| GET | `/orders/mine` | CLIENTE | 200, 401, 403 |
| GET | `/orders/sales` | VENDEDOR | 200, 401, 403 |
| GET | `/orders` | ADMIN | 200, 401, 403 |
| PATCH | `/orders/{id}/status` | VENDEDOR (solo sus productos), ADMIN | 200, 400, 401, 403, 404, 409 |
| PATCH | `/orders/{id}/cancel` | CLIENTE (solo sus pedidos PENDIENTES) | 200, 401, 403, 404, 409 |

Estados de un pedido: `PENDIENTE`, `ENVIADO`, `ENTREGADO`, `CANCELADO`.

## Colección de Postman

Importa `postman/TiendaApp.postman_collection.json`. Variables de la colección:

| Variable | Uso |
|---|---|
| `baseUrl` | `http://localhost:9090/api/v1/tienda` |
| `token` | Se llena sola al ejecutar **Login** (y **Refresh token**). Todas las peticiones protegidas la usan |
| `loginUser` / `loginPassword` | Credenciales que usa la petición Login. Cámbialas para entrar como otro usuario |
| `categoryId`, `productId`, `orderId`, `userId` | Ids que tú anotas de las respuestas y usas en las rutas con `{id}` |

## Cómo obtener las evidencias que pide la guía

Las capturas las generas tú con tu propia base y tu Postman. En cada captura deben verse el método, la URL, el código de estado y el cuerpo de la respuesta.

**Preparación (una sola vez, con la app corriendo):**

1. Ejecuta **Login** con `loginUser=admin` y `loginPassword=Admin2026!` (el token queda en `{{token}}`).
2. Con el token del ADMIN, ejecuta **POST /users** dos veces cambiando el body: `vendedor1` y `vendedor2`, ambos con `"rolId": 3`, email distinto y contraseña `Vendedor123!`.
3. Ejecuta **POST /auth/register** con `cliente1` / `cliente1@tienda.com` / `Cliente123!` (queda como CLIENTE).
4. Con el ADMIN, ejecuta **POST /categories** y anota el `id` en la variable `categoryId`.
5. Cambia `loginUser` a `vendedor1` (clave `Vendedor123!`), ejecuta **Login** y luego **POST /products** (stock 5). Anota el `id` del producto en `productId`.

**Los 6 casos:**

1. **Login correcto (200).** Con `admin`, ejecuta *0. Autenticación > Login*. Captura el 200 con el `jwt` y los datos del usuario (sin password).
2. **Token ausente (401).** Ejecuta *5. Evidencias > 2. Token ausente (401)*. Esa petición no envía el header `Authorization`. Debe responder 401.
3. **Token expirado (401).** Detén la app, pon `$env:JWT_EXPIRATION_MINUTES = "1"` y vuelve a arrancarla. Ejecuta **Login**, espera más de 1 minuto (por ejemplo 70 segundos) y ejecuta *5. Evidencias > 3. Token expirado (401)* con ese mismo token. Debe responder 401 con "El token ha expirado". Después vuelve a poner `10` y reinicia la app.
4. **Rol no permitido (403).** Cambia `loginUser` a `cliente1`, ejecuta **Login** y luego *5. Evidencias > 4. Rol no permitido (403)* (`GET /users`, que es solo para ADMIN). Debe responder 403.
5. **Vendedor editando un producto ajeno (403).** Cambia `loginUser` a `vendedor2`, ejecuta **Login** y luego *5. Evidencias > 5. Producto ajeno (403)*: intenta editar el producto `{{productId}}`, que es de `vendedor1`. Debe responder 403. Como contraste, edita ese mismo producto entrando como `vendedor1` (200).
6. **Pedido sin stock (400).** Entra como `cliente1` y ejecuta *5. Evidencias > 6. Pedido sin stock (400)* (cantidad 999999). Debe responder 400. Luego ejecuta *3. Productos > Detalle* y muestra que el stock no cambió.

## Decisiones de diseño (cosas que la guía no define)

- **Stock insuficiente:** se valida antes de modificar nada; si falla, responde 400 y el inventario queda igual. Al crear el pedido el stock se descuenta en la misma transacción, con la fila del producto bloqueada para que dos pedidos simultáneos no gasten el mismo stock.
- **Estados del pedido:** un pedido `CANCELADO` es definitivo (409 si se intenta cambiar). Si un VENDEDOR o ADMIN lo pasa a `CANCELADO` desde `/status`, el stock también se devuelve.
- **Cancelar un pedido que no está PENDIENTE:** responde 409 (la petición es válida, pero choca con el estado actual).
- **Eliminar un producto con pedidos asociados:** responde 409 para no romper la clave foránea.
- **Refresh token:** exige un token todavía válido (uno vencido responde 401) y vuelve a leer al usuario en la base de datos, así el token nuevo refleja su rol actual y se niega si fue desactivado.
- **Cambios de rol o estado:** un token ya emitido conserva el rol que tenía hasta que expira o se renueva.
- **Esquema:** `ddl-auto: none`; las tablas las crea únicamente `tienda.sql`.
- **Lombok** se usa solo en las entidades; los DTO son `record` de Java.

## Estructura de paquetes (`com.david.tienda`)

```
config      AppConfig, FilterConfig, WebConfig
controller  BaseController, AuthController, UsersController, CategoriesController,
            ProductsController, OrdersController
dto         HttpGlobalResponseDTO<T> y los DTO de request/response
entity      Roles, Users, Categories, Products, Orders
enums       RoleEnum, OrderStatusEnum
filter      JwtValidationFilter
repository  un repositorio por entidad
security    RequiresRole, RoleInterceptor, SecurityResponseWriter
service     JwtService, AuthService, RolesService, UsersService, CategoriesService,
            ProductsService, OrdersService
```

## Retos extra

No se implementaron (validación con `spring-boot-starter-validation`, `@RestControllerAdvice`, paginación de `GET /products`). Se dejan para después de compilar y verificar los requisitos obligatorios. `@RequiresRole` sí funciona en métodos y en clases.
