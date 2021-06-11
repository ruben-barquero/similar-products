# SimilarProductsApi
Este proyecto es una de las soluciones (API no reactiva) a la prueba técnica, donde se pide construir un API REST que devuelva el resultado combinado de la ejecución de dos API´s REST externas proporcionadas.

La API construida tiene dos modos de ejecución:
> Modo REST_TEMPLATE (asignando el valor REST_TEMPLATE en la propiedad 'serviceType' del archivo 'application.properties'), en el que se recuperan los datos de las API´s externas mediante RestTemplate y paralelizando las llamadas mediante parallel Streams.

> Modo WEB_CLIENT (asignando el valor WEB_CLIENT en la propiedad 'serviceType' del archivo 'application.properties'), en el que se recuperan los datos de las API´s externas mediante WebClient y paralelizando las llamadas mediante parallel Flux.

Por defecto el API se ejecuta en modo WEB_CLIENT.

## Herramientas utilizadas
Se ha creado un **ApiREST** con **springboot**, **Java11**, **Lombok**, **MapStruct**, etc. Para la compilación y gestión de dependencias se ha utilizado **maven**.

La definición del Api se ha realizado mediante **Swagger**, haciendo uso de **OpenAPI** para la generación automática de código.
El archivo que define el Api ha sido proporcionado para realizar la prueba y se ha modificado para crear el esquema ProductDetail con el objeto resultado. Este archivo se encuentra ubicado en la siguiente ruta:
> src/main/resources/similarProducts.yaml

Además, se han realizado test de integración (**JUnit**, **MockWebServer** y **MockRestServiceServer**).

## Compile, Run & Test
Todas acciones se realizan a través **maven**.

Para compilar y ejecutar los test:
> mvn clean install

Los lanzar la aplicación:
> mvn spring-boot:run

## Endpoints
La definición del API puede verse [aquí](http://localhost:5000/swagger-ui/). Esta página permite explorar los diferentes recursos del api y así como realizar pruebas desde el navegador.
