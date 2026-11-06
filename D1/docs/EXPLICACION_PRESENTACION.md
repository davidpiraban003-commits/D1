# Explicación para la presentación - TiendaApp (JWT y roles)

## 1. ¿Qué hace un JWT?

Un JWT (JSON Web Token) es un **pase firmado** que el servidor entrega al hacer login. Piénsalo como la manilla de un concierto: la compras una vez en la entrada (login) y después solo la muestras en cada puerta, sin volver a pagar.

Tiene tres partes separadas por puntos: `encabezado.contenido.firma`.

- **Contenido (payload):** aquí van `sub` = username, `userId`, `rolId` y la fecha de vencimiento (`exp`, a los 10 minutos).
- **Firma:** se calcula con el contenido y un **secreto que solo conoce el servidor** (HMAC-SHA256). Si alguien cambia una sola letra del contenido (por ejemplo, su `rolId` de 2 a 1), la firma deja de coincidir y el servidor lo rechaza con 401.
- **El contenido no está cifrado**, solo firmado. Por eso el token **nunca** lleva la contraseña.

Ventajas: el servidor no guarda sesiones (cada petición trae su propia identidad) y el token vence solo. Cuando vence se pide uno nuevo con `POST /auth/refreshtoken`.

Las contraseñas se guardan con **BCrypt**: un hash de un solo sentido con sal aleatoria. Ni siquiera la base de datos puede revelar la clave original, y ninguna respuesta de la API incluye el campo `password`.

## 2. Cómo colaboran el filtro y el interceptor

```
Petición
   |
   v
JwtValidationFilter        -> "¿Quién eres?"      Token ausente, vencido o inválido  => 401
   |  (guarda username, userId y rolId en el request)
   v
RoleInterceptor            -> "¿Puedes entrar aquí?"  Tu rol no está en @RequiresRole  => 403
   |
   v
Controller  ->  Service    -> "¿Este recurso es tuyo?" Es de otro usuario              => 403
   |
   v
Base de datos
```

- **El filtro autentica.** Revisa el header `Authorization: Bearer ...`, verifica firma y vencimiento y deja `userId` y `rolId` como atributos del request. Deja pasar sin token solo `/auth/**`, `GET /products` y `GET /products/{id}`.
- **El interceptor autoriza.** Necesita saber qué método del controller se va a ejecutar para leer su `@RequiresRole`, y eso solo se sabe dentro de Spring MVC; por eso es un interceptor y no parte del filtro.
- **401 vs 403:** 401 significa "no sé quién eres" (token mal o ausente). 403 significa "sé quién eres, pero no tienes permiso".

Ejemplo: un CLIENTE llama `GET /users`. El filtro ve un token válido y deja pasar. El interceptor lee que `GET /users` exige ADMIN, ve `rolId = 2` y responde 403. Sin token habría recibido 401 en el filtro.

## 3. Cómo se comprueba que cada usuario solo modifica lo suyo

La regla de oro: **la identidad sale únicamente del token**, nunca del body.

- Al crear un producto, `seller_id` es el `userId` del token. El body ni siquiera tiene campo `sellerId`.
- Al crear un pedido, `client_id` es el `userId` del token y el `total` lo calcula el servidor (precio x cantidad).
- Al editar o borrar un producto, `ProductsService` compara `producto.seller.id` con el `userId` del token. Si no coinciden y el usuario no es ADMIN, responde 403.
- Al cancelar un pedido, `OrdersService` exige que `pedido.client.id` sea el `userId` del token y que el pedido esté `PENDIENTE`.
- Un VENDEDOR solo cambia el estado de pedidos cuyos productos son suyos.

Ejemplo: `vendedor2` hace `PUT /products/7`, pero el producto 7 es de `vendedor1`. El filtro lo deja pasar (token válido), el interceptor también (VENDEDOR está permitido), y el servicio ve que el dueño no es él: **403**. Esa es la demostración en vivo más clara (evidencia 5 del README).

## 4. Preguntas que podrían hacerte

- **¿Por qué BCrypt y no SHA-256?** BCrypt agrega sal aleatoria y es lento a propósito, lo que encarece los ataques de fuerza bruta.
- **¿Qué pasa si alguien modifica el token?** La firma no coincide y el filtro responde 401.
- **¿Por qué el filtro se registra con `FilterRegistrationBean` y no con `@Component`?** Para controlar explícitamente el registro y el orden, y evitar que Spring Boot lo registre dos veces.
- **¿Por qué `/products/mine` está protegido si empieza como `/products/{id}`?** La regla pública exige que el id sea numérico; `mine` no lo es.
- **¿Qué pasa si un ADMIN le cambia el rol a alguien?** Su token viejo conserva el rol anterior hasta que vence (10 minutos) o se renueva; el refresh vuelve a leer el rol actual en la base de datos.
- **¿Cómo evitan vender más stock del que hay?** El stock se valida antes de descontar, todo ocurre en una transacción y la fila del producto se bloquea mientras se crea el pedido.
