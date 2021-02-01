# test-microservices

En este repositorio se encuentra todos los microservicios necesarios para cumplir con la especificación. También se añade un fichero docker-compose para poder levantar un base de datos MongoDB que es utilizado por alguno de estos servicios.

La solución esta formada por 5 microservicios:

- **config-server**. Para la gestión de la configuración (levanta en el puerto 8888)

- **eureka-server**. Para el registro y descubrimiento de los distintos microservicios (levanta en el puerto 8761)

- **daas-phones**. Microservicio encargado de las operaciones sobre teléfonos. (levanta en el puerto 8082)
  
  - Expone 3 endpoints:
    
    - Un endpoint que permite crear nuevos telefonos.
    
    - Un endpoint que permite recuperar un teléfono por su identificador
    
    - Un endpoint que permite recuperar el catálogo completo de teléfonos (permite paginación)
  
  - El microservicio se persiste la información en una BBDD mongo DB
  
  - El microservicio dispone de una consola swagger (basada en OpenApi v3) desde la que poder acceder a los distintos endpoints disponibles, de modo que resulte más amigable el acceso a dichos endpoints.
    
    - [Consola](http://localhost:8082/webjars/swagger-ui/index.html)

- **daas-orders**. Microservicio encargado de las operaciones sobre teléfonos. (levanta en el puerto 8083)
  
  - Expone 3 endpoints:
    
    - Un endpoint que permite crear nuevas ordenes.
    
    - Un endpoint que permite recuperar una orden por su identificador
    
    - Un endpoint que permite recuperar el listado de ordenes (permite paginación)
  
  - El microservicio se persiste la información en una BBDD mongo DB
  
  - El microservicio dispone de una consola swagger (basada en OpenApi v3) desde la que poder acceder a los distintos endpoints disponibles, de modo que resulte más amigable el acceso a dichos endpoints.
    
    - [Consola](http://localhost:8083/webjars/swagger-ui/index.html)

- **bs-orders**. Microservicio encargado de orquestar la validación de ordenes y creación de ordenes. Este servicio no accede directamente a BBDD, sino que realiza el acceso a las fuentes de datos a través de los servicios anteriores. (levanta en el puerto 8081)
  
  - Expone 1 endpoints que permite crear una nueva orden a partir de los datos del cliente (nombre, apellidos e email ) y una lista de identificadores de teléfonos (que previamente deberá conocer). 
    
    Con esta información el servicio comprueba que los teléfonos indicados están disponibles en el catálogo (consultando el endpoint a tal efecto) y si algún de los teléfonos indicados no está en dicho catálogo devuelve un error indicando que no puede llevarse a cabo la creación de la orden. 
    
    Si todos los telefonos indicados se encuentran en el catálogo, se procede a crear la orden utilizando la información de los telefonos que ha devuelto el servicio de catalogo para calcular el precio final de la orden, asi como el incluir el nombre del teléfono. 
    
    Con toda la información ya disponible se persiste la orden en BBDD haciendo uso del microservicio de ordenes.
    
    Una vez persistidos los datos, se devuelven como respuesta a la invocación del endpoint. Adicionalmente se imprime en la consola el json de la orden devuelta.
  
  - El microservicio dispone de una consola swagger (basada en OpenApi v3) desde la que poder acceder a los distintos endpoints disponibles, de modo que resulte más amigable el acceso a dichos endpoints.
    
    - [Consola](http://localhost:8081/webjars/swagger-ui/index.html)

## Dockerización

Todos los servicios disponen de un fichero Dockerfile para la creación de un contenedor docker que permita la ejecución del servicio

**_config-server_**:

```bash
cd ./config-server
docker build -t demo/config-server .
docker run -p 8888:8888 demo/config-server
```

**_eureka-server_**:

```bash
cd ./eureka-server
docker build -t demo/eureka-server .
docker run -p 8761:8761 demo/eureka-server
```

**_daas-phones_**:

```bash
cd ./daas-phones
docker build -t demo/daas-phones .
docker run -p 8082:8082 demo/daas-phones
```

***daas-orders***:

```bash
cd ./daas-orders
docker build -t demo/daas-orders . 
docker run -p 8083:8083 demo/daas-orders
```

***bs-orders***:

```bash
cd ./bs-orders
docker build -t demo/bs-orders . 
docker run -p 8081:8081 demo/bs-orders
```

## **MongoDB**

Para levantar la base de datos MongoDB

```bash
cd .
docker-compose up -d --build 
```

Para detener la base datos (y eliminarlo por completo).

```bash
cd ..
docker-compose down
```

### Repositorio de configuración

[GitHub - jjtoribio/test-config](https://github.com/jjtoribio/test-config)
