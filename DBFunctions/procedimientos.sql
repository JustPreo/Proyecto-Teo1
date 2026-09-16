--REVISAR DESPUES LA COMPATIBILIDAD CON JAVA , PORFAVOR AARON DEL FUTURO
--PROBAR TODO BIEN XD

--USUARIO
-------------------------------------------------------------------------
CREATE OR ALTER PROCEDURE dbo.sp_insertar_usuario
    @nombre VARCHAR(50),
    @apellido VARCHAR(50),
    @correo_electronico VARCHAR(100),
    @salario_base DECIMAL(12,2),
    @creado_por VARCHAR(100)
AS
BEGIN
	
	
	IF EXISTS (SELECT 1 FROM usuario WHERE correo_electronico = @correo_electronico)
        THROW 50079, 'Ya existe un usuario con ese correo electronico', 1;

    IF (@salario_base < 0)
        THROW 50080, 'El salario base no puede ser negativo', 1
	
	 INSERT INTO usuario
    (nombre,apellido,
     correo_electronico,salario_base,
     creado_por
    )
    VALUES
    (@nombre,@apellido,
     @correo_electronico,@salario_base,
     @creado_por);

END



CREATE OR ALTER PROCEDURE dbo.sp_actualizar_usuario
    @id_usuario INT,
    @nombre VARCHAR(50),
    @apellido VARCHAR(50),
    @correo_electronico VARCHAR(100),
    @salario_base DECIMAL(12,2),
    @modificado_por VARCHAR(100)
AS
BEGIN

	IF NOT EXISTS (SELECT 1 FROM usuario WHERE id_usuario = @id_usuario)
        THROW 50081, 'El usuario no existe', 1;

    IF EXISTS (SELECT 1 FROM usuario WHERE id_usuario = @id_usuario AND estado = 0)
        THROW 50082, 'El usuario esta inactivo', 1;

    IF EXISTS (SELECT 1 FROM usuario
               WHERE correo_electronico = @correo_electronico
                 AND id_usuario != @id_usuario)
        THROW 50083, 'Ya existe otro usuario con ese correo electronico', 1;

    IF (@salario_base < 0)
        THROW 50084, 'El salario base no puede ser negativo', 1;
	
	UPDATE usuario
    SET nombre = @nombre,
        apellido = @apellido,
        correo_electronico = @correo_electronico,
        salario_base = @salario_base,
        modificado_por = @modificado_por,
        modificado_en = SYSDATETIME()
    WHERE id_usuario = @id_usuario;

END



CREATE OR ALTER PROCEDURE dbo.sp_eliminar_usuario
    @id_usuario INT,@modificado_por VARCHAR(100)
AS
BEGIN
	IF NOT EXISTS (SELECT 1 FROM usuario WHERE id_usuario = @id_usuario)
        THROW 50085, 'El usuario no existe', 1;

    IF EXISTS (SELECT 1 FROM usuario WHERE id_usuario = @id_usuario AND estado = 0)
        THROW 50086, 'El usuario ya esta inactivo', 1;
    
    
	 UPDATE usuario
    SET estado = 0,modificado_por = @modificado_por,
    modificado_en = SYSDATETIME()
    WHERE id_usuario = @id_usuario;
END


CREATE OR ALTER PROCEDURE dbo.sp_consultar_usuario
    @id_usuario INT
AS
BEGIN
	
	IF NOT EXISTS (SELECT 1 FROM usuario WHERE id_usuario = @id_usuario)
        THROW 50087, 'El usuario no existe', 1;

	
	SELECT
        u.id_usuario,
        u.nombre,
        u.apellido,
        u.correo_electronico,
        u.fecha_registro,
        u.salario_base,
        u.estado,
        (CASE u.estado 
        WHEN 1 THEN 'Activo' 
        ELSE 'Inactive' END) AS nombre_estado,
        u.creado_por,
        u.modificado_por,
        u.creado_en,
        u.modificado_en
    FROM usuario u
    WHERE u.id_usuario = @id_usuario;
END



CREATE OR ALTER PROCEDURE dbo.sp_listar_usuarios
AS
BEGIN
	SELECT
        u.id_usuario,
        u.nombre,
        u.apellido,
        u.correo_electronico,
        u.fecha_registro,
        u.salario_base,
        u.estado,
        (CASE u.estado 
        WHEN 1 THEN 'Activo' 
        ELSE 'Inactivo' END) AS nombre_estado
    FROM usuario u
    ORDER BY u.nombre, u.apellido;
END

--CATEGORIA
-------------------------------------------------------------------------

--1 = Ingreso  2 = Gasto  3 = Ahorro

CREATE OR ALTER PROCEDURE dbo.sp_insertar_categoria
    @nombre_categoria VARCHAR(50),@descripcion VARCHAR(255),
    @tipo_categoria SMALLINT,@order_presentacion SMALLINT,
    @creado_por VARCHAR(100)
AS
BEGIN
	
	
	IF (@tipo_categoria NOT IN (1,2,3))
        THROW 50088, 'El tipo de categoria debe ser 1, 2 o 3', 1;
	
	if(@order_presentacion < 0)
		THROW 50001, 'El orden de presentacion no puede ser negativo',1;
	
	if exists(select 1 from categoria where nombre_categoria = @nombre_categoria and tipo_categoria = @tipo_categoria)
		throw 50002,'Ya existe una categoria con ese nombre y tipo',1;
	
	insert into categoria(nombre_categoria,
	descripcion,
	tipo_categoria,
	order_presentacion,
	creado_por)VALUES(
	@nombre_categoria,
	@descripcion,
	@tipo_categoria,
	@order_presentacion,
	@creado_por
	);

END;




CREATE OR ALTER PROCEDURE dbo.sp_actualizar_categoria
    @id_categoria INT,@nombre_categoria VARCHAR(50),
    @descripcion VARCHAR(255),@tipo_categoria SMALLINT,
    @order_presentacion SMALLINT,@modificado_por VARCHAR(100)
