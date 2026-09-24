# Sistema de Presupuesto Personal

Proyecto académico desarrollado para la asignatura **Fundamentos de Sistemas de Bases de Datos**. Consiste en una aplicación de gestión financiera personal que permite planificar, registrar y analizar los ingresos, gastos, ahorros y obligaciones mensuales de un usuario.

El sistema centraliza la información financiera en presupuestos con vigencia definida y ofrece reportes para comparar lo planificado con el comportamiento real de cada período.

## Objetivo

Diseñar, implementar y desplegar una solución completa de gestión de presupuesto personal, aplicando procedimientos almacenados, funciones, triggers, reglas de negocio y una aplicación cliente conectada a una base de datos relacional.

## Funcionalidades principales

- Gestión de usuarios, perfiles, salario base y estado de actividad.
- Creación de presupuestos mensuales, trimestrales, semestrales o anuales.
- Clasificación de movimientos mediante categorías y subcategorías.
- Registro de ingresos, gastos y ahorros.
- Administración de obligaciones fijas, como alquileres, servicios, préstamos y seguros.
- Asignación de montos mensuales presupuestados por subcategoría.
- Registro de transacciones con fecha real y período presupuestal imputado.
- Cálculo de balances, montos ejecutados, porcentajes de ejecución y saldos disponibles.
- Alertas y seguimiento de obligaciones próximas a vencer.
- Reportes analíticos exportables a PDF.

## Modelo de datos

El sistema se organiza alrededor de las siguientes entidades:

- **Usuario:** información básica, correo electrónico, salario base y estado.
- **Presupuesto:** período de vigencia, totales planificados y estado.
- **Categoría:** clasificación principal de ingresos, gastos o ahorros.
- **Subcategoría:** clasificación detallada asociada a una categoría.
- **Presupuesto detalle:** monto mensual asignado a una subcategoría durante la vigencia del presupuesto.
- **Obligación fija:** compromiso recurrente con monto y fecha de vencimiento.
- **Transacción:** movimiento financiero real asociado a un presupuesto y una subcategoría.

Toda categoría debe contar con al menos una subcategoría. Al crear una categoría, un trigger genera automáticamente la subcategoría predeterminada **General**.

Los presupuestos se definen para un rango de año/mes. El monto de cada detalle es mensual y se aplica durante toda la vigencia; la ejecución se calcula dinámicamente sumando las transacciones imputadas al año y mes correspondiente.

## Reglas de negocio destacadas

- Solo puede existir un presupuesto activo por usuario para un mismo período.
- El período final de un presupuesto no puede ser anterior al período inicial.
- Todo presupuesto detalle y toda transacción debe estar asociado a una subcategoría.
- El tipo de transacción debe coincidir con el tipo de la categoría padre.
- Las obligaciones fijas solo pueden asociarse a categorías de tipo gasto.
- Las transacciones deben pertenecer al período de vigencia del presupuesto asociado.
- Los campos de año y mes permiten imputar una transacción a un período presupuestal distinto al de su fecha real.
- Las tablas incluyen campos de auditoría para registrar quién creó o modificó cada registro y cuándo lo hizo.

## Reportería

La aplicación contempla reportes financieros con filtros por usuario y período, entre ellos:

1. Resumen mensual de ingresos, gastos, ahorros y balance final.
2. Distribución de gastos por categoría.
3. Cumplimiento del presupuesto por categoría y subcategoría.
4. Estado y cumplimiento de pagos de obligaciones fijas.

Los reportes incluyen gráficos, tablas comparativas e indicadores de ejecución. El proyecto requiere que puedan exportarse a formato PDF.

## Arquitectura y tecnologías

El proyecto sigue una arquitectura de tres capas:

- **Presentación:** aplicación de escritorio desarrollada con Java Swing.
- **Lógica de aplicación:** clases Java para conexión, operaciones CRUD, navegación y funcionalidades de la interfaz.
- **Datos:** Microsoft SQL Server con procedimientos almacenados, funciones, triggers y scripts de datos de prueba.

Tecnologías utilizadas:

- Java 25 y Maven.
- Java Swing.
- Microsoft SQL Server 2022.
- JDBC Driver para SQL Server.
- OpenPDF para generación de reportes.
- Docker Compose para ejecutar SQL Server.

## Estructura del repositorio

```text
DBFunctions/       Procedimientos, funciones, triggers y datos de prueba
DBML/              Modelo relacional y definición de la base de datos
Java/ProyectoTeo/  Aplicación Java y sus módulos de interfaz y CRUD
docker-compose.yml Configuración del contenedor de SQL Server
```

## Base de datos

Para iniciar SQL Server mediante Docker Compose, primero define las variables requeridas en un archivo `.env`, especialmente `MSSQL_SA_PASSWORD`, y ejecuta:

```bash
docker compose up -d
```

Después, ejecuta en SQL Server los scripts del directorio `DBFunctions/` y el script de definición disponible en `DBML/`, respetando las dependencias entre tablas, procedimientos, funciones y triggers.

## Ejecución de la aplicación

Desde el módulo Java:

```bash
cd Java/ProyectoTeo
mvn package
```

La clase principal es `com.aaron.proyectoteo.ProyectoTeo`. La aplicación verifica la conexión con SQL Server, muestra el inicio de sesión y luego carga el panel principal del sistema.

## Datos de prueba

El proyecto incluye datos de prueba para simular dos meses completos de actividad financiera. Estos datos contemplan variación entre meses, gastos distribuidos en el tiempo, obligaciones cercanas a sus fechas de vencimiento y diferencias entre los montos presupuestados y ejecutados.
