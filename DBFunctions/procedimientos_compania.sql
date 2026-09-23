--1 sp_crear_presupuesto_completo(p_id_usuario, p_nombre, p_descripcion, p_periodo_inicio, p_periodo_fin, p_lista_subcategorias_json, p_creado_por)
--se supone que el json da:id_subcategoria,monto_mensual

CREATE OR ALTER PROCEDURE sp_crear_presupuesto_completo
    @p_id_usuario INT,@p_nombre VARCHAR(50),
    @p_descripcion VARCHAR(255),@p_periodo_inicio DATE,
    @p_periodo_fin DATE,@p_lista_subcategorias_json NVARCHAR(MAX),
    @p_creado_por VARCHAR(100)
AS
BEGIN

    IF NOT EXISTS (SELECT 1 FROM usuario
    WHERE id_usuario = @p_id_usuario AND estado = 1)
        THROW 50080, 'El usuario no existe o esta inactivo', 1;

    IF @p_periodo_fin < @p_periodo_inicio
        THROW 50081, 'La fecha final no puede ser anterior a la inicial', 1;

    IF ISJSON(@p_lista_subcategorias_json) != 1
        THROW 50082, 'El formato JSON no es valido', 1;

    INSERT INTO presupuesto (
        id_usuario,
        nombre_descriptivo,
        ano_inicio,
        mes_inicio,
        ano_fin,
        mes_fin,
        total_ingresos,
        total_gastos,
        total_ahorro,
        estado_presupuesto,
        creado_por
    )
    VALUES (
        @p_id_usuario,
        @p_nombre,
        YEAR(@p_periodo_inicio),
        MONTH(@p_periodo_inicio),
        YEAR(@p_periodo_fin),
        MONTH(@p_periodo_fin),
        0,
        0,
        0,
        1,
        @p_creado_por
    );

    DECLARE @id_presupuesto INT = SCOPE_IDENTITY();

    INSERT INTO presupuesto_detalle (
        id_presupuesto,
        id_subcategoria,
        monto_mensual,
        creado_por
    )
    SELECT
        @id_presupuesto,
        id_subcategoria,
        monto_mensual,
        @p_creado_por
    FROM OPENJSON(@p_lista_subcategorias_json)
	    WITH (
	        id_subcategoria INT '$.id_subcategoria',
	        monto_mensual DECIMAL(12,2) '$.monto_mensual'
	    );

    SELECT @id_presupuesto AS id_presupuesto;

END


--2 sp_registrar_transaccion_completa(p_id_usuario, p_id_presupuesto, p_anio, p_mes, p_id_subcategoria, p_tipo, p_descripcion, p_monto, p_fecha, p_metodo_pago, p_creado_por)

CREATE OR ALTER PROCEDURE sp_registrar_transaccion_completa
    @p_id_usuario INT,@p_id_presupuesto INT,
    @p_anio INT,@p_mes INT,
    @p_id_subcategoria INT,@p_tipo SMALLINT,
    @p_descripcion VARCHAR(255),@p_monto DECIMAL(12,2),
    @p_fecha DATE,@p_metodo_pago VARCHAR(30),
    @p_creado_por VARCHAR(100),
    @p_numero_factura VARCHAR(50) = NULL,
    @p_observaciones VARCHAR(255) = NULL,
    @p_id_obligacion INT = NULL