AS
BEGIN
    IF NOT EXISTS (SELECT 1 FROM categoria WHERE id_categoria = @id_categoria)
        THROW 50100, 'La categoria no existe', 1;

    IF (@tipo_categoria NOT IN (1,2,3))
        THROW 50003, 'El tipo de categoria debe ser 1, 2 o 3', 1;

    IF (@order_presentacion < 0)
        THROW 50004, 'El orden de presentacion no puede ser negativo', 1;

    IF EXISTS (SELECT 1 FROM categoria
               WHERE nombre_categoria = @nombre_categoria
                 AND tipo_categoria   = @tipo_categoria
                 AND id_categoria    != @id_categoria)
        THROW 50103, 'Ya existe una categoria con ese nombre y tipo', 1;

    
    UPDATE categoria
    SET nombre_categoria   = @nombre_categoria,
        descripcion        = @descripcion,
        tipo_categoria     = @tipo_categoria,
        order_presentacion = @order_presentacion,
        modificado_por     = @modificado_por,
        modificado_en      = SYSDATETIME()
    WHERE id_categoria = @id_categoria;
END;




CREATE OR ALTER PROCEDURE dbo.sp_eliminar_categoria
    @id_categoria INT,@modificado_por VARCHAR(100)
AS
BEGIN
	if not exists(select 1 from categoria where id_categoria = @id_categoria)
		throw 50005,'La categoria no existe',1;

	if exists(select 1 from subcategoria where id_categoria = @id_categoria and es_default = 0 and estado = 1)
		throw 50006, 'La categoria tiene subcategorias activas',1;
	
	
	--subcategorias que no estan en uso 
	IF EXISTS (SELECT 1 FROM subcategoria sub WHERE sub.id_categoria = @id_categoria AND (
              EXISTS (SELECT 1 FROM presupuesto_detalle pd WHERE pd.id_subcategoria = sub.id_subcategoria)
              OR EXISTS (SELECT 1 FROM transaccion t WHERE t.id_subcategoria = sub.id_subcategoria)
              OR EXISTS (SELECT 1 FROM obligacion_fija o WHERE o.id_subcategoria = sub.id_subcategoria)
          )
    )
    	throw 50007, 'La categoria tiene subcategorias en uso',1;
	
	delete from subcategoria where id_categoria = @id_categoria;
	delete from categoria where id_categoria = @id_categoria;


END;



CREATE OR ALTER PROCEDURE dbo.sp_consultar_categoria
    @id_categoria INT
AS
BEGIN
	if not exists(select 1 from categoria where id_categoria = @id_categoria)
		throw 50008,'La categoria no existe',1;
	select * from categoria where id_categoria = @id_categoria;
	
END;


CREATE OR ALTER PROCEDURE dbo.sp_listar_categorias
    @tipo_categoria SMALLINT = NULL
AS
BEGIN
	
	if (@tipo_categoria is not null and @tipo_categoria not in (1,2,3))
		throw 50009,'El tipo de categoria deberia ser 1,2 o 3',1;
	
	SELECT * from categoria where @tipo_categoria IS NULL or tipo_categoria = @tipo_categoria
END


--SUBCATEGORIA
-------------------------------------------------------------------------
CREATE OR ALTER PROCEDURE dbo.sp_insertar_subcategoria
    @id_categoria INT,@nombre VARCHAR(50),
    @descripcion VARCHAR(255),@creado_por VARCHAR(100)
AS
BEGIN

    IF NOT EXISTS (SELECT 1 FROM categoria WHERE id_categoria = @id_categoria)
        THROW 50010, 'La categoria seleccionada no existe', 1;

    IF EXISTS (SELECT 1 FROM subcategoria WHERE id_categoria = @id_categoria AND nombre = @nombre)
        THROW 50011, 'Ya existe una subcategoria con ese nombre', 1;

    INSERT INTO subcategoria (id_categoria,nombre,descripcion,estado,es_default,creado_por)
    VALUES (@id_categoria,@nombre,@descripcion,1,0,@creado_por);
END;




CREATE OR ALTER PROCEDURE dbo.sp_actualizar_subcategoria
    @id_subcategoria INT,
    @nombre VARCHAR(50),
    @descripcion VARCHAR(255),
    @estado BIT,
    @modificado_por VARCHAR(100)
AS
BEGIN
    IF NOT EXISTS (SELECT 1 FROM subcategoria WHERE id_subcategoria = @id_subcategoria)
        THROW 50012, 'La subcategoria no existe', 1;

    IF EXISTS (SELECT 1 FROM subcategoria sub1 INNER JOIN subcategoria sub2 ON sub1.id_categoria = sub2.id_categoria
        WHERE sub1.id_subcategoria = @id_subcategoria AND sub2.nombre = @nombre AND sub2.id_subcategoria != @id_subcategoria)
        THROW 50013, 'Ya existe una subcategoria con ese nombre', 1;

    UPDATE subcategoria SET 
    	nombre = @nombre,
        descripcion = @descripcion,
        estado = @estado,
        modificado_por = @modificado_por,
        modificado_en = SYSDATETIME()
    WHERE id_subcategoria = @id_subcategoria;
END;




CREATE OR ALTER PROCEDURE dbo.sp_eliminar_subcategoria
    @id_subcategoria INT
AS
BEGIN

    IF NOT EXISTS (SELECT 1 FROM subcategoria WHERE id_subcategoria = @id_subcategoria)
        THROW 50014, 'La subcategoria no existe', 1;

    IF EXISTS (SELECT 1 FROM subcategoria WHERE id_subcategoria = @id_subcategoria AND es_default = 1)
        THROW 50015, 'No se puede eliminar la subcategoria default', 1;

    IF EXISTS (SELECT 1 FROM presupuesto_detalle WHERE @id_subcategoria = id_subcategoria)
        THROW 50016, 'La subcategoria esta siendo usada en un "presupuesto"', 1;

    IF EXISTS (SELECT 1 FROM transaccion WHERE id_subcategoria = @id_subcategoria)
        THROW 50017, 'La subcategoria esta siendo usada en "transacciones"', 1;

    IF EXISTS (SELECT 1 FROM obligacion_fija WHERE id_subcategoria = @id_subcategoria)
        THROW 50018, 'La subcategoria esta siendo usada en "obligaciones"', 1;

    DELETE FROM subcategoria WHERE id_subcategoria = @id_subcategoria;
