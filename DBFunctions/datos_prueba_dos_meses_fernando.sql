/*
    Datos de prueba para Fernando Madrid.
    Periodo: julio y agosto de 2026.

    Ejecutar despues de:
      1. DBML/Proyecto_2026-09-02T02_54_10.675Z.sql
      2. DBFunctions/trigger.sql
      3. Los procedimientos de DBFunctions/procedimientos.sql

    Este script no utiliza GO para que pueda ejecutarse desde editores
    que no reconocen ese separador de lotes.
*/

SET NOCOUNT ON;
SET XACT_ABORT ON;

BEGIN TRY
    BEGIN TRANSACTION;

    DECLARE @nombre VARCHAR(50) = 'Fernando';
    DECLARE @apellido VARCHAR(50) = 'Madrid';
    DECLARE @correo VARCHAR(100) = 'fernando.madrid@gmail.com';
    DECLARE @creado_por VARCHAR(100) = 'datos_prueba_fernando';

    DECLARE @id_usuario INT;
    DECLARE @id_categoria_ingreso INT;
    DECLARE @id_categoria_gasto INT;
    DECLARE @id_categoria_ahorro INT;
    DECLARE @id_subcategoria_ingreso INT;
    DECLARE @id_subcategoria_alquiler INT;
    DECLARE @id_subcategoria_ahorro INT;
    DECLARE @id_presupuesto INT;
    DECLARE @id_obligacion INT;

    IF EXISTS (
        SELECT 1
        FROM dbo.usuario
        WHERE correo_electronico = @correo
    )
        THROW 51100, 'Ya existe el usuario de prueba de Fernando Madrid.', 1;

    IF NOT EXISTS (
        SELECT 1
        FROM sys.triggers
        WHERE name = 'tr_crear_subcategoria_default'
          AND parent_id = OBJECT_ID('dbo.categoria')
    )
        THROW 51101, 'Debe ejecutarse DBFunctions/trigger.sql antes de este script.', 1;

    EXEC dbo.sp_insertar_usuario
        @nombre = @nombre,
        @apellido = @apellido,
        @correo_electronico = @correo,
        @salario_base = 18000.00,
        @creado_por = @creado_por;

    SELECT @id_usuario = id_usuario
    FROM dbo.usuario
    WHERE correo_electronico = @correo;

    IF @id_usuario IS NULL
        THROW 51102, 'No se pudo obtener el id del usuario de Fernando.', 1;

    -- Las categorias se crean para Fernando mediante id_usuario.
    -- "Alimentacion" tambien puede existir para otro usuario sin conflicto.
    EXEC dbo.sp_insertar_categoria
        @id_usuario = @id_usuario,
        @nombre_categoria = 'Salario',
        @descripcion = 'Ingresos por salario',
        @tipo_categoria = 1,
        @order_presentacion = 1,
        @creado_por = @creado_por;

    EXEC dbo.sp_insertar_categoria
        @id_usuario = @id_usuario,
        @nombre_categoria = 'Alimentacion',
        @descripcion = 'Gastos de comida y supermercado',
        @tipo_categoria = 2,
        @order_presentacion = 1,
        @creado_por = @creado_por;

    EXEC dbo.sp_insertar_categoria
        @id_usuario = @id_usuario,
        @nombre_categoria = 'Ahorro',
        @descripcion = 'Ahorro mensual',
        @tipo_categoria = 3,
        @order_presentacion = 1,
        @creado_por = @creado_por;

    SELECT @id_categoria_ingreso = id_categoria
    FROM dbo.categoria
    WHERE id_usuario = @id_usuario
      AND nombre_categoria = 'Salario'
      AND tipo_categoria = 1;

    SELECT @id_categoria_gasto = id_categoria
    FROM dbo.categoria
    WHERE id_usuario = @id_usuario
      AND nombre_categoria = 'Alimentacion'
      AND tipo_categoria = 2;

    SELECT @id_categoria_ahorro = id_categoria
    FROM dbo.categoria
    WHERE id_usuario = @id_usuario
      AND nombre_categoria = 'Ahorro'
      AND tipo_categoria = 3;

    IF @id_categoria_ingreso IS NULL
       OR @id_categoria_gasto IS NULL
       OR @id_categoria_ahorro IS NULL
        THROW 51103, 'No se pudieron crear las categorias de Fernando.', 1;

    -- Se prueba la insercion de una subcategoria no default.
    EXEC dbo.sp_insertar_subcategoria
        @id_categoria = @id_categoria_gasto,
        @nombre = 'Alquiler',
        @descripcion = 'Pago mensual de vivienda',
        @creado_por = @creado_por;

    -- El trigger debe haber creado una subcategoria General activa por categoria.
    SELECT @id_subcategoria_ingreso = id_subcategoria
    FROM dbo.subcategoria
    WHERE id_categoria = @id_categoria_ingreso
      AND es_default = 1
      AND estado = 1;

    SELECT @id_subcategoria_alquiler = id_subcategoria
    FROM dbo.subcategoria
    WHERE id_categoria = @id_categoria_gasto
      AND nombre = 'Alquiler'
      AND es_default = 0
      AND estado = 1;

    SELECT @id_subcategoria_ahorro = id_subcategoria
    FROM dbo.subcategoria
    WHERE id_categoria = @id_categoria_ahorro
      AND es_default = 1
      AND estado = 1;

    IF @id_subcategoria_ingreso IS NULL
       OR @id_subcategoria_alquiler IS NULL
       OR @id_subcategoria_ahorro IS NULL
        THROW 51104, 'Falta una subcategoria activa; revise el trigger de subcategoria default.', 1;

    EXEC dbo.sp_insertar_presupuesto
        @id_usuario = @id_usuario,
        @nombre = 'Presupuesto Fernando Jul-Ago 2026',
        @ano_inicio = 2026,
        @mes_inicio = 7,
        @ano_fin = 2026,
        @mes_fin = 8,
        @total_ingresos = 36000.00,
        @total_gastos = 5000.00,
        @total_ahorro = 2000.00,
        @creado_por = @creado_por;

    SELECT @id_presupuesto = id_presupuesto
    FROM dbo.presupuesto
    WHERE id_usuario = @id_usuario
      AND nombre_descriptivo = 'Presupuesto Fernando Jul-Ago 2026';

    IF @id_presupuesto IS NULL
        THROW 51105, 'No se pudo crear el presupuesto de Fernando.', 1;

    EXEC dbo.sp_insertar_presupuesto_detalle
        @id_presupuesto = @id_presupuesto,
        @id_subcategoria = @id_subcategoria_ingreso,
        @monto_mensual = 18000.00,
        @justificacion_monto = 'Salario mensual',
        @creado_por = @creado_por;

    EXEC dbo.sp_insertar_presupuesto_detalle
        @id_presupuesto = @id_presupuesto,
        @id_subcategoria = @id_subcategoria_alquiler,
        @monto_mensual = 2500.00,
        @justificacion_monto = 'Alquiler mensual',
        @creado_por = @creado_por;

    EXEC dbo.sp_insertar_presupuesto_detalle
        @id_presupuesto = @id_presupuesto,
        @id_subcategoria = @id_subcategoria_ahorro,
        @monto_mensual = 1000.00,
        @justificacion_monto = 'Ahorro mensual',
        @creado_por = @creado_por;

    EXEC dbo.sp_insertar_obligacion
        @id_usuario = @id_usuario,
        @id_subcategoria = @id_subcategoria_alquiler,
        @nombre = 'Alquiler de Fernando',
        @descripcion = 'Obligacion fija de vivienda',
        @monto_fijo_mensual = 2500.00,
        @dia_vencimiento = 5,
        @fecha_inicio = '2026-07-01',
        @fecha_fin = NULL,
        @creado_por = @creado_por;

    SELECT @id_obligacion = id_obligacion
    FROM dbo.obligacion_fija
    WHERE id_usuario = @id_usuario
      AND nombre = 'Alquiler de Fernando';

    IF @id_obligacion IS NULL
        THROW 51106, 'No se pudo crear la obligacion de Fernando.', 1;

    -- Julio: ingreso, gasto asociado a obligacion y ahorro.
    EXEC dbo.sp_insertar_transaccion
        @id_usuario = @id_usuario,
        @id_presupuesto = @id_presupuesto,
        @id_subcategoria = @id_subcategoria_ingreso,
        @id_obligacion = NULL,
        @ano = 2026,
        @mes = 7,
        @tipo = 1,
        @descripcion = 'Salario de julio',
        @monto = 18000.00,
        @fecha = '2026-07-01',
        @metodo_pago = 'transferencia',
        @numero_factura = NULL,
        @observaciones = 'Ingreso mensual',
        @creado_por = @creado_por;

    EXEC dbo.sp_insertar_transaccion
        @id_usuario = @id_usuario,
        @id_presupuesto = @id_presupuesto,
        @id_subcategoria = @id_subcategoria_alquiler,
        @id_obligacion = @id_obligacion,
        @ano = 2026,
        @mes = 7,
        @tipo = 2,
        @descripcion = 'Alquiler de julio',
        @monto = 2500.00,
        @fecha = '2026-07-05',
        @metodo_pago = 'transferencia',
        @numero_factura = 'FER-ALQ-JUL',
        @observaciones = NULL,
        @creado_por = @creado_por;

    EXEC dbo.sp_insertar_transaccion
        @id_usuario = @id_usuario,
        @id_presupuesto = @id_presupuesto,
        @id_subcategoria = @id_subcategoria_ahorro,
        @id_obligacion = NULL,
        @ano = 2026,
        @mes = 7,
        @tipo = 3,
        @descripcion = 'Ahorro de julio',
        @monto = 1000.00,
        @fecha = '2026-07-30',
        @metodo_pago = 'transferencia',
        @numero_factura = NULL,
        @observaciones = NULL,
        @creado_por = @creado_por;

    -- Agosto: se repite el flujo para validar el segundo mes.
    EXEC dbo.sp_insertar_transaccion
        @id_usuario = @id_usuario,
        @id_presupuesto = @id_presupuesto,
        @id_subcategoria = @id_subcategoria_ingreso,
        @id_obligacion = NULL,
        @ano = 2026,
        @mes = 8,
        @tipo = 1,
        @descripcion = 'Salario de agosto',
        @monto = 18000.00,
        @fecha = '2026-08-01',
        @metodo_pago = 'transferencia',
        @numero_factura = NULL,
        @observaciones = 'Ingreso mensual',
        @creado_por = @creado_por;

    EXEC dbo.sp_insertar_transaccion
        @id_usuario = @id_usuario,
        @id_presupuesto = @id_presupuesto,
        @id_subcategoria = @id_subcategoria_alquiler,
        @id_obligacion = @id_obligacion,
        @ano = 2026,
        @mes = 8,
        @tipo = 2,
        @descripcion = 'Alquiler de agosto',
        @monto = 2500.00,
        @fecha = '2026-08-05',
        @metodo_pago = 'transferencia',
        @numero_factura = 'FER-ALQ-AGO',
        @observaciones = NULL,
        @creado_por = @creado_por;

    EXEC dbo.sp_insertar_transaccion
        @id_usuario = @id_usuario,
        @id_presupuesto = @id_presupuesto,
        @id_subcategoria = @id_subcategoria_ahorro,
        @id_obligacion = NULL,
        @ano = 2026,
        @mes = 8,
        @tipo = 3,
        @descripcion = 'Ahorro de agosto',
        @monto = 1000.00,
        @fecha = '2026-08-30',
        @metodo_pago = 'transferencia',
        @numero_factura = NULL,
        @observaciones = NULL,
        @creado_por = @creado_por;

    IF (SELECT COUNT(*) FROM dbo.categoria WHERE id_usuario = @id_usuario) <> 3
        THROW 51107, 'La validacion de categorias por usuario fallo.', 1;

    IF (SELECT COUNT(*) FROM dbo.presupuesto_detalle WHERE id_presupuesto = @id_presupuesto) <> 3
        THROW 51108, 'La validacion de detalles de presupuesto fallo.', 1;

    IF (SELECT COUNT(*) FROM dbo.transaccion WHERE id_usuario = @id_usuario AND id_presupuesto = @id_presupuesto) <> 6
        THROW 51109, 'La validacion de transacciones de dos meses fallo.', 1;

    COMMIT TRANSACTION;

    SELECT
        @id_usuario AS id_usuario_fernando,
        @id_presupuesto AS id_presupuesto_fernando,
        @id_obligacion AS id_obligacion_fernando,
        'Datos de prueba de Fernando Madrid creados y validados correctamente.' AS resultado;
END TRY
BEGIN CATCH
    IF XACT_STATE() <> 0
        ROLLBACK TRANSACTION;
    THROW;
END CATCH;