AS
BEGIN

    IF NOT EXISTS (SELECT 1 FROM usuario
    WHERE id_usuario = @p_id_usuario AND estado = 1)
        THROW 50083, 'El usuario no existe o esta inactivo', 1;

    IF NOT EXISTS (SELECT 1 FROM presupuesto
    WHERE id_presupuesto = @p_id_presupuesto AND id_usuario = @p_id_usuario)
        THROW 50084, 'El presupuesto no pertenece al usuario', 1;

    IF @p_mes NOT BETWEEN 1 AND 12
        THROW 50085, 'El mes debe estar entre 1 y 12', 1;

    IF @p_tipo NOT IN (1,2,3)
        THROW 50086, 'El tipo debe ser 1 2 o 3', 1;

    IF @p_monto <= 0
        THROW 50087, 'El monto debe ser mayor que cero', 1;

    IF NOT EXISTS (SELECT 1 FROM presupuesto
    WHERE id_presupuesto = @p_id_presupuesto
    AND DATEFROMPARTS(@p_anio, @p_mes, 1) BETWEEN DATEFROMPARTS(ano_inicio, mes_inicio, 1)
    AND DATEFROMPARTS(ano_fin, mes_fin, 1))
        THROW 50088, 'El ano y mes estan fuera del periodo del presupuesto', 1;

    IF NOT EXISTS (SELECT 1 FROM subcategoria
    WHERE id_subcategoria = @p_id_subcategoria AND estado = 1)
        THROW 50089, 'La subcategoria no existe o esta inactiva', 1;

    IF @p_id_obligacion IS NOT NULL AND NOT EXISTS (
        SELECT 1 FROM obligacion_fija
        WHERE id_obligacion = @p_id_obligacion
          AND id_usuario = @p_id_usuario
          AND id_subcategoria = @p_id_subcategoria
    )
        THROW 50090, 'La obligacion no existe, no pertenece al usuario o usa otra subcategoria', 1;

    INSERT INTO transaccion (
        id_usuario,
        id_presupuesto,
        id_obligacion,
        ano,
        mes,
        id_subcategoria,
        tipo,
        descripcion,
        monto,
        fecha,
        metodo_pago,
        numero_factura,
        observaciones,
        creado_por
    )
    VALUES (
        @p_id_usuario,
        @p_id_presupuesto,
        @p_id_obligacion,
        @p_anio,
        @p_mes,
        @p_id_subcategoria,
        @p_tipo,
        @p_descripcion,
        @p_monto,
        @p_fecha,
        @p_metodo_pago,
        @p_numero_factura,
        @p_observaciones,
        @p_creado_por
    );

    SELECT SCOPE_IDENTITY() AS id_transaccion;

END


--3 sp_procesar_obligaciones_mes(p_id_usuario, p_anio, p_mes, p_id_presupuesto)

CREATE OR ALTER PROCEDURE sp_procesar_obligaciones_mes
    @p_id_usuario INT,@p_anio INT,
    @p_mes INT,@p_id_presupuesto INT
AS
BEGIN

    IF @p_mes NOT BETWEEN 1 AND 12
        THROW 50090, 'El mes debe estar entre 1 y 12', 1;

    IF NOT EXISTS (SELECT 1 FROM usuario
    WHERE id_usuario = @p_id_usuario)
        THROW 50091, 'El usuario no existe', 1;

    IF NOT EXISTS (SELECT 1 FROM presupuesto
    WHERE id_presupuesto = @p_id_presupuesto AND id_usuario = @p_id_usuario)
        THROW 50092, 'El presupuesto no pertenece al usuario', 1;

    SELECT
        o.id_obligacion,
        o.nombre,
        o.descripcion,
        o.monto_fijo_mensual,
        o.dia_vencimiento,
        o.fecha_inicio,
        o.fecha_fin,
        o.id_subcategoria,
        s.nombre AS nombre_subcategoria
    FROM obligacion_fija o
    INNER JOIN subcategoria s
        ON o.id_subcategoria = s.id_subcategoria
    WHERE o.id_usuario = @p_id_usuario
    AND o.vigente = 1
    AND DATEFROMPARTS(@p_anio, @p_mes, 1) >= DATEFROMPARTS(YEAR(o.fecha_inicio), MONTH(o.fecha_inicio), 1)
    AND (
        o.fecha_fin IS NULL
        OR DATEFROMPARTS(@p_anio, @p_mes, 1) <= DATEFROMPARTS(YEAR(o.fecha_fin), MONTH(o.fecha_fin), 1)
    )
    ORDER BY o.dia_vencimiento;

END


--4 sp_calcular_balance_mensual(p_id_usuario, p_id_presupuesto, p_anio,
--p_mes, OUT p_total_ingresos, OUT p_total_gastos, OUT p_total_ahorros, OUT p_balance_final)

CREATE OR ALTER PROCEDURE sp_calcular_balance_mensual
    @p_id_usuario INT,@p_id_presupuesto INT,
    @p_anio INT,@p_mes INT,
    @p_total_ingresos DECIMAL(12,2) OUTPUT,@p_total_gastos DECIMAL(12,2) OUTPUT,
    @p_total_ahorros DECIMAL(12,2) OUTPUT,@p_balance_final DECIMAL(12,2) OUTPUT