END;




CREATE OR ALTER PROCEDURE dbo.sp_consultar_subcategoria @id_subcategoria INT
AS
BEGIN
    SELECT
        sub.id_subcategoria,
        sub.nombre AS nombre_subcategoria,
        sub.descripcion,
        sub.estado,
        sub.es_default,
        c.id_categoria,
        c.nombre_categoria,
        c.tipo_categoria,
        sub.creado_por,
        sub.modificado_por,
        sub.creado_en,
        sub.modificado_en
    FROM subcategoria sub
    INNER JOIN categoria c
        ON sub.id_categoria = c.id_categoria
    WHERE sub.id_subcategoria = @id_subcategoria;
END;




CREATE OR ALTER PROCEDURE dbo.sp_listar_subcategorias_por_categoria
    @id_categoria INT
AS
BEGIN

    IF NOT EXISTS (SELECT 1 FROM categoria WHERE id_categoria = @id_categoria)
        THROW 50019, 'La caegoria no existe', 1;

    SELECT * FROM subcategoria sub WHERE sub.id_categoria = @id_categoria
END;




--PRESUPUESTO
-------------------------------------------------------------------------

--1 = Activo  2 = Cerrado  3 = Borrador

CREATE OR ALTER PROCEDURE dbo.sp_insertar_presupuesto
    @id_usuario INT,@nombre VARCHAR(50),
    @ano_inicio INT,@mes_inicio SMALLINT,
    @ano_fin INT,@mes_fin SMALLINT,
    @total_ingresos DECIMAL(12,2),@total_gastos DECIMAL(12,2),
    @total_ahorro DECIMAL(12,2),@creado_por VARCHAR(100)
AS
BEGIN
	DECLARE @inicio DATE,
			@fin DATE
    IF NOT EXISTS (SELECT 1 FROM usuario WHERE id_usuario = @id_usuario AND estado = 1)
        THROW 50020, 'El usuario no existe', 1;


	IF (@mes_inicio NOT BETWEEN 1 AND 12 OR @mes_fin NOT BETWEEN 1 AND 12)
        THROW 50092, 'Los meses deben estar entre 1 y 12',1;
	
	SET @inicio = DATEFROMPARTS(@ano_inicio, @mes_inicio,1);
    SET @fin    = DATEFROMPARTS(@ano_fin,@mes_fin,1);
	
    IF (DATEFROMPARTS(@ano_fin, @mes_fin, 1)< DATEFROMPARTS(@ano_inicio, @mes_inicio, 1))
        THROW 50021, 'La fecha final no puede ser anterior a la inicial', 1;

    IF (@total_ingresos < 0 OR @total_gastos < 0 OR @total_ahorro < 0)
        THROW 50022, 'Los montos no pueden ser negativos', 1;

     IF EXISTS (SELECT 1
               FROM presupuesto p
               WHERE p.id_usuario = @id_usuario
                 AND p.estado_presupuesto = 1
                 AND @inicio <= DATEFROMPARTS(p.ano_fin,    p.mes_fin,    1)
                 AND @fin    >= DATEFROMPARTS(p.ano_inicio, p.mes_inicio, 1))
        THROW 50093, 'Ya existe un presupuesto activo que se solapa con ese periodo', 1;

    IF (@total_ingresos < @total_gastos + @total_ahorro)
        THROW 50094, 'Los ingresos presupuestados deben cubrir gastos mas ahorros', 1;
    
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
        @id_usuario,
        @nombre,
        @ano_inicio,
        @mes_inicio,
        @ano_fin,
        @mes_fin,
        @total_ingresos,
        @total_gastos,
        @total_ahorro,
        1,
        @creado_por
    );
END;




CREATE OR ALTER PROCEDURE dbo.sp_actualizar_presupuesto
    @id_presupuesto INT,
    @nombre VARCHAR(50),
    @ano_inicio INT,
    @mes_inicio SMALLINT,
    @ano_fin INT,
    @mes_fin SMALLINT,
    @total_ingresos DECIMAL(12,2),
    @total_gastos DECIMAL(12,2),
    @total_ahorro DECIMAL(12,2),
    @estado_presupuesto SMALLINT,
    @modificado_por VARCHAR(100)
AS
BEGIN
    DECLARE @id_usuario INT, 
	@estado_actual SMALLINT, 
	@inicio DATE, 
	@fin DATE;

    IF NOT EXISTS (SELECT 1 FROM presupuesto WHERE id_presupuesto = @id_presupuesto)
        THROW 50023, 'El presupuesto no existe', 1;

    SELECT @id_usuario    = id_usuario,
           @estado_actual = estado_presupuesto
    FROM presupuesto
    WHERE id_presupuesto = @id_presupuesto;

    IF (@estado_actual = 2)
        THROW 50095, 'No se puede modificar un presupuesto cerrado', 1;

    IF (@mes_inicio NOT BETWEEN 1 AND 12 OR @mes_fin NOT BETWEEN 1 AND 12)
        THROW 50096, 'Los meses deben estar entre 1 y 12', 1;

    SET @inicio = DATEFROMPARTS(@ano_inicio, @mes_inicio, 1);
    SET @fin    = DATEFROMPARTS(@ano_fin,@mes_fin,1);

    IF (@fin < @inicio)
        THROW 50024, 'La fecha final no puede ser anterior a la inicial', 1;

    IF (@estado_presupuesto NOT IN (1,2,3))
        THROW 50025, 'El estado debe ser 1 2 o 3', 1;

    IF (@total_ingresos < 0 OR @total_gastos < 0 OR @total_ahorro < 0)
        THROW 50026, 'Los montos no pueden ser negativos', 1;

    IF EXISTS (SELECT 1
               FROM transaccion t
               WHERE t.id_presupuesto = @id_presupuesto
                 AND (t.fecha < @inicio
                      OR t.fecha > EOMONTH(@fin)
                      OR DATEFROMPARTS(t.ano, t.mes, 1) < @inicio
                      OR DATEFROMPARTS(t.ano, t.mes, 1) > @fin))
        THROW 50097, 'Existen transacciones fuera del nuevo periodo de vigencia', 1;

    IF (@estado_presupuesto = 1)
	    BEGIN
	        IF EXISTS (SELECT 1
	                   FROM presupuesto p
	                   WHERE p.id_usuario = @id_usuario
	                     AND p.id_presupuesto != @id_presupuesto
	                     AND p.estado_presupuesto = 1
	                     AND @inicio <= DATEFROMPARTS(p.ano_fin,    p.mes_fin,    1)
	                     AND @fin    >= DATEFROMPARTS(p.ano_inicio, p.mes_inicio, 1))
	            THROW 50098, 'Ya existe otro presupuesto activo que coincide con ese periodo', 1;
	    END

    UPDATE presupuesto
    SET nombre_descriptivo  = @nombre,
        ano_inicio          = @ano_inicio,
        mes_inicio          = @mes_inicio,
        ano_fin             = @ano_fin,
        mes_fin             = @mes_fin,
        total_ingresos      = @total_ingresos,
        total_gastos        = @total_gastos,
        total_ahorro        = @total_ahorro,
        estado_presupuesto  = @estado_presupuesto,
        modificado_por      = @modificado_por,
        modificado_en       = SYSDATETIME()
    WHERE id_presupuesto = @id_presupuesto;
END;




CREATE OR ALTER PROCEDURE dbo.sp_eliminar_presupuesto
    @id_presupuesto INT
AS
BEGIN
    IF NOT EXISTS (SELECT 1 FROM presupuesto WHERE id_presupuesto = @id_presupuesto)
        THROW 50027, 'No existe esepresupuesto', 1;

    IF EXISTS (SELECT 1 FROM transaccion WHERE id_presupuesto = @id_presupuesto)
        THROW 50028, 'El presupuesto esta siendo utilizado en "Transaccion"', 1;

        DELETE FROM presupuesto_detalle
        WHERE id_presupuesto = @id_presupuesto;

        DELETE FROM presupuesto
        WHERE id_presupuesto = @id_presupuesto;
END;


CREATE OR ALTER PROCEDURE dbo.sp_consultar_presupuesto
    @id_presupuesto INT
AS
BEGIN
    SELECT
        p.id_presupuesto,
        p.id_usuario,
        u.nombre,
        u.apellido,
        p.nombre_descriptivo,
        p.ano_inicio,
        p.mes_inicio,
        DATEFROMPARTS(p.ano_inicio,p.mes_inicio,1) AS fecha_inicio,
        p.ano_fin,
        p.mes_fin,
        EOMONTH(DATEFROMPARTS(p.ano_fin,p.mes_fin,1)) AS fecha_fin,
        p.total_ingresos,
        p.total_gastos,
        p.total_ahorro,
        p.estado_presupuesto,
        (CASE p.estado_presupuesto
            WHEN 1 THEN 'Activo'
            WHEN 2 THEN 'Cerrado'
            WHEN 3 THEN 'Borrador'
        END)AS nombre_estado,
        p.fecha_hora_creacion,
        p.creado_por,
        p.modificado_por,
        p.creado_en,
        p.modificado_en
    FROM presupuesto p
    INNER JOIN usuario u
        ON p.id_usuario = u.id_usuario
    WHERE p.id_presupuesto = @id_presupuesto;
END;




CREATE OR ALTER PROCEDURE dbo.sp_listar_presupuestos_usuario
    @id_usuario INT,
    @estado_presupuesto SMALLINT = NULL
AS
BEGIN
    IF NOT EXISTS (SELECT 1 FROM usuario WHERE id_usuario = @id_usuario)
        THROW 50029, 'El usuario no existe', 1;

    IF @estado_presupuesto IS NOT NULL AND @estado_presupuesto NOT IN (1,2,3)
        THROW 50030, 'El estado debe ser 1 2 o 3', 1;

    SELECT
        p.id_presupuesto,
        p.nombre_descriptivo,
        DATEFROMPARTS(p.ano_inicio,p.mes_inicio,1) AS fecha_inicio,
        EOMONTH(DATEFROMPARTS(p.ano_fin,p.mes_fin,1)) AS fecha_fin,
        p.total_ingresos,
        p.total_gastos,
        p.total_ahorro,
        p.estado_presupuesto,
        (CASE p.estado_presupuesto
            WHEN 1 THEN 'Activo'
            WHEN 2 THEN 'Cerrado'
            WHEN 3 THEN 'Borrador'
        END) AS nombre_estado
    FROM presupuesto p
    WHERE p.id_usuario = @id_usuario
      AND (@estado_presupuesto IS NULL OR p.estado_presupuesto = @estado_presupuesto)
      
      --maybe despues agregar algun order by)?
END;



--PRESUPUESTO_DETALLE
-------------------------------------------------------------------------


CREATE OR ALTER PROCEDURE dbo.sp_insertar_presupuesto_detalle
    @id_presupuesto INT,@id_subcategoria INT,
    @monto_mensual DECIMAL(12,2),@justificacion_monto VARCHAR(255),
    @creado_por VARCHAR(100)