AS
BEGIN

    IF @p_mes NOT BETWEEN 1 AND 12
        THROW 50093, 'El mes debe estar entre 1 y 12', 1;

    IF NOT EXISTS (SELECT 1 FROM presupuesto
    WHERE id_presupuesto = @p_id_presupuesto AND id_usuario = @p_id_usuario)
        THROW 50094, 'El presupuesto no pertenece al usuario', 1;

    SELECT
        @p_total_ingresos = COALESCE(SUM(CASE 
	        WHEN tipo = 1 THEN monto 
	        ELSE 0 
	        END), 0),
        @p_total_gastos = COALESCE(SUM(CASE 
	        WHEN tipo = 2 THEN monto 
	        ELSE 0 
	        END), 0),
        @p_total_ahorros = COALESCE(SUM(CASE 
	        WHEN tipo = 3 THEN monto 
	        ELSE 0 
	        END), 0)
    FROM transaccion
    WHERE id_usuario = @p_id_usuario
    AND id_presupuesto = @p_id_presupuesto
    AND ano = @p_anio
    AND mes = @p_mes;

    SET @p_balance_final = @p_total_ingresos - @p_total_gastos - @p_total_ahorros;

    SELECT
        @p_total_ingresos AS total_ingresos,
        @p_total_gastos AS total_gastos,
        @p_total_ahorros AS total_ahorros,
        @p_balance_final AS balance_final;

END


--5 sp_calcular_monto_ejecutado_mes(p_id_subcategoria, p_id_presupuesto, p_anio, p_mes, OUT p_monto_ejecutado)

CREATE OR ALTER PROCEDURE sp_calcular_monto_ejecutado_mes
    @p_id_subcategoria INT,@p_id_presupuesto INT,
    @p_anio INT,@p_mes INT,
    @p_monto_ejecutado DECIMAL(12,2) OUTPUT
AS
BEGIN

    IF @p_mes NOT BETWEEN 1 AND 12
        THROW 50095, 'El mes debe estar entre 1 y 12', 1;

    IF NOT EXISTS (SELECT 1 FROM presupuesto_detalle
    WHERE id_presupuesto = @p_id_presupuesto AND id_subcategoria = @p_id_subcategoria)
        THROW 50096, 'La subcategoria no existe en el presupuesto', 1;

    SELECT
        @p_monto_ejecutado = COALESCE(SUM(monto), 0)
    FROM transaccion
    WHERE id_subcategoria = @p_id_subcategoria
    AND id_presupuesto = @p_id_presupuesto
    AND ano = @p_anio
    AND mes = @p_mes
    AND tipo = 2;

    SELECT @p_monto_ejecutado AS monto_ejecutado;

END


--6 sp_calcular_porcentaje_ejecucion_mes(p_id_subcategoria, p_id_presupuesto, p_anio, p_mes, OUT p_porcentaje)

CREATE OR ALTER PROCEDURE sp_calcular_porcentaje_ejecucion_mes
    @p_id_subcategoria INT,@p_id_presupuesto INT,
    @p_anio INT,@p_mes INT,
    @p_porcentaje DECIMAL(12,2) OUTPUT
AS
BEGIN

    DECLARE @monto_presupuestado DECIMAL(12,2);
    DECLARE @monto_ejecutado DECIMAL(12,2);

    IF @p_mes NOT BETWEEN 1 AND 12
        THROW 50097, 'El mes debe estar entre 1 y 12', 1;

    IF NOT EXISTS (SELECT 1 FROM presupuesto_detalle
    WHERE id_presupuesto = @p_id_presupuesto AND id_subcategoria = @p_id_subcategoria)
        THROW 50098, 'La subcategoria no existe en este presupuesto', 1;

    SELECT
        @monto_presupuestado = monto_mensual
    FROM presupuesto_detalle
    WHERE id_presupuesto = @p_id_presupuesto
    AND id_subcategoria = @p_id_subcategoria;

    SELECT
        @monto_ejecutado = COALESCE(SUM(monto), 0)
    FROM transaccion
    WHERE id_presupuesto = @p_id_presupuesto
    AND id_subcategoria = @p_id_subcategoria
    AND ano = @p_anio
    AND mes = @p_mes
    AND tipo = 2;

    IF @monto_presupuestado = 0
        SET @p_porcentaje = 0;
    ELSE
        SET @p_porcentaje = (@monto_ejecutado / @monto_presupuestado) * 100;

    SELECT @p_porcentaje AS porcentaje;

END