AS
BEGIN
    IF NOT EXISTS (SELECT 1 FROM presupuesto WHERE id_presupuesto = @id_presupuesto)
        THROW 50031, 'El presupuesto no existe', 1;
 
    IF EXISTS (SELECT 1 FROM presupuesto WHERE id_presupuesto = @id_presupuesto AND estado_presupuesto = 2)
        THROW 50032, 'No se puede agregar detalle a un presupuesto cerrado', 1;
 
    IF NOT EXISTS (SELECT 1 FROM subcategoria WHERE id_subcategoria = @id_subcategoria AND estado = 1)
        THROW 50033, 'La subcategoria no existe o esta inactiva', 1;
 
    IF EXISTS (SELECT 1 FROM presupuesto_detalle WHERE id_presupuesto = @id_presupuesto AND id_subcategoria = @id_subcategoria)
        THROW 50034, 'Ya existe un detalle para esa subcategoria en el presupuesto', 1;
 
    IF (@monto_mensual < 0)
        THROW 50035, 'El monto mensual no puede ser negativo', 1;
 
    INSERT INTO presupuesto_detalle (id_presupuesto,id_subcategoria,
    monto_mensual,justificacion_monto,creado_por)
    VALUES (@id_presupuesto,@id_subcategoria,
    @monto_mensual,@justificacion_monto,@creado_por);
END;
 
 
 
CREATE OR ALTER PROCEDURE dbo.sp_actualizar_presupuesto_detalle
    @id_detalle INT,@monto_mensual DECIMAL(12,2),
    @justificacion_monto VARCHAR(255),@modificado_por VARCHAR(100)
AS
BEGIN
    IF NOT EXISTS (SELECT 1 FROM presupuesto_detalle WHERE id_detalle = @id_detalle)
        THROW 50036, 'El detalle de presupuesto no existe', 1;
 
    IF EXISTS (SELECT 1 FROM presupuesto_detalle pd INNER JOIN presupuesto p 
    		   ON p.id_presupuesto = pd.id_presupuesto
               WHERE pd.id_detalle = @id_detalle AND p.estado_presupuesto = 2)
        THROW 50037, 'No se puede modificar el detalle de un presupuesto cerrado', 1;
 
    IF (@monto_mensual < 0)
        THROW 50038, 'El monto mensual no puede ser negativo', 1;
 
    UPDATE presupuesto_detalle
    SET monto_mensual = @monto_mensual,
        justificacion_monto = @justificacion_monto,
        modificado_por = @modificado_por,
        modificado_en = SYSDATETIME()
    WHERE id_detalle = @id_detalle;
END;
 
 
 
CREATE OR ALTER PROCEDURE dbo.sp_eliminar_presupuesto_detalle
    @id_detalle INT
AS
BEGIN
    IF NOT EXISTS (SELECT 1 FROM presupuesto_detalle WHERE id_detalle = @id_detalle)
        THROW 50039, 'El detalle de presupuesto no existe', 1;
 
    IF EXISTS (SELECT 1 FROM presupuesto_detalle pd
               INNER JOIN presupuesto p ON p.id_presupuesto = pd.id_presupuesto
               WHERE pd.id_detalle = @id_detalle AND p.estado_presupuesto = 2)
        THROW 50040, 'No se puede eliminar el detalle de un presupuesto cerrado', 1;
 
    --si transacciones then no borrar
    IF EXISTS (SELECT 1 FROM presupuesto_detalle pd
               INNER JOIN transaccion t ON t.id_presupuesto = pd.id_presupuesto
                    AND t.id_subcategoria = pd.id_subcategoria
               WHERE pd.id_detalle = @id_detalle)
        THROW 50041, 'El detalle tiene transacciones asociadas', 1;
 
    DELETE FROM presupuesto_detalle WHERE id_detalle = @id_detalle;
END;
 
 
 
CREATE OR ALTER PROCEDURE dbo.sp_consultar_presupuesto_detalle
    @id_detalle INT
AS
BEGIN
    IF NOT EXISTS (SELECT 1 FROM presupuesto_detalle WHERE id_detalle = @id_detalle)
        THROW 50042, 'El detalle de presupuesto no existe', 1;
 
    SELECT
        pd.id_detalle,
        pd.id_presupuesto,
        p.nombre_descriptivo,
        pd.monto_mensual,
        pd.justificacion_monto,
        sub.id_subcategoria,
        sub.nombre AS nombre_subcategoria,
        sub.descripcion AS descripcion_subcategoria,
        sub.estado,
        c.id_categoria,
        c.nombre_categoria,
        c.tipo_categoria,
        pd.creado_por,
        pd.modificado_por,
        pd.creado_en,
        pd.modificado_en
    FROM presupuesto_detalle pd
    INNER JOIN presupuesto p
        ON p.id_presupuesto = pd.id_presupuesto
    INNER JOIN subcategoria sub
        ON sub.id_subcategoria = pd.id_subcategoria
    INNER JOIN categoria c
        ON c.id_categoria = sub.id_categoria
    WHERE pd.id_detalle = @id_detalle;
END;
 
 
 
CREATE OR ALTER PROCEDURE dbo.sp_listar_detalles_presupuesto
    @id_presupuesto INT
AS
BEGIN
    IF NOT EXISTS (SELECT 1 FROM presupuesto WHERE id_presupuesto = @id_presupuesto)
        THROW 50043, 'El presupuesto no existe', 1;
 
    SELECT
        pd.id_detalle,
        pd.id_presupuesto,
        pd.monto_mensual,
        pd.justificacion_monto,
        sub.id_subcategoria,
        sub.nombre AS nombre_subcategoria,
        c.id_categoria,
        c.nombre_categoria,
        c.tipo_categoria,
        pd.creado_por,
        pd.modificado_por,
        pd.creado_en,
        pd.modificado_en
    FROM presupuesto_detalle pd
    INNER JOIN subcategoria sub
        ON sub.id_subcategoria = pd.id_subcategoria
    INNER JOIN categoria c
        ON c.id_categoria = sub.id_categoria
    WHERE pd.id_presupuesto = @id_presupuesto
    ORDER BY c.tipo_categoria, c.order_presentacion, sub.nombre;
END;

--OBLIGACION_FIJA
-------------------------------------------------------------------------


CREATE OR ALTER PROCEDURE dbo.sp_insertar_obligacion
    @id_usuario INT,@id_subcategoria INT,
    @nombre VARCHAR(50),@descripcion VARCHAR(255),
    @monto_fijo_mensual DECIMAL(12,2),@dia_vencimiento SMALLINT,
    @fecha_inicio DATE,@fecha_fin DATE,
    @creado_por VARCHAR(100)
AS
BEGIN
    IF NOT EXISTS (SELECT 1 FROM usuario WHERE id_usuario = @id_usuario AND estado = 1)
        THROW 50044, 'El usuario no existe', 1;
 
    IF NOT EXISTS (SELECT 1 FROM subcategoria WHERE id_subcategoria = @id_subcategoria AND estado = 1)
        THROW 50045, 'La subcategoria no existe', 1;
 
    IF EXISTS (SELECT 1 FROM obligacion_fija WHERE id_usuario = @id_usuario AND nombre = @nombre)
        THROW 50046, 'Ya existe una obligacion con ese nombre para el usuario', 1;
 
    IF (@monto_fijo_mensual <= 0)
        THROW 50047, 'El monto fijo mensual debe ser mayor a cero', 1;
 
    IF (@dia_vencimiento < 1 OR @dia_vencimiento > 31)
        THROW 50048, 'El dia de vencimiento debe estar entre 1 y 31', 1;
 
    IF (@fecha_fin IS NOT NULL AND @fecha_fin < @fecha_inicio)
        THROW 50049, 'La fecha final no puede ser anterior a la inicial', 1;
 
    INSERT INTO obligacion_fija (id_usuario,id_subcategoria,
    nombre,descripcion,
    monto_fijo_mensual,dia_vencimiento,
    vigente,fecha_inicio,fecha_fin,creado_por)
    VALUES (@id_usuario,@id_subcategoria,
    @nombre,@descripcion,
    @monto_fijo_mensual,@dia_vencimiento,
    1,@fecha_inicio,@fecha_fin,@creado_por);
END;
 
 
 
CREATE OR ALTER PROCEDURE dbo.sp_actualizar_obligacion
    @id_obligacion INT,@id_subcategoria INT,
    @nombre VARCHAR(50),@descripcion VARCHAR(255),
    @monto_fijo_mensual DECIMAL(12,2),@dia_vencimiento SMALLINT,
    @fecha_inicio DATE,@fecha_fin DATE,
    @vigente BIT,@modificado_por VARCHAR(100)
AS
BEGIN
    IF NOT EXISTS (SELECT 1 FROM obligacion_fija WHERE id_obligacion = @id_obligacion)
        THROW 50050, 'La obligacion no existe', 1;
 
    IF NOT EXISTS (SELECT 1 FROM subcategoria WHERE id_subcategoria = @id_subcategoria AND estado = 1)
        THROW 50051, 'La subcategoria no existe', 1;
 
    IF EXISTS (SELECT 1 FROM obligacion_fija o1 INNER JOIN obligacion_fija o2
               ON o1.id_usuario = o2.id_usuario
               WHERE o1.id_obligacion = @id_obligacion AND o2.nombre = @nombre
                 AND o2.id_obligacion != @id_obligacion)
        THROW 50052, 'Ya existe una obligacion con ese nombre', 1;
 
    IF (@monto_fijo_mensual <= 0)
        THROW 50053, 'El monto fijo mensual debe ser mayor a cero', 1;
 
    IF (@dia_vencimiento < 1 OR @dia_vencimiento > 31)
        THROW 50054, 'El dia de vencimiento debe estar entre 1 y 31', 1;
 
    IF (@fecha_fin IS NOT NULL AND @fecha_fin < @fecha_inicio)
        THROW 50055, 'La fecha final no puede ser anterior a la inicial', 1;
 
    UPDATE obligacion_fija
    SET id_subcategoria = @id_subcategoria,
        nombre = @nombre,
        descripcion = @descripcion,
        monto_fijo_mensual = @monto_fijo_mensual,
        dia_vencimiento = @dia_vencimiento,
        fecha_inicio = @fecha_inicio,
        fecha_fin = @fecha_fin,
        vigente = @vigente,
        modificado_por = @modificado_por,
        modificado_en = SYSDATETIME()
    WHERE id_obligacion = @id_obligacion;
END;
 
 
 
CREATE OR ALTER PROCEDURE dbo.sp_eliminar_obligacion
    @id_obligacion INT,@modificado_por VARCHAR(100)
AS
BEGIN
    IF NOT EXISTS (SELECT 1 FROM obligacion_fija WHERE id_obligacion = @id_obligacion)
        THROW 50056, 'La obligacion no existe', 1;
 
    IF EXISTS (SELECT 1 FROM obligacion_fija WHERE id_obligacion = @id_obligacion AND vigente = 0)
        THROW 50057, 'La obligacion ya esta desactivada', 1;
 
    --mantenerlo como historial osea no force delete algo asi se llama
    UPDATE obligacion_fija
    SET vigente = 0,
        modificado_por = @modificado_por,
        modificado_en = SYSDATETIME()
    WHERE id_obligacion = @id_obligacion;
END;
 
 
 
CREATE OR ALTER PROCEDURE dbo.sp_consultar_obligacion
    @id_obligacion INT
AS
BEGIN
    IF NOT EXISTS (SELECT 1 FROM obligacion_fija WHERE id_obligacion = @id_obligacion)
        THROW 50058, 'La obligacion no existe', 1;
 
    SELECT
        o.id_obligacion,
        o.id_usuario,
        u.nombre AS nombre_usuario,
        u.apellido,
        o.nombre,
        o.descripcion,
        o.monto_fijo_mensual,
        o.dia_vencimiento,
        o.vigente,
        (CASE o.vigente 
        WHEN 1 THEN 'Vigente' 
        ELSE 'No vigente' 
        END) AS nombre_vigencia,
        o.fecha_inicio,
        o.fecha_fin,
        sub.id_subcategoria,
        sub.nombre AS nombre_subcategoria,
        c.id_categoria,
        c.nombre_categoria,
        c.tipo_categoria,
        o.creado_por,
        o.modificado_por,
        o.creado_en,
        o.modificado_en
    FROM obligacion_fija o
    INNER JOIN usuario u
        ON u.id_usuario = o.id_usuario
    INNER JOIN subcategoria sub
        ON sub.id_subcategoria = o.id_subcategoria
    INNER JOIN categoria c
        ON c.id_categoria = sub.id_categoria
    WHERE o.id_obligacion = @id_obligacion;