--7 sp_cerrar_presupuesto(p_id_presupuesto, p_modificado_por)

CREATE OR ALTER PROCEDURE sp_cerrar_presupuesto
    @p_id_presupuesto INT,@p_modificado_por VARCHAR(100)
AS
BEGIN

    DECLARE @ano_fin INT;
    DECLARE @mes_fin INT;

    IF NOT EXISTS (SELECT 1 FROM presupuesto
    WHERE id_presupuesto = @p_id_presupuesto)
        THROW 50099, 'El presupuesto no existe', 1;

    SELECT
        @ano_fin = ano_fin,
        @mes_fin = mes_fin
    FROM presupuesto
    WHERE id_presupuesto = @p_id_presupuesto;

    IF DATEFROMPARTS(@ano_fin, @mes_fin, 1) >= DATEFROMPARTS(YEAR(GETDATE()), MONTH(GETDATE()), 1)
        THROW 50100, 'El periodo del presupuesto aun no termina', 1;

    UPDATE presupuesto
    SET estado_presupuesto = 2,
        modificado_por = @p_modificado_por,
        modificado_en = SYSDATETIME()
    WHERE id_presupuesto = @p_id_presupuesto;

    SELECT
        pd.id_subcategoria,
        s.nombre AS nombre_subcategoria,
        pd.monto_mensual AS monto_presupuestado,
        COALESCE(SUM(CASE 
	        WHEN t.tipo = 2 THEN t.monto 
	        ELSE 0 
	        END), 0) AS monto_ejecutado,
        pd.monto_mensual - COALESCE(SUM(CASE 
	        WHEN t.tipo = 2 THEN t.monto 
	        ELSE 0 
	        END), 0) AS diferencia
    FROM presupuesto_detalle pd
    INNER JOIN subcategoria s
        ON pd.id_subcategoria = s.id_subcategoria
    LEFT JOIN transaccion t
        ON t.id_presupuesto = pd.id_presupuesto
        AND t.id_subcategoria = pd.id_subcategoria
    WHERE pd.id_presupuesto = @p_id_presupuesto
    GROUP BY
        pd.id_subcategoria,
        s.nombre,
        pd.monto_mensual
    ORDER BY s.nombre;

END


--8 sp_obtener_resumen_categoria_mes(p_id_categoria, p_id_presupuesto, p_anio, p_mes, OUT p_monto_presupuestado, OUT p_monto_ejecutado, OUT p_porcentaje)

CREATE OR ALTER PROCEDURE sp_obtener_resumen_categoria_mes
    @p_id_categoria INT,@p_id_presupuesto INT,
    @p_anio INT,@p_mes INT,
    @p_monto_presupuestado DECIMAL(12,2) OUTPUT,@p_monto_ejecutado DECIMAL(12,2) OUTPUT,
    @p_porcentaje DECIMAL(12,2) OUTPUT
AS
BEGIN

    IF @p_mes NOT BETWEEN 1 AND 12
        THROW 50101, 'El mes debe estar entre 1 y 12', 1;

    IF NOT EXISTS (SELECT 1 FROM categoria
    WHERE id_categoria = @p_id_categoria)
        THROW 50102, 'La categoria no existe', 1;

    SELECT
        @p_monto_presupuestado = COALESCE(SUM(pd.monto_mensual), 0)
    FROM presupuesto_detalle pd
    INNER JOIN subcategoria s
        ON pd.id_subcategoria = s.id_subcategoria
    WHERE pd.id_presupuesto = @p_id_presupuesto
    AND s.id_categoria = @p_id_categoria;

    SELECT
        @p_monto_ejecutado = COALESCE(SUM(t.monto), 0)
    FROM transaccion t
    INNER JOIN subcategoria s
        ON t.id_subcategoria = s.id_subcategoria
    WHERE t.id_presupuesto = @p_id_presupuesto
    AND s.id_categoria = @p_id_categoria
    AND t.ano = @p_anio
    AND t.mes = @p_mes
    AND t.tipo = 2;

    IF @p_monto_presupuestado = 0
        SET @p_porcentaje = 0;
    ELSE
        SET @p_porcentaje = (@p_monto_ejecutado / @p_monto_presupuestado) * 100;

    SELECT
        @p_monto_presupuestado AS monto_presupuestado,
        @p_monto_ejecutado AS monto_ejecutado,
        @p_porcentaje AS porcentaje;

END