END;
 
 
 
CREATE OR ALTER PROCEDURE dbo.sp_listar_obligaciones_usuario
    @id_usuario INT,
    @vigente BIT = NULL
AS
BEGIN
    IF NOT EXISTS (SELECT 1 FROM usuario WHERE id_usuario = @id_usuario)
        THROW 50059, 'El usuario no existe', 1;
 
    SELECT
        o.id_obligacion,
        o.nombre,
        o.descripcion,
        o.monto_fijo_mensual,
        o.dia_vencimiento,
        o.vigente,
        (CASE o.vigente 
        WHEN 1 THEN 'Vigente' 
        ELSE 'No vigente' 
        END) AS nombre_vigencia,
        o.fecha_inicio,
        o.fecha_fin,
        sub.id_subcategoria,
        sub.nombre AS nombre_subcategoria,
        c.nombre_categoria,
        c.tipo_categoria
    FROM obligacion_fija o
    INNER JOIN subcategoria sub
        ON sub.id_subcategoria = o.id_subcategoria
    INNER JOIN categoria c
        ON c.id_categoria = sub.id_categoria
    WHERE o.id_usuario = @id_usuario
      AND (@vigente IS NULL OR o.vigente = @vigente)
    ORDER BY o.dia_vencimiento, o.nombre;
END;




--TRANSACCION
-------------------------------------------------------------------------

--1 = Ingreso  2 = Gasto  3 = Ahorro

CREATE OR ALTER PROCEDURE dbo.sp_insertar_transaccion
    @id_usuario INT,@id_presupuesto INT,
    @id_subcategoria INT,@id_obligacion INT = NULL,
    @ano INT,@mes SMALLINT,
    @tipo SMALLINT,@descripcion VARCHAR(255),
    @monto DECIMAL(12,2),@fecha DATE,
    @metodo_pago VARCHAR(50),@numero_factura VARCHAR(50),
    @observaciones VARCHAR(255),@creado_por VARCHAR(100)
AS
BEGIN
    DECLARE @fecha_inicio DATE, @fecha_fin DATE, @estado_presupuesto SMALLINT;
 
    IF NOT EXISTS (SELECT 1 FROM usuario WHERE id_usuario = @id_usuario AND estado = 1)
        THROW 50060, 'El usuario no existe', 1;
 
    IF NOT EXISTS (SELECT 1 FROM presupuesto WHERE id_presupuesto = @id_presupuesto AND id_usuario = @id_usuario)
        THROW 50061, 'El presupuesto no existe', 1;
 
    SELECT @fecha_inicio = DATEFROMPARTS(ano_inicio,mes_inicio,1),
           @fecha_fin = EOMONTH(DATEFROMPARTS(ano_fin,mes_fin,1)),
           @estado_presupuesto = estado_presupuesto
    FROM presupuesto WHERE id_presupuesto = @id_presupuesto;
 
    IF (@estado_presupuesto = 2)
        THROW 50062, 'No se pueden registrar transacciones en un presupuesto cerrado', 1;
 
    IF NOT EXISTS (SELECT 1 FROM subcategoria WHERE id_subcategoria = @id_subcategoria AND estado = 1)
        THROW 50063, 'La subcategoria no existe', 1;
 
    IF (@id_obligacion IS NOT NULL AND NOT EXISTS
        (SELECT 1 FROM obligacion_fija WHERE id_obligacion = @id_obligacion AND id_usuario = @id_usuario))
        THROW 50064, 'La obligacion no existe o no pertenece al usuario', 1;
 
    IF (@monto <= 0)
        THROW 50065, 'El monto debe ser mayor a cero', 1;
 
    IF (@fecha < @fecha_inicio OR @fecha > @fecha_fin)
        THROW 50066, 'La fecha esta fuera del periodo del presupuesto', 1;
 
    IF (YEAR(@fecha) != @ano OR MONTH(@fecha) != @mes)
        THROW 50067, 'El ano y mes no coinciden con la fecha', 1;
 
    INSERT INTO transaccion (id_usuario,id_presupuesto,
    id_subcategoria,id_obligacion,
    ano,mes,tipo,descripcion,
    monto,fecha,metodo_pago,
    numero_factura,observaciones,creado_por)
    VALUES (@id_usuario,@id_presupuesto,
    @id_subcategoria,@id_obligacion,
    @ano,@mes,@tipo,@descripcion,
    @monto,@fecha,@metodo_pago,
    @numero_factura,@observaciones,@creado_por);
END;
 
 
 
CREATE OR ALTER PROCEDURE dbo.sp_actualizar_transaccion
    @id_transaccion INT,@id_subcategoria INT,
    @id_obligacion INT = NULL,@ano INT,
    @mes SMALLINT,@tipo SMALLINT,
    @descripcion VARCHAR(255),@monto DECIMAL(12,2),
    @fecha DATE,@metodo_pago VARCHAR(50),
    @numero_factura VARCHAR(50),@observaciones VARCHAR(255),
    @modificado_por VARCHAR(100)
AS
BEGIN
    DECLARE @id_usuario INT, 
	@fecha_inicio DATE, 
	@fecha_fin DATE, 
	@estado_presupuesto SMALLINT;
 
    IF NOT EXISTS (SELECT 1 FROM transaccion WHERE id_transaccion = @id_transaccion)
        THROW 50068, 'La transaccion no existe', 1;
 
    SELECT @id_usuario = t.id_usuario,
           @fecha_inicio = DATEFROMPARTS(p.ano_inicio,p.mes_inicio,1),
           @fecha_fin = EOMONTH(DATEFROMPARTS(p.ano_fin,p.mes_fin,1)),
           @estado_presupuesto = p.estado_presupuesto
    FROM transaccion t
    INNER JOIN presupuesto p ON p.id_presupuesto = t.id_presupuesto
    WHERE t.id_transaccion = @id_transaccion;
 
    IF (@estado_presupuesto = 2)
        THROW 50069, 'No se puede modificar una transaccion de un presupuesto cerrado', 1;
 
    IF NOT EXISTS (SELECT 1 FROM subcategoria WHERE id_subcategoria = @id_subcategoria AND estado = 1)
        THROW 50070, 'La subcategoria no existe', 1;
 
    IF (@id_obligacion IS NOT NULL AND NOT EXISTS
        (SELECT 1 FROM obligacion_fija WHERE id_obligacion = @id_obligacion AND id_usuario = @id_usuario))
        THROW 50071, 'La obligacion no existe', 1;
    
    IF (@monto <= 0)
        THROW 50072, 'El monto debe ser mayor a cero', 1;
 
    IF (@fecha < @fecha_inicio OR @fecha > @fecha_fin)
        THROW 50073, 'La fecha esta fuera del periodo del presupuesto', 1;
 
    IF (YEAR(@fecha) != @ano OR MONTH(@fecha) != @mes)
        THROW 50074, 'El ano y mes no coinciden con la fecha', 1;
 
    UPDATE transaccion
    SET id_subcategoria = @id_subcategoria,
        id_obligacion = @id_obligacion,
        ano = @ano,
        mes = @mes,
        tipo = @tipo,
        descripcion = @descripcion,
        monto = @monto,
        fecha = @fecha,
        metodo_pago = @metodo_pago,
        numero_factura = @numero_factura,
        observaciones = @observaciones,
        modificado_por = @modificado_por,
        modificado_en = SYSDATETIME()
    WHERE id_transaccion = @id_transaccion;
END;
 
 
 
CREATE OR ALTER PROCEDURE dbo.sp_eliminar_transaccion
    @id_transaccion INT
AS
BEGIN
    IF NOT EXISTS (SELECT 1 FROM transaccion WHERE id_transaccion = @id_transaccion)
        THROW 50075, 'La transaccion no existe', 1;
 
    IF EXISTS (SELECT 1 FROM transaccion t
               INNER JOIN presupuesto p ON p.id_presupuesto = t.id_presupuesto
               WHERE t.id_transaccion = @id_transaccion AND p.estado_presupuesto = 2)
        THROW 50076, 'No se puede eliminar una transaccion de un presupuesto cerrado', 1;
 
    DELETE FROM transaccion WHERE id_transaccion = @id_transaccion;
END;
 
 
 
CREATE OR ALTER PROCEDURE dbo.sp_consultar_transaccion
    @id_transaccion INT
AS
BEGIN
    IF NOT EXISTS (SELECT 1 FROM transaccion WHERE id_transaccion = @id_transaccion)
        THROW 50077, 'La transaccion no existe', 1;
 
    SELECT
        t.id_transaccion,
        t.id_usuario,
        u.nombre AS nombre_usuario,
        u.apellido,
        t.id_presupuesto,
        p.nombre_descriptivo,
        t.ano,
        t.mes,
        t.fecha,
        t.tipo,
        (CASE t.tipo
            WHEN 1 THEN 'Ingreso'
            WHEN 2 THEN 'Gasto'
            WHEN 3 THEN 'Ahorro'
        END) AS nombre_tipo,
        t.descripcion,
        t.monto,
        t.metodo_pago,
        t.numero_factura,
        t.observaciones,
        t.fecha_registro,
        t.id_obligacion,
        o.nombre AS nombre_obligacion,
        sub.id_subcategoria,
        sub.nombre AS nombre_subcategoria,
        c.id_categoria,
        c.nombre_categoria,
        c.tipo_categoria,
        t.creado_por,
        t.modificado_por,
        t.creado_en,
        t.modificado_en
    FROM transaccion t
    INNER JOIN usuario u
        ON u.id_usuario = t.id_usuario
    INNER JOIN presupuesto p
        ON p.id_presupuesto = t.id_presupuesto
    INNER JOIN subcategoria sub
        ON sub.id_subcategoria = t.id_subcategoria
    INNER JOIN categoria c
        ON c.id_categoria = sub.id_categoria
    LEFT JOIN obligacion_fija o
        ON o.id_obligacion = t.id_obligacion
    WHERE t.id_transaccion = @id_transaccion;
END;
 
 
 
CREATE OR ALTER PROCEDURE dbo.sp_listar_transacciones_presupuesto
    @id_presupuesto INT,
    @tipo SMALLINT = NULL,
    @id_subcategoria INT = NULL,
    @ano INT = NULL,
    @mes SMALLINT = NULL
AS
BEGIN
    IF NOT EXISTS (SELECT 1 FROM presupuesto WHERE id_presupuesto = @id_presupuesto)
        THROW 50078, 'El presupuesto no existe', 1;

    SELECT
        t.id_transaccion,
        t.fecha,
        t.ano,
        t.mes,
        t.tipo,
        (CASE t.tipo
            WHEN 1 THEN 'Ingreso'
            WHEN 2 THEN 'Gasto'
            WHEN 3 THEN 'Ahorro'
        END) AS nombre_tipo,
        t.descripcion,
        t.monto,
        t.metodo_pago,
        t.numero_factura,
        sub.id_subcategoria,
        sub.nombre AS nombre_subcategoria,
        c.nombre_categoria,
        c.tipo_categoria,
        o.nombre AS nombre_obligacion
    FROM transaccion t
    INNER JOIN subcategoria sub
        ON sub.id_subcategoria = t.id_subcategoria
    INNER JOIN categoria c
        ON c.id_categoria = sub.id_categoria
    LEFT JOIN obligacion_fija o
        ON o.id_obligacion = t.id_obligacion
    WHERE t.id_presupuesto = @id_presupuesto
      AND (@tipo IS NULL OR t.tipo = @tipo)
      AND (@id_subcategoria IS NULL OR t.id_subcategoria = @id_subcategoria)
      AND (@ano IS NULL OR t.ano = @ano)
      AND (@mes IS NULL OR t.mes = @mes)
    ORDER BY t.fecha DESC, t.id_transaccion DESC;
END;






